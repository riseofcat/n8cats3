package com.riseofcat;

import com.badlogic.gdx.utils.Json;
import com.n8cats.share.IStringSerializer;

import java.io.Reader;
public class JsonSerializer<T> implements IStringSerializer<T> {
public static final Json JSON = new Json();
private final Class<T> clazz;
public JsonSerializer(Class<T> clazz) {
	this.clazz = clazz;
}
@Override
public T fromStr(String str) {
	return JSON.fromJson(clazz, str);
}
@Override
public T fromStr(Reader reader) {
	return JSON.fromJson(clazz, reader);
}
@Override
public String toStr(T obj) {
	return JSON.toJson(obj);
}
}
