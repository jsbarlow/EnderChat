package com.worksinprogress.joesbarlow.enderchat;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

public class Polling extends Service {

    LocalBroadcastManager broadcaster;
    private static final String TAG = "Polling Service";

    public Polling() {
    }

    // Binder given to clients
    private final IBinder mBinder = new PollBinder();

    //private Handler mHandler = null;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Bundle extras = intent.getExtras();
        update(extras);

        broadcaster = LocalBroadcastManager.getInstance(this);

        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_NOT_STICKY;
    }

    public class PollBinder extends Binder {
        Polling getService() {
            // Return this instance of LocalService so clients can call public methods
            return Polling.this;
        }
    }

    private void update(Bundle extras) {
        final FTPConnect FTPconn = new FTPConnect();
        final String host = extras.getString("host");
        final String user = extras.getString("user");
        final String pass = extras.getString("pass");
        final Integer port = extras.getInt("port");
        final String filepath = extras.getString("filepath");
        final String infilePath = getResources().getString(R.string.incoming, filepath);
        new Thread(new Runnable() {
            public void run() {
                if (FTPconn.ftpConnect(host, user, pass, port)) {
                    if (FTPconn.ftpChangeDirectory(infilePath)) {
                        long size = FTPconn.ftpSize(infilePath);
                        if (size > 0) {
                            if (FTPconn.ftpDownload(infilePath, getString(R.string.temp))) {
                                FTPconn.ftpRemoveFile(infilePath);
                                File tempFile = new File(getString(R.string.temp));
                                try {
                                    FileOutputStream outputStream = new FileOutputStream(getString(R.string.input), true);
                                    String tempLog = getFile(tempFile);
                                    outputStream.write(tempLog.getBytes());
                                    outputStream.close();
                                    tempFile.delete();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                    FTPconn.ftpDisconnect();
                }
            }
        }).start();
    }

    private String getFile(File file) {
        StringBuilder text = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            text.append('\n');
            br.close();
        } catch (IOException e) {
            Log.d(TAG, "Error occurred while reading enterText");
        }
        return text.toString();
    }
}
