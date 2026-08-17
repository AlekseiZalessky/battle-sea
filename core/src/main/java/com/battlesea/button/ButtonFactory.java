package com.battlesea.button;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import com.battlesea.client.Client;
import com.battlesea.Main;
import com.battlesea.constants.Commands;
import com.battlesea.enums.GameMode;
import com.battlesea.screens.*;

public class ButtonFactory {

    // Создание кнопки "Menu"
    public static TextButton createMenuButton(BitmapFont font) {
        TextButton button = new TextButton("Menu", ButtonStyles.createDefaultStyle(font));
        button.setSize(100, 60);
        button.setPosition(
            (Gdx.graphics.getWidth() - 120),
            (Gdx.graphics.getHeight() - 60)
        );
        return button;
    }

    // Создание кнопки "Abort"
    public static TextButton createAbortButton(Client client, BitmapFont font) {
        TextButton button = new TextButton("Abort", ButtonStyles.createDefaultStyle(font));
        button.setSize(100, 60);
        button.setPosition(
            (Gdx.graphics.getWidth() - 120),
            (Gdx.graphics.getHeight() - 60)
        );
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("click abort");
                client.sendMessage(Commands.ABORTING);
                client.setGameOver(true);
            }
        });
        return button;
    }

    // Создание кнопки "Start"
    public static TextButton createStartButton(BitmapFont font) {
        TextButton button = new TextButton("START", ButtonStyles.createDefaultStyle(font));
        button.setSize(100, 60);
        button.setPosition(700, 50);
        button.setDisabled(true);
        return button;
    }

    // Создание кнопки "Auto"
    public static TextButton createAutoButton(Client client, BitmapFont font) {
        TextButton button = new TextButton("AUTO", ButtonStyles.createDefaultStyle(font));
        button.setSize(100, 60);
        button.setPosition(500, 50);
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("click auto");
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                client.sendMessage(Commands.AUTO_PLACE);
            }
        });
        return button;
    }

    // Создание кнопки "PvE"
    public static TextButton createPvEButton(Main game, Client client, BitmapFont font) {
        TextButton button = new TextButton("PvE", ButtonStyles.createDefaultStyle(font));
        button.setSize(100, 60);
        button.setPosition(
            (Gdx.graphics.getWidth()) / 4,
            (Gdx.graphics.getHeight() - 60) / 2
        );
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("click PVE");
                game.setScreen(new PlacementScreen(client, game, GameMode.PVE));
            }
        });
        return button;
    }

    // Создание кнопки "Online"
    public static TextButton createOnlineButton(Main game, Client client, BitmapFont font) {
        TextButton button = new TextButton("Online", ButtonStyles.createDefaultStyle(font));
        button.setSize(100, 60);
        button.setPosition(
            (Gdx.graphics.getWidth()) / 2,
            (Gdx.graphics.getHeight() - 60) / 2
        );
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("click Online");
                game.setScreen(new PlacementScreen(client, game, GameMode.PVP_ONLINE));
            }
        });
        return button;
    }

    public static TextButton createExitButton(Main game, BitmapFont font) {
        TextButton button = new TextButton("Exit", ButtonStyles.createDefaultStyle(font));
        button.setSize(100, 60);
        button.setPosition(
            (Gdx.graphics.getWidth() - 120),
            (Gdx.graphics.getHeight() - 60)
        );
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ((Main) game).gracefulExit();
            }
        });
        return button;
    }
}
