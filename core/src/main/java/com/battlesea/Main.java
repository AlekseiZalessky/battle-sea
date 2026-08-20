package com.battlesea;

import com.badlogic.gdx.Game;
import com.battlesea.client.Client;
import com.battlesea.screens.MenuScreen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends Game {
    private static final Logger log = LoggerFactory.getLogger(Main.class);
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

    public void gracefulExit() {
        if (client != null) {
            client.disconnect();
        }
        try {
            Thread.sleep(200);
        } catch (InterruptedException ignored) {
        }
        System.exit(0);
    }
}
