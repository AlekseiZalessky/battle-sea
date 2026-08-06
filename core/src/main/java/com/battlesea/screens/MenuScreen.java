package com.battlesea.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.battlesea.client.Client;
import com.battlesea.Main;
import com.battlesea.button.ButtonFactory;

public class MenuScreen implements Screen {
    private final Main game;
    private Stage stage;
    private BitmapFont font;
    private Client client;

    public MenuScreen(Client client, Main game) {
        this.game = game;
        this.client = client;
    }

    @Override
    public void show() {
        font = new BitmapFont();
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        // Кнопка режим PvE
        TextButton pveButton = ButtonFactory.createPvEButton(game, client, font);
        stage.addActor(pveButton);

        // Кнопка режим Online
        TextButton onlineButton = ButtonFactory.createOnlineButton(game, client, font);
        stage.addActor(onlineButton);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.3f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        if (stage != null) {
            stage.dispose();
            stage = null;
        }
        if (font != null) {
            font.dispose();
            font = null;
        }
    }
}
