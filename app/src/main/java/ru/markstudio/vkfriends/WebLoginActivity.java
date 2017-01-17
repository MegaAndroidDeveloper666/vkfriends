package ru.markstudio.vkfriends;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.regex.Pattern;

import data.VKAccount;

// Активити, с помощью которого пользователь логинится через свой аккаунт вконтакте с помощью формы вк
public class WebLoginActivity extends AppCompatActivity {

    WebView webLogin;

    String loginUrl = "https://oauth.vk.com/authorize?client_id=5820389&display=popup&redirect_uri=https://oauth.vk.com/blank.html&scope=1030&response_type=token&v=5.62";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_login);

        initViews();
    }

    // Инициализируем вьюшки
    private void initViews() {
        webLogin = (WebView)findViewById(R.id.webview_login);

        // Чистим кэш, историю и куки, это нужно для того, чтобы при смене пользователя вэбформа не помнила, что
        // уже был залогинен какой то пользователь и чтобы вк не давал в таком случае старый токен
        webLogin.clearCache(true);
        webLogin.clearHistory();
        clearCookies();

        // Загружаем форму авторизации
        webLogin.loadUrl(loginUrl);
        webLogin.setWebViewClient(vkWebClient);
    }

    // Чистим куки
    private void clearCookies() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().flush();
        } else
        {
            CookieSyncManager cookieSyncMngr= CookieSyncManager.createInstance(getApplicationContext());
            cookieSyncMngr.startSync();
            CookieManager cookieManager= CookieManager.getInstance();
            cookieManager.removeAllCookie();
            cookieManager.removeSessionCookie();
            cookieSyncMngr.stopSync();
            cookieSyncMngr.sync();
        }
    }

    // ВК отдает токен вместе со строкой адреса, куда ВК перенаправляет после успешной авторизации,
    // вылавливаем токен
    WebViewClient vkWebClient = new WebViewClient(){
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            checkForTokenAndLogIn(url);
            return super.shouldOverrideUrlLoading(view, url);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                checkForTokenAndLogIn(request.getUrl().toString());
            }
            return super.shouldOverrideUrlLoading(view, request);
        }

        private void checkForTokenAndLogIn(String url){
            if(url.contains("access_token")) {
                //Сюда попадаем, только если прошли авторизацию и получили нужные разрешения, сохраняем токен и id пользователя и открываем активити со списком друзей.
                String[] params = url.split(Pattern.quote("="));
                String token = params[1].substring(0, params[1].indexOf('&'));
                long expiresAt = Long.valueOf(params[2].substring(0, params[2].indexOf('&'))) * 1000 + System.currentTimeMillis();
                String id = params[3];
                VKAccount.getInstance(getApplicationContext()).saveTokenAndId(token, id, expiresAt);

                startActivity(new Intent(WebLoginActivity.this, MainActivity.class));
                finish();
            }
        }
    };
}
