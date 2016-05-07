package com.group.jetapp;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import java.util.Random;

public class Enemies_Heli extends AppObject {
    private int score;
    private int velocity;
    private Random rand = new Random();
    private Movement movement = new Movement();
    private Bitmap spritesheet;

    public Enemies_Heli(Bitmap res, int x, int y, int w, int h, int s, int numFrames)
    {
        this.x = x;
        this.y = y;
        width = w;
        height = h;
        score = s;

        velocity = 8 + (int) (rand.nextDouble()*score/30);

        // Ceiling velocity of rocket
        if(velocity>50)velocity = 50;

        Bitmap[] image = new Bitmap[numFrames];

        spritesheet = res;

        for(int i = 0; i<image.length; i++)
        {
            image[i] = Bitmap.createBitmap(spritesheet, 0, i*height, width, height);
        }

        movement.setFrames(image);
        movement.setDelay(100-velocity);

    }
    public void update()
    {
        x-=velocity;
        y += (Math.cos(-90) * -4); // Move downwards direction
        movement.update();
    }

    public void draw(Canvas canvas)
    {
        try{
            canvas.drawBitmap(movement.getImage(), x, y, null);
        }catch (Exception e){}
    }

    @Override
    public int getWidth()
    {
        // Slight collision at the tail of rocket won't cause collision
        return width-10;
    }
}
