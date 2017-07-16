package com.riseofcat;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.n8cats.lib_gwt.SignalListener;
import com.n8cats.share.ClientPayload;
import com.n8cats.share.ServerPayload;

public class Core extends ApplicationAdapter {
public static final int WIDTH = 640, HEIGHT = 480;
public static final String SERVER = "n8cats3.herokuapp.com";
private SpriteBatch batch;
private BitmapFont font;
private RealTimeClient<ServerPayload, ClientPayload> client;
private ServerPayload answer;

@Override
public void create() {
	Gdx.app.setLogLevel(Application.LOG_DEBUG);
	batch = new SpriteBatch();
	font = new BitmapFont();
	boolean local = true;
	if(local) {
		client = new RealTimeClient("localhost", 5000, "socket", ServerSayS.class);
	} else {
		client = new RealTimeClient(SERVER, 80, "socket", ServerSayS.class);
	}
	client.incoming.add(new SignalListener<ServerPayload>() {
		@Override
		public void onSignal(ServerPayload arg) {
			answer = arg;
		}
	});
}
@Override
public void render() {
	Gdx.gl.glClearColor(0, 0, 0, 1);
	Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
	batch.begin();
	if(answer != null) {
		font.draw(batch, "latency: " + client.latency + " message:" + answer.message, 10f, 20);
	}
	batch.end();
}
@Override
public void dispose() {
	client.close();
	batch.dispose();
	font.dispose();
}
}