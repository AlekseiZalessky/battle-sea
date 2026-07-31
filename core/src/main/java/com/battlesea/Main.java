package com.battlesea;

import com.badlogic.gdx.Game;
import com.battlesea.screens.FirstScreen;
import com.battlesea.screens.MenuScreen;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends Game {
    @Override
    public void create() {
        setScreen(new MenuScreen(this));
    }
}
