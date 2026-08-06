package com.battlesea.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.battlesea.client.Client;
import com.battlesea.Main;
import com.battlesea.button.ButtonFactory;
import com.battlesea.enums.Cell;
import com.battlesea.enums.GameMode;
import com.battlesea.model.Board;

public class PlacementScreen implements Screen {
    private final Main game;
    private Stage stage;
    private GameMode gameMode;
    private BitmapFont font;
    private Client client;
    private SpriteBatch batch;
    private Board boardPlayer1;
    private Texture cellEmpty;
    private Texture cellShip;
    private Texture cellHalo;
    private TextButton startGame;

    public PlacementScreen(Client client, Main game, GameMode gameMode) {
        this.game = game;
        this.gameMode = gameMode;
        this.client = client;
        this.boardPlayer1 = new Board();
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        cellEmpty = new Texture("cellEmpty.png");
        cellShip = new Texture("cellShip.png");
        cellHalo = new Texture("cellHalo.png");

        font = new BitmapFont();
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        TextButton autoButton  = ButtonFactory.createAutoButton(client, font);
        stage.addActor(autoButton);

        startGame = ButtonFactory.createStartButton(font);
        startGame.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!startGame.isDisabled()) {
                    System.out.println("click start");
                    if(gameMode == GameMode.PVE) {
                        client.sendMessage("START_GAME_PVE");
                        timeout(2000);
                        game.setScreen(new BattleScreen(client, game, gameMode));
                    } else  {
                        client.sendMessage("START_GAME_PVP_ONLINE");
                        timeout(1000);
                        game.setScreen(new WaitingStartBattleScreen(client, game, gameMode));
                    }
                }
            }
        });
        stage.addActor(startGame);

        // Кнопка возврата в меню
        TextButton menuButton = ButtonFactory.createMenuButton(font);
        menuButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("click menu");
                client.setGameOver(true);
                client.updateOnStartGame();
                game.setScreen(new MenuScreen(client, game));
            }
        });
        stage.addActor(menuButton);
    }

    private static void timeout(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.3f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (client != null) {
            Board newBoard = client.getBoardCreator();
            if (newBoard != null) {
                boardPlayer1 = newBoard;
                if (!boardPlayer1.getShips().isEmpty()) {
                    startGame.setDisabled(false);
                }
            }
        }

        batch.begin();

        if (boardPlayer1 != null) {
            drawMyBoard(batch, boardPlayer1);
        } else {
            drawEmptyBoard(batch);
        }

        stage.act(delta);
        stage.draw();

        batch.end();
    }

    private void drawEmptyBoard(SpriteBatch batch) {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                batch.draw(cellEmpty, 32 * i + 50, 32 * j + 50);
            }
        }
    }

    private void drawMyBoard(SpriteBatch batch, Board board) {
        if (board.getShips().isEmpty()) {
            drawEmptyBoard(batch);
            return;
        }

        Cell[][] cells = board.getCells();

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (cells[i][j] == Cell.EMPTY) {
                    batch.draw(cellEmpty, 32 * i + 50, 32 * j + 50);
                }
                if (cells[i][j] == Cell.SHIP) {
                    batch.draw(cellShip, 32 * i + 50, 32 * j + 50);
                }
            }
        }

    }

    @Override
    public void resize(int i, int i1) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void dispose() {
        if (batch != null) {
            batch.dispose();
            batch = null;
        }
        if (font != null) {
            font.dispose();
            font = null;
        }
        if (stage != null) {
            stage.dispose();
            stage = null;
        }
        if (cellEmpty != null) {
            cellEmpty.dispose();
            cellEmpty = null;
        }
        if (cellShip != null) {
            cellShip.dispose();
            cellShip = null;
        }
        if (cellHalo != null) {
            cellHalo.dispose();
            cellHalo = null;
        }
    }
}
