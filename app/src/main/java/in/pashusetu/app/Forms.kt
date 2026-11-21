package `in`.pashusetu.app

import android.app.DatePickerDialog
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate

data class FormField(val key:String,val label:String,val hindi:String,val type:String="text",val required:Boolean=false,val choices:List<String> = emptyList())
val animalFields=listOf(
 FormField("name","Animal name","पशु का नाम",required=true),
 FormField("species","Species","पशु प्रकार","choice",true,species),
 FormField("breed","Breed","नस्ल"),
 FormField("tag","Ear tag / ID","कान का टैग"),
 FormField("sex","Sex","लिंग","choice",choices=listOf("Unknown","Female","Male")),
 FormField("dob","Date of birth","जन्म तिथि","date"),
 FormField("age","Estimated age (months)","उम्र (महीने)","number"),
 FormField("weight","Body weight (kg)","वजन (किलो)","number"),
 FormField("color","Colour / markings","रंग / पहचान"),
 FormField("status","Health status","स्वास्थ्य","choice",true,statuses),
 FormField("pregnancy","Pregnancy","गर्भावस्था","choice",choices=listOf("Unknown","Not applicable","Not pregnant","Pregnant")),
 FormField("owner","Owner name","मालिक का नाम"),
 FormField("phone","Mobile number","मोबाइल नंबर","phone"),
 FormField("village","Village","गांव"),
 FormField("address","Address","पता","multi"),
 FormField("purchase","Purchase date","खरीद तिथि","date"),
 FormField("cost","Purchase cost (₹)","खरीद खर्च (₹)","number"),
 FormField("notes","Other notes","अन्य जानकारी","multi")
)
fun recordFields(k:String):List<FormField> = (when(k){
 "health"->listOf(FormField("title","Reason / diagnosis","कारण / निदान",required=true),FormField("symptoms","Symptoms","लक्षण","multi"),FormField("treatment","Treatment provided","उपचार","multi"),FormField("medicine","Vet-prescribed medicine and dose","डॉक्टर की दवा और मात्रा","multi"),FormField("temperature","Temperature (°C)","तापमान (°C)","number"),FormField("vet","Veterinarian","पशु चिकित्सक"),FormField("due","Follow-up date","अगली जांच","future"))
 "vaccine","deworm"->listOf(FormField("title",if(k=="vaccine")"Vaccine name" else "Deworming medicine",if(k=="vaccine")"टीका" else "कृमिनाशक दवा",required=true),FormField("batch","Batch number","बैच नंबर"),FormField("medicine","Dose given by vet","डॉक्टर की मात्रा"),FormField("vet","Veterinarian","पशु चिकित्सक"),FormField("due","Next due (vet advised)","अगली तारीख","future"))
 "breeding"->listOf(FormField("title","Event description","घटना",required=true),FormField("event","Event type","प्रकार","choice",choices=listOf("Heat","Insemination","Pregnancy check","Birth","Other")),FormField("due","Expected birth / follow-up","संभावित प्रसव / जांच","future"),FormField("offspring","Offspring details","बच्चों की जानकारी","multi"),FormField("vet","Veterinarian","पशु चिकित्सक"))
 "production"->listOf(FormField("milk","Milk (litres)","दूध (लीटर)","number"),FormField("weight","Body weight (kg)","वजन (किलो)","number"),FormField("eggs","Eggs (count)","अंडे","number"))
 "feed"->listOf(FormField("title","Feed type","चारा",required=true),FormField("quantity","Quantity (kg)","मात्रा (किलो)","number"),FormField("schedule","Feeding schedule","खिलाने का समय"),FormField("cost","Feed cost (₹)","चारे का खर्च (₹)","number"))
 "finance"->listOf(FormField("title","Description","विवरण",required=true),FormField("direction","Transaction type","लेनदेन","choice",true,listOf("Income","Expense")),FormField("amount","Amount (₹)","राशि (₹)","number",true))
 else->emptyList()
})+FormField("notes","Notes","टिप्पणी","multi")
@Composable fun optionLabel(key:String,value:String):String=when(key){
 "species"->spName(value);"status"->stName(value);else->tr(value,mapOf("Unknown" to "अज्ञात","Female" to "मादा","Male" to "नर","Not applicable" to "लागू नहीं","Not pregnant" to "गर्भवती नहीं","Pregnant" to "गर्भवती","Income" to "आय","Expense" to "खर्च","Heat" to "हीट","Insemination" to "गर्भाधान","Pregnancy check" to "गर्भ जांच","Birth" to "प्रसव","Other" to "अन्य")[value]?:value)
}
@Composable fun FieldInput(f:FormField,value:String,onChange:(String)->Unit,customLabels:List<String>?=null){val label=tr(f.label,f.hindi)+(if(f.required)" *"else "");when(f.type){
 "choice"->{var expanded by remember{mutableStateOf(false)};Column{Text(label,fontSize=13.sp,fontWeight=FontWeight.Medium);Spacer(Modifier.height(6.dp));Box{OutlinedButton(onClick={expanded=true},modifier=Modifier.fillMaxWidth().heightIn(min=54.dp),shape=RoundedCornerShape(14.dp),border=BorderStroke(1.dp,Line),contentPadding=PaddingValues(horizontal=15.dp)){Text(customLabels?.getOrNull(f.choices.indexOf(value))?:optionLabel(f.key,value).ifBlank{tr("Choose","चुनें")},modifier=Modifier.weight(1f),color=Ink);Icon(Icons.Outlined.ExpandMore,null,tint=Muted)};DropdownMenu(expanded,{expanded=false}){f.choices.forEachIndexed{i,v->DropdownMenuItem(text={Text(customLabels?.getOrNull(i)?:optionLabel(f.key,v))},onClick={onChange(v);expanded=false})}}}}}
 "date","future"->{val context=LocalContext.current;Column{Text(label,fontSize=13.sp,fontWeight=FontWeight.Medium);Spacer(Modifier.height(6.dp));Row(verticalAlignment=Alignment.CenterVertically){OutlinedButton(onClick={val d=runCatching{LocalDate.parse(value)}.getOrDefault(LocalDate.now());val picker=DatePickerDialog(context,{_,y,m,day->onChange(LocalDate.of(y,m+1,day).toString())},d.year,d.monthValue-1,d.dayOfMonth);if(f.type=="date")picker.datePicker.maxDate=System.currentTimeMillis();picker.show()},modifier=Modifier.weight(1f).heightIn(min=54.dp),shape=RoundedCornerShape(14.dp),border=BorderStroke(1.dp,Line)){Icon(Icons.Outlined.CalendarMonth,null);Spacer(Modifier.width(8.dp));Text(value.ifBlank{tr("Choose date","तारीख चुनें")})};if(value.isNotBlank()&&!f.required)IconButton(onClick={onChange("")}){Icon(Icons.Outlined.Close,"Clear date")}}}}
 else->OutlinedTextField(value,onValueChange={if(it.length<=if(f.type=="multi")3000 else 240)onChange(it)},modifier=Modifier.fillMaxWidth(),label={Text(label)},shape=RoundedCornerShape(14.dp),singleLine=f.type!="multi",minLines=if(f.type=="multi")3 else 1,keyboardOptions=KeyboardOptions(keyboardType=when(f.type){"number"->KeyboardType.Decimal;"phone"->KeyboardType.Phone;else->KeyboardType.Text}),colors=OutlinedTextFieldDefaults.colors(unfocusedBorderColor=Line,focusedBorderColor=Green))
}}
@OptIn(ExperimentalMaterial3Api::class)
@Composable fun FormShell(title:String,description:String,busy:Boolean,back:()->Unit,primary:String,onNext:()->Unit,secondary:String?=null,onSecondary:(()->Unit)?=null,content:@Composable ColumnScope.()->Unit){var confirm by remember{mutableStateOf(false)};BackHandler(enabled=true){if(!busy)confirm=true};Scaffold(containerColor=CanvasBg,topBar={TopAppBar(title={Text(title,maxLines=1)},navigationIcon={IconButton(onClick={if(!busy)confirm=true}){Icon(Icons.Outlined.ArrowBack,"Back")}},colors=TopAppBarDefaults.topAppBarColors(containerColor=CanvasBg))},bottomBar={Surface(color=SurfaceWhite,tonalElevation=1.dp){Row(Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp),horizontalArrangement=Arrangement.spacedBy(10.dp)){if(secondary!=null&&onSecondary!=null)OutlinedButton(onClick=onSecondary,enabled=!busy,modifier=Modifier.weight(.8f).heightIn(min=52.dp),shape=RoundedCornerShape(14.dp)){Text(secondary)};Button(onClick=onNext,enabled=!busy,modifier=Modifier.weight(1.5f).heightIn(min=52.dp),shape=RoundedCornerShape(14.dp)){if(busy){CircularProgressIndicator(Modifier.size(19.dp),strokeWidth=2.dp,color=SurfaceWhite);Spacer(Modifier.width(8.dp))};Text(primary)}}}}){pad->Column(Modifier.fillMaxSize().padding(pad).imePadding().verticalScroll(rememberScrollState()).padding(horizontal=20.dp).padding(top=8.dp,bottom=22.dp),verticalArrangement=Arrangement.spacedBy(16.dp)){Text(description,color=Muted,fontSize=13.sp);content()}}
 if(confirm)AlertDialog(onDismissRequest={confirm=false},title={Text(tr("Discard unsaved changes?","बदलाव छोड़ें?"))},text={Text(tr("Your existing records will not change.","पुराने रिकॉर्ड नहीं बदलेंगे।"))},confirmButton={TextButton(onClick={confirm=false;back()}){Text(tr("Discard","छोड़ें"),color=Rust)}},dismissButton={TextButton(onClick={confirm=false}){Text(tr("Keep editing","जारी रखें"))}})
}
@Composable fun AnimalForm(animal:Animal?,busy:Boolean,back:()->Unit,save:(Map<String,String>)->Unit){
 var draft by rememberSaveable(animal?.id){mutableStateOf(HashMap(animal?.fields()?:mapOf("id" to id(),"species" to "Cow","status" to "Unknown","sex" to "Unknown","pregnancy" to "Unknown")))}
 var step by rememberSaveable{mutableIntStateOf(0)};var error by rememberSaveable{mutableStateOf("")}
 val steps=listOf(tr("Animal identity","पशु पहचान"),tr("Body & health","शरीर और स्वास्थ्य"),tr("Owner & details","मालिक और विवरण"));val errorName=tr("Enter the animal name.","पशु का नाम भरें।");val invalid=tr("Check the entered values.","दर्ज जानकारी जांचें।")
 FormShell(if(animal==null)tr("Register animal","पशु जोड़ें")else tr("Edit animal","पशु बदलें"),tr("Only the essential information is required. You can add more later.","केवल आवश्यक जानकारी भरें। बाकी बाद में जोड़ें।"),busy,back,if(step<2)tr("Continue","आगे")else tr("Save animal","पशु सहेजें"),onNext={
  error="";if(step==0&&draft["name"].isNullOrBlank())error=errorName else if(step<2)step++ else{try{animalFrom(draft);save(draft)}catch(e:Exception){error=e.message?:invalid}}
 },secondary=if(step>0)tr("Back","पीछे")else null,onSecondary=if(step>0){{step--;error=""}}else null){
 Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){repeat(3){i->Box(Modifier.weight(1f).height(5.dp).clip(RoundedCornerShape(10.dp)).background(if(i<=step)Green else Line))}}
 Text(tr("STEP ${step+1} OF 3","चरण ${step+1} / 3"),fontSize=11.sp,letterSpacing=1.3.sp,color=Green,fontWeight=FontWeight.Bold);Text(steps[step],style=MaterialTheme.typography.headlineMedium)
 val fields=when(step){0->animalFields.subList(0,5);1->animalFields.subList(5,11);else->animalFields.subList(11,animalFields.size)}
 Sheet{fields.forEach{f->FieldInput(f,draft[f.key].orEmpty(),{v->draft=HashMap(draft).apply{put(f.key,v)};error=""});Spacer(Modifier.height(13.dp))}}
 if(error.isNotBlank())Text(error,color=Rust,fontSize=13.sp)
 }}
@Composable fun CareForm(kind:String,aid:String?,data:Register,record:Care?,busy:Boolean,back:()->Unit,save:(Map<String,String>)->Unit){
 val chosen=data.animals.find{it.id==aid}?.id?:data.animals.firstOrNull()?.id.orEmpty()
 var draft by rememberSaveable(record?.id,kind,aid){mutableStateOf(HashMap(record?.fields()?:mapOf("id" to id(),"animal" to chosen,"date" to today(),"type" to kind,"direction" to "Income","event" to "Other")))}
 var error by rememberSaveable{mutableStateOf("")};val invalid=tr("Check the entered values.","दर्ज जानकारी जांचें।");val name=typeName(kind)
 FormShell(name,tr("Keep a dated record of the care you provide.","देखभाल की तारीख और जानकारी दर्ज करें।"),busy,back,tr("Save record","रिकॉर्ड सहेजें"),onNext={try{careFrom(draft);error="";save(draft)}catch(e:Exception){error=e.message?:invalid}}){
 Sheet{Row(verticalAlignment=Alignment.CenterVertically){IconTile(kind,Green,Mint,48);Spacer(Modifier.width(12.dp));Column{Text(name,style=MaterialTheme.typography.titleMedium);Text(tr("Animal care entry","पशु देखभाल रिकॉर्ड"),color=Muted,fontSize=12.sp)}};Spacer(Modifier.height(18.dp));FieldInput(FormField("animal","Animal","पशु","choice",true,data.animals.map{it.id}),draft["animal"].orEmpty(),{v->draft=HashMap(draft).apply{put("animal",v)}},data.animals.map{it.name+if(it.tag.isBlank())""else " · "+it.tag});Spacer(Modifier.height(13.dp));FieldInput(FormField("date","Record date","रिकॉर्ड की तारीख","date",true),draft["date"].orEmpty(),{v->draft=HashMap(draft).apply{put("date",v)}})}
 Sheet{Text(tr("Care details","देखभाल का विवरण"),style=MaterialTheme.typography.titleMedium);Spacer(Modifier.height(13.dp));recordFields(kind).forEach{f->FieldInput(f,draft[f.key].orEmpty(),{v->draft=HashMap(draft).apply{put(f.key,v)};error=""});Spacer(Modifier.height(13.dp))}}
 if(kind in listOf("health","vaccine","deworm","breeding"))Text(tr("Record medication, dose and next date only as advised by a veterinarian.","दवा, मात्रा और अगली तारीख डॉक्टर की सलाह से दर्ज करें।"),color=Muted,fontSize=12.sp)
 if(error.isNotBlank())Text(error,color=Rust,fontSize=13.sp)
 }}
