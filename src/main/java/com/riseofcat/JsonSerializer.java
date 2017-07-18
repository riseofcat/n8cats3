package com.riseofcat;

import com.badlogic.gdx.utils.Json;
import com.n8cats.share.ClientSay;
import com.n8cats.share.ServerSay;

import java.io.Reader;
public class JsonSerializer<C,S> implements StringSerializedRealTimeServer.IStringSerializer<C,S> {
public static final Json JSON = new Json();
private final Class<ClientSay<C>> typeC;
private final Class<ServerSay<S>> typeS;
public JsonSerializer(Class<ClientSay<C>> typeC, Class<ServerSay<S>> typeS) {
	this.typeC = typeC;
	this.typeS = typeS;
}
@Override
public ClientSay<C> fromStringC(String str) {
	return JSON.fromJson(typeC, str);
}
@Override
public ServerSay<S> fromStringS(String str) {
	return JSON.fromJson(typeS, str);
}
@Override
public ClientSay<C> fromStringC(Reader reader) {
	return JSON.fromJson(typeC, reader);
}
@Override
public ServerSay<S> fromStringS(Reader reader) {
	return JSON.fromJson(typeS, reader);
}
@Override
public String toStringC(ClientSay<C> c) {
	return JSON.toJson(c);
}
@Override
public String toStringS(ServerSay<S> s) {
	return JSON.toJson(s);
}
}
