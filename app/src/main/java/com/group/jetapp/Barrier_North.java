package com.group.jetapp;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

public class Barrier_North extends AppObject {
    private Bitmap image;

    public Barrier_North(Bitmap res, int x, int y, int h)
    {
        height = h;
        width = 20;

        this.x = x;
        this.y = y;

        dx = AppPanel.MOVESPEED;
        image = Bitmap.createBitmap(res, 0, 0, width, height);
    }

    public void update()
    {
        x += dx;
    }

    public void draw(Canvas canvas)
    {
        try{canvas.drawBitmap(image,x,y,null);}catch(Exception e){};
    }
}
