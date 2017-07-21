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
import com.badlogic.gdx.utils.Json;
import com.n8cats.lib_gwt.SignalListener;
import com.n8cats.share.ClientPayload;
import com.n8cats.share.ClientPayload2;
import com.n8cats.share.ClientSay;
import com.n8cats.share.ClientSay2;
import com.n8cats.share.ClientSay3;
import com.n8cats.share.ClientSayC;
import com.n8cats.share.Logic;
import com.n8cats.share.ServerPayload;
import com.n8cats.share.ServerSay;
import com.n8cats.share.ServerSayS;
import com.n8cats.share.Static;

import java.util.ArrayList;
import java.util.HashSet;
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
private ClientPayload2 serialized1;
private ClientSay<ClientPayload2> serialized2;
private ClientSay2 serialized3;
private ClientSay<ClientPayload2> serialized4;
private ClientSay3 serialized5;
private ServerSay<ServerPayload> serialized6;

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
		boolean local = true;
		if(local) {
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
	ClientPayload2 payload = new ClientPayload2();
	payload.message = " payload message";
	serialized1 = new Json().fromJson(ClientPayload2.class, new Json().toJson(payload));
	ClientSay<ClientPayload2> say = new ClientSay<>();
	say.payload = payload;
	serialized2 = new Json().fromJson(say.getClass(), new Json().toJson(say));
	serialized3 = new Json().fromJson(ClientSay2.class, new Json().toJson(say));
	serialized4 = new Json().fromJson(ClientSay2.class, new Json().toJson(say));
	ClientSay<Static.ClientPayload3> say2 = new ClientSay<>();
	say2.payload = new Static.ClientPayload3();
	say2.payload.message = "payload message 2";
	serialized5 = new Json().fromJson(ClientSay3.class, new Json().toJson(say2));
	ServerSay<ServerPayload> say3 = new ServerSay<>();
	say3.payload = new ServerPayload();
	say3.payload.state = new ServerPayload.State();
	say3.payload.state.cars = new Logic.Car[1];
	Logic.Car c = new Logic.Car();
	c.playerId = new Logic.Player.Id(10);
	say3.payload.state.cars[0] = c;
	serialized6 = new Json().fromJson(ServerSayS.class, new Json().toJson(say3));
}
@Override
public void render() {
	Gdx.gl.glClearColor(0, 0, 0, 1);
	Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
	batch.begin();
	font.draw(batch, "test", 0, 400);
	if(serialized1 != null) {
		font.draw(batch, "1" + serialized1.message, 0, 100);
	}
	if(serialized2 != null) {
		font.draw(batch, "2" + serialized2.payload.message, 0, 130);
	}
	if(serialized3 != null) {
		font.draw(batch, "3" + serialized3.payload.message, 0, 160);
	}
	if(serialized4 != null) {
		font.draw(batch, "4" + serialized4.payload.message, 0, 190);
	}
	if(serialized5 != null) {
		font.draw(batch, "5" + serialized5.payload.message, 0, 230);
	}
	if(serialized6 != null) {
		for(Logic.Car car : serialized6.payload.state.cars) {
			font.draw(batch, "6 " + car.playerId.id, 0, 260);
		}
	}
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