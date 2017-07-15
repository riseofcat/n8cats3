package com.riseofcat;

import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;//import static spark.Spark.*;//http://sparkjava.com/documentation

public class MainJava {
public static final float MEGA = 1E6f;
public static void main(String[] args) {
	String port = System.getenv("PORT");
	if(port != null) {
		Spark.port(Integer.parseInt(port));
	} else {
		Spark.port(5000);
	}
	if(false) {//todo
		Spark.threadPool(10, 2, 30 * 1000);
	}
	Spark.staticFiles.location("/public");
	Spark.staticFiles.expireTime(600);
	//https://github.com/tipsy/spark-websocket
	Spark.webSocket("/socket", EchoWebSocket.class);
	Spark.init();
	Spark.get("/", new Route() {
		@Override
		public Object handle(Request request, Response response) throws Exception {
			return new StringBuilder().append("maxMemoty = ")
					.append(Runtime.getRuntime().maxMemory()/MEGA)
					.append("\n<br/>usedMemory = ")
					.append((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())/MEGA);
		}
	});
	if(false) {
		Spark.stop();
	}
}
}
