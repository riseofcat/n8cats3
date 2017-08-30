package com.riseofcat;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Resources {
public static class Textures {
	public static final Texture green = new Texture("green_tank.png");
	public static final Texture font = new Texture(Gdx.files.internal("dejavu_sans_mono.png"));
	public static Texture background;
	static {
		font.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
		if(false) {
			background.setWrap(Texture.TextureWrap.MirroredRepeat, Texture.TextureWrap.MirroredRepeat);
		}
	}
}
public static class Font {
	private static BitmapFont font = new BitmapFont(Gdx.files.internal("dejavu_sans_mono.fnt"), new TextureRegion(Textures.font));

	public static BitmapFont loseFont() {
		font.setColor(Color.RED);
		font.getData().setScale(2);
		return font;
	}
	public static BitmapFont winFont() {
		font.setColor(Color.GREEN);
		font.getData().setScale(2);
		return font;
	}
	public static BitmapFont normalFont() {
		font.setColor(Color.WHITE);
		font.getData().setScale(2);
		return font;
	}
}
public static void dispose() {
	//todo
}

}
