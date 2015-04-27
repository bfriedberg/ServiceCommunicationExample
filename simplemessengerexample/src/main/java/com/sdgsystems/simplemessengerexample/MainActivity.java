package com.sdgsystems.simplemessengerexample;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;


public class MainActivity extends Activity implements View.OnClickListener{

    private LinearLayout logMessages;
    private Messenger mServiceMessenger = null;

    private Button asyncButton, clearButton;
    private boolean mBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        logMessages = (LinearLayout) findViewById(R.id.results);

        asyncButton = (Button) findViewById(R.id.btnAsync);
        clearButton = (Button) findViewById(R.id.btnClear);

        asyncButton.setOnClickListener(this);
        clearButton.setOnClickListener(this);

        Intent asyncServiceintent = new Intent(this, BinderServiceWithMessenger.class);
        this.bindService(asyncServiceintent, mAsynchronousConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onClick(View view) {
        if( view == asyncButton) {

            logMessages.addView(getTextView("Calling asynchronous service"));

            Message msg = Message.obtain(null, BinderServiceWithMessenger.RANDOM_NUMBER_REQUEST_MESSAGE);
            Bundle bundle = new Bundle();
            bundle.putInt(BinderServiceWithMessenger.SLEEP_SECONDS_FIELD, 5);
            msg.replyTo = mWatsonMessenger;
            msg.setData(bundle);

            try {
                mServiceMessenger.send(msg);
                logMessages.addView(getTextView("Returned from asynchronous service call, waiting for the number message to arrive"));
            } catch (RemoteException e) {
                logMessages.addView(getTextView("error requesting random number"));
                e.printStackTrace();
            }

        } else if(view == clearButton) {

            logMessages.removeAllViews();

        }
    }

    //Define the messenger and how to handle incoming messages
    private Messenger mWatsonMessenger = new Messenger(new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //HANDLE THE MESSAGE based on msg.what
            if(msg.what == BinderServiceWithMessenger.RANDOM_NUMBER_RESPONSE_MESSAGE) {

                logMessages.addView(getTextView("Received asynchronous random number message"));
                int randomNumber = msg.getData().getInt(BinderServiceWithMessenger.RANDOM_NUMBER_FIELD, 0);
                logMessages.addView(getTextView("Random number is: " + randomNumber));

            }
        }
    });

    private ServiceConnection mAsynchronousConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            mBound = true;
            logMessages.addView(getTextView("Bound to Asynchronous Service, registering messenger"));
            mServiceMessenger = new Messenger (service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

            logMessages.addView(getTextView("Unbound from Asynchronous Service, unregistering messenger"));
            mServiceMessenger = null;
            mBound = false;
        }
    };

    @Override
    public void onPause() {
        super.onPause();

        if(mBound) {
            unbindService(mAsynchronousConnection);
        }
    }


    private View getTextView(String s) {
        TextView tv = new TextView(this);
        tv.setText(s);
        return tv;
    }
}
