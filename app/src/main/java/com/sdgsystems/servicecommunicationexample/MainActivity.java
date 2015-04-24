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

    private BinderService.SynchronousServiceBinder mSyncServiceBinder;
    private BinderServiceWithMessenger.AsynchronousServiceBinder mAsyncServiceBinder;

    private Button asyncButton, syncButton, syncTaskButton, clearButton, heartbeatStart, heartbeatStop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        logMessages = (LinearLayout) findViewById(R.id.results);

        syncButton = (Button) findViewById(R.id.btnSync);
        syncTaskButton = (Button) findViewById(R.id.btnSyncTask);
        asyncButton = (Button) findViewById(R.id.btnAsync);
        clearButton = (Button) findViewById(R.id.btnClear);
        heartbeatStart = (Button) findViewById(R.id.btnHeartbeatStart);
        heartbeatStop = (Button) findViewById(R.id.btnHeartbeatStop);

        syncButton.setOnClickListener(this);
        syncTaskButton.setOnClickListener(this);
        asyncButton.setOnClickListener(this);
        clearButton.setOnClickListener(this);
        heartbeatStart.setOnClickListener(this);
        heartbeatStop.setOnClickListener(this);


        Intent syncServiceIntent = new Intent(this, BinderService.class);
        Intent asyncServiceintent = new Intent(this, BinderServiceWithMessenger.class);
        this.bindService(syncServiceIntent, mSynchronousConnection, Context.BIND_AUTO_CREATE);
        this.bindService(asyncServiceintent, mAsynchronousConnection, Context.BIND_AUTO_CREATE);
    }


    @Override
    public void onClick(View view) {
        if(view == syncButton) {

            logMessages.addView(getTextView("Calling synchronous service"));
            int random = mSyncServiceBinder.getRandomNumber(5);
            logMessages.addView(getTextView("Got synchronous random number: " + random));

        } else if(view == syncTaskButton) {

            logMessages.addView(getTextView("Calling synchronous service in an AsyncTask"));
            CallSyncServiceTask task = new CallSyncServiceTask();
            task.execute(5);

        } else if( view == asyncButton) {

            logMessages.addView(getTextView("Calling asynchronous service"));
            mAsyncServiceBinder.requestRandomNumberAsync(5);
            logMessages.addView(getTextView("Returned from asynchronous service call, waiting for the number message to arrive"));

        } else if(view == clearButton) {
            logMessages.removeAllViews();
        } else if (view == heartbeatStart) {
            logMessages.addView(getTextView("Starting heartbeat thread"));

            Log.d("service_comms", "starting heartbeat");
            mAsyncServiceBinder.startHeartBeatThread();
        } else if (view == heartbeatStop) {
            logMessages.addView(getTextView("Stopping heartbeat thread"));
            mAsyncServiceBinder.stopHeartBeatThread();
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

    private class CallSyncServiceTask extends AsyncTask<Integer, Void, Integer> {

        @Override
        protected Integer doInBackground(Integer... parms) {
            return mSyncServiceBinder.getRandomNumber(parms[0]);
        }

        protected void onPostExecute(Integer randomNumber) {
            logMessages.addView(getTextView("Received service response at end of asynctask: " + randomNumber));
        }
    }

    private View getTextView(String s) {
        TextView tv = new TextView(this);
        tv.setText(s);
        return tv;
    }
}
