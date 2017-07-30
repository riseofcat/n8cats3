package com.riseofcat;

import com.n8cats.lib.LibAll;
import com.n8cats.share.redundant.ClientSayC;
import com.n8cats.share.Logic;
import com.n8cats.share.redundant.ServerSayS;

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
	RoomsRTServer roomsServer = new RoomsRTServer();
	roomsServer.onRoomCreated.add(room -> new TickGame(room, new Logic()));
	PingPongRTServer stringSerialized = new PingPongRTServer(roomsServer, 1000, new JsonSerializer(ClientSayC.class), new JsonSerializer(ServerSayS.class));
	Spark.webSocket("/socket", new SparkWebSocket(stringSerialized));
	Spark.get("/", new Route() {
		@Override
		public Object handle(Request request, Response response) {
			return LibAll.JSON.toPrettyStr(App.info);
		}
	});
	Spark.init();//Spark.stop();
}
}
