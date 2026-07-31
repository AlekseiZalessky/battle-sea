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
public class FirstScreen implements Screen {
    private Client client;
    private Board board;
    private SpriteBatch batch;
    private Texture cellEmpty;
    private Texture cellShip;
    private Texture cellHalo;
    private Main game;
    private GameMode gamemode;

    public FirstScreen(Client client, Main game, GameMode gamemode) {
        this.client = client;
        this.game = game;
        this.gamemode = gamemode;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        cellEmpty = new Texture("cellEmpty.png");
        cellShip = new Texture("cellShip.png");
        cellHalo = new Texture("cellHalo.png");

        // Prepare your screen here.
    }

    @Override
    public void render(float delta) {
        if (client != null) {
            Board newBoard = client.getBoard();
            if (newBoard != null && newBoard != board) {
                board = newBoard;
            }
        }
        batch.begin();

        if (board != null) {
            drawMyBoard(batch, board);
            drawOpponentBoard(batch, board);
        }

        batch.end();
    }

    private void drawMyBoard(SpriteBatch batch, Board board) {
//        Cell[][] cells = board.getCells();
//
//        for (int i = 0; i < 10; i++) {
//            for (int j = 0; j < 10; j++) {
//                if (cells != null) {
//                    if (cells[i][j] == Cell.EMPTY || cells[i][j] == Cell.HALO) {
//                        batch.draw(cellEmpty, 32 * i + 50, 32 * j + 50);
//                    }
//                    if (cells[i][j] == Cell.SHIP) {
//                        batch.draw(cellShip, 32 * i + 50, 32 * j + 50);
//                    }
////                    if (cells[i][j] == Cell.HALO) {
////                        batch.draw(cellHalo, 32 * i, 32 * j);
////                    }
//                }
//            }
//        }

    }

    private void drawOpponentBoard(SpriteBatch batch, Board board) {

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
