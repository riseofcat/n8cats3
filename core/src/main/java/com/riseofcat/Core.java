package com.riseofcat;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
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

public class Core extends ApplicationAdapter {
public static final int WIDTH = 640, HEIGHT = 640;
private SpriteBatch batch;
private BitmapFont font;
private RealTimeClient<ServerPayload, ClientPayload> client;
private ShapeRenderer shapeRenderer;
float scaleX = WIDTH / Logic.width;
float scaleY = HEIGHT / Logic.height;
final static boolean LOCAL = true;

public void create() {
	Gdx.app.setLogLevel(Application.LOG_DEBUG);
	batch = new SpriteBatch();
	font = new BitmapFont();
	shapeRenderer = new ShapeRenderer(200);
	Gdx.input.setInputProcessor(new InputAdapter() {
		public boolean touchDown(int screenX, int screenY, int pointer, int button) {
			float x = (screenX) / scaleX;
			float y = (Gdx.graphics.getHeight() - screenY) / scaleY;
			return true;
		}
	});
	if(LOCAL) {
		client = new RealTimeClient("localhost", 5000, "socket", ServerSayS.class);
	} else {
		client = new RealTimeClient("n8cats3.herokuapp.com", 80, "socket", ServerSayS.class);
	}
	client.incoming.add(new SignalListener<ServerPayload>() {
		public void onSignal(ServerPayload arg) {
		}
	});
	client.say(new ClientPayload());
}
public void render() {
	Gdx.gl.glClearColor(0, 0, 0, 1);
	Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
	batch.begin();
	font.draw(batch, "test", 0, 400);
	batch.end();
	if(false) {
		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		shapeRenderer.setColor(Color.BLUE);
		Logic.Car car = new Logic.Car();
		shapeRenderer.circle(car.x * scaleX, car.y * scaleY, 10);
		shapeRenderer.end();
	}
}
public void dispose() {
	client.close();
	batch.dispose();
	font.dispose();
	shapeRenderer.dispose();
}
}