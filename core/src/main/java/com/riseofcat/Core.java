package com.riseofcat;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.n8cats.share.Logic;

public class Core extends ApplicationAdapter {
public static final int WIDTH = 640, HEIGHT = 640;
private SpriteBatch batch;
private BitmapFont font;
private ShapeRenderer shapeRenderer;
float scaleX = WIDTH / Logic.width;
float scaleY = HEIGHT / Logic.height;
private Model model;

public void create() {
	model = new Model();
	batch = new SpriteBatch();
	font = new BitmapFont();
	shapeRenderer = new ShapeRenderer(200);
	Gdx.input.setInputProcessor(new InputAdapter() {
		public boolean touchDown(int screenX, int screenY, int pointer, int button) {
			float x = (screenX) / scaleX;
			float y = (Gdx.graphics.getHeight() - screenY) / scaleY;
			model.touch(x,y);
			return true;
		}
	});
	JsonTest.test(App.log);
}
public void render() {
	model.update(Gdx.graphics.getDeltaTime());
	Gdx.gl.glClearColor(0, 0, 0, 1);
	Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
	batch.begin();
	font.draw(batch, "test", 0, 400);
	batch.end();
	shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
	for(Logic.Car car : model.getDisplayState().cars) {
		shapeRenderer.setColor(Color.BLUE);
		shapeRenderer.circle(car.x * scaleX, car.y * scaleY, 10);
	}
	shapeRenderer.end();
}
public void dispose() {
	model.dispose();
	batch.dispose();
	font.dispose();
	shapeRenderer.dispose();
}
}