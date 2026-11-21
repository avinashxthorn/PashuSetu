package `in`.pashusetu.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.util.UUID

val species = listOf("Cow","Buffalo","Goat","Sheep","Rabbit","Poultry","Dog","Other")
val statuses = listOf("Healthy","Sick","Recovering","Unknown")
val careTypes = linkedMapOf("health" to "Health visit","vaccine" to "Vaccination","deworm" to "Deworming","breeding" to "Breeding","production" to "Milk & growth","feed" to "Feed & nutrition","finance" to "Income & expense")
fun today() = LocalDate.now().toString()
fun id() = UUID.randomUUID().toString()
fun String.number() = toDoubleOrNull()?.takeIf { it.isFinite() && it >= 0 } ?: 0.0
fun format(v:Double)=java.text.DecimalFormat("0.##").format(v)
fun rupees(v:Double)="₹"+java.text.NumberFormat.getNumberInstance(java.util.Locale("en","IN")).format(v)
fun JSONObject.fields(): Map<String,String> = keys().asSequence().associateWith { optString(it,"") }
fun String.jsonFields():Map<String,String> = JSONObject(this).fields()
fun Map<String,String>.json() = JSONObject(this).toString()

@Entity(tableName="animals",indices=[Index("name"),Index("tag")])
data class Animal(@PrimaryKey val id:String,val name:String,val species:String,val breed:String,val tag:String,val status:String,val details:String){fun fields()=details.jsonFields()+mapOf("id" to id,"name" to name,"species" to species,"breed" to breed,"tag" to tag,"status" to status)}
@Entity(tableName="care",foreignKeys=[ForeignKey(entity=Animal::class,parentColumns=["id"],childColumns=["animalId"],onDelete=ForeignKey.CASCADE)],indices=[Index("animalId"),Index("date"),Index("type")])
data class Care(@PrimaryKey val id:String,val animalId:String,val type:String,val date:String,val title:String,val due:String,val details:String){fun fields()=details.jsonFields()+mapOf("id" to id,"animal" to animalId,"type" to type,"date" to date,"title" to title,"due" to due)}
@Entity(tableName="preferences")
data class Preferences(@PrimaryKey val key:Int=1,val hindi:Boolean=false)
@Dao interface RegisterDao {
 @Query("SELECT * FROM animals ORDER BY name COLLATE NOCASE") suspend fun animals():List<Animal>
 @Query("SELECT * FROM care ORDER BY date DESC, rowid DESC") suspend fun care():List<Care>
 @Query("SELECT * FROM preferences WHERE `key`=1") suspend fun prefs():Preferences?
 @Upsert suspend fun upsertAnimal(a:Animal)
 @Upsert suspend fun upsertCare(c:Care)
 @Upsert suspend fun prefs(p:Preferences)
 @Query("DELETE FROM animals WHERE id=:id") suspend fun deleteAnimal(id:String)
 @Query("DELETE FROM care WHERE id=:id") suspend fun deleteCare(id:String)
 @Query("DELETE FROM care") suspend fun clearCare()
 @Query("DELETE FROM animals") suspend fun clearAnimals()
}
@Database(entities=[Animal::class,Care::class,Preferences::class],version=1,exportSchema=false)
abstract class RegisterDb:RoomDatabase(){abstract fun dao():RegisterDao}
data class Register(val animals:List<Animal> = emptyList(),val care:List<Care> = emptyList(),val hindi:Boolean=false){
 fun history(id:String)=care.filter{it.animalId==id}
 fun reminders():List<Care> = care.filter{it.type in listOf("health","vaccine","deworm","breeding")}.distinctBy{ Triple(it.animalId,it.type,if(it.type in listOf("vaccine","deworm")) it.title.trim().lowercase() else "") }.filter{it.due.isNotBlank()}.sortedBy{it.due}
 fun backup():String=JSONObject().put("animals",JSONArray(animals.map{JSONObject(it.fields())})).put("records",JSONArray(care.map{JSONObject(it.fields())})).put("hi",hindi).toString(2)
}
fun animalFrom(f:Map<String,String>):Animal {
 val name=f["name"].orEmpty().trim();require(name.isNotBlank()){"Enter the animal name."}
 val sp=f["species"].orEmpty();require(sp in species){"Select the species."}
 val st=f["status"].orEmpty().ifBlank{"Unknown"};require(st in statuses){"Select a health status."}
 listOf("weight","age","cost").forEach{ k -> f[k]?.takeIf{it.isNotBlank()}?.let{require(it.toDoubleOrNull()?.let{n->n.isFinite()&&n>=0}==true){"Invalid "+k}} }
 listOf("dob","purchase").forEach{ k -> f[k]?.takeIf{it.isNotBlank()}?.let{require(!LocalDate.parse(it).isAfter(LocalDate.now())){"Date cannot be in the future."}} }
 return Animal(f["id"].orEmpty().ifBlank{ id() },name,sp,f["breed"].orEmpty(),f["tag"].orEmpty().trim(),st,f.json())
}
fun careFrom(f:Map<String,String>):Care{
 val type=f["type"].orEmpty();require(type in careTypes){"Select a record type."}
 val date=f["date"].orEmpty();require(!LocalDate.parse(date).isAfter(LocalDate.now())){"Record date cannot be in the future."}
 val due=f["due"].orEmpty();if(due.isNotBlank())require(!LocalDate.parse(due).isBefore(LocalDate.parse(date))){"Next date must be on or after record date."}
 val title=f["title"].orEmpty().trim();if(type!="production")require(title.isNotBlank()){"Enter the record description."}
 if(type=="production")require(listOf("milk","weight","eggs").any{!f[it].isNullOrBlank()}){"Enter at least one measurement."}
 if(type=="finance")require(!f["amount"].isNullOrBlank()&&f["direction"] in listOf("Income","Expense")){"Enter amount and transaction type."}
 listOf("milk","weight","eggs","amount","temperature","quantity","cost").forEach{k->f[k]?.takeIf{it.isNotBlank()}?.let{require(it.toDoubleOrNull()?.let{n->n.isFinite()&&n>=0}==true){"Invalid "+k}}}
 return Care(f["id"].orEmpty().ifBlank{id()},f["animal"].orEmpty(),type,date,title,due,f.json())
}
fun parseBackup(raw:String):Register{
 val x=JSONObject(raw);val a=x.getJSONArray("animals");val c=x.getJSONArray("records");require(a.length()<=50000&&c.length()<=200000){"Backup too large."}
 val animals=(0 until a.length()).map{animalFrom(a.getJSONObject(it).fields())}
 val care=(0 until c.length()).map{careFrom(c.getJSONObject(it).fields())}
 require(animals.map{it.id}.distinct().size==animals.size&&care.map{it.id}.distinct().size==care.size){"Duplicate IDs."}
 val tags=animals.map{it.tag.lowercase()}.filter{it.isNotBlank()};require(tags.distinct().size==tags.size){"Duplicate ear tags."}
 val ids=animals.map{it.id}.toSet();require(care.all{it.animalId in ids}){"Missing animal for care record."}
 return Register(animals,care,x.optBoolean("hi"))
}
class RegisterRepository(private val app:Application){
 private val db=Room.databaseBuilder(app,RegisterDb::class.java,"pashusetu-room-v1").build()
 private val dao=db.dao()
 suspend fun snapshot()=db.withTransaction{ Register(dao.animals(),dao.care(),dao.prefs()?.hindi?:false) }
 suspend fun initialize()=withContext(Dispatchers.IO){db.withTransaction{if(dao.prefs()==null){val legacy=RegisterStore(app).read();val old=parseBackup(legacy.toString());old.animals.forEach{dao.upsertAnimal(it)};old.care.forEach{dao.upsertCare(it)};dao.prefs(Preferences(hindi=old.hindi))}};snapshot()}
 suspend fun putAnimal(a:Animal)=db.withTransaction{require(a.tag.isBlank()||dao.animals().none{it.id!=a.id&&it.tag.equals(a.tag,true)}){"Ear tag already exists."};dao.upsertAnimal(a)}
 suspend fun putCare(c:Care)=db.withTransaction{require(dao.animals().any{it.id==c.animalId}){"Choose an animal."};dao.upsertCare(c)}
 suspend fun deleteAnimal(id:String)=dao.deleteAnimal(id)
 suspend fun deleteCare(id:String)=dao.deleteCare(id)
 suspend fun language(hi:Boolean)=dao.prefs(Preferences(hindi=hi))
 suspend fun replace(r:Register)=db.withTransaction{dao.clearCare();dao.clearAnimals();r.animals.forEach{dao.upsertAnimal(it)};r.care.forEach{dao.upsertCare(it)};dao.prefs(Preferences(hindi=r.hindi))}
}
data class UiState(val register:Register=Register(),val loading:Boolean=true,val saving:Boolean=false,val error:String?=null,val ready:Boolean=false)
class RegisterModel(app:Application):AndroidViewModel(app){
 private val repo=RegisterRepository(app)
 private val mutable=MutableStateFlow(UiState());val state=mutable.asStateFlow()
 init{load()}
 fun load(){if(mutable.value.saving)return;mutable.value=mutable.value.copy(loading=true,error=null);viewModelScope.launch{try{mutable.value=UiState(repo.initialize(),loading=false,ready=true)}catch(e:Exception){mutable.value=mutable.value.copy(loading=false,error=e.message?:"Unable to open records.")}}}
 fun clearError(){mutable.value=mutable.value.copy(error=null)}
 fun mutate(op:suspend()->Unit,done:()->Unit={}){if(mutable.value.saving)return;mutable.value=mutable.value.copy(saving=true,error=null);viewModelScope.launch{try{withContext(Dispatchers.IO){op()};mutable.value=UiState(repo.snapshot(),loading=false,ready=true);done()}catch(e:Exception){mutable.value=mutable.value.copy(saving=false,error=e.message?:"Unable to save records.")}}}
 fun saveAnimal(fields:Map<String,String>,done:(String)->Unit){mutate({repo.putAnimal(animalFrom(fields))},{done(fields["id"].orEmpty())})}
 fun saveCare(fields:Map<String,String>,done:()->Unit){mutate({repo.putCare(careFrom(fields))},done)}
 fun removeAnimal(id:String,done:()->Unit)=mutate({repo.deleteAnimal(id)},done)
 fun removeCare(id:String)=mutate({repo.deleteCare(id)})
 fun language(hi:Boolean)=mutate({repo.language(hi)})
 fun restore(r:Register,done:()->Unit)=mutate({repo.replace(r)},done)
 fun demo(){if(mutable.value.register.animals.isNotEmpty())return;mutate({val a=listOf("Gauri","Kali","Chutki").mapIndexed{i,n->animalFrom(mapOf("name" to n,"species" to listOf("Cow","Buffalo","Goat")[i],"breed" to listOf("Gir","Murrah","Sirohi")[i],"tag" to "DEMO-0"+(i+1),"status" to if(i==2)"Recovering" else "Healthy","weight" to listOf("350","480","28")[i]))};val c=a.flatMapIndexed{i,x->(0..6).map{d->careFrom(mapOf("animal" to x.id,"type" to "production","date" to LocalDate.now().minusDays((6-d).toLong()).toString(),"weight" to format(listOf(345.0,474.0,27.0)[i]+d*.6),"milk" to if(i<2)format(6.0+i+d*.3)else ""))}};repo.replace(Register(a,c,mutable.value.register.hindi))})}
}
