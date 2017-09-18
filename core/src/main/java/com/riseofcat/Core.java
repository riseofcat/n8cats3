package com.riseofcat;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.n8cats.lib_gwt.LibAllGwt;
import com.n8cats.share.Logic;

public class Core extends ApplicationAdapter {
private SpriteBatch batch;
private ShapeRenderer shapeRenderer;
private Model model;
private Viewport viewport1;
private Viewport viewport2;
private Stage stage;
private static final Color[] colors = {Color.BLUE, Color.GOLD, Color.PINK, Color.RED, Color.GREEN, Color.VIOLET, Color.LIME, Color.TEAL, Color.YELLOW};
private static final boolean MULTIPLE_VIEWPORTS = false;
public Core(App.Context context) {
	App.context = context;
}
public void create() {
	App.create();
	batch = new SpriteBatch();
	viewport1 = new ExtendViewport(1000f, 1000f, new OrthographicCamera());//todo 1000f
	viewport2 = new ExtendViewport(500, 500, new OrthographicCamera());
	stage = new Stage(viewport2/*, batch*/);
	stage.addActor(new GradientShapeRect(200, 50));
	stage.addActor(new Image(Resources.Textures.green));
	model = new Model();
	shapeRenderer = new ShapeRenderer(10000);
	shapeRenderer.setAutoShapeType(false);
	Gdx.input.setInputProcessor(new InputMultiplexer(stage, new InputAdapter() {
		public boolean touchDown(int screenX, int screenY, int pointer, int button) {
			model.touch(new GdxXY(viewport1.unproject(new Vector2(screenX, screenY))));
			return true;
		}
	}));
}
public void resize(int width, int height) {
	if(MULTIPLE_VIEWPORTS) {
		viewport1.update(width / 2, height, true);
		viewport1.setScreenX(width / 2);
		viewport2.update(width / 2, height, true);
		batch.setProjectionMatrix(viewport2.getCamera().combined);
	} else {
		viewport1.update(width, height, true);
		batch.setProjectionMatrix(viewport1.getCamera().combined);
	}
	shapeRenderer.setProjectionMatrix(viewport1.getCamera().combined);
}
public void render() {
	final boolean TEST_TEXTURE = false;
	model.update(Gdx.graphics.getDeltaTime());
	Logic.State state = model.getDisplayState();
	if(state != null) {
		for(Logic.Car car : state.cars) {
			if(car.owner.equals(model.playerId)) {
				viewport1.getCamera().position.x = car.pos.x;
				viewport1.getCamera().position.y = car.pos.y;
				viewport1.getCamera().update();
				shapeRenderer.setProjectionMatrix(viewport1.getCamera().combined);
			}
		}
	}
	Gdx.gl.glClearColor(0, 0, 0, 1);
	Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
	if(TEST_TEXTURE) {
		if(false)stage.getViewport().apply();
		stage.act(/*Gdx.graphics.getDeltaTime()*/);
		stage.draw();
	}
	viewport1.apply();
	if(state != null) {
		shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
		shapeRenderer.setColor(Color.WHITE);
		float gridSize = 100;
		for(int x = 0; x*gridSize <= state.width; x++) shapeRenderer.line(x*gridSize, 0, 0, x*gridSize, state.height, 0);
		for(int y = 0; y*gridSize < state.height; y++) shapeRenderer.line(0, y*gridSize, 0, state.width, y*gridSize, 0);
		shapeRenderer.end();

		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		shapeRenderer.setColor(Color.GRAY);
		for(Logic.Food food : state.foods) shapeRenderer.circle(food.pos.x, food.pos.y, food.radius());
		for(Logic.Reactive react : state.reactive) {
			Color color = colors[react.owner.id % (colors.length - 1)];
			shapeRenderer.setColor(color);
			shapeRenderer.circle(react.pos.x, react.pos.y, react.radius());
		}
		for(Logic.Car car : state.cars) {
			Color color = colors[car.owner.id % (colors.length - 1)];
			shapeRenderer.setColor(color);
			shapeRenderer.circle(car.pos.x, car.pos.y, car.radius());
		}
		shapeRenderer.end();
	}
	if(MULTIPLE_VIEWPORTS) viewport2.apply();
	batch.begin();
	Resources.Font.loadedFont().draw(batch, "fps: " + Gdx.graphics.getFramesPerSecond(), 0, 150);
	Resources.Font.loadedFont().draw(batch, model.getPlayerName(), 0, 200);
	Resources.Font.loadedFont().draw(batch, "latency:       " + (int) (model.client.latencyS * LibAllGwt.MILLIS_IN_SECCOND), 0, 250);
	Resources.Font.loadedFont().draw(batch, "smart latency: " + (int) (model.client.smartLatencyS * LibAllGwt.MILLIS_IN_SECCOND), 0, 300);
	if(TEST_TEXTURE) batch.draw(Resources.Textures.green, Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);
	batch.end();
}
public void dispose() {
	model.dispose();
	batch.dispose();
	shapeRenderer.dispose();
	Resources.dispose();
}
}