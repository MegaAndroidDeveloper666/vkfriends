package data;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.File;

/**
 * Created by Владислав on 14.01.2017.
 */

//Класс для сохранения информации об аккаунте пользователя
public class VKAccount{

    private static volatile VKAccount localInstance;

    private static final String vkPrefs = "vkPrefs";

    private static final String vkToken = "token";
    private static final String vkId = "id";
    private static final String vkExpiresAt = "expiresAt";

    private static final String vkName = "name";
    private static final String vkStatus = "status";

    private SharedPreferences sPref;
    private String token;
    private String id;
    private long expiresAt;
    private String name;
    private String status;

    public static VKAccount getInstance(Context context){
        if(localInstance == null) {
            synchronized(VKAccount.class) {
                localInstance = new VKAccount(context);
            }
        }
        return localInstance;
    }

    private VKAccount(Context context){
        sPref = context.getSharedPreferences(vkPrefs, Context.MODE_PRIVATE);
        token = sPref.getString(vkToken, "");
        id = sPref.getString(vkId, "");
        expiresAt = sPref.getLong(vkExpiresAt, 0);
        name = sPref.getString(vkName, "");
        status = sPref.getString(vkStatus, "");
    }

    public void saveTokenAndId(String token, String id, long expiresAt){
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString(vkToken, token);
        ed.putString(vkId, id);
        ed.putLong(vkExpiresAt, expiresAt);
        ed.apply();

        this.token = token;
        this.id = id;
        this.expiresAt = expiresAt;
    }

    public void saveNameAndStatus(String name, String status){
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString(vkName, name);
        ed.putString(vkStatus, status);
        ed.apply();

        this.name = name;
        this.status = status;
    }

    public void clearData(){
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString(vkToken, "");
        ed.putString(vkId, "");
        ed.putLong(vkExpiresAt, 0);
        ed.putString(vkName, "");
        ed.putString(vkStatus, "");
        ed.apply();

        token = "";
        id = "";
        expiresAt = 0;
        name = "";
        status = "";

    }

    public String getToken(){
        return token;
    }

    public String getId(){
        return id;
    }

    public long getExpiresAt(){
        return expiresAt;
    }

    public String getName(){
        return name;
    }

    public String getStatus(){
        return status;
    }
}
