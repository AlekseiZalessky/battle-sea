package com.battlesea.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.battlesea.client.Client;
import com.battlesea.Main;
import com.battlesea.button.ButtonFactory;

public class GameOverScreen implements Screen {
    private Main game;
    private Client client;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private BitmapFont font;

    private TextButton menuButton;
    private Stage stage;


    public GameOverScreen(Client client, Main game) {
        this.client = client;
        this.game = game;
    }
    @Override
    public void show() {
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(2f);
        font.setColor(Color.WHITE);

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        // Кнопка "В меню"
        menuButton = ButtonFactory.createMenuButton(font);
        menuButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                dispose();
                client.sendMessage("UPDATE_FIELDS");
                client.updateOnStartGame();
                game.setScreen(new MenuScreen(client, game));
            }
        });
        stage.addActor(menuButton);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0.2f, 1);

        camera.update();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();

        String text;
        if(client.getCurrentPlayer().equals(client.winner())) {
            text = "You won!";
        } else {
            text = "You lost!";
        }

        float textWidth = font.getData().scaleX * text.length() * 20;
        float x = (Gdx.graphics.getWidth() - textWidth) / 2;
        float y = Gdx.graphics.getHeight() / 2f;

        font.draw(batch, text, x, y);
        batch.end();

        stage.act();
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

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
    }
}
