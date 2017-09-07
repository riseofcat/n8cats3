package com.riseofcat;

import com.badlogic.gdx.utils.Json;

public class CoreUtils {
private static final Json json = new Json();
public static <T> T copy(T value) {
	return (T) json.fromJson(value.getClass(), json.toJson(value));//todo better without json
}
}
