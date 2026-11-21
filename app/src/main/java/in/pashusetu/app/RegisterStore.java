package in.pashusetu.app;

import android.content.Context;
import android.util.AtomicFile;
import org.json.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

final class RegisterStore {
    private final AtomicFile file;
    static final int LIMIT=20*1024*1024;
    RegisterStore(Context c){file=new AtomicFile(new File(c.getFilesDir(),"register-v3.json"));}
    static JSONObject empty() throws JSONException { return new JSONObject().put("animals",new JSONArray()).put("records",new JSONArray()).put("hi",false); }
    JSONObject read() throws Exception {
        if(!file.getBaseFile().exists()&&!new File(file.getBaseFile().getPath()+".bak").exists()) return empty();
        try(InputStream in=file.openRead()){return validate(new JSONObject(readText(in)));}
    }
    void write(JSONObject data) throws Exception {
        byte[] bytes=data.toString().getBytes(StandardCharsets.UTF_8);
        if(bytes.length>LIMIT) throw new IOException("Register is too large. Export and archive older records.");
        FileOutputStream out=null;
        try{out=file.startWrite();out.write(bytes);file.finishWrite(out);}
        catch(Exception e){if(out!=null)file.failWrite(out);throw e;}
    }
    static String readText(InputStream in) throws IOException {
        if(in==null)throw new IOException("File unavailable");
        ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] buf=new byte[8192];int n;
        while((n=in.read(buf))!=-1){if(out.size()+n>LIMIT)throw new IOException("Backup exceeds 20 MB");out.write(buf,0,n);}
        return out.toString("UTF-8");
    }
    static JSONObject copy(JSONObject o)throws JSONException{return new JSONObject(o.toString());}
    static ArrayList<JSONObject> rows(JSONArray a){ArrayList<JSONObject> r=new ArrayList<>();if(a!=null)for(int i=0;i<a.length();i++){JSONObject v=a.optJSONObject(i);if(v!=null)r.add(v);}return r;}
    static String str(JSONObject o,String key){return o==null?"":o.optString(key,"");}
    static String id(){return UUID.randomUUID().toString();}
    static double num(JSONObject o,String k){try{double d=Double.parseDouble(str(o,k));return Double.isFinite(d)&&d>=0?d:0;}catch(Exception e){return 0;}}
    static boolean date(String s){try{java.time.LocalDate.parse(s);return true;}catch(Exception e){return false;}}
    static JSONObject validate(JSONObject x)throws Exception{
        JSONArray a=x.optJSONArray("animals"),r=x.optJSONArray("records");
        if(a==null||r==null||a.length()>50000||r.length()>200000)throw new IOException("Invalid register format");
        Set<String> ids=new HashSet<>(),tags=new HashSet<>(),recs=new HashSet<>();
        for(int i=0;i<a.length();i++){
            JSONObject o=a.optJSONObject(i);if(o==null)throw new IOException("Invalid animal");
            String id=str(o,"id"),name=str(o,"name"),tag=str(o,"tag").trim().toLowerCase(Locale.ROOT);
            if(id.isEmpty()||!ids.add(id)||name.trim().isEmpty())throw new IOException("Missing or duplicate animal identity");
            if(!tag.isEmpty()&&!tags.add(tag))throw new IOException("Duplicate ear tag");
            if(!Arrays.asList("Cow","Buffalo","Goat","Sheep","Rabbit","Poultry","Dog","Other").contains(str(o,"species")))throw new IOException("Unknown species");
            if(!Arrays.asList("Healthy","Sick","Recovering","Unknown").contains(str(o,"status")))throw new IOException("Invalid health status");
        }
        for(int i=0;i<r.length();i++){
            JSONObject o=r.optJSONObject(i);if(o==null)throw new IOException("Invalid record");
            String id=str(o,"id"),type=str(o,"type");
            if(id.isEmpty()||!recs.add(id)||!ids.contains(str(o,"animal"))||!date(str(o,"date"))||!Arrays.asList("health","vaccine","breeding","production","feed","finance","deworm").contains(type))throw new IOException("Invalid care record");
            if(!str(o,"due").isEmpty()&&!date(str(o,"due")))throw new IOException("Invalid due date");
        }
        return x;
    }
}
