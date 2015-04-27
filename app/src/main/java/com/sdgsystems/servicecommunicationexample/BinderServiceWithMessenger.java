package com.sdgsystems.servicecommunicationexample;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
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
    public static final int HEARTBEAT_MESSAGE = 2;
    public static final int REGISTER_CLIENT_MESSENGER_MESSAGE = 3;
    public static final int UNREGISTER_CLIENT_MESSENGER_MESSAGE = 4;

    public static final String RANDOM_NUMBER_FIELD = "RandomNumber";
    public static final String SLEEP_SECONDS_FIELD = "SleepSeconds";

    private ArrayList<Messenger> clientMessengers;


    private HeartBeatThread mHeartBeatThread;

    public BinderServiceWithMessenger() {
    }

    @Override
    public IBinder onBind(Intent intent) {

        mHeartBeatThread = new HeartBeatThread();
        mHeartBeatThread.start();

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
                case REGISTER_CLIENT_MESSENGER_MESSAGE:
                    if(clientMessengers == null) {
                        clientMessengers = new ArrayList<Messenger>();
                    }

                    clientMessengers.add(msg.replyTo);
                    break;
                case UNREGISTER_CLIENT_MESSENGER_MESSAGE:
                    if(clientMessengers != null && clientMessengers.contains(msg.replyTo)) {
                        clientMessengers.remove(msg.replyTo);
                    }
                    break;

                default:
                    super.handleMessage(msg);
            }
        }
    }

    final Messenger mMessenger = new Messenger(new IncomingHandler());

    private void sendMessageToClients(Message randomNumberMessage) {
        for (int i=clientMessengers.size()-1; i>=0; i--) {
            Messenger clientMessenger = clientMessengers.get(i);

            if(clientMessenger != null) {
                try {
                    clientMessenger.send(randomNumberMessage);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            } else {
                clientMessengers.remove(i);
            }
        }
    }


    private boolean mHeartBeatThreadRunning = false;

    private class HeartBeatThread extends Thread {
        public void run() {

            mHeartBeatThreadRunning = true;
            while (mHeartBeatThreadRunning) {

                try {
                    Thread.sleep(5000);
                } catch (Exception e) {
                }

                new Thread(heartBeatRunnable).start();
            }
            mHeartBeatThreadRunning = false;
        }
    }

    final Runnable heartBeatRunnable = new Runnable() {
        public void run() {
            Message heartBeatMessage = Message.obtain(null, HEARTBEAT_MESSAGE);
            sendMessageToClients(heartBeatMessage);
        }
    };

}
