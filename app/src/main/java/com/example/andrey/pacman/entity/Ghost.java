package com.example.andrey.pacman.entity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.util.Log;
import android.view.View;
import com.example.andrey.pacman.Direction;
import com.example.andrey.pacman.GameMode;
import com.example.andrey.pacman.Playfield;
import com.example.andrey.pacman.R;

import java.util.ArrayList;
import java.util.Random;

public abstract class Ghost extends Actor {

    private Bitmap scaryGhost;
    private Bitmap eyesGhost;

    boolean inCage;

    Point destPoint;
    Point scatterPoint;

    private float bottomCagePosition;
    private float topCagePosition;
    private float middleCagePositionX;
    private float middleCagePositionY;
    private boolean isExiting;

    private float speedInCage;
    private float speedInTonel;
    private float speedInFrightened;

    private boolean touchTheBottom;

    private boolean isFrightened;
    private boolean isWhite;


    private Point cagePoint;
    private boolean isEyes;
    private float eyesSpeed;

    Ghost(Playfield playfield, View view, Bitmap bitmap, Point scatterPoint, float x, float y) {
        super(playfield, bitmap, x, y, 8, 8, 2,4);
        currentPoint = new Point(x,y);

        scaryGhost = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(view.getResources(), R.mipmap.frightened), frameWidth * 2, frameHeight* 2,false);
        eyesGhost = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(view.getResources(), R.mipmap.eyes_moving), frameWidth, frameHeight * 4,false);

        this.scatterPoint = scatterPoint;
        destPoint = this.scatterPoint;

        cagePoint = new Point(13.5f, 10);

        movementDirection = Direction.LEFT;
        nextDirection = Direction.NONE;

        topCagePosition = 12.50f;
        bottomCagePosition = 13.50f;
        middleCagePositionX = 13.5f;
        middleCagePositionY = (bottomCagePosition + topCagePosition) / 2;

        speedInTonel = 0.003f;
        speedInCage = 0.002f;
        eyesSpeed = 0.01f;
        speedInFrightened = 0.003f;
        frameLengthInMillisecond = 70;
        setSpeed(0.005f);
    }

    public void startExit()
    {
        isExiting = true;
    }

    private void exitFromCage(long deltaTime)
    {
        float frameSpeed = speedInCage * deltaTime;

        if(movementDirection == Direction.RIGHT)
            nextPositionX = x + frameSpeed;
        if(movementDirection == Direction.LEFT)
            nextPositionX = x - frameSpeed;
        if(movementDirection == Direction.UP)
            nextPositionY = y - frameSpeed;
        if(movementDirection == Direction.DOWN)
            nextPositionY = y + frameSpeed;

        if(movementDirection == Direction.DOWN)
        {
            if(nextPositionY >= bottomCagePosition)
            {
                nextPositionY = bottomCagePosition;
                touchTheBottom = true;
                movementDirection = movementDirection.getOposite();
            }
        }

        if(movementDirection == Direction.UP && touchTheBottom)
        {
            if(x < middleCagePositionX && nextPositionY <= middleCagePositionY)
            {
                nextPositionY = middleCagePositionY;
                movementDirection = Direction.RIGHT;
            }
            if(x > middleCagePositionX && nextPositionY <= middleCagePositionY)
            {
                nextPositionY = middleCagePositionY;
                movementDirection = Direction.LEFT;
            }
        }

        if(movementDirection == Direction.LEFT && nextPositionX <= middleCagePositionX
                || movementDirection == Direction.RIGHT && nextPositionX >= middleCagePositionX)
        {
            nextPositionX = middleCagePositionX;
            movementDirection = Direction.UP;
        }

        if(movementDirection == Direction.UP)
        {
            if(touchTheBottom)
            {
                if(nextPositionY <= cagePoint.y)
                {
                    movementDirection = Direction.LEFT;
                    nextPositionY = cagePoint.y;
                    inCage = false;
                }
            }
            else if(nextPositionY <= topCagePosition)
                    movementDirection = movementDirection.getOposite();
        }
    }

    private void moveInCage(long deltaTime)
    {
        float frameSpeed = speedInCage * deltaTime;

        if(movementDirection == Direction.UP)
            nextPositionY -= frameSpeed;
        if(movementDirection == Direction.DOWN)
            nextPositionY += frameSpeed;

        if(nextPositionY >= bottomCagePosition)
        {
            nextPositionY = bottomCagePosition;
            movementDirection = movementDirection.getOposite();
        }

        if(nextPositionY < topCagePosition)
        {
            nextPositionY = topCagePosition;
            movementDirection = movementDirection.getOposite();
        }

        if(nextPositionY > bottomCagePosition)
        {
            nextPositionY = bottomCagePosition;
            movementDirection = movementDirection.getOposite();
        }
    }

    private void eyesMove(float frameSpeed)
    {
        if(movementDirection == Direction.RIGHT)
            nextPositionX = x + frameSpeed;
        if(movementDirection == Direction.LEFT)
            nextPositionX = x - frameSpeed;
        if(movementDirection == Direction.UP)
            nextPositionY = y - frameSpeed;
        if(movementDirection == Direction.DOWN)
            nextPositionY = y + frameSpeed;

        if(x <= cagePoint.floatX && nextPositionX >= cagePoint.floatX && y == cagePoint.floatY
                || x >= cagePoint.floatX && nextPositionX <= cagePoint.floatX && y == cagePoint.floatY) {
            nextPositionX = cagePoint.floatX;
            movementDirection = Direction.DOWN;
            nextDirection = Direction.NONE;
            return;
        }

        if(x == cagePoint.floatX && y <= middleCagePositionY && nextPositionY >= middleCagePositionY) {
            isEyes = false;
            movementDirection = Direction.UP;
            inCage = true;
            isExiting = true;
            touchTheBottom = true;
            return;
        }

        choseDirection(movementDirection);
        this.checkNextDirection();
    }

    public boolean isEyes() {
        return isEyes;
    }

    public void ping() {
        isWhite = !isWhite;
    }

    public void changeMode(GameMode prevGameMode,GameMode newGameMode)
    {
        if(isEyes)
            return;
        isFrightened = newGameMode == GameMode.FRIGHTENED;
        //if(prevGameMode == GameMode.FRIGHTENED)
            isWhite = false;

        if(inCage)
            return;

        if(prevGameMode != GameMode.FRIGHTENED) {
            movementDirection = movementDirection.getOposite();
            nextDirection = Direction.NONE;
        }

        switch (newGameMode)
        {
            case CHASE:
                choseNextPoint();
                isWhite = false;
                isFrightened = false;
                break;
            case SCATTER:
                isWhite = false;
                destPoint = scatterPoint;
                isFrightened = false;
                break;

        }

        choseDirection(movementDirection);
    }

    @Override
    public void animate(long deltaTime) {

        if(movementDirection == Direction.NONE)
            return;

        animationTime += deltaTime;
        if (animationTime > frameLengthInMillisecond) {
            currentFrame++;
            animationTime = 0;
            if (currentFrame >= frameCount) {
                currentFrame = 0;
            }
        }

        if(!isFrightened)
        {
            frameToDraw.left = currentFrame * frameWidth;
            frameToDraw.right = frameToDraw.left + frameWidth;

            frameToDraw.top = movementDirection.getValue() * frameHeight;
            frameToDraw.bottom = frameToDraw.top + frameHeight;
        }
        else {
            frameToDraw.left = currentFrame * frameWidth;
            frameToDraw.right = frameToDraw.left + frameWidth;
            if(!isWhite) {
                frameToDraw.top = 0;
                frameToDraw.bottom = frameToDraw.top + frameHeight;
            }
            else {
                frameToDraw.top = frameHeight;
                frameToDraw.bottom = frameToDraw.top + frameHeight;
            }

        }

        if(isEyes)
        {
            frameToDraw.left = 0;
            frameToDraw.right = frameToDraw.left + frameWidth;
            frameToDraw.top = movementDirection.getValue() * frameHeight;
            frameToDraw.bottom = frameToDraw.top + frameHeight;
        }

    }

    @Override
    public void onDraw(Canvas canvas)
    {
        if(!isVisible)
            return;

        float left = playfield.X_OFFSET + x * playfield.CELLS_SPACE_PERCENT * playfield.mapTexture.getWidth() - ACTOR_X_OFFSET;

        float top = playfield.Y_OFFSET + y * playfield.CELLS_SPACE_PERCENT * playfield.mapTexture.getWidth() - ACTOR_Y_OFFSET  + playfield.STARTPOS_Y;

        RectF whereToDraw = new RectF(left, top, left + actorWidth, top + actorHeight);

        if(isEyes) canvas.drawBitmap(eyesGhost, frameToDraw, whereToDraw, null);
        else canvas.drawBitmap(isFrightened ? scaryGhost: bitmap, frameToDraw, whereToDraw, null);
    }

    public boolean isInCage() {
        return inCage;
    }

    @Override
    public void move(long deltaTime) {
        currentPoint = new Point(Math.round(x), Math.round(y));

        float frameSpeed = deltaTime;
        if(isFrightened) {
            frameSpeed *= speedInFrightened;
        } else if(isEyes) {
            frameSpeed *= eyesSpeed;
        } else if(isInTonel()) {
            frameSpeed *= speedInTonel;
        } else
            frameSpeed *= speed;

        if(inCage)
        {
            if(!isExiting)
                moveInCage(deltaTime);
            else
                exitFromCage(deltaTime);
        }
        else if(isEyes) {
            eyesMove(frameSpeed);
        }
        else {
            switch (movementDirection) {
                case RIGHT:
                    nextPositionX = x + frameSpeed;
                    break;
                case LEFT:
                    nextPositionX = x - frameSpeed;
                    break;
                case UP:
                    nextPositionY = y - frameSpeed;
                    break;
                case DOWN:
                    nextPositionY = y + frameSpeed;
                    break;
            }

            if (playfield.getGameMode() == GameMode.CHASE)
                ghostLogic();

            choseDirection(movementDirection);
            this.checkNextDirection();
        }

        x = nextPositionX;
        y = nextPositionY;

        checkTunnel();
        animate(deltaTime);
    }

    void ghostLogic() {
        choseNextPoint();
    }

    public void beEaten() {
        isEyes = true;
        isWhite = false;
        isFrightened = false;
        destPoint = cagePoint;
        //nextDirection = Direction.NONE;
        findShortestDirection(Direction.NONE, new Point(Math.round(x), Math.round(y)));
    }

    abstract void choseNextPoint();

    private void choseDirection(Direction currentDirection) {
        if (nextDirection != Direction.NONE || isInTonel())
            return;

        Point nextPoint = new Point(currentPoint.x, currentPoint.y);

        switch (currentDirection) {
            case RIGHT:
                nextPoint = new Point(currentPoint.x + 1, currentPoint.y);
                break;
            case LEFT:
                nextPoint = new Point(currentPoint.x - 1, currentPoint.y);
                break;
            case UP:
                nextPoint = new Point(currentPoint.x, currentPoint.y - 1);
                break;
            case DOWN:
                nextPoint = new Point(currentPoint.x, currentPoint.y + 1);
                break;
        }

        if(nextPoint.isWall(map))
        {
            findShortestDirection(currentDirection, currentPoint);
        }
        else if(nextPoint.isFork(map, currentDirection))
        {
            findShortestDirection(currentDirection, nextPoint);
        }
    }


    private void checkNextDirection()
    {

        switch (nextDirection)
        {
            case NONE:
                return;
            case UP:
                if(!new Point(currentPoint.x, currentPoint.y - 1).isWall(map)
                        && (x <= currentPoint.x && nextPositionX >= currentPoint.x
                        || x >= currentPoint.x && nextPositionX <= currentPoint.x))
                {
                    this.nextPositionX = currentPoint.x;

                    movementDirection = Direction.UP;
                    nextDirection = Direction.NONE;
                }
                break;
            case DOWN:
                if(!new Point(currentPoint.x, currentPoint.y + 1).isWall(map)
                        && (x <= currentPoint.x && nextPositionX >= currentPoint.x
                        || x >= currentPoint.x && nextPositionX <= currentPoint.x))
                {
                    this.nextPositionX = currentPoint.x;
                    movementDirection = Direction.DOWN;
                    nextDirection = Direction.NONE;
                }
                break;
            case RIGHT:
                if(!new Point(currentPoint.x + 1, currentPoint.y).isWall(map)
                        && (y <= currentPoint.y && nextPositionY >= currentPoint.y
                        || y >= currentPoint.y && nextPositionY <= currentPoint.y))
                {
                    this.nextPositionY = currentPoint.y;
                    movementDirection = Direction.RIGHT;
                    nextDirection = Direction.NONE;
                }
                break;
            case LEFT:
                if(!new Point(currentPoint.x - 1, currentPoint.y).isWall(map)
                        && (y <= currentPoint.y && nextPositionY >= currentPoint.y
                        || y >= currentPoint.y && nextPositionY <= currentPoint.y))
                {
                    this.nextPositionY = currentPoint.y;
                    movementDirection = Direction.LEFT;
                    nextDirection = Direction.NONE;
                }
                break;

        }

        lookingDirection = movementDirection;
    }

    private void findShortestDirection(Direction currentDirection, Point point) {

        Direction bestDirection = Direction.NONE;
        double minDistance = Double.MAX_VALUE;

        Point rightPoint =  new Point(point.x + 1, point.y);
        Point leftPoint =  new Point(point.x - 1, point.y);
        Point upPoint =  new Point(point.x, point.y - 1);
        Point downPoint =  new Point(point.x, point.y + 1);


        if(isFrightened)
        {
            ArrayList<Direction> directions = new ArrayList<>();
            if(currentDirection != Direction.LEFT  && !rightPoint.isWall(map) )
                directions.add(Direction.RIGHT);
            if(currentDirection != Direction.RIGHT  &&!leftPoint.isWall(map))
                directions.add(Direction.LEFT);
            if(currentDirection != Direction.DOWN  &&!upPoint.isWall(map))
                directions.add(Direction.UP);
            if(currentDirection != Direction.UP  &&!downPoint.isWall(map))
                directions.add(Direction.DOWN);

            nextDirection = directions.get(new Random().nextInt(directions.toArray().length));
            return;
        }

        if(currentDirection != Direction.RIGHT) {
            if (!leftPoint.isWall(map) && leftPoint.distance(destPoint) < minDistance) {
                minDistance = leftPoint.distance(destPoint);
                bestDirection = Direction.LEFT;
            }
        }

        if(currentDirection != Direction.LEFT) {
            if (!rightPoint.isWall(map) && rightPoint.distance(destPoint) < minDistance) {
                minDistance = rightPoint.distance(destPoint);
                bestDirection = Direction.RIGHT;
            }
        }

        if(currentDirection != Direction.UP)
        {
            if (!downPoint.isWall(map) && downPoint.distance(destPoint) < minDistance) {
                minDistance = downPoint.distance(destPoint);
                bestDirection = Direction.DOWN;
            }
        }

        if(currentDirection != Direction.DOWN)
        {
            if (!upPoint.isWall(map) && upPoint.distance(destPoint) < minDistance) {
                bestDirection = Direction.UP;
            }
        }

       nextDirection = bestDirection;
    }

    public boolean isFrightened() {
        return isFrightened;
    }
}
