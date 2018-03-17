package com.example.andrey.pacman.cutscenes;

import android.graphics.Canvas;
import com.example.andrey.pacman.PacmanGame;
import com.example.andrey.pacman.Playfield;
import com.example.andrey.pacman.entity.Ghost;

public class KillPacmanCutscene extends Cutscene{

    private PacmanGame pacmanGame;

    public KillPacmanCutscene(PacmanGame game, Playfield playfield) {
        super(playfield, 1500);

        pacmanGame = game;
    }

    @Override
    public void onDraw(Canvas canvas) {

    }

    @Override
    public void startOfScene() {
        for(Ghost ghost : playfield.getGhosts()) {
            ghost.isVisible = false;
        }
        playfield.getPacman().startDying();
    }

    @Override
    public void endOfScene() {
        for(Ghost ghost : playfield.getGhosts()) {
            ghost.isVisible = true;
        }
        playfield.getPacman().stopDying();

        pacmanGame.killPacman();
    }

    @Override
    public void play(long deltaTime) {

        playfield.getPacman().animate(deltaTime);
    }
}
