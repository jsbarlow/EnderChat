package com.worksinprogress.joesbarlow.enderchat;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

public class LoginFragment extends Fragment {

    LoginListener mCallback;

    private boolean conn;
    private boolean direct;
    private FTPConnect FTPTest;

    private String host;
    private String user;
    private String pass;
    private int port;
    private String world;
    private String comp;
    private String filepath;
    private String currdirect;

    private Button mbutton;
    private RadioButton mconnect;
    private RadioButton mdirectory;

    private TextView mcurrent;

    private EditText mhost;
    private EditText muser;
    private EditText mpass;
    private EditText mport;
    private EditText mworld;
    private EditText mcomp;

    public LoginFragment() {
    }

    // Container Activity must implement this interface
    public interface LoginListener {
        void loginInfo(String host, String user, String pass, int port, String filepath);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View _loginView = inflater.inflate(R.layout.fragment_login,container,false);

        mbutton = (Button) _loginView.findViewById(R.id.connectButton);
        mhost = (EditText) _loginView.findViewById(R.id.hostText);
        muser = (EditText) _loginView.findViewById(R.id.usernameText);
        mpass = (EditText) _loginView.findViewById(R.id.passwordText);
        mport = (EditText) _loginView.findViewById(R.id.portText);
        mworld = (EditText) _loginView.findViewById(R.id.worldText);
        mcomp = (EditText) _loginView.findViewById(R.id.computerText);
        mconnect = (RadioButton) _loginView.findViewById(R.id.connectionStatus);
        mdirectory = (RadioButton) _loginView.findViewById(R.id.directoryStatus);
        mcurrent = (TextView) _loginView.findViewById(R.id.directory);
        conn = false;
        FTPTest = new FTPConnect();

        Typeface typeFace= Typeface.createFromAsset(getActivity().getAssets(), "fonts/Minecraftia.ttf");
        mbutton.setTypeface(typeFace);
        mhost.setTypeface(typeFace);
        muser.setTypeface(typeFace);
        mpass.setTypeface(typeFace);
        mport.setTypeface(typeFace);
        mworld.setTypeface(typeFace);
        mcomp.setTypeface(typeFace);
        mconnect.setTypeface(typeFace);
        mdirectory.setTypeface(typeFace);
        mcurrent.setTypeface(typeFace);

        mbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                direct = true;
                host = mhost.getText().toString();
                user = muser.getText().toString();
                pass = mpass.getText().toString();
                String tempport = mport.getText().toString();
                if (!tempport.isEmpty())
                    port = Integer.parseInt(mport.getText().toString());
                else
                    port = 21;
                world = mworld.getText().toString();
                if (world.isEmpty())
                    world = "world";
                comp = mcomp.getText().toString();

                filepath = getResources().getString(R.string.filepath, world, comp);

                new Thread(new Runnable() {
                    public void run() {
                        conn = FTPTest.ftpConnect(host, user, pass, port);
                        direct = FTPTest.ftpChangeDirectory(filepath);
                        currdirect = FTPTest.ftpGetCurrentWorkingDirectory();
                        mconnect.post(new Runnable() {
                            public void run() {
                                if (conn) {
                                    mconnect.setText(R.string.validDirectory);
                                    mconnect.setChecked(true);
                                } else {
                                    mconnect.setText(R.string.invalidDirectory);
                                    mconnect.setChecked(false);
                                }
                            }
                        });
                        if (conn) {
                            mdirectory.post(new Runnable() {
                                public void run() {
                                    if (direct) {
                                        mdirectory.setText(R.string.validDirectory);
                                        mdirectory.setChecked(true);
                                    } else {
                                        mdirectory.setText(R.string.invalidDirectory);
                                        mdirectory.setChecked(false);
                                    }
                                }
                            });
                        } else {
                            mdirectory.post(new Runnable() {
                                public void run() {
                                    mdirectory.setText("");
                                    mdirectory.setChecked(false);
                                }
                            });
                        }
                        mcurrent.post(new Runnable() {
                            public void run() {
                                mcurrent.setText(currdirect);
                            }
                        });
                        if (conn && direct)
                            mCallback.loginInfo(host, user, pass, port, filepath);
                        FTPTest.ftpDisconnect();
                    }
                }).start();
            }
        });

        mbutton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch(motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mbutton.setBackgroundColor(Color.parseColor("#ff32922C"));
                        return true;
                    case MotionEvent.ACTION_UP:
                        mbutton.setBackgroundColor(Color.parseColor("#ff5bff55"));
                        return true;
                }
                return false;
            }
        });

        // Inflate the layout for this fragment
        return _loginView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (LoginListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }

    public void onPause(){
        super.onPause();

        mconnect.setChecked(false);
        mdirectory.setChecked(false);
    }
}