package com.riseofcat;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.n8cats.lib_gwt.LibAllGwt;
import com.n8cats.share.Logic;

public class Core extends ApplicationAdapter {
private SpriteBatch batch;
private SpriteBatch backgroundBatch;
private ShapeRenderer2 shapeRenderer;
private Model model;
private Viewport viewport1;
private Viewport viewport2;
private Stage stage;
private static final Color[] colors = {Color.BLUE, Color.GOLD, Color.PINK, Color.RED, Color.GREEN, Color.VIOLET, Color.LIME, Color.TEAL, Color.YELLOW};
private static final boolean MULTIPLE_VIEWPORTS = false;
private static final boolean BACKGROUND_BATCH = true;
private static final boolean DRAW_GRID = true;
private ShaderProgram backgroundBatchShader;
private ShaderProgram batchShader;
public Core(App.Context context) {
	App.context = context;
}
public void create() {
	final FileHandle defaultVertex = Gdx.files.internal("shaders/default_vertex_shader.vert");
	ShaderProgram.pedantic = false;
	App.create();//todo
	batch = new SpriteBatch();
	if(BACKGROUND_BATCH) {
		backgroundBatch = new SpriteBatch();
		backgroundBatchShader = new ShaderProgram(defaultVertex, Gdx.files.internal("shaders/background/stars.frag"));
		boolean compiled = backgroundBatchShader.isCompiled();
		String log = backgroundBatchShader.getLog();
		backgroundBatch.setShader(backgroundBatchShader);
	}
	viewport1 = new ExtendViewport(1000f, 1000f, new OrthographicCamera());//todo 1000f
	if(MULTIPLE_VIEWPORTS) viewport2 = new ExtendViewport(500, 500, new OrthographicCamera());
	else viewport2 = viewport1;
	stage = new Stage(viewport2/*, batch*/);
	stage.addActor(new GradientShapeRect(200, 50));
	stage.addActor(new Image(Resources.Textures.tank));
	model = new Model();
	FileHandle fragmentShader = Gdx.files.internal("shaders/good_blur.frag");
	batchShader = new ShaderProgram(defaultVertex, fragmentShader);
	if(!batchShader.isCompiled()) {
		App.log.error(batchShader.getLog());
	}
	batch.setShader(batchShader);
	shapeRenderer = new ShapeRenderer2(10000, null);
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
	} else viewport1.update(width, height, true);
	batch.setProjectionMatrix(viewport2.getCamera().combined);
	shapeRenderer.setProjectionMatrix(viewport1.getCamera().combined);
}
Logic.XY backgroundOffset = new GdxXY(new Logic.XY());
public void render() {
	final boolean TEST_TEXTURE = LibAllGwt.TRUE();
	model.update(Gdx.graphics.getDeltaTime());
	Logic.State state = model.getDisplayState();
	if(state != null) {
		for(Logic.Car car : state.cars) {
			if(car.owner.equals(model.playerId)) {
				Logic.XY previous = new GdxXY(viewport1.getCamera().position);
				viewport1.getCamera().position.x = car.pos.x;
				viewport1.getCamera().position.y = car.pos.y;
				viewport1.getCamera().update();
				shapeRenderer.setProjectionMatrix(viewport1.getCamera().combined);
				Logic.XY change = new GdxXY(viewport1.getCamera().position).sub(previous);
				if(change.x > state.width/2) change.x -= state.width;
				else if(change.x < -state.width/2) change.x += state.width;
				if(change.y > state.height/2) change.y -= state.height;
				else if(change.y < -state.height/2) change.y += state.height;
				backgroundOffset = backgroundOffset.add(new GdxXY(change).scale(0.0001f));

				break;
			}
		}
	}
	Gdx.gl.glClearColor(0.f, 0.f, 0.f, 1);
	Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
	if(TEST_TEXTURE) {
		if(LibAllGwt.FALSE())stage.getViewport().apply();
		stage.act(/*Gdx.graphics.getDeltaTime()*/);
		stage.draw();
	}
	if(BACKGROUND_BATCH) {
		viewport2.apply();
//		backgroundBatchShader.setUniformf(), viewport2.getWorldWidth(), viewport2.getWorldHeight());
		backgroundBatch.begin();
		if(true)backgroundBatchShader.setUniformf(backgroundBatchShader.fetchUniformLocation("resolution", false), Gdx.graphics.getWidth(), Gdx.graphics.getHeight());//todo width height reverse in landscape
		else backgroundBatchShader.setUniformf("resolution", viewport2.getWorldWidth(), viewport2.getWorldHeight());
		backgroundBatchShader.setUniformf("time", App.sinceStartS());//30f
		backgroundBatchShader.setUniformf("mouse", backgroundOffset.x, backgroundOffset.y);
		backgroundBatch.draw(Resources.Textures.tank, 0, 0, viewport2.getWorldWidth(), viewport2.getWorldHeight());//todo change to mesh https://github.com/mc-imperial/libgdx-get-image
		backgroundBatch.end();
	}
	viewport1.apply();
	if(state != null) {
		if(DRAW_GRID) {
			shapeRenderer.begin(ShapeRenderer2.ShapeType.Line);
			shapeRenderer.setColor(Color.WHITE);
			float gridSize = 100;
			for(int x = 0; x*gridSize <= state.width; x++) shapeRenderer.line(x*gridSize, 0, 0, x*gridSize, state.height, 0);
			for(int y = 0; y*gridSize < state.height; y++) shapeRenderer.line(0, y*gridSize, 0, state.width, y*gridSize, 0);
			shapeRenderer.end();
		}
		shapeRenderer.begin(ShapeRenderer2.ShapeType.Filled);
		shapeRenderer.setColor(Color.GRAY);
		for(Logic.Food food : state.foods) {
			Logic.XY r = calcRenderXY(state, food.pos);
			shapeRenderer.circle(r.x, r.y, food.radius());
		}
		for(Logic.Reactive react : state.reactive) {
			Color color = colors[react.owner.id % (colors.length - 1)];
			shapeRenderer.setColor(color);
			shapeRenderer.circle(react.pos.x, react.pos.y, react.radius());
		}
		for(Logic.Car car : state.cars) {
			Color color = colors[car.owner.id % (colors.length - 1)];
			shapeRenderer.setColor(color);
			shapeRenderer.circle(car.pos.x, car.pos.y, car.radius(), 20);
		}
		shapeRenderer.end();
	}
	if(MULTIPLE_VIEWPORTS) viewport2.apply();
	batch.begin();
	float width = Gdx.graphics.getWidth();
	float height = Gdx.graphics.getHeight();
	batchShader.setUniformf("u_viewportInverse", new Vector2(1f / width, 1f / height));
	batchShader.setUniformf("u_offset",2f);
	batchShader.setUniformf("u_step", Math.min(1f, width / 70f));
	batchShader.setUniformf("u_color", new Vector3(0, 1, 1));
	Resources.Font.loadedFont().draw(batch, "fps: " + Gdx.graphics.getFramesPerSecond(), 0, 150);
	Resources.Font.loadedFont().draw(batch, model.getPlayerName(), 0, 200);
	Resources.Font.loadedFont().draw(batch, "latency:       " + (int) (model.client.latencyS * LibAllGwt.MILLIS_IN_SECCOND), 0, 250);
	Resources.Font.loadedFont().draw(batch, "smart latency: " + (int) (model.client.smartLatencyS * LibAllGwt.MILLIS_IN_SECCOND), 0, 300);
	if(TEST_TEXTURE) {
		batch.draw(Resources.Textures.tank, viewport2.getWorldWidth()/2, viewport2.getWorldHeight()/2);
		batch.draw(Resources.Textures.red, viewport2.getWorldWidth()/3, viewport2.getWorldHeight()/2);
		batch.draw(Resources.Textures.green, viewport2.getWorldWidth()/2, viewport2.getWorldHeight()/3);
		batch.draw(Resources.Textures.blue, viewport2.getWorldWidth()/3, viewport2.getWorldHeight()/3);
		batch.draw(Resources.Textures.yellow, viewport2.getWorldWidth()*2/3, viewport2.getWorldHeight()/2);
	}
	batch.end();
}
private Logic.XY calcRenderXY(Logic.State state, Logic.XY pos) {
	float x = pos.x;
	float dx = viewport1.getCamera().position.x - x;
	if(dx > state.width/2) x += state.width;
	else if(dx < -state.width/2) x -= state.width;
	return new Logic.XY(x, pos.y);//todo y
}
public void dispose() {
	model.dispose();
	batch.dispose();
	shapeRenderer.dispose();
	Resources.dispose();
}
}