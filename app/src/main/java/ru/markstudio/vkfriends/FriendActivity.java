package ru.markstudio.vkfriends;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import adapters.PhotosPagerAdapter;
import cz.msebera.android.httpclient.Header;
import data.DBHelper;
import data.VKAccount;

// Активити для отображения информации о друге и его фотографий
public class FriendActivity extends AppCompatActivity {

    DBHelper dbHelper;
    long id;
    String name;
    String status;

    TextView textName;
    TextView textStatus;
    ViewPager viewpagerPhotos;

    TextView textNumber;
    TextView textCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend);

        getValues();
        initViews();

        //Сначала загружаем в пэйджер данные из базы, потом загрузаем данные из ВК, перезаписываем базу и отображаем актуальные данные
        loadPhotosFromDB();
        getPhotos();
    }

    // Достаем из базы данных информацию о пользователе
    private void getValues() {
        dbHelper = new DBHelper(this);
        id = getIntent().getLongExtra("friend_id", 0);

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("select * from friendsTable where id = " + id, null);
        if(cursor.moveToFirst()){
            int nameIndex = cursor.getColumnIndex("name");
            int statusIndex = cursor.getColumnIndex("status");
            name = cursor.getString(nameIndex);
            status = cursor.getString(statusIndex);
        } else {
            name = "";
            status = "";
        }
        cursor.close();
        db.close();
        dbHelper.close();
    }

    // Инициализация вьюшек
    private void initViews() {
        textName = (TextView) findViewById(R.id.text_name);
        textName.setText(name);
        textStatus = (TextView) findViewById(R.id.text_status);
        textStatus.setText(status);
        viewpagerPhotos = (ViewPager) findViewById(R.id.viewpager_photos);

        textNumber = (TextView) findViewById(R.id.text_number);
        textCount = (TextView) findViewById(R.id.text_count);
    }

    // Загружаем в пэйждер данные из локальнойбазы данных
    private void loadPhotosFromDB() {
        dbHelper = new DBHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        ArrayList<String> photoUrls = new ArrayList<>();
        Cursor cursor = db.rawQuery("select * from friendsProfilePhotos where friend_id = " + id, null);
        if(cursor.moveToFirst()){
            int photoURLindex = cursor.getColumnIndex("photo_url");
            do{
                photoUrls.add(cursor.getString(photoURLindex));
            }while(cursor.moveToNext());
        } else {

        }
        cursor.close();
        db.close();
        dbHelper.close();
        String strCount = "" + photoUrls.size();
        textCount.setText(strCount);
        viewpagerPhotos.setAdapter(new PhotosPagerAdapter(getApplicationContext(), photoUrls));
        viewpagerPhotos.addOnPageChangeListener(pageChangeListener);
    }

    // Загружаем ссылки на фотографии друга
    private void getPhotos() {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.add("v", "5.62");
        params.add("album_id", "profile");
        params.add("access_token", VKAccount.getInstance(getApplicationContext()).getToken());
        params.add("rev", "1");
        params.add("owner_id", "" + id);
        client.get("https://api.vk.com/method/photos.get", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String ans = new String(responseBody);
                dbHelper = new DBHelper(getApplicationContext());
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                // Сотрем все локальные данные, так как с сервера всегда загружаются акутальные данные
                db.delete("friendsProfilePhotos", "friend_id = " + id, null);
                // И запишем новые данные
                try {
                    JSONObject response = new JSONObject(ans).getJSONObject("response");
                    JSONArray items = response.getJSONArray("items");
                    for(int i = 0; i < items.length(); i++){
                        String photoUrl = items.getJSONObject(i).getString("photo_604");
                        ContentValues cv = new ContentValues();
                        cv.put("friend_id", id);
                        cv.put("photo_url", photoUrl);
                        db.insert("friendsProfilePhotos", null, cv);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                db.close();
                dbHelper.close();

                // Показываем пользователю подгруженные с сервера данные
                loadPhotosFromDB();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                String s = error.getMessage();
            }
        });
    }

    // Отлавливаем перелистывание страницы пэйджера и показываем какая это страница по счету из общего количества
    ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            int photoNumber = position + 1;
            String s = photoNumber + "";
            textNumber.setText(s);
        }

        @Override
        public void onPageSelected(int position) {

        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

}
