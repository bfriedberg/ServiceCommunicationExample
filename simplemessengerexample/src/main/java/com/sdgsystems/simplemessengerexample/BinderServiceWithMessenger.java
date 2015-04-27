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

    private ArrayList<Messenger> clientMessengers;

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

                    final Messenger responseMessenger = msg.replyTo;
                    final int sleepSeconds = msg.getData().getInt(SLEEP_SECONDS_FIELD, 0);

                    //Run in a new thread due to some binder specifics...
                    final Runnable randomNumberRunnable = new Runnable() {
                        public void run() {
                            try {
                                Thread.sleep(sleepSeconds * 1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            int randomNumber = new Random().nextInt(9999);

                            Message randomNumberMessage = Message.obtain(null, RANDOM_NUMBER_RESPONSE_MESSAGE);
                            Bundle dataBundle = new Bundle();
                            dataBundle.putInt(RANDOM_NUMBER_FIELD, randomNumber);
                            randomNumberMessage.setData(dataBundle);

                            try {
                                responseMessenger.send(randomNumberMessage);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                    };

                    new Thread(randomNumberRunnable).start();

                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    final Messenger mMessenger = new Messenger(new IncomingHandler());
}
