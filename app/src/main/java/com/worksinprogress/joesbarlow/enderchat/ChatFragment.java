package com.worksinprogress.joesbarlow.enderchat;

import android.content.Context;
import android.graphics.Typeface;
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

public class ChatFragment extends Fragment {

    private static final String TAG = "ChatFragment";
    private Button send;
    private EditText input;
    private TextView output;
    private ScrollView scroll;
    public FTPConnect FTPconn;

    private String infilePath;
    private String outfilePath;
    private String filepath;
    private String chatlog;
    private String outstring;
    private String host;
    private String user;
    private String pass;
    private int port;

    public ChatFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View _view = inflater.inflate(R.layout.fragment_chat, container, false);
        send = (Button) _view.findViewById(R.id.SendButton);
        input = (EditText) _view.findViewById(R.id.InputText);
        output = (TextView) _view.findViewById(R.id.OutputText);
        scroll = (ScrollView) _view.findViewById(R.id.scrollView);
        FTPconn = new FTPConnect();

        Typeface typeFace= Typeface.createFromAsset(getActivity().getAssets(), "fonts/Minecraftia.ttf");
        send.setTypeface(typeFace);
        input.setTypeface(typeFace);
        output.setTypeface(typeFace);

        File chat = new File(getResources().getString(R.string.store) + "chatlog.txt");
        if (chat.exists()) {
            chatlog = getFile("chatlog.txt").toString();
            outPrint();
        }
        else {
            output.setText("No previous chatlog.");
            chatlog = "";
        }

        inFileManager();

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userText = input.getText().toString();
                if (userText != "")
                {
                    chatlog += "User: " + userText + "\n\n";
                    outstring = "Host: "+ userText + '\n';
                    outFileManager();
                    outPrint();
                    input.setText("");
                }
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
            }
        });

        return _view;
    }

    private StringBuilder getFile(String file){
        File fileName = new File(getResources().getString(R.string.store) + file);
        StringBuilder text = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            text.append('\n');
            br.close();
        }
        catch (IOException e) {
            Log.d(TAG, "Error occurred while reading input");
        }

        return text;
    }

    private boolean outPrint(){

        output.setText(chatlog);

        scroll.post(new Runnable() {
            @Override
            public void run() {
                scroll.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
        return true;
    }

    private void inFileManager(){
        infilePath = getResources().getString(R.string.incoming, filepath);

        new Thread(new Runnable() {
            public void run() {
                String inName = getResources().getString(R.string.store) + "input";
                File inFile = new File(getResources().getString(R.string.store) + "input");
                Boolean inCond;
                Boolean downCond;
                while (true){
                    inCond = false;
                    downCond = false;
                    Boolean conn = FTPconn.ftpConnect(host, user, pass, port);
                    if (conn)
                        downCond = FTPconn.ftpChangeDirectory(infilePath);
                    try {
                        Thread.sleep(3000);
                        // Do some stuff
                    } catch (Exception e) {
                        e.getLocalizedMessage();
                    }
                    if (downCond) {
                        long size = FTPconn.ftpSize(infilePath);
                        if (size > 0) {
                            inCond = FTPconn.ftpDownload(infilePath, inName);
                        }
                    }
                    if (inCond) {
                        String temptext = getFile("input").toString();
                        chatlog += temptext;
                        Boolean delCond = output.post(new Runnable() {
                            public void run() {
                                outPrint();
                            }
                        });

                        if (delCond) {
                            inFile.delete();
                            FTPconn.ftpRemoveFile(infilePath);
                            FTPconn.ftpDisconnect();
                        }
                    }
                    try {
                        Thread.sleep(3000);
                        // Do some stuff
                    } catch (Exception e) {
                        e.getLocalizedMessage();
                    }
                    FTPconn.ftpDisconnect();
                }
            }
        }).start();
    }

    private void outFileManager(){

        new Thread(new Runnable() {
            public void run() {
                outfilePath = getResources().getString(R.string.outgoing, filepath);
                FTPconn.ftpConnect(host, user, pass, port);
                FTPconn.ftpAppend(outfilePath, outstring);
                FTPconn.ftpDisconnect();
            }
        }).start();
    }

    public void updateLogin(String temphost, String tempuser, String temppass, int tempport, String temppath){
        host = temphost;
        user = tempuser;
        pass = temppass;
        port = tempport;
        filepath = temppath;
    }

    public void onPause(){
        super.onPause();

        try {
            FileOutputStream outputStream = new FileOutputStream(getResources().getString(R.string.store) + "chatlog.txt");
            String tempLog = chatlog;
            tempLog += "------------------------------------------\n\n";
            outputStream.write(tempLog.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}