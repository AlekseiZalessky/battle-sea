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
import com.battlesea.model.Game;
import com.battlesea.model.Player;

public class GameOverScreen implements Screen {
    private Main game;
    private Client client;
    private Player winner;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private BitmapFont font;

    private TextButton menuButton;
    private Stage stage;


    public GameOverScreen(Client client, Main game) {
        this.client = client;
        this.game = game;
        this.winner = client.winner();
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
                client.sendMessage("UPDATE_FIELDS");
                client.updateOnStartGame();
                game.setScreen(new MenuScreen(client, game));
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

        batch.begin();

        String text;
        if(client.getCurrentPlayer().equals(winner)) {
            text = "You won!";
        } else {
            text = "You lost!";
        }

        float textWidth = font.getData().scaleX * text.length() * 20;
        float x = (Gdx.graphics.getWidth() - textWidth) / 2;
        float y = Gdx.graphics.getHeight() / 2f;

        font.draw(batch, text, x, y);
        stage.draw();
        batch.end();
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

    }
}
