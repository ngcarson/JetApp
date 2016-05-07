package com.group.jetapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.Random;

import static com.group.jetapp.R.*;

public class AppPanel extends SurfaceView implements SurfaceHolder.Callback{

    public static final int WIDTH = 856;
    public static final int HEIGHT = 480;
    public static final int MOVESPEED = -5;
    private long exhaustStartTime;
    private long rocketsStartTime;
    private long rocketsElapsed;
    private PrimaryThread thread;
    private Wallpaper wp;
    private Character character;
    private ArrayList<Exhaust> exhaust;
    private ArrayList<Enemies> enemies;
    private ArrayList<Enemies_Heli> enemies_heli;
    private ArrayList<Barrier_North> barrier_north;
    private ArrayList<Barrier_South> barrier_south;
    private Random rand = new Random();
    private int maxHeightBarrier;
    private int minHeightBarrier;
    private boolean northDown = true;
    private boolean southDown = true;
    private boolean newRoundCreated = true;
    private ArrayList<Star> star;
    private ArrayList<Coins> coins;
    // Increase to slow down difficulty progress, decrease to speed up difficulty progression
    private int progressDenom = 20;

    // Restart user after explosion
    private Bang bang;
    private long startReset;
    private boolean reset;
    private boolean disappear;
    private boolean started;
    private int HighScore;
    //private int high;

    private static SharedPreferences prefs;
    private String saveScore = "HighScore";

    private SoundPool sounds;
    private int sExplosion;
    private int sFly;
    private int sCrash;
    private int sCrashStar;
    private int sEatCoins;

    MediaPlayer bkground;

    public AppPanel(Context context)
    {
        super(context);

        prefs = context.getSharedPreferences("com.group.jetapp",context.MODE_PRIVATE);

        String spackage = "com.group.jetapp";

        HighScore = prefs.getInt(saveScore, 0);

        //add the callback to the surfaceholder to intercept events
        getHolder().addCallback(this);

        thread = new PrimaryThread(getHolder(), this);

        //make appPanel focusable so it can handle envets
        setFocusable(true);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height){}

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        prefs.edit().putInt(saveScore, HighScore).commit();
        boolean retry = true;
        int counter = 0;
        while(retry && counter<1000)
        {
            counter++;
            //sometimes it takes multiple attempts to stop the thread
            try{thread.setRunning(false);
                thread.join();
                retry = false;
                thread = null; // Garbage object is picked up
            }catch(InterruptedException e){e.printStackTrace();}
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
        wp = new Wallpaper(BitmapFactory.decodeResource(getResources(), drawable.desert));
        character = new Character(BitmapFactory.decodeResource(getResources(), drawable.jet4), 100, 48, 3); // First version
        exhaust = new ArrayList<Exhaust>();
        enemies = new ArrayList<Enemies>();
        enemies_heli = new ArrayList<Enemies_Heli>();
        barrier_north = new ArrayList<Barrier_North>();
        barrier_south = new ArrayList<Barrier_South>();
        star = new ArrayList<Star>();
        coins = new ArrayList<Coins>();

        // Allow small stream of smoke coming out of helicopter
        exhaustStartTime = System.nanoTime();
        rocketsStartTime = System.nanoTime();

        // We can safely start the game loop
        thread.setRunning(true);
        thread.start();

        // Load Sound Pool for short sound effects (wav format)
        sounds = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        sExplosion = sounds.load(getContext(), R.raw.boom, 1);
        sFly = sounds.load(getContext(), R.raw.jump, 1);
        sCrash = sounds.load(getContext(), R.raw.crash, 1); //sound when crashing enemies
        sCrashStar = sounds.load(getContext(), R.raw.star, 1); //sound when crashing star
        sEatCoins = sounds.load(getContext(), R.raw.coins, 1); //sound when eating coins
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if(event.getAction()==MotionEvent.ACTION_DOWN){
            if(!character.getPlaying() && newRoundCreated && reset)
            {
                bkground = MediaPlayer.create(getContext(), R.raw.bkground);
                bkground.setLooping(true);
                bkground.setVolume(1.0f,1.0f);
                //bkground.setVolume(0.8f,0.8f); // Set background music volume
                bkground.start();

                character.setPlaying(true);
                character.setUp(true);
            }
            if(character.getPlaying())
            {
                if(!started)started = true;
                reset = false;
                character.setUp(true);

                // Play sound effect when pressing to fly higher
                sounds.play(sFly, 1.0f, 1.0f, 0, 0, 1.5f);
            }
            return true;
        }
        if(event.getAction()==MotionEvent.ACTION_UP)
        {
            character.setUp(false);
            return true;
        }

        return super.onTouchEvent(event);
    }

    public void update()
    {
        if (character.getPlaying()) {

            wp.update();
            character.update();

            if(character.getScore() > HighScore)
            {
                HighScore = (character.getScore());
            }

            // Calculated threshold height of barrier changes depending on score
            // Border switch direction when max or min are reached
            maxHeightBarrier = 30+character.getScore()/progressDenom;

            // Max cap limits barrier to occupy 1/2 of screen
            if(maxHeightBarrier > HEIGHT/4)maxHeightBarrier = HEIGHT/4;
            minHeightBarrier = 5+character.getScore()/progressDenom;

            // Check upper barrier collision
            for(int i = 0; i<barrier_north.size(); i++)
            {
                if(collision(barrier_north.get(i), character))
                {
                    // Load explosion sound effect
                    sounds.play(sExplosion, 1.0f, 1.0f, 0, 0, 1.5f);

                    bkground.release();

                    character.setPlaying(false);
                }
            }

            // Check lower barrier collision
            for(int i = 0; i<barrier_south.size(); i++)
            {
                if(collision(barrier_south.get(i), character))
                {
                    // Load explosion sound effect
                    sounds.play(sExplosion, 1.0f, 1.0f, 0, 0, 1.5f);

                    bkground.release();

                    character.setPlaying(false);
                }
            }

            // Update north barrier
            this.updateNorthBarrier();

            // Update south barrier
            this.updateSouthBarrier();

            // Add enemies on the timer. And as score goes higher, enemies become faster.
            long rocketsElapsed = (System.nanoTime()-rocketsStartTime)/1000000;
            if(rocketsElapsed > (2000 - character.getScore()/4)){
                enemies.add(new Enemies(BitmapFactory.decodeResource(getResources(), drawable.skull),
                        WIDTH+10, (int)(rand.nextDouble()*(HEIGHT/1.5 - (maxHeightBarrier * 2))+maxHeightBarrier), 40, 40, character.getScore(), 3));

                // Timer reset
                rocketsStartTime = System.nanoTime();
            }

            // Loop through all enemies to check for any collisions and remove
            for(int i = 0; i<enemies.size(); i++)
            {
                // Update rocket
                enemies.get(i).update();

                if(collision(enemies.get(i),character)) {

                    // Breaks off loop when player stops playing
                    sounds.play(sCrash, 1.0f, 1.0f, 0, 0, 1.5f);

                    character.deductLives();
                    enemies.remove(i);
                    //character.setPlaying(false);
                    if (character.getLives() == 0) {

                        // Load explosion sound effect
                        sounds.play(sExplosion, 1.0f, 1.0f, 0, 0, 1.5f);

                        bkground.release();

                        character.setPlaying(false);
                    }
                    break;
                }

                // Remove enemies once pass across the screen
                if(enemies.get(i).getX()<-100)
                {
                    enemies.remove(i);
                    break;
                }
            }

            // Add enemies on the timer. And as score goes higher, enemies become faster.
            rocketsElapsed = (System.nanoTime()-rocketsStartTime)/995000;
            if(rocketsElapsed > (2000 - character.getScore()/4)) {
                enemies_heli.add(new Enemies_Heli(BitmapFactory.decodeResource(getResources(), drawable.demon),
                        WIDTH+10, (int)(rand.nextDouble()*(HEIGHT/1.5 - (maxHeightBarrier * 2))+maxHeightBarrier), 68, 87, character.getScore(), 4));
            }

            // Loop through all enemies to check for any collisions and remove
            for(int i = 0; i<enemies_heli.size(); i++)
            {
                // Update rocket
                enemies_heli.get(i).update();

                if(collision(enemies_heli.get(i),character)) {

                    // Breaks off loop when player stops playing
                    sounds.play(sCrash, 1.0f, 1.0f, 0, 0, 1.5f);

                    character.deductLives();
                    enemies_heli.remove(i);
                    //character.setPlaying(false);
                    if (character.getLives() == 0) {

                        // Load explosion sound effect
                        sounds.play(sExplosion, 1.0f, 1.0f, 0, 0, 1.5f);

                        bkground.release();

                        character.setPlaying(false);
                    }
                    break;
                }

                // Remove enemies once pass across the screen
                if(enemies_heli.get(i).getX()<-100)
                {
                    enemies_heli.remove(i);
                    break;
                }
            }

            // generate star randomly
            rocketsElapsed = (System.nanoTime()-rocketsStartTime)/990000;
            if(rocketsElapsed > (2000 - character.getScore()/4)) {
                star.add(new Star(BitmapFactory.decodeResource(getResources(), drawable.star),
                            WIDTH + 10, (int) (rand.nextDouble() * (HEIGHT/1.5 - (maxHeightBarrier * 2)) + maxHeightBarrier), 30, 28, character.getScore(), 7));
            }

            // Loop through all star to check for any collisions and remove
            for(int i = 0; i<star.size(); i++)
            {
                // Update rocket
                star.get(i).update();

                if(collision(star.get(i), character))
                {
                    // Add lives when crashing star

                    if (character.getLives() < 5) {
                        sounds.play(sCrashStar, 1.0f, 1.0f, 0, 0, 1.5f);
                        character.addLives();
                        star.remove(i);
                        break;
                    }
                }

                // Remove star once pass across the screen
                if(star.get(i).getX()<-100)
                {
                    star.remove(i);
                    break;
                }
            }

            // generate coins randomly
            rocketsElapsed = (System.nanoTime()-rocketsStartTime)/990000;
            if(rocketsElapsed > (2000 - character.getScore()/4)) {
                coins.add(new Coins(BitmapFactory.decodeResource(getResources(), drawable.coins),
                        WIDTH + 10, (int) (rand.nextDouble() * (HEIGHT/1.5 - (maxHeightBarrier * 2)) + maxHeightBarrier), 30, 28, character.getScore(), 7));
            }

            // Loop through all coins to check for any collisions and remove
            for(int i = 0; i<coins.size(); i++)
            {
                // Update rocket
                coins.get(i).update();

                if(collision(coins.get(i), character))
                {
                    // Add lives when crashing coins
                    sounds.play(sEatCoins, 1.0f, 1.0f, 0, 0, 1.5f);
                    //character.addCoins();
                    character.addScore();
                    coins.remove(i);
                    break;
                }

                // Remove coins once pass across the screen
                if(coins.get(i).getX()<-100)
                {
                    coins.remove(i);
                    break;
                }
            }

            // Add smog on the timer
            long elapsed = (System.nanoTime() - exhaustStartTime)/1000000;
            if(elapsed > 120){
                exhaust.add(new Exhaust(character.getX(), character.getY()+35));
                exhaustStartTime = System.nanoTime();
            }

            // Remove smoke that reaches off the screen
            for(int i = 0; i<exhaust.size(); i++)
            {
                exhaust.get(i).update();
                if(exhaust.get(i).getX()<-10)
                {
                    exhaust.remove(i);
                }
            }
        }
        else {
            // New round created if user collides
            character.resetDYA();
            if(!reset)
            {
                bang = new Bang(BitmapFactory.decodeResource(getResources(), drawable.explosion),character.getX(),
                        character.getY()-30, 100, 100, 25); // Explosion occur nearly above the user.

                newRoundCreated = false;
                startReset = System.nanoTime();
                reset = true;
                disappear = true;
            }

            bang.update();
            long resetElapsed = (System.nanoTime()-startReset)/1000000;

            if(resetElapsed > 2500 && !newRoundCreated)
            {
                newRound();
            }
        }
    }

    public boolean collision(AppObject a, AppObject b) {
        if (Rect.intersects(a.getRectangle(), b.getRectangle())) {

            return true;

        }
        return false;
    }

    @Override
    public void draw(Canvas canvas)
    {
        final float scaleFactorX = getWidth()/WIDTH; // Scale wallpaper to entire screen.
        final float scaleFactorY = getWidth()/HEIGHT;

        if(canvas != null) {
            final int savedState = canvas.save();

            canvas.scale(scaleFactorX, scaleFactorY);
            wp.draw(canvas);

            // Jet disappear after bang effect
            if(!disappear){
                character.draw(canvas);
            }

            // Draw smog effects
            for(Exhaust sp: exhaust)
            {
                sp.draw(canvas);
            }

            // Draw enemies
            for(Enemies m: enemies)
            {
                m.draw(canvas);
            }

            // Draw enemies #2
            for(Enemies_Heli m: enemies_heli)
            {
                m.draw(canvas);
            }

            // Draw Star
            for(Star h: star){
                h.draw(canvas);
            }

            // Draw Coins
            for(Coins h: coins){
                h.draw(canvas);
            }

            //draw topborder
            for(Barrier_North tb: barrier_north)
            {
                tb.draw(canvas);
            }

            // Draw bang effect
            if(started)
            {
                bang.draw(canvas);
            }
            drawText(canvas);
            canvas.restoreToCount(savedState); // Return back to original state to prevent wallpaper from keep scaling.
        }
    }

    public void updateSouthBarrier()
    {

    }

    public void updateNorthBarrier()
    {
        // Random upper barriers is inserted for every 40 points reached
        if(character.getScore()%40 == 0)
        {
            barrier_north.add(new Barrier_North(BitmapFactory.decodeResource(getResources(), drawable.brick
            ),barrier_north.get(barrier_north.size()-1).getX()+20,0,(int)((rand.nextDouble()*(maxHeightBarrier
            ))+1)));
        }

        for (int i = 0; i<barrier_north.size(); i++)
        {
            barrier_north.get(i).update();

            if(barrier_north.get(i).getX()<-20) {

                // Remove border element from array list and replace with a new one once moved off screen
                barrier_north.remove(i);

                //calculate topdown which determines the direction the border is moving (up or down)
                if(barrier_north.get(barrier_north.size()-1).getHeight()>=maxHeightBarrier)
                {
                    northDown = false;
                }

                if(barrier_north.get(barrier_north.size()-1).getHeight()<=minHeightBarrier)
                {
                    northDown = true;
                }

                // New border added will have larger height
                if(northDown)
                {
                    barrier_north.add(new Barrier_North(BitmapFactory.decodeResource(getResources(),
                            drawable.brick),barrier_north.get(barrier_north.size()-1).getX()+20,
                            0, barrier_north.get(barrier_north.size()-1).getHeight()+1));
                }
                // New border added will have smaller height
                else
                {
                    barrier_north.add(new Barrier_North(BitmapFactory.decodeResource(getResources(),
                            drawable.brick),barrier_north.get(barrier_north.size()-1).getX()+20,
                            0, barrier_north.get(barrier_north.size()-1).getHeight()-1));
                }
            }
        }
    }

    public void newRound()
    {
        // Have jet reappear again for new game
        disappear = false;

        barrier_south.clear();
        barrier_north.clear();

        enemies.clear();
        enemies_heli.clear();
        exhaust.clear();
        star.clear();
        coins.clear();

        // Reset barrier size
        minHeightBarrier = 5;
        maxHeightBarrier = 30;

        // Reset speed after collision
        character.resetDYA();

        // Reset score, player position and lives
        character.resetScore();
        character.setY(HEIGHT / 4);
        character.resetLives();

        // Create initial upper barriers
        for (int i = 0; i*20<WIDTH+40; i++)
        {
            if(i==0)
            {
                // First initial barrier
                barrier_north.add(new Barrier_North(BitmapFactory.decodeResource(getResources(), drawable.brick
                ),i*20, 0, 10));
            }
            else
            {
                // Add on to the first barrier until screen filled
                barrier_north.add(new Barrier_North(BitmapFactory.decodeResource(getResources(), drawable.brick
                ),i*20, 0, barrier_north.get(i-1).getHeight()+1));
            }

        }

        // Create initial lower barriers
        for (int i = 0; i*20<WIDTH+40; i++)
        {
            if(i==0)
            {
                // First initial barrier
                barrier_south.add(new Barrier_South(BitmapFactory.decodeResource(getResources(), drawable.brick
                ), i * 20,HEIGHT - minHeightBarrier));
            }
            else
            {
                // Add on to the first barrier until screen filled
                barrier_south.add(new Barrier_South(BitmapFactory.decodeResource(getResources(), drawable.brick
                ), i * 20, barrier_south.get(i - 1).getY() - 1));
            }
        }

        newRoundCreated = true;
    }

    public void drawText(Canvas canvas)
    {
        // Draw score indicator
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setTextSize(30);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("LIVES: " + character.getLives(), WIDTH/2-400, HEIGHT/2+60, paint);
        canvas.drawText("HIGH SCORE: " + HighScore, WIDTH/2-400, HEIGHT/2+90, paint);
        canvas.drawText("SCORE: " + (character.getScore()), WIDTH-400, HEIGHT/2+90, paint);

        // Start menu
        if(!character.getPlaying()&&newRoundCreated&&reset)
        {
            Paint paint1 = new Paint();
            paint1.setTextSize(40);
            paint1.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            canvas.drawText("PRESS TO START", WIDTH/2-50, HEIGHT/2-20, paint1);

            paint1.setTextSize(20);
            canvas.drawText("PRESS AND HOLD TO GO UP", WIDTH/2-50, HEIGHT/2, paint1);
            canvas.drawText("RELEASE TO GO DOWN", WIDTH/2-50, HEIGHT/2+20, paint1);
        }
    }
}