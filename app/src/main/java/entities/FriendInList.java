package entities;

/**
 * Created by Владислав on 16.01.2017.
 */

// Класс для отображения данных друга в списке друзей
public class FriendInList {

    private long id;
    private String name;
    private String status;
    private String photo100url;

    public FriendInList(long id, String name, String status, String photo100url){
        this.id = id;
        this.name = name;
        this.status = status;
        this.photo100url = photo100url;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    public String getPhoto100url() {
        return photo100url;
    }

    public long getId() {
        return id;
    }
}
