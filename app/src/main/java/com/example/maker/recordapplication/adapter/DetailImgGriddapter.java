package com.example.maker.recordapplication.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.maker.recordapplication.R;

import java.io.File;
import java.util.List;
import java.util.Map;

public class DetailImgGriddapter extends BaseAdapter {
    private Context context;
    private List<Map<String,Object>> data;

    public DetailImgGriddapter(Context context, List<Map<String, Object>> data) {
        this.context = context;
        this.data = data;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int i) {
        return data.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if(view==null){
            viewHolder = new ViewHolder();
            view = LayoutInflater.from(context).inflate(R.layout.layout_detail_img_grid,null);
            viewHolder.imageView = view.findViewById(R.id.image);
            viewHolder.title = view.findViewById(R.id.title);
            view.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder) view.getTag();
        }
      //  File imagePath = (File) data.get(i).get("image");
     //   Log.e("makerLog",(String) data.get(i).get("image"));
        viewHolder.title.setText((String) data.get(i).get("title"));

        Bitmap bitmap = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile((String) data.get(i).get("image")), 100, 100);

        viewHolder.imageView.setImageBitmap(bitmap);
        return view;
    }

    class ViewHolder{
        ImageView imageView;
        TextView title;
    }
}
