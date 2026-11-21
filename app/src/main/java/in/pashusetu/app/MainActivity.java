package in.pashusetu.app;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.graphics.Color;
import android.webkit.*;
import android.net.Uri;
import android.widget.Toast;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class MainActivity extends Activity {
    private WebView web;
    private String pendingData;
    private static final int SAVE = 17;
    private static final String HOME = "https://appassets.androidplatform.net/index.html";

    @Override public void onCreate(Bundle saved) {
        super.onCreate(saved);
        getWindow().setStatusBarColor(Color.rgb(19,63,49));
        getWindow().setNavigationBarColor(Color.rgb(19,63,49));
        web = new WebView(this);
        setContentView(web);
        WebSettings settings = web.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        settings.setJavaScriptCanOpenWindowsAutomatically(false);
        web.setWebChromeClient(new WebChromeClient());
        web.addJavascriptInterface(new ExportBridge(), "PashuAndroid");
        web.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return !HOME.equals(request.getUrl().toString());
            }
            @Override public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                if (HOME.equals(request.getUrl().toString())) {
                    try { return new WebResourceResponse("text/html", "UTF-8", getAssets().open("index.html")); }
                    catch (IOException e) { return new WebResourceResponse("text/plain", "UTF-8", new ByteArrayInputStream("App asset missing".getBytes(StandardCharsets.UTF_8))); }
                }
                return new WebResourceResponse("text/plain", "UTF-8", new ByteArrayInputStream(new byte[0]));
            }
        });
        if(saved != null) pendingData = saved.getString("pendingData");
        web.loadUrl(HOME);
    }
    public class ExportBridge {
        @JavascriptInterface public void save(String name, String data, String mime) {
            runOnUiThread(() -> {
                if(pendingData != null) return;
                pendingData = data;
                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType(name.endsWith(".csv") ? "text/csv" : "application/json");
                intent.putExtra(Intent.EXTRA_TITLE, name);
                try { startActivityForResult(intent, SAVE); }
                catch (Exception e) { pendingData = null; Toast.makeText(MainActivity.this,"No file picker available",Toast.LENGTH_LONG).show(); }
            });
        }
    }
    @Override protected void onActivityResult(int request, int result, Intent data) {
        super.onActivityResult(request,result,data);
        if(request != SAVE) return;
        if(result == RESULT_OK && data != null && data.getData() != null && pendingData != null) {
            try(OutputStream out = getContentResolver().openOutputStream(data.getData())) {
                if(out == null) throw new IOException("No output stream");
                out.write(pendingData.getBytes(StandardCharsets.UTF_8));
                Toast.makeText(this,"Backup saved",Toast.LENGTH_SHORT).show();
            } catch(IOException e) { Toast.makeText(this,"Could not save backup",Toast.LENGTH_LONG).show(); }
        }
        pendingData = null;
    }
    @Override public void onBackPressed() {
        web.evaluateJavascript("(function(){const d=document.querySelector('#modal'); if(d&&d.open){d.close();return true;} if(page==='profile'){go('animals');return true;} if(page!=='home'){go('home');return true;} return false;})()", result -> {
            if(!"true".equals(result)) super.onBackPressed();
        });
    }
    @Override protected void onDestroy() { if(web != null){web.removeJavascriptInterface("PashuAndroid");web.destroy();} super.onDestroy(); }
}