package com.riseofcat;

import com.badlogic.gdx.utils.Json;
import com.n8cats.lib.LibAll;
import com.n8cats.share.ClientPayload;
import com.n8cats.share.Logic;
import com.n8cats.share.ServerPayload;
import com.n8cats.share.redundant.ClientSayC;

import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;
//import static spark.Spark.*;
//http://sparkjava.com/documentation

public class MainJava {
public static void main(String[] args) {
	String port = java.lang.System.getenv("PORT");
	Spark.port(port != null ? Integer.parseInt(port) : 5000);
	if(false) {
		Spark.threadPool(30, 2, 30_000);
		Spark.webSocketIdleTimeoutMillis(30_000);
	}
	Spark.staticFiles.location("/public");
	Spark.staticFiles.expireTime(600);
	ConcreteRoomsServer roomsServer = new ConcreteRoomsServer();
	roomsServer.onRoomCreated.add(room -> new TickGame(room, new Logic()));

	final Json JSON = new Json();
	Spark.webSocket("/socket", new SparkWebSocket(new CountSesServ<>(new ConvertSesServ<>(new PingPongServ<>(roomsServer, 1000),
			obj -> JSON.fromJson(ClientSayC.class, obj),
			JSON::toJson))));
	Spark.get("/", new Route() {
		@Override
		public Object handle(Request request, Response response) {
			return LibAll.JSON.toPrettyStr(App.info);
		}
	});
	Spark.init();//Spark.stop();
}
}
