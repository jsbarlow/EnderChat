package com.worksinprogress.joesbarlow.enderchat;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Objects;

public class ChatFragment extends Fragment {

    // Properties
    private static final String TAG = "ChatFragment";
    private EditText enterText;
    private TextView displayText;
    private ScrollView scroll;
    private FTPConnect FTPconn;
    private String filepath;
    private String chatlog;
    private String outstring;
    private String host;
    private String user;
    private String pass;
    private int port;
    private long interval;
    Handler handler;

    public ChatFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View _view = inflater.inflate(R.layout.fragment_chat, container, false);
        Button send = (Button) _view.findViewById(R.id.SendButton);
        enterText = (EditText) _view.findViewById(R.id.InputText);
        displayText = (TextView) _view.findViewById(R.id.OutputText);
        scroll = (ScrollView) _view.findViewById(R.id.scrollView);
        FTPconn = new FTPConnect();

        // Starts alarm to update input.
        interval = 90000;
        setUpAlarm(getActivity().getApplication());

        // Sets the typefaces.
        Typeface typeFace = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Minecraftia.ttf");
        send.setTypeface(typeFace);
        enterText.setTypeface(typeFace);
        displayText.setTypeface(typeFace);

        // Initializes the chatlog string from the chatlog file and the possible input file.\
        File chat = new File(getResources().getString(R.string.chatlog));
        if (chat.exists()) {
            chatlog = getFile(chat);
            chatLogUpdate();
        } else {
            displayText.setText(R.string.no_chatlog);
            chatlog = "";
        }

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userText = enterText.getText().toString();
                if (!Objects.equals(userText, "")) {
                    chatlog += "User: " + userText + "\n\n";
                    outstring = "Host: " + userText + '\n';
                    sendMessage();
                    updateChatDisplay();
                    enterText.setText("");
                }
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(enterText.getWindowToken(), 0);
            }
        });

        handler = new Handler();
        handler.post(new Runnable(){
            @Override
            public void run() {
                chatLogUpdate();
                handler.postDelayed(this, 500);
            }
        });

        return _view;
    }

    // Parses through files to create a string from the text in them.
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

    // Updates the displayText when a new message is received or sent.
    private void updateChatDisplay() {

        displayText.setText(chatlog);

        scroll.post(new Runnable() {
            @Override
            public void run() {
                scroll.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    // Checks for new messages and updates the input file accordingly.
    private void setUpAlarm(Application context) {
        Intent intent = new Intent(context, Polling.class);
        intent.putExtra("host", host);
        intent.putExtra("user", user);
        intent.putExtra("pass", pass);
        intent.putExtra("port", port);
        intent.putExtra("filepath", filepath);
        PendingIntent pending_intent = PendingIntent.getService(context, 0, intent, 0);

        AlarmManager alarm_mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm_mgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime(), interval, pending_intent);
    }

    // Sends messages to Minecraft server
    private void sendMessage() {

        new Thread(new Runnable() {
            public void run() {
                FTPconn.ftpConnect(host, user, pass, port);
                FTPconn.ftpAppend(getResources().getString(R.string.outgoing, filepath), outstring);
                FTPconn.ftpDisconnect();
            }
        }).start();
    }

    // Updates the chatlog string with text from the input file.
    public void chatLogUpdate() {
        File input = new File(getResources().getString(R.string.input));
        if (input.exists()) {
            chatlog += getFile(input);
            updateChatDisplay();
            input.delete();
        }
    }

    // Used to update login information, if necessary.
    public void updateLogin(String temphost, String tempuser, String temppass, int tempport, String temppath) {
        host = temphost;
        user = tempuser;
        pass = temppass;
        port = tempport;
        filepath = temppath;
    }

    // If the view is paused, the chatlog so far is saved and a line is placed after it.
    @Override
    public void onPause() {
        super.onPause();
        interval = AlarmManager.INTERVAL_FIFTEEN_MINUTES;
        try {
            FileOutputStream outputStream = new FileOutputStream(getResources().getString(R.string.chatlog));
            outputStream.write(chatlog.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Checks for new messages and then displays the chatlog so far.
    @Override
    public void onResume() {
        super.onResume();
        interval = 90000;
        chatLogUpdate();
        updateChatDisplay();
    }

    @Override
    public void onDestroy() {
        try {
            FileOutputStream outputStream = new FileOutputStream(getResources().getString(R.string.chatlog));
            outputStream.write(chatlog.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}