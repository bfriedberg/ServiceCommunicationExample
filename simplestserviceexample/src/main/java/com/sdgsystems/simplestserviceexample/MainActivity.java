package com.sdgsystems.simplestserviceexample;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;


public class MainActivity extends Activity implements View.OnClickListener{

    private LinearLayout logMessages;

    private BinderService.SynchronousServiceBinder mSyncServiceBinder;

    private Button syncButton, clearButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        logMessages = (LinearLayout) findViewById(R.id.results);

        syncButton = (Button) findViewById(R.id.btnSync);
        clearButton = (Button) findViewById(R.id.btnClear);

        syncButton.setOnClickListener(this);
        clearButton.setOnClickListener(this);


        Intent syncServiceIntent = new Intent(this, BinderService.class);

        this.bindService(syncServiceIntent, mSynchronousConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onPause() {
        super.onPause();
        unbindService(mSynchronousConnection);
    }

    @Override
    public void onClick(View view) {
        if(view == syncButton) {

            logMessages.addView(getTextView("Calling synchronous service"));
            int random = mSyncServiceBinder.getRandomNumber(5);
            logMessages.addView(getTextView("Got synchronous random number: " + random));

        } else if(view == clearButton) {
            logMessages.removeAllViews();
        }
    }

    private ServiceConnection mSynchronousConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            logMessages.addView(getTextView("Bound to Synchronous Service"));
            mSyncServiceBinder = (BinderService.SynchronousServiceBinder) service;

        }
        @Override
        public void onServiceDisconnected(ComponentName name) {

            logMessages.addView(getTextView("Unbound from Synchronous Service"));
            mSyncServiceBinder = null;
        }
    };

    private View getTextView(String s) {
        TextView tv = new TextView(this);
        tv.setText(s);
        return tv;
    }
}
