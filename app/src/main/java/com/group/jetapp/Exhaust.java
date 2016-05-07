package com.group.jetapp;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class Exhaust extends AppObject {
    public int r;
    public Exhaust(int x, int y)
    {
        r = 5;
        super.x = x;
        super.y = y;
    }

    public void update()
    {
        x -= 10;
    }

    public void draw(Canvas canvas)
    {
        Paint overlay = new Paint();
        overlay.setColor(Color.GRAY);
        overlay.setStyle(Paint.Style.FILL);

        // Draw three circle effects
        canvas.drawCircle(x-r, y-r, r, overlay);
        canvas.drawCircle(x-r+2, y-r-2, r, overlay);
        canvas.drawCircle(x-r+4, y-r+1, r, overlay);
    }
}
