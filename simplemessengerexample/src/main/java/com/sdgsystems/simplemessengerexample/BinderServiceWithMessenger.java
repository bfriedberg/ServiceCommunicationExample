package com.sdgsystems.simplemessengerexample;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import java.util.ArrayList;
import java.util.Random;

public class BinderServiceWithMessenger extends Service {

    public static final int RANDOM_NUMBER_REQUEST_MESSAGE = 0;
    public static final int RANDOM_NUMBER_RESPONSE_MESSAGE = 1;

    public static final String RANDOM_NUMBER_FIELD = "RandomNumber";
    public static final String SLEEP_SECONDS_FIELD = "SleepSeconds";

    public BinderServiceWithMessenger() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {

                case RANDOM_NUMBER_REQUEST_MESSAGE:
                    try {
                        Thread.sleep(msg.getData().getInt(SLEEP_SECONDS_FIELD, 0) * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    int randomNumber = new Random().nextInt(9999);

                    Message randomNumberMessage = Message.obtain(null, RANDOM_NUMBER_RESPONSE_MESSAGE);
                    Bundle dataBundle = new Bundle();
                    dataBundle.putInt(RANDOM_NUMBER_FIELD, randomNumber);
                    randomNumberMessage.setData(dataBundle);

                    try {
                        msg.replyTo.send(randomNumberMessage);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    final Messenger mMessenger = new Messenger(new IncomingHandler());
}