package com.riseofcat;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.n8cats.share.Logic;

public class GdxXY extends Logic.XY {
public GdxXY(Vector2 vector) {
	super(vector.x, vector.y);
}
public GdxXY(Vector3 vector) {
	super(vector.x, vector.y);
}
public Vector2 getVector() {
	return new Vector2(x, y);
}
}
