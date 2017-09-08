package com.riseofcat.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.github.czyzby.websocket.CommonWebSockets;
import com.n8cats.share.Params;
import com.riseofcat.App;
import com.riseofcat.Core;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Launches the desktop (LWJGL) application.
 */
public class DesktopLauncher {
public static void main(final String[] args) {
	// Initiating web sockets module:
	CommonWebSockets.initiate();
	createApplication();
}

private static LwjglApplication createApplication() {
	return new LwjglApplication(new Core(new App.Context() {
		public <T> List<T> createConcurrentList() {
			return new CopyOnWriteArrayList<>();
		}
	}), getDefaultConfiguration());
}

private static LwjglApplicationConfiguration getDefaultConfiguration() {
	final LwjglApplicationConfiguration configuration = new LwjglApplicationConfiguration();
	configuration.title = Params.TITLE;
	configuration.width = 640;
	configuration.height = 480;
//	for(int size : new int[]{128, 64, 32, 16}) {
//		configuration.addIcon("libgdx" + size + ".png", FileType.Internal);
//	}
	return configuration;
}
}