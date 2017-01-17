package ru.markstudio.vkfriends;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import data.VKAccount;

//Сплэш экран, перенаправляет на окно логина или сразу на список друзей, если пользователь уже залогинился ранее
public class SplashActivity extends Activity {

    Runnable timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        //Intent loginIntent = new Intent(this, LoginActivity.class);
        Intent intent;
        if(!VKAccount.getInstance(getApplicationContext()).getToken().equals("") && VKAccount.getInstance(getApplicationContext()).getExpiresAt() > System.currentTimeMillis()){
            intent = new Intent(this, MainActivity.class);
        } else {
            intent = new Intent(this, WebLoginActivity.class);
        }
        startActivity(intent);
        finish();
    }
}
