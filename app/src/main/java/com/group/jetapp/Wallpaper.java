package com.group.jetapp;

import android.graphics.Bitmap;
import android.graphics.Canvas;

public class Wallpaper {

    private Bitmap image;
    private int x, y, dx;

    public Wallpaper(Bitmap res)
    {
        image = res;
        dx = AppPanel.MOVESPEED;
    }

    public void update()
    {
        x += dx;
        if(x <- AppPanel.WIDTH) {   // Wallpaper scaled all the way to fit screen.
            x = 0;
        }
    }

    public void draw(Canvas canvas)
    {
        canvas.drawBitmap(image, x, y, null);
        if(x < 0)
        {
            canvas.drawBitmap(image, x+AppPanel.WIDTH, y, null);
        }
    }
}
