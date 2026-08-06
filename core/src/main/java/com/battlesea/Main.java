package com.battlesea;

import com.badlogic.gdx.Game;
import com.battlesea.Client.Client;
import com.battlesea.screens.MenuScreen;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends Game {
    private Client client;

    @Override
    public void create() {
        try {
            client = new Client("localhost", 8189);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        setScreen(new MenuScreen(client, this));
    }
}
