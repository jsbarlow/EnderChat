package com.worksinprogress.joesbarlow.enderchat;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

public class LoginFragment extends Fragment {

    LoginListener mCallback;

    private boolean conn;
    private boolean direct;
    private boolean remember;
    private FTPConnect FTPTest;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

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

    private CheckBox mcheck;

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = this.getActivity().getSharedPreferences("pref", Context.MODE_PRIVATE);
        editor = preferences.edit();
        remember = preferences.getBoolean("remember", false);
        host = preferences.getString("host", "");
        user = preferences.getString("user", "");
        pass = preferences.getString("pass", "");
        port = preferences.getInt("port", 21);
        world = preferences.getString("world", "");
        comp = preferences.getString("comp", "");
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
        mcheck = (CheckBox) _loginView.findViewById(R.id.rememberMe);
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
        mcheck.setTypeface(typeFace);

        if (remember) {
            mcheck.setChecked(true);
            mhost.setText(host);
            muser.setText(user);
            mpass.setText(pass);
            mworld.setText(world);
            mcomp.setText(comp);

            if (port != 21)
                mport.setText(port);
        }

        mbutton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch(motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mbutton.setBackgroundColor(Color.parseColor("#ff32922C"));
                        return true;
                    case MotionEvent.ACTION_UP:
                        mbutton.setBackgroundColor(Color.parseColor("#ff5bff55"));
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

                        if (mcheck.isChecked()){
                            editor.putBoolean("remember", true);
                            editor.putString("host", host);
                            editor.putString("user", user);
                            editor.putString("pass", pass);
                            editor.putInt("port", port);
                            editor.putString("world", world);
                            editor.putString("comp", comp);
                            editor.commit();
                        }
                        else{
                            editor.putBoolean("remember", false);
                            editor.putString("host", "");
                            editor.putString("user", "");
                            editor.putString("pass", "");
                            editor.putInt("port", 21);
                            editor.putString("world", "");
                            editor.putString("comp", "");
                            editor.commit();
                        }

                        filepath = getResources().getString(R.string.filepath, world, comp);

                        new Thread(new Runnable() {
                            public void run() {
                                conn = FTPTest.ftpConnect(host, user, pass, port);
                                direct = FTPTest.ftpChangeDirectory(filepath);
                                currdirect = FTPTest.ftpGetCurrentWorkingDirectory();
                                mconnect.post(new Runnable() {
                                    public void run() {
                                        if (conn) {
                                            mconnect.setText(R.string.connection_successful);
                                            mconnect.setChecked(true);
                                        } else {
                                            mconnect.setText(R.string.connection_failed);
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