package com.worksinprogress.joesbarlow.enderchat;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

import java.io.File;


public class MainActivity extends AppCompatActivity implements LoginFragment.LoginListener{

    private LoginFragment loginFragment;
    private ChatFragment chatFragment;
    private Boolean enableButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        enableButton = false;
        File mydir = new File(getResources().getString(R.string.store));
        if (!mydir.exists())
            mydir.mkdirs();

        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (findViewById(R.id.fragment_container) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }

            loginFragment = new LoginFragment();
            //boolean remember = settings.getBoolean("rememberMe", false);

            // In case this activity was started with special instructions from an
            // Intent, pass the Intent's extras to the fragment as arguments
            loginFragment.setArguments(getIntent().getExtras());

            // Add the fragment to the 'fragment_container' FrameLayout
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, loginFragment).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.chat_button:
                openChat();
                return true;
            case R.id.login_button:
                openLogin();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void openChat(){

        if (!enableButton) {
            DialogFragment newFragment = WarningFragment.newInstance();
            newFragment.show(getFragmentManager(), "Warning");
        }
        else {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            transaction.replace(R.id.fragment_container, chatFragment);
            transaction.addToBackStack(null);

            transaction.commit();
        }
    }

    public void openLogin(){

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        transaction.replace(R.id.fragment_container, loginFragment);
        transaction.addToBackStack(null);

        transaction.commit();
    }

    public void loginInfo(String host, String user, String pass, int port, String filepath){
        chatFragment = new ChatFragment();
        Bundle args = new Bundle();
        chatFragment.setArguments(args);

        chatFragment.updateLogin(host, user, pass, port, filepath);
        enableButton = true;
    }

    public static class WarningFragment extends DialogFragment {

        static WarningFragment newInstance() {
            return new WarningFragment();
        }

        @Override
        public Dialog onCreateDialog(Bundle bundle)
        {
            AlertDialog.Builder builder =
                    new AlertDialog.Builder(getActivity());
            builder.setCancelable(false);

            builder.setMessage(getResources().getString(R.string.chatwarning));
            return builder.create();
        }
    }
}
