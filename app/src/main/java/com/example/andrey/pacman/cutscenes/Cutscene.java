package com.example.andrey.pacman.cutscenes;

import android.graphics.Canvas;
import com.example.andrey.pacman.Playfield;

public abstract class Cutscene {

    Playfield playfield;
    private long sceneTime;

    public Cutscene(Playfield playfield,long sceneTime)
    {
        this.playfield = playfield;
        this.sceneTime = sceneTime;
    }

    public long getSceneTime() {
        return sceneTime;
    }

    public abstract void onDraw(Canvas canvas);

    public abstract void play(long deltaTime);

    public void startOfScene(){}
    public void endOfScene(){}

}
