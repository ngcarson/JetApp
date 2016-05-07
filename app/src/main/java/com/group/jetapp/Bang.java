package com.group.jetapp;

import android.graphics.Bitmap;
import android.graphics.Canvas;

public class Bang {
    private int x;
    private int y;
    private int width;
    private int height;
    private int row;
    private Movement movement = new Movement();
    private Bitmap spritesheet;

    public Bang(Bitmap res, int x, int y, int w, int h, int numFrames)
    {
        this.x = x;
        this.y = y;
        this.width = w;
        this.height = h;

        Bitmap[] image = new Bitmap[numFrames];

        spritesheet = res;

        for(int i = 0; i<image.length; i++)
        {
            if(i%5==0 && i>0)row++;
            image[i] = Bitmap.createBitmap(spritesheet, (i-(5*row))*width, row*height, width, height);
        }
        movement.setFrames(image);
        movement.setDelay(10);

    }
    public void draw(Canvas canvas)
    {
        if(!movement.playedOnce())
        {
            canvas.drawBitmap(movement.getImage(),x,y,null);
        }

    }
    public void update()
    {
        if(!movement.playedOnce())
        {
            movement.update();
        }
    }
    public int getHeight(){return height;}
}