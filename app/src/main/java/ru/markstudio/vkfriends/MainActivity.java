package ru.markstudio.vkfriends;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;

import adapters.FriendListAdapter;
import cz.msebera.android.httpclient.Header;
import data.DBHelper;
import data.VKAccount;
import entities.FriendInList;

// Активити для просмотра списка друзей и выхода из аккаунта
public class MainActivity extends AppCompatActivity {

    Toolbar toolbar;

    Drawer drawer;
    ProfileDrawerItem profile;
    AccountHeader accountHeader;

    RecyclerView recyclerFriendList;
    RecyclerView.LayoutManager manager;

    ArrayList<FriendInList> friends;
    DBHelper dbHelper;

    // Идентификаторы кнопок в левом меню
    private static final int DRAWER_BUTTON_LOGOUT = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DBHelper(this);

        initViews();

        // Сначала загружаем друзей из базы, затем загружаем друзей с сервера, затем перезаписываем
        // локальные данные в базе новыми
        loadFriendsFromDB();
        getFriendsFromVK();
    }

    // Инициализируем вьюшки
    private void initViews() {
        toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initDrawer();

        recyclerFriendList = (RecyclerView)findViewById(R.id.recycler_friends);
        manager = new LinearLayoutManager(this);
        recyclerFriendList.setLayoutManager(manager);
    }

    // Инициализируем левое меню с профилем и кнопкой выхода из аккаунта
    private void initDrawer() {
        String name = VKAccount.getInstance(getApplicationContext()).getName();
        String status = VKAccount.getInstance(getApplicationContext()).getStatus();

        Bitmap icon = null;
        File iconFile = new File(getFilesDir() + "/profileIcon.jpg");
        boolean iconExists = iconFile.exists();
        // Если фото профиля еще не было загружено, вставляем вместо него placeholder
        if(iconExists){
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            icon = BitmapFactory.decodeFile(iconFile.getAbsolutePath(),bmOptions);
        } else {
            icon = BitmapFactory.decodeResource(getResources(), R.drawable.placeholder75);
        }

        // Профиль пользователя
        profile = new ProfileDrawerItem()
                .withName(name)
                .withIcon(icon)
                .withEmail(status);

        // Заголовок левого меню
        accountHeader = new AccountHeaderBuilder()
                .withActivity(this)
                .withCompactStyle(true)
                .withHeaderBackground(new ColorDrawable(ContextCompat.getColor(getApplicationContext(), R.color.background_account)))
                .addProfiles(profile)
                .build();

        // Само левое меню
        drawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withAccountHeader(accountHeader)

                // Кнопка выхода
                .addDrawerItems(
                        new PrimaryDrawerItem().withName(R.string.logout).withIdentifier(DRAWER_BUTTON_LOGOUT)
                )

                // Обработчик нажатий на кнопки левого меню
                .withOnDrawerItemClickListener(drawerItemClickListener)

                .build();

        // Если имя не было сохранено, подгружаем информацию о профиле пользователя
        if("".equals(name)){
            getProfileInfo();
        }
        // Если нет картинки профиля, подгружаем ее
        if(!iconExists){
            getAccountImage();
        }
    }

    // Обработчик нажатий на кнопки левого меню
    Drawer.OnDrawerItemClickListener drawerItemClickListener = new Drawer.OnDrawerItemClickListener() {
        @Override
        public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
            int id = (int) drawerItem.getIdentifier();
            switch (id){
                // Кнопка выхода из аккаунта
                case DRAWER_BUTTON_LOGOUT:
                    // Нажали на кнопку выхода, спросить подтверждение
                    AlertDialog.Builder adb = new AlertDialog.Builder(new ContextThemeWrapper(MainActivity.this, R.style.myDialog))
                            .setTitle(R.string.logout)
                            .setMessage(R.string.logout_message)
                            .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            })
                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // Пользователь подтвердил выход из аккаунта, стереть все локальные данные

                                    // Стираем данные из таблиц базы данных
                                    DBHelper dbHelper = new DBHelper(getApplicationContext());
                                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                                    db.delete("friendsTable", null, null);
                                    db.delete("friendsProfilePhotos", null, null);
                                    VKAccount.getInstance(getApplicationContext()).clearData();
                                    db.close();
                                    dbHelper.close();

                                    // Стираем картинку профиля пользователя
                                    File iconFile = new File(getFilesDir() + "/profileIcon.jpg");
                                    iconFile.delete();

                                    // Запускаем активити логина
                                    startActivity(new Intent(MainActivity.this, WebLoginActivity.class));
                                    finish();
                                }
                            });
                    adb.show();
                    break;
            }
            return false;
        }
    };

    // Загружаем в recyclerView друзей из базы данных
    private void loadFriendsFromDB() {
        friends = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursorFiends = db.query("friendsTable", null, null, null, null, null, null);
        if(cursorFiends.moveToFirst()){
            int id = cursorFiends.getColumnIndex("id");
            int name = cursorFiends.getColumnIndex("name");
            int status = cursorFiends.getColumnIndex("status");
            int photo100url = cursorFiends.getColumnIndex("photo100url");
            do{
                friends.add(new FriendInList(cursorFiends.getLong(id), cursorFiends.getString(name), cursorFiends.getString(status), cursorFiends.getString(photo100url)));
            }while(cursorFiends.moveToNext());
        }else{

        }
        cursorFiends.close();
        recyclerFriendList.setAdapter(new FriendListAdapter(getApplicationContext(), friends, friendsClickListener));
    }

    // Загружаем друзей с сервера
    private void getFriendsFromVK() {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.add("access_token", VKAccount.getInstance(getApplicationContext()).getToken());
        params.add("v", "5.62");
        params.add("order", "name");
        params.add("fields", "photo_100,status");
        client.get("https://api.vk.com/method/friends.get", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                friends = new ArrayList<>();
                String ans = new String(responseBody);
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                // Стираем старую информацию
                db.delete("friendsTable", "", null);
                // Записываем новую
                try {
                    JSONArray friendsArray = new JSONObject(ans).getJSONObject("response").getJSONArray("items");
                    for(int i = 0; i < friendsArray.length(); i++){
                        JSONObject friendObj = friendsArray.getJSONObject(i);
                        String status = "";
                        try{
                            status = friendObj.getString("status");
                        } catch (JSONException e){
                            e.printStackTrace();
                        }
                        ContentValues cv = new ContentValues();
                        cv.put("id", friendObj.getLong("id"));
                        cv.put("name", friendObj.getString("first_name") + " " + friendObj.getString("last_name"));
                        cv.put("photo100url", friendObj.getString("photo_100"));
                        cv.put("status", status);
                        db.insert("friendsTable", null, cv);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                db.close();
                dbHelper.close();
                loadFriendsFromDB();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                String s = error.getMessage();
            }
        });
    }

    // Подгружаем информацию о профиле пользователя, сохраняем ее локально и отображаем ее в левом меню
    private void getProfileInfo() {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.add("access_token", VKAccount.getInstance(getApplicationContext()).getToken());
        params.add("v", "5.62");
        client.get("https://api.vk.com/method/account.getProfileInfo", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String name = "";
                String status = "";
                String ans = new String(responseBody);
                try {
                    JSONObject response = new JSONObject(ans).getJSONObject("response");
                    name = response.getString("first_name") + " " + response.getString("last_name");
                    status = response.getString("status");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                VKAccount.getInstance(getApplicationContext()).saveNameAndStatus(name, status);
                profile.withName(name);
                profile.withEmail(status);
                accountHeader.updateProfile(profile);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });
    }

    // Загружаем и сохраняем миниатюру фотографии пользователя для отображения в левом меню приложения,
    // даже в случае отсутствия интернета и в случае, если кэш фотографий был очищен
    private void getAccountImage() {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.add("access_token", VKAccount.getInstance(getApplicationContext()).getToken());
        params.add("v", "5.62");
        params.add("album_id", "profile");
        params.add("rev", "1");
        params.add("count", "1");
        // Получаем ссылку на изображение пользователя
        client.get("https://api.vk.com/method/photos.get", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String photo75 = "";
                String ans = new String(responseBody);
                try {
                    JSONObject response = new JSONObject(ans).getJSONObject("response");
                    photo75 = response.getJSONArray("items").getJSONObject(0).getString("photo_75");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                // Загружаем саму картинку
                new AcyncImageDownloader().execute(photo75);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });
    }

    // Обработчик нажатия на строку в списке, запускаем FriendActivity
    View.OnClickListener friendsClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int pos = recyclerFriendList.getChildAdapterPosition(v);
            Intent intent = new Intent(MainActivity.this, FriendActivity.class);
            intent.putExtra("friend_id", friends.get(pos).getId());
            startActivity(intent);
        }
    };

    // Если открыто левое мен, по нажатии кнопки "Назад" закрываем его, иначе обрабатываем как обычно
    @Override
    public void onBackPressed() {
        if(drawer.isDrawerOpen()){
            drawer.closeDrawer();
        } else {
            super.onBackPressed();
        }
    }

    // Класс для загрузки картинки профиля
    class AcyncImageDownloader extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            getProfileIconFromURLandSave(params[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            File iconFile = new File(getFilesDir() + "/profileIcon.jpg");
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            Bitmap bitmap = BitmapFactory.decodeFile(iconFile.getAbsolutePath(),bmOptions);
            profile.withIcon(bitmap);
            accountHeader.updateProfile(profile);
            super.onPostExecute(aVoid);
        }

        public Bitmap getProfileIconFromURLandSave(String src) {
            try {
                java.net.URL url = new java.net.URL(src);
                HttpURLConnection connection = (HttpURLConnection) url
                        .openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);
                saveProfileIcon(myBitmap);
                return myBitmap;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        private void saveProfileIcon(Bitmap bitmap){
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(getFilesDir() + "/profileIcon.jpg");
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}
