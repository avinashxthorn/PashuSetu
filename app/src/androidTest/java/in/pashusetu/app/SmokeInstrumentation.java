package in.pashusetu.app;

import android.app.*;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.util.*;

public class SmokeInstrumentation extends Instrumentation {
    private Activity activity;
    @Override public void onCreate(Bundle args){super.onCreate(args);start();}
    @Override public void onStart(){
        Bundle result=new Bundle();
        try{
            Intent i=new Intent(Intent.ACTION_MAIN).setClassName(getTargetContext(),"in.pashusetu.app.MainActivity").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity=startActivitySync(i);
            waitFor("Farm workspace"); shot("qa-dashboard-empty.png");
            click("Register animal");waitFor("Step 1 / 3"); 
            fill(0,"Gauri QA");fill(1,"Gir");fill(2,"QA-0001");
            click("Continue ›");waitFor("Step 2 / 3");click("Continue ›");waitFor("Step 3 / 3");click("Save record");
            waitFor("Gauri QA");waitFor("Daily care");shot("qa-passport.png");
            click("Milk & growth");waitFor("Milk (litres)");fill(0,"8.5");fill(1,"350");click("Save record");waitFor("350 kg");
            click("Growth");waitFor("Weight history · kg");shot("qa-growth.png");
            click("Home");waitFor("Farm workspace");waitFor("Gauri QA");shot("qa-dashboard.png");
            click("Reports");waitFor("Farm reports");waitFor("Recorded herd milk · litres/day");shot("qa-reports.png");
            runOnMainSync(()->activity.finish());waitForIdleSync();
            activity=startActivitySync(i);waitFor("Farm workspace");waitFor("Gauri QA");
            click("Animals");waitFor("My animals");waitFor("Gauri QA");click("Gauri QA");waitFor("350 kg");
            click("Vaccination");waitFor("Vaccine name");fill(0,"QA vaccine");click("Save record");waitFor("QA vaccine");
            result.putString("stream","\nNATIVE_SMOKE_PASS: startup, registration, persistence, measurements, vaccination, navigation and charts.\n");finish(Activity.RESULT_OK,result);
        }catch(Throwable e){result.putString("stream","\nNATIVE_SMOKE_FAIL: "+e+"\n");finish(Activity.RESULT_CANCELED,result);}
    }
    private View find(View v,String text){if(v instanceof TextView&&((TextView)v).getText().toString().contains(text))return v;if(v.getContentDescription()!=null&&v.getContentDescription().toString().equals(text))return v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int j=0;j<g.getChildCount();j++){View hit=find(g.getChildAt(j),text);if(hit!=null)return hit;}}return null;}
    private View lookup(String s){final View[] v={null};runOnMainSync(()->v[0]=find(activity.getWindow().getDecorView(),s));return v[0];}
    private View waitFor(String s)throws Exception{long deadline=SystemClock.uptimeMillis()+15000;while(SystemClock.uptimeMillis()<deadline){View v=lookup(s);if(v!=null)return v;SystemClock.sleep(100);}throw new AssertionError("Missing screen text: "+s);}
    private void click(String s)throws Exception{View hit=waitFor(s);runOnMainSync(()->{View v=hit;while(v!=null&&!v.isClickable())v=v.getParent() instanceof View?(View)v.getParent():null;if(v==null||!v.performClick())throw new AssertionError("Not clickable: "+s);});waitForIdleSync();}
    private void edits(View v,List<EditText> out){if(v instanceof EditText)out.add((EditText)v);if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int j=0;j<g.getChildCount();j++)edits(g.getChildAt(j),out);}}
    private void fill(int index,String value){runOnMainSync(()->{List<EditText> list=new ArrayList<>();edits(activity.getWindow().getDecorView(),list);if(index>=list.size())throw new AssertionError("Missing input "+index);list.get(index).setText(value);});}
    private void shot(String file)throws Exception{waitForIdleSync();Bitmap b=getUiAutomation().takeScreenshot();if(b==null)return;try(FileOutputStream out=new FileOutputStream(new File(getTargetContext().getFilesDir(),file))){b.compress(Bitmap.CompressFormat.PNG,100,out);}finally{b.recycle();}}
}
