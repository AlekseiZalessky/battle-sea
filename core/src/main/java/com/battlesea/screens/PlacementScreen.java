package com.battlesea.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.battlesea.Client.Client;
import com.battlesea.Main;
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
    private Board boardPlayer2;
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

        // Создаём текстуры для кнопок
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);

        // Активная кнопка (синяя)
        pixmap.setColor(0.2f, 0.4f, 0.6f, 1);
        pixmap.fill();
        Texture activeTexture = new Texture(pixmap);
        TextureRegionDrawable activeUp = new TextureRegionDrawable(new TextureRegion(activeTexture));

        // Неактивная кнопка (серая)
        pixmap.setColor(0.4f, 0.4f, 0.4f, 1);
        pixmap.fill();
        Texture disabledTexture = new Texture(pixmap);
        TextureRegionDrawable disabledUp = new TextureRegionDrawable(new TextureRegion(disabledTexture));

        // Кнопка при наведении (светло-синяя)
        pixmap.setColor(0.3f, 0.5f, 0.7f, 1);
        pixmap.fill();
        Texture overTexture = new Texture(pixmap);
        TextureRegionDrawable overUp = new TextureRegionDrawable(new TextureRegion(overTexture));

        // Кнопка при нажатии (темно-синяя)
        pixmap.setColor(0.1f, 0.2f, 0.4f, 1);
        pixmap.fill();
        Texture downTexture = new Texture(pixmap);
        TextureRegionDrawable downUp = new TextureRegionDrawable(new TextureRegion(downTexture));

        pixmap.dispose();

        // Создаём стиль для кнопки
        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = font;
        buttonStyle.fontColor = Color.WHITE;
        buttonStyle.up = activeUp;
        buttonStyle.over = overUp;
        buttonStyle.down = downUp;
        buttonStyle.disabledFontColor = Color.GRAY; // Цвет текста для неактивной кнопки
        buttonStyle.disabled = disabledUp; // Фон для неактивной кнопки

        TextButton pveButton = new TextButton("AUTO", buttonStyle);
        pveButton.setSize(100, 60);
        pveButton.setPosition(500, 200);
        pveButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("click auto");
                timeout(200);
                client.sendMessage("PVE_AUTO");
            }
        });
        stage.addActor(pveButton);

        startGame = new TextButton("START", buttonStyle);
        startGame.setSize(100, 60);
        startGame.setPosition(700, 200);
        startGame.setDisabled(true);
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
        stage.dispose();
        font.dispose();
    }

    @Override
    public void dispose() {
        stage.dispose();
        font.dispose();
    }
}
