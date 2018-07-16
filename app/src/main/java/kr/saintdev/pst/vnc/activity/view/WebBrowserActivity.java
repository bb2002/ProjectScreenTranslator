package kr.saintdev.pst.vnc.activity.view;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;

import kr.saintdev.pst.R;
/**
 * Copyright (c) 2015-2018 Saint software All rights reserved.
 *
 * @Date 2018-06-08
 */

public class WebBrowserActivity extends AppCompatActivity {
    private String targetUrl = null;
    private WebView webView = null;
    private ImageButton closeButton = null;      // 대시보드를 나간다.
    private ValueCallback<Uri[]> filePathCallbackForFive;
    private static final int REQUEST_FILE_UPLOAD = 0x0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_browser);

        Intent intent = getIntent();
        this.targetUrl = intent.getStringExtra("url");

        if(this.targetUrl == null) {
            finish();
            return;
        }

        // 웹뷰 객체를 찾습니다.
        this.webView = findViewById(R.id.browser_webview);
        this.closeButton = findViewById(R.id.browser_exit);
        this.closeButton.setOnClickListener(new OnDashboardExitListener());

        // WebView 를 setting 합니다.
        WebSettings settings = this.webView.getSettings();
        settings.setJavaScriptEnabled(true);        // 자바스크립트 허용
        webView.setWebViewClient(new WebViewClient());
        webView.setWebViewClient(new WebViewClientService());

        WebChromeClient webChromeClient = new WebChromeClient() {

            /*
            Android 5.0 에서 html 폼으로 이미지를 보내기 위한 작업
             */
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                if(filePathCallbackForFive != null) {
                    filePathCallbackForFive.onReceiveValue(null);
                    filePathCallbackForFive = null;
                }

                filePathCallbackForFive = filePathCallback;

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");

                startActivityForResult(Intent.createChooser(intent, "Select image"), REQUEST_FILE_UPLOAD);
                return true;
            }
        };
        this.webView.setWebChromeClient(webChromeClient);

        // 페이지를 로드합니다.
        webView.loadUrl(this.targetUrl);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_FILE_UPLOAD) {
            if(filePathCallbackForFive == null) return;

            // Chrome Client 으로 업로드 데이터를 보냅니다.
            filePathCallbackForFive.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data));
            filePathCallbackForFive = null;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack();
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    private class WebViewClientService extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if(url.startsWith("sms:")) {
                // SMS 를 보냅니다.
                Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(url));
                startActivity(intent);
                return true;
            }

            return false;
        }
    }

    class OnDashboardExitListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            finish();
        }
    }
}
