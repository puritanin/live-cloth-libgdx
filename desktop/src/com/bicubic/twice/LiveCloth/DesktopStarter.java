package com.bicubic.twice.LiveCloth;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class DesktopStarter {
    public static void main(String[] args) {
        LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
        cfg.title = "Desktop";
        cfg.width = 600;
        cfg.height = 900;
        cfg.foregroundFPS = 60;
        new LwjglApplication(new Main(), cfg);
    }
}
