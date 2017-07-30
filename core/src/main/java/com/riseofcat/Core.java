package com.riseofcat;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.n8cats.lib_gwt.SignalListener;
import com.n8cats.share.ClientPayload;
import com.n8cats.share.Logic;
import com.n8cats.share.ServerPayload;
import com.n8cats.share.redundant.ServerSayS;

import java.util.ArrayList;
import java.util.List;

public class Core extends ApplicationAdapter {
public static final int WIDTH = 640, HEIGHT = 640;
private SpriteBatch batch;
private BitmapFont font;
private RealTimeClient<ServerPayload, ClientPayload> client;
private List<RealTimeClient<ServerPayload, ClientPayload>> clients = new ArrayList<>();
private ServerPayload answer;
private ShapeRenderer shapeRenderer;
float scaleX = WIDTH/Logic.width;
float scaleY = HEIGHT/Logic.height;
final static boolean LOCAL = true;

@Override
public void create() {
	Gdx.input.setInputProcessor(new InputProcessor() {
		@Override
		public boolean keyDown(int keycode) {
			return false;
		}
		@Override
		public boolean keyUp(int keycode) {
			return false;
		}
		@Override
		public boolean keyTyped(char character) {
			return false;
		}
		@Override
		public boolean touchDown(int screenX, int screenY, int pointer, int button) {

			ClientPayload payload = new ClientPayload();
			payload.action = new Logic.Action();
			payload.action.touchX = (screenX)/scaleX;
			payload.action.touchY = (Gdx.graphics.getHeight() - screenY)/scaleY;
			client.say(payload);
			return true;
		}
		@Override
		public boolean touchUp(int screenX, int screenY, int pointer, int button) {
			return false;
		}
		@Override
		public boolean touchDragged(int screenX, int screenY, int pointer) {
			return false;
		}
		@Override
		public boolean mouseMoved(int screenX, int screenY) {
			return false;
		}
		@Override
		public boolean scrolled(int amount) {
			return false;
		}
	});
	shapeRenderer = new ShapeRenderer(200);
	Gdx.app.setLogLevel(Application.LOG_DEBUG);
	batch = new SpriteBatch();
	font = new BitmapFont();
	for(int i = 0; i < 1; i++) {
		if(LOCAL) {
			client = new RealTimeClient("localhost", 5000, "socket", ServerSayS.class);
		} else {
			client = new RealTimeClient("n8cats3.herokuapp.com", 80, "socket", ServerSayS.class);
		}
		clients.add(client);
	}
	if(client != null) {
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
}
@Override
public void render() {
	Gdx.gl.glClearColor(0, 0, 0, 1);
	Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
	batch.begin();
	font.draw(batch, "test", 0, 400);
	int y = 20;
	for(RealTimeClient<ServerPayload, ClientPayload> client : clients) {
		font.draw(batch, client.id + ": latency = " + client.latency, 10f, y);
		y += 20;
	}
	if(answer != null) {
		if(answer.message != null) {
			font.draw(batch, answer.message, Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);
		}
	}
	batch.end();
	if(answer != null && answer.state != null) {
		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		shapeRenderer.setColor(Color.BLUE);
		for(Logic.Car car : answer.state.cars) {
			shapeRenderer.circle(car.x*scaleX, car.y*scaleY, 10);
		}
		shapeRenderer.end();
	}
}
@Override
public void dispose() {
	client.close();
	batch.dispose();
	font.dispose();
}
}