package com.battlesea.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.battlesea.Client.Client;
import com.battlesea.Main;
import com.battlesea.enums.Cell;
import com.battlesea.enums.GameMode;
import com.battlesea.model.Board;


public class BattleScreen implements Screen {
    private final Client client;
    private Board boardCreator;
    private Board boardOpponent;
    private SpriteBatch batch;
    private Texture cellEmpty;
    private Texture cellShip;
    private Texture cellHalo;
    private Texture cellMiss;
    private Texture cellHit;
    private Main game;
    private GameMode gamemode;
    private final Vector2 position;

    public BattleScreen(Client client, Main game, GameMode gamemode) {
        this.client = client;
        this.game = game;
        this.gamemode = gamemode;
        this.position = new Vector2(500, 50);
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        cellEmpty = new Texture("cellEmpty.png");
        cellShip = new Texture("cellShip.png");
        cellHalo = new Texture("cellHalo.png");
        cellMiss = new Texture("cellMiss.png");
        cellHit = new Texture("cellHit.png");
    }

    @Override
    public void render(float delta) {
        update();
        if (client != null) {
            Board boardCurrentPlayer;
            Board boardOpponentPlayer;
            if(client.getCreatorPlayer().equals(client.getCurrentPlayer())){
                boardCurrentPlayer = client.getBoardCreator();
                boardOpponentPlayer = client.getBoardOpponent();
            } else {
                boardCurrentPlayer = client.getBoardOpponent();
                boardOpponentPlayer = client.getBoardCreator();
            }

            Board newBoardCurrentPlayer = boardCurrentPlayer;
            if (newBoardCurrentPlayer != null && !newBoardCurrentPlayer.equals(boardCreator)) {
                boardCreator = newBoardCurrentPlayer;
            }
            boardOpponent = boardOpponentPlayer;
        }

        batch.begin();

        drawMyBoard(batch, boardCreator);

        if (boardOpponent != null) {
            drawOpponentBoard(batch, boardOpponent);
        } else {
            drawEmptyBoard(batch);
        }

        batch.end();

        if(client.isGameOver()){
            game.setScreen(new GameOverScreen(client, game));
        }
    }

    private void update() {
        if (Gdx.input.justTouched()) {
            int x = Gdx.input.getX();
            int y = Gdx.graphics.getHeight() - Gdx.input.getY();
            if (x > position.x && x < position.x + 10 * 32
                && y > position.y && y < position.y + 10 * 32) {
                int cellX = (int) ((x - position.x) / 32);
                int cellY = (int) ((y - position.y) / 32);

                client.sendAttack(cellX, cellY);
            }
        }
    }

    private void drawMyBoard(SpriteBatch batch, Board board) {
        Cell[][] cells = board.getCells();
        Texture currentTexture;
        if (cells != null) {
            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 10; j++) {
                    currentTexture = switch (cells[i][j]) {
                        case MISS -> cellMiss;
                        case HIT -> cellHit;
                        case HALO -> cellHalo;
                        case SHIP -> cellShip;
                        default -> cellEmpty;
                    };
                    batch.draw(currentTexture, 32 * i + 50, 32 * j + 50);
                }
            }
        }
    }

    private void drawOpponentBoard(SpriteBatch batch, Board board) {
        Cell[][] cells = board.getCells();
        Texture currentTexture;
        if (cells != null) {
            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 10; j++) {
                    currentTexture = switch (cells[i][j]) {
                        case MISS -> cellMiss;
                        case HIT -> cellHit;
                        case HALO -> cellHalo;
                        default -> cellEmpty;
                    };
                    batch.draw(currentTexture, 32 * i + 500, 32 * j + 50);
                }
            }
        }

//        Cell[][] cells = board.getCells();
//        Texture currentTexture;
//        if (cells != null) {
//            for (int i = 0; i < 10; i++) {
//                for (int j = 0; j < 10; j++) {
//                    currentTexture = switch (cells[i][j]) {
//                        case MISS -> cellMiss;
//                        case HIT -> cellHit;
//                        case HALO -> cellHalo;
//                        case SHIP -> cellShip;
//                        default -> cellEmpty;
//                    };
//                    batch.draw(currentTexture, 32 * i + 500, 32 * j + 50);
//                }
//            }
//        }
    }

    private void drawEmptyBoard(SpriteBatch batch) {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                batch.draw(cellEmpty, 32 * i + 500, 32 * j + 50);
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        // If the window is minimized on a desktop (LWJGL3) platform, width and height are 0, which causes problems.
        // In that case, we don't resize anything, and wait for the window to be a normal size before updating.
        if (width <= 0 || height <= 0) return;

        // Resize your screen here. The parameters represent the new window size.
    }

    @Override
    public void pause() {
        // Invoked when your application is paused.
    }

    @Override
    public void resume() {
        // Invoked when your application is resumed after pause.
    }

    @Override
    public void hide() {
        // This method is called when another screen replaces this one.
    }

    @Override
    public void dispose() {
        batch.dispose();
        cellEmpty.dispose();
    }
}
