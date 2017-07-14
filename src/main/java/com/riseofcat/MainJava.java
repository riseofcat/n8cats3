package com.riseofcat;

import static spark.Spark.*;//http://sparkjava.com/documentation

public class MainJava {
public static final float MEGA = 1E6f;
public static void main(String[] args) {
	String port = System.getenv("PORT");
	if(port != null && port.length() > 0) {
		port(Integer.parseInt(port));
	} else {
		port(5000);
	}
	if(false) {//todo
		threadPool(10, 2, 30 * 1000);
	}
	staticFiles.location("/public");
	//staticFileLocation("/public");
	staticFiles.expireTime(600);
	//https://github.com/tipsy/spark-websocket
	webSocket("/socket", EchoWebSocket.class);
	init();
	get("/", (req, res) -> {
		return new StringBuilder().append("maxMemoty = ")
				.append(Runtime.getRuntime().maxMemory()/MEGA)
				.append("\n<br/>totalMemory = ")
				.append(Runtime.getRuntime().totalMemory()/MEGA)
				.append("\n<br/>freeMemory = ")
				.append(Runtime.getRuntime().freeMemory()/MEGA);
	});
	if(false) {
		stop();
	}
}
}
