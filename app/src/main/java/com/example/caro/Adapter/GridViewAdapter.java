package com.example.caro.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;


import com.example.caro.Model.ItemState;
import com.example.caro.R;

import java.util.List;

public class GridViewAdapter extends BaseAdapter {
    private Context context;
    private List<ItemState> board;

    public GridViewAdapter(Context context, List<ItemState> board) {
        this.context = context;
        this.board = board;

    }


    @Override
    public int getCount() {
        return board.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    private class ViewHolder {
        ImageView picture;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_gameboard, null);
            viewHolder.picture=convertView.findViewById(R.id.statusPicture);
            convertView.setTag(viewHolder);
        }
        else
        {
            viewHolder = (ViewHolder) convertView.getTag();
        }
            ItemState itemState=board.get(position);
            viewHolder.picture.setImageResource(itemState.getImage());
        return convertView;
    }
}
