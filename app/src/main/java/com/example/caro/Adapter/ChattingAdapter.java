package com.example.caro.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.example.caro.Caro.Field;
import com.example.caro.R;

import java.util.List;

public class ChattingAdapter extends BaseAdapter {
    List<Integer> mStickerList;
    Context context;

    public ChattingAdapter(List<Integer> mStickerList, Context context) {
        this.mStickerList = mStickerList;
        this.context = context;
    }

    @Override
    public int getCount() {
        return mStickerList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.item_sticker, null);
        ImageView sticker = convertView.findViewById(R.id.sticker_img);
        int idDrawable=mStickerList.get(position);
        sticker.setImageResource(idDrawable);
        return  convertView;
    }
}
