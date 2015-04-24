package com.sdgsystems.servicecommunicationexample;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.Random;


public class MainActivity extends Activity implements View.OnClickListener{

    private LinearLayout logMessages;

    private BinderServiceWithMessenger.AsynchronousServiceBinder mAsyncServiceBinder;

    private Button asyncButton, clearButton, heartbeatStart, heartbeatStop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        logMessages = (LinearLayout) findViewById(R.id.results);

        asyncButton = (Button) findViewById(R.id.btnAsync);
        clearButton = (Button) findViewById(R.id.btnClear);
        heartbeatStart = (Button) findViewById(R.id.btnHeartbeatStart);
        heartbeatStop = (Button) findViewById(R.id.btnHeartbeatStop);

        asyncButton.setOnClickListener(this);
        clearButton.setOnClickListener(this);
        heartbeatStart.setOnClickListener(this);
        heartbeatStop.setOnClickListener(this);

        Intent asyncServiceintent = new Intent(this, BinderServiceWithMessenger.class);
        this.bindService(asyncServiceintent, mAsynchronousConnection, Context.BIND_AUTO_CREATE);
    }


    @Override
    public void onClick(View view) {
        if( view == asyncButton) {
            
            logMessages.addView(getTextView("Calling asynchronous service"));
            mAsyncServiceBinder.requestRandomNumberAsync(5);
            logMessages.addView(getTextView("Returned from asynchronous service call, waiting for the number message to arrive"));

        } else if(view == clearButton) {

            logMessages.removeAllViews();

        } else if (view == heartbeatStart) {

            logMessages.addView(getTextView("Starting heartbeat thread"));
            mAsyncServiceBinder.startHeartBeatThread();

        } else if (view == heartbeatStop) {

            logMessages.addView(getTextView("Stopping heartbeat thread"));
            mAsyncServiceBinder.stopHeartBeatThread();

        }
    }


    //Define the messenger and how to handle incoming messages
    private Messenger mWatsonMessenger = new Messenger(new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //HANDLE THE MESSAGE based on msg.what
            if(msg.what == BinderServiceWithMessenger.RANDOM_NUMBER_MESSAGE) {

                logMessages.addView(getTextView("Received asynchronous random number message"));
                int randomNumber = msg.getData().getInt(BinderServiceWithMessenger.RANDOM_NUMBER_FIELD, 0);
                logMessages.addView(getTextView("Random number is: " + randomNumber));

            } else if (msg.what == BinderServiceWithMessenger.HEARTBEAT_MESSAGE) {

                logMessages.addView(getTextView("heartbeat!"));

            }
        }
    });

    private ServiceConnection mAsynchronousConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            logMessages.addView(getTextView("Bound to Asynchronous Service, registering messenger"));
            mAsyncServiceBinder = (BinderServiceWithMessenger.AsynchronousServiceBinder) service;

            //register a messenger
            mAsyncServiceBinder.registerMessenger(mWatsonMessenger);

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

            logMessages.addView(getTextView("Unbound from Asynchronous Service, unregistering messenger"));
            mAsyncServiceBinder = null;

            //unregister the messenger
            mAsyncServiceBinder.unregisterMessenger(mWatsonMessenger);

        }
    };

    private View getTextView(String s) {
        TextView tv = new TextView(this);
        tv.setText(s);
        return tv;
    }
}
