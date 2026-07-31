package com.battlesea.screens;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.battlesea.Client.Client;
import com.battlesea.Main;
import com.battlesea.enums.Cell;
import com.battlesea.enums.GameMode;
import com.battlesea.model.Board;

/**
 * First screen of the application. Displayed after the application is created.
 */
public class BattleScreen implements Screen {
    private final Client client;
    private Board boardPlayer1;
    private Board boardPlayer2;
    private SpriteBatch batch;
    private Texture cellEmpty;
    private Texture cellShip;
    private Texture cellHalo;
    private Texture cellMiss;
    private Texture cellHit;
    private Main game;
    private GameMode gamemode;

    public BattleScreen(Client client, Main game, GameMode gamemode) {
        this.client = client;
        this.game = game;
        this.gamemode = gamemode;
    }

    @Override
    public void show() {
        System.out.println("FirstScreen show");
        batch = new SpriteBatch();
        cellEmpty = new Texture("cellEmpty.png");
        cellShip = new Texture("cellShip.png");
        cellHalo = new Texture("cellHalo.png");
        cellMiss = new Texture("cellMiss.png");
        cellHit = new Texture("cellHit.png");
    }

    @Override
    public void render(float delta) {
        if (client != null) {
            Board newBoardPlayer1 = client.getBoardPlayer1();
            if (newBoardPlayer1 != null && newBoardPlayer1 != boardPlayer1) {
                boardPlayer1 = newBoardPlayer1;
            }
            boardPlayer2 = client.getBoardPlayer2();
        }

        batch.begin();

        drawMyBoard(batch, boardPlayer1);
        drawEmptyBoard(batch);
        if (boardPlayer2 != null) {
            drawOpponentBoard(batch, boardPlayer2);
        } else {
            drawEmptyBoard(batch);
        }

        batch.end();
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
