package com.group.jetapp;

import android.graphics.Bitmap;
import android.graphics.Canvas;

public class Character extends AppObject {
    private Bitmap spritesheet;
    private int score;
    private boolean up;
    private boolean flying;
    private Movement movement = new Movement();
    private long startTime;
    private int lives;
    private int coins;

    public Character(Bitmap res, int w, int h, int numFrames) {

        x = 100;
        y = AppPanel.HEIGHT / 2;
        dy = 0;
        score = 0;
        height = h;
        width = w;
        lives = 3;
        coins = 0;

        Bitmap[] image = new Bitmap[numFrames];
        spritesheet = res;

        for (int i = 0; i < image.length; i++) {
            image[i] = Bitmap.createBitmap(spritesheet, i * width, 0, width, height);
        }

        movement.setFrames(image);
        movement.setDelay(10);
        startTime = System.nanoTime();

    }

    public void setUp(boolean b) {
        up = b;
    }

    public void update() {
        long elapsed = (System.nanoTime() - startTime) / 1000000;
        if (elapsed > 100) {
            score++;
            startTime = System.nanoTime();
        }
        movement.update();

        if (up) {
            dy -= 1.1;

        } else {
            dy += 1.1;
        }

        if (dy > 14) dy = 14;
        if (dy < -14) dy = -14;

        y += dy * 2;
    }

    public void draw(Canvas canvas) {
        canvas.drawBitmap(movement.getImage(), x, y, null);
    }

    public int getScore() {
        return score;
    }

    public boolean getPlaying() {
        return flying;
    }

    public void setPlaying(boolean b) {
        flying = b;
    }

    public void resetDYA() {
        dy = 0;
    }

    public void resetScore() {
        score = 0;
    }

    public int getLives() {
        return lives;
    }

    public void resetLives() {
        lives = 3;
    }

    public void deductLives() {
        lives --;
    }

    public void addLives() {
        lives ++;
    }

    public void addCoins() { coins ++; }

    public int getCoins() { return coins; }

    public void addScore() { score = score + 10; }
}