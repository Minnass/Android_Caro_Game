package com.example.caro.Model;

import com.example.caro.R;

public class ItemState {
    private int status;
    private int image;

    public ItemState(int status, int image) {
        this.status = status;
        this.image = image;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
        switch (status)
        {
            case 0: image = R.drawable.field_empty; break;
            case 1: image = R.drawable.field_player; break;
            case 2: image = R.drawable.field_opponent; break;
        }
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }
}
