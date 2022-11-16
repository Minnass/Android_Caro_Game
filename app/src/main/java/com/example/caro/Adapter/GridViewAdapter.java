package com.example.caro.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.example.caro.Caro.Board;
import com.example.caro.Caro.Field;
import com.example.caro.R;

public class GridViewAdapter extends BaseAdapter {
    private Context context;
    private Board board;

    public GridViewAdapter(Context context, Board board) {
        this.context = context;
        this.board = board;
    }

    @Override
    public int getCount() {
        return board.getDimensionX() * board.getDimensionY();
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
        ImageView image;
        public ViewHolder(View view) {
            image = view.findViewById(R.id.img_field);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.board_cell, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Field field = board.getField(position);
        int image;
        if (field == Field.EMPTY) {
            image = R.drawable.field_empty;
        } else if (field == Field.PLAYER) {
            image = R.drawable.field_player;
        } else {
            image = R.drawable.field_opponent;
        }
        viewHolder.image.setImageResource(image);
        return convertView;
    }
}

