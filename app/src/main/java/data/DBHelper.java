package data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Владислав on 16.01.2017.
 */

// Класс для работы с базой данных
public class DBHelper extends SQLiteOpenHelper {

    public DBHelper(Context context){
        super(context, "friendsDB", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Создаем две таблицы
        // Первая для хранения данных о всех друзьях (Имя, статус и маленькое фото) для отображения в списке друзей
        db.execSQL("create table friendsTable (" +
                "id long primary key," +
                "name text," +
                "status text," +
                "photo100url text" +
                ")");
        // Вторая таблица хранит информацию о фотографиях каждого пользователя
        db.execSQL("create table friendsProfilePhotos (" +
                "id integer primary key autoincrement," +
                "friend_id long," +
                "photo_url text" +
                ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
