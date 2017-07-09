package com.riseofcat;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.github.czyzby.websocket.WebSocket;
import com.github.czyzby.websocket.WebSocketAdapter;
import com.github.czyzby.websocket.WebSockets;
import com.github.czyzby.websocket.data.WebSocketCloseCode;
import com.github.czyzby.websocket.net.ExtendedNet;
import com.n8cats.share.Share;
import com.n8cats.lib_gwt.Const;

public class Small extends ApplicationAdapter {
    private SpriteBatch batch;
    private BitmapFont font;
    private WebSocket socket;

    @Override
    public void create() {
        Gdx.app.setLogLevel(Application.LOG_DEBUG);
        batch = new SpriteBatch();
        font = new BitmapFont();
        // Note: you can also use WebSockets.newSocket() and WebSocket.toWebSocketUrl() methods.
//        socket = ExtendedNet.getNet().newWebSocket("localhost", 8000);
        socket = ExtendedNet.getNet().newWebSocket("localhost", 5000, "socket");
//        socket = ExtendedNet.getNet().newWebSocket("n8cats-test.herokuapp.com", 80);
        socket.addListener(getListener());
        socket.connect();
    }

    private static WebSocketAdapter getListener() {
        return new WebSocketAdapter() {
            @Override
            public boolean onOpen(final WebSocket webSocket) {
                Gdx.app.log("WS", "Connected!");
                webSocket.send("Hello from client!");
                return FULLY_HANDLED;
            }

            @Override
            public boolean onClose(final WebSocket webSocket, final WebSocketCloseCode code, final String reason) {
                Gdx.app.log("WS", "Disconnected - status: " + code + ", reason: " + reason);
                return FULLY_HANDLED;
            }

            @Override
            public boolean onMessage(final WebSocket webSocket, final String packet) {
                Gdx.app.log("WS", "Got message: " + packet);
                return FULLY_HANDLED;
            }

            @Override
            public boolean onMessage(WebSocket webSocket, byte[] packet) {
                return super.onMessage(webSocket, packet);
            }

            @Override
            public boolean onError(WebSocket webSocket, Throwable error) {
                return super.onError(webSocket, error);
            }
        };
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();
        font.draw(batch, socket.getState().name(), 10f, Gdx.graphics.getHeight() / 2f);
        font.draw(batch, Share.testShare() + Const.BREAK, 10f, Gdx.graphics.getHeight() / 1.5f);
        batch.end();
    }

    @Override
    public void dispose() {
        WebSockets.closeGracefully(socket); // Null-safe closing method that catches and logs any exceptions.
        batch.dispose();
        font.dispose();
    }
}
