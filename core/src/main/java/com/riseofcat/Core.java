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

import java.util.ArrayList;
import java.util.List;

public class Core extends ApplicationAdapter {
public static final int WIDTH = 640, HEIGHT = 640;
private SpriteBatch batch;
private BitmapFont font;
private RealTimeClient<ServerPayload, ClientPayload> client;
private List<RealTimeClient<ServerPayload, ClientPayload>> clients = new ArrayList<>();
private ServerPayload answer;

@Override
public void create() {
	Gdx.app.setLogLevel(Application.LOG_DEBUG);
	batch = new SpriteBatch();
	font = new BitmapFont();
	for(int i=0;i < 1; i++) {
		boolean local = true;
		if(local) {
			client =  new RealTimeClient("localhost", 5000, "socket", ServerSayS.class);
		} else {
			client = new RealTimeClient("n8cats3.herokuapp.com", 80, "socket", ServerSayS.class);
		}
		clients.add(client);
	}
	client.incoming.add(new SignalListener<ServerPayload>() {
		@Override
		public void onSignal(ServerPayload arg) {
			answer = arg;
		}
	});
	ClientPayload payload = new ClientPayload();
	payload.message = "from client";
	client.say(payload);
}
@Override
public void render() {
	Gdx.gl.glClearColor(0, 0, 0, 1);
	Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
	batch.begin();
	int y = 20;
	for(RealTimeClient<ServerPayload, ClientPayload> client : clients) {
		font.draw(batch, client.id +  ": latency = " + client.latency, 10f, y);
		y+=20;
	}
	if(answer != null) {

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