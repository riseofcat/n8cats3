package com.riseofcat.desktop;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.github.czyzby.websocket.CommonWebSockets;
import com.riseofcat.Big;
import com.riseofcat.MainClass;

/** Launches the desktop (LWJGL) application. */
public class DesktopLauncher {
    public static void main(final String[] args) {
        // Initiating web sockets module:
        CommonWebSockets.initiate();
        createApplication();
    }

    private static LwjglApplication createApplication() {
        return new LwjglApplication(new MainClass(), getDefaultConfiguration());
    }

    private static LwjglApplicationConfiguration getDefaultConfiguration() {
        final LwjglApplicationConfiguration configuration = new LwjglApplicationConfiguration();
        configuration.title = "libgdxwebsocket";
        configuration.width = Big.WIDTH;
        configuration.height = Big.HEIGHT;
        for (int size : new int[] { 128, 64, 32, 16 }) {
            configuration.addIcon("libgdx" + size + ".png", FileType.Internal);
        }
        return configuration;
    }
}