package com.riseofcat.gwt;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;
import com.github.czyzby.websocket.GwtWebSockets;
import com.google.gwt.core.client.Duration;
import com.n8cats.share.Params;
import com.riseofcat.App;
import com.riseofcat.Core;

import java.util.ArrayList;
import java.util.List;

/**
 * Launches the GWT application.
 */
public class GwtLauncher extends GwtApplication {
@Override
public GwtApplicationConfiguration getConfig() {
	GwtApplicationConfiguration configuration = new GwtApplicationConfiguration(640, 640);
	configuration.preferFlash = false;
	return configuration;
}

@Override
public ApplicationListener createApplicationListener() {
	// Initiating GWT web sockets module:
	if(false) {
		Duration.currentTimeMillis();
	}
	GwtWebSockets.initiate();
	return new Core(new App.Context() {
		public <T> List<T> createConcurrentList() {
			return new ArrayList<>();
		}
	});
}
}