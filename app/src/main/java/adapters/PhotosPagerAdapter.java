package adapters;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;

import ru.markstudio.vkfriends.R;

/**
 * Created by Владислав on 17.01.2017.
 */

// Адаптер для просмотра фотографий пользователя
public class PhotosPagerAdapter extends PagerAdapter {

    private Context context;
    private ArrayList<String> photoURLs;

    public PhotosPagerAdapter(Context context, ArrayList<String> photoURLs){
        this.photoURLs = photoURLs;
        this.context = context;
    }

    @Override
    public Object instantiateItem(final ViewGroup container, int position) {

        LayoutInflater inflater = LayoutInflater.from(context);
        RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.item_pager, container, false);
        ImageView iv =  (ImageView) layout.findViewById(R.id.img_glide);
        final ProgressBar pBar = (ProgressBar) layout.findViewById(R.id.progress);

        Glide.with(context)
                .load(photoURLs.get(position))
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        //Toast.makeText(context, "Проверьте соединение с сетью Интернет!", Toast.LENGTH_LONG).show();
                        pBar.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        pBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(iv);

        container.addView(layout);
        return layout;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View)object);
    }

    @Override
    public int getCount() {
        return photoURLs.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }
}
