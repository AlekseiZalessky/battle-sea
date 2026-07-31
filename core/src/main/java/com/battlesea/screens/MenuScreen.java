package com.battlesea.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.battlesea.Client.Client;
import com.battlesea.Main;
import com.battlesea.enums.GameMode;

public class MenuScreen implements Screen {
    private final Main game;
    private Stage stage;
    private BitmapFont font;
    private Client client;

    public MenuScreen(Main game) {
        this.game = game;
        System.out.println("game: " + game);
    }

    @Override
    public void show() {
        try {
            client = new Client("localhost", 8189);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        font = new BitmapFont();
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        // Создаём простую текстуру для кнопки
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(0.2f, 0.4f, 0.6f, 1); // синий цвет
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();

        TextureRegionDrawable buttonUp = new TextureRegionDrawable(new TextureRegion(texture));

        // Создаём стиль
        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = font;
        buttonStyle.up = buttonUp; // ← добавляем фон

        // Кнопка PvE
        TextButton pveButton = new TextButton("Играть против компьютера (PvE)", buttonStyle);
        pveButton.setSize(400, 60);
        pveButton.setPosition(
            (Gdx.graphics.getWidth() - 400) / 2,  // центрируем по горизонтали
            (Gdx.graphics.getHeight() - 60) / 2   // центрируем по вертикали
        );
        pveButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("click PVE");
                game.setScreen(new PlacementScreen(client, game, GameMode.PVE));
            }
        });
        stage.addActor(pveButton);
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
        stage.dispose();
        font.dispose();
    }
}
