package com.group.jetapp;

import android.graphics.Bitmap;
import android.graphics.Canvas;

public class Barrier_South extends AppObject {
    private Bitmap image;
    public Barrier_South(Bitmap res, int x, int y)
    {
        height = 200;
        width = 20;

        this.x = x;
        this.y = y-13; // Collision height for ground
        dx = AppPanel.MOVESPEED;

        image = Bitmap.createBitmap(res, 0, 0, width, height);
    }

    public void update()
    {
        x += dx;
    }

    public void draw(Canvas canvas)
    {
        canvas.drawBitmap(image, x, y, null);
    }
}
