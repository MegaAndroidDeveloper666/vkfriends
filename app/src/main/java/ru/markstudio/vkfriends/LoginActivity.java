package ru.markstudio.vkfriends;

import android.graphics.PorterDuff;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

// Активити логина, пользователь вводит логин и пароль
// НЕ ИСПОЛЬЗУЕТСЯ, вместо этой активити используется WebLoginActivity с веб формой авторизации,
// предоставляемой самим вконтакте, так как для возможности прямой авторизации нужно делать
// запрос в тех.поддержку, и не факт, что одобрят

public class LoginActivity extends AppCompatActivity {

    ProgressBar pBar;

    RelativeLayout layoutLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
    }

    //Инициализируем вьюшки
    private void initViews() {

        //Прогресбар для кнопки логина
        pBar = (ProgressBar) findViewById(R.id.progress_login);
        //Делаем его нужного нам цвета
        pBar.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.vk), PorterDuff.Mode.MULTIPLY);

        //"Кнопка" логина
        layoutLoginButton = (RelativeLayout)findViewById(R.id.layout_login);
        layoutLoginButton.setOnClickListener(loginClickListener);
    }

    View.OnClickListener loginClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            layoutLoginButton.setOnClickListener(null);
            layoutLoginButton.setBackgroundResource(R.color.button_disabled);
            pBar.setVisibility(View.VISIBLE);
        }
    };
}
