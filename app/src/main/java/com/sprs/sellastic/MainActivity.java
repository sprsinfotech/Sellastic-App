package com.sprs.sellastic;
import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.URLUtil;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.sprs.sellastic.onboarding.OnboardingActivity;

public class MainActivity extends AppCompatActivity {
    String prevStart = "prevStart";
    ProgressBar progressbar;
    WebView webview;
    private String token;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences sharedpreferences = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);
        String fcmtoken = sharedpreferences.getString("fcmtoken", "");

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                            return;
                        }
                        // Get new FCM registration token
                        token = task.getResult();
                    }
                });


        if (!sharedpreferences.getBoolean(prevStart, false)){
            startActivity(new Intent(this, OnboardingActivity.class));
        }

        FirebaseMessaging.getInstance().subscribeToTopic("allDevices");

        //now Let's see server code
        webview=findViewById(R.id.webview);
        progressbar=findViewById(R.id.progressbar);
        WebSettings webSettings = webview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);

        webview.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressbar.setVisibility(View.VISIBLE);
            }
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressbar.setVisibility(View.GONE);
            }
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);

                if (url.startsWith("http") || url.startsWith("https")) {
                    return true;
                }else if(url.startsWith("whatsapp://")){
                    webview.stopLoading();
                    try {
                        Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
                        whatsappIntent.setType("text/plain");
                        whatsappIntent.setPackage("com.whatsapp");
                        whatsappIntent.putExtra(Intent.EXTRA_TEXT, webview.getUrl() + "  - by SPRS");
                        startActivity(whatsappIntent);
                    } catch (android.content.ActivityNotFoundException ex) {
                        String MakeShortText = "Whatsapp have not been installed";
                        Toast.makeText(MainActivity.this, MakeShortText, Toast.LENGTH_SHORT).show();
                    }
                }else if(url.startsWith("market://")){
                    webview.stopLoading();
                    //  Toast.makeText(MainActivity.this, "Unknown Link, unable to handle", Toast.LENGTH_SHORT).show();
                }else if(url.startsWith("shareall://")){
                    webview.stopLoading();
                    try {
                        Intent sendIntent = new Intent();
                        sendIntent.setAction(Intent.ACTION_SEND);
                        sendIntent.putExtra(Intent.EXTRA_TEXT, webview.getUrl() +"  - by SPRS");
                        sendIntent.setType("text/plain");
                        Intent shareIntent = Intent.createChooser(sendIntent, null);
                        startActivity(shareIntent);
                    } catch (android.content.ActivityNotFoundException ex) {
                        webview.goBack();
                        Toast.makeText(MainActivity.this, "Unknown Link, unable to handle", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    webview.stopLoading();
                    try {
                        Intent sendIntent = new Intent();
                        sendIntent.setAction(Intent.ACTION_SEND);
                        sendIntent.putExtra(Intent.EXTRA_TEXT, webview.getUrl() +"  - by SPRS");
                        sendIntent.setType("text/plain");
                        Intent shareIntent = Intent.createChooser(sendIntent, null);
                        startActivity(shareIntent);
                    } catch (android.content.ActivityNotFoundException ex) {
                        webview.goBack();
                        Toast.makeText(MainActivity.this, "Unknown Link, unable to handle", Toast.LENGTH_SHORT).show();
                    }
                }
                return false;
            }
        });
        if (token==""){
            Toast.makeText(MainActivity.this, "Multiple Device found. Contact Admin", Toast.LENGTH_SHORT).show();
            this.finishAffinity();
        }else{
            webview.loadUrl("https://app.sellasticopvtltd.com/api?token="+token);
        }
    }

    @Override
    public void onBackPressed() {
        if(webview.canGoBack()){
            webview.goBack();
        }else{
            finish();
        }
    }
    @Override
    public void onResume() {
        super.onResume();
    }
    @Override
    protected void onStart() {
        super.onStart();
    }

}
