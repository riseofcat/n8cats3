package com.riseofcat.android;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.github.czyzby.websocket.CommonWebSockets;
import com.riseofcat.MainClass;

/** Launches the Android application. */
public class AndroidLauncher extends AndroidApplication {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initiating web sockets module:
        CommonWebSockets.initiate();
        AndroidApplicationConfiguration configuration = new AndroidApplicationConfiguration();
        initialize(new MainClass(), configuration);
    }
}