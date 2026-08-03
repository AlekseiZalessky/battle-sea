package com.battlesea.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
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
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.battlesea.Client.Client;
import com.battlesea.Main;
import com.battlesea.enums.GameMode;

public class TimeOutScreen implements Screen {
    private final Client client;
    private final Main game;
    private final GameMode gameMode;

    private Stage stage;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private BitmapFont font;
    private TextButton menuButton;

    public TimeOutScreen(Client client, Main game, GameMode gameMode) {
        this.client = client;
        this.game = game;
        this.gameMode = gameMode;
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
        Gdx.input.setInputProcessor(stage);  // ← ВАЖНО: обработка ввода

        // Стиль для кнопки
        TextButton.TextButtonStyle buttonStyle = createButtonStyle();

        // Кнопка "В меню"
        menuButton = new TextButton("Menu", buttonStyle);
        menuButton.setSize(150, 60);
        menuButton.setPosition(
            (Gdx.graphics.getWidth() - 150) / 2f,
            Gdx.graphics.getHeight() / 2f - 100
        );
        menuButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("Возврат в меню из TimeOutScreen");
                dispose();  // ← очищаем ресурсы
                game.setScreen(new MenuScreen(game));
            }
        });
        stage.addActor(menuButton);
    }

    private TextButton.TextButtonStyle createButtonStyle() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);

        // Активная кнопка (синяя)
        pixmap.setColor(0.2f, 0.4f, 0.6f, 1);
        pixmap.fill();
        Texture activeTexture = new Texture(pixmap);
        TextureRegionDrawable activeUp = new TextureRegionDrawable(new TextureRegion(activeTexture));

        // При наведении
        pixmap.setColor(0.3f, 0.5f, 0.7f, 1);
        pixmap.fill();
        Texture overTexture = new Texture(pixmap);
        TextureRegionDrawable overUp = new TextureRegionDrawable(new TextureRegion(overTexture));

        // При нажатии
        pixmap.setColor(0.1f, 0.2f, 0.4f, 1);
        pixmap.fill();
        Texture downTexture = new Texture(pixmap);
        TextureRegionDrawable downUp = new TextureRegionDrawable(new TextureRegion(downTexture));

        pixmap.dispose();

        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
        style.font = font;
        style.fontColor = Color.WHITE;
        style.up = activeUp;
        style.over = overUp;
        style.down = downUp;

        return style;
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0.2f, 1);

        camera.update();
        batch.setProjectionMatrix(camera.combined);

        // Рисуем текст через batch
        batch.begin();
        String text = "TIMEOUT!";
        float textWidth = font.getData().scaleX * text.length() * 20;
        float x = (Gdx.graphics.getWidth() - textWidth) / 2;
        float y = Gdx.graphics.getHeight() / 2f + 50;
        font.draw(batch, text, x, y);
        batch.end();

        // Обновляем и рисуем Stage (кнопки)
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        camera.setToOrtho(false, width, height);
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
        if (batch != null) {
            batch.dispose();
            batch = null;
        }
        if (camera != null) {
            camera = null;
        }
    }
}
