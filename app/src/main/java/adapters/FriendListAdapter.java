package adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;

import java.util.ArrayList;

import entities.FriendInList;
import ru.markstudio.vkfriends.R;

/**
 * Created by Владислав on 16.01.2017.
 */

// Адаптер для отображения списка друзей
public class FriendListAdapter extends RecyclerView.Adapter<FriendListAdapter.ViewHolder>{

    private ArrayList<FriendInList> friends;
    private Context context;
    private View.OnClickListener friendClickListener;

    public FriendListAdapter(Context context, ArrayList<FriendInList> friends, View.OnClickListener friendClickListener){
        this.context = context;
        this.friends = friends;
        this.friendClickListener = friendClickListener;
    }

    @Override
    public FriendListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend, parent, false);
        v.setOnClickListener(friendClickListener);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final FriendListAdapter.ViewHolder holder, int position) {
        holder.textName.setText(friends.get(position).getName());
        holder.textStatus.setText(friends.get(position).getStatus());
        Glide.with(context).load(friends.get(position).getPhoto100url()).asBitmap().into(new BitmapImageViewTarget(holder.imageIcon){
            @Override
            protected void setResource(Bitmap resource) {
                RoundedBitmapDrawable circularBitmapDrawable =
                        RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                circularBitmapDrawable.setCircular(true);
                holder.imageIcon.setImageDrawable(circularBitmapDrawable);
            }
        });
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        TextView textName;
        TextView textStatus;
        ImageView imageIcon;

        private ViewHolder(View itemView) {
            super(itemView);
            textName = (TextView) itemView.findViewById(R.id.text_friend_name);
            textStatus = (TextView) itemView.findViewById(R.id.text_friend_status);
            imageIcon = (ImageView) itemView.findViewById(R.id.image_friend_icon);
        }
    }

}
