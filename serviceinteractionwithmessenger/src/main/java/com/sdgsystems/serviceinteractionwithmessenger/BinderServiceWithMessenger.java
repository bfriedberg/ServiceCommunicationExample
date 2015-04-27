package com.sdgsystems.serviceinteractionwithmessenger;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import java.util.ArrayList;
import java.util.Random;

public class BinderServiceWithMessenger extends Service {

    public static final int RANDOM_NUMBER_MESSAGE = 0;
    public static final int HEARTBEAT_MESSAGE = 1;

    public static final String RANDOM_NUMBER_FIELD = "RandomNumber";

    private static AsynchronousServiceBinder mBinderInstance;
    private ArrayList<Messenger> clientMessengers;


    private HeartBeatThread mHeartBeatThread;

    public BinderServiceWithMessenger() {
    }

    @Override
    public IBinder onBind(Intent intent) {

        mBinderInstance = new AsynchronousServiceBinder();

        return mBinderInstance;
    }

    public class AsynchronousServiceBinder extends Binder {


        public BinderServiceWithMessenger getService() {
            return BinderServiceWithMessenger.this;
        }

        public void registerMessenger(Messenger messenger) {

            if(clientMessengers == null) {
                clientMessengers = new ArrayList<Messenger>();
            }

            clientMessengers.add(messenger);
        }

        public void unregisterMessenger(Messenger messenger) {
            if(clientMessengers.contains(messenger)) {
                clientMessengers.remove(messenger);
            }
        }

        public void requestRandomNumberAsync(int sleepSeconds) {
            GenerateRandomNumberTask genRandomNumber = new GenerateRandomNumberTask();
            genRandomNumber.execute(sleepSeconds);
        }

        public void startHeartBeatThread() {

            mHeartBeatThread = new HeartBeatThread();
            mHeartBeatThread.start();

        }

        public void stopHeartBeatThread() {
            if(mHeartBeatThreadRunning) {
                mHeartBeatThreadRunning = false;
            }
        }
    }

    private class GenerateRandomNumberTask extends AsyncTask <Integer, Void, Integer> {
        @Override
        protected Integer doInBackground(Integer... parms) {
            try {
                Thread.sleep(parms[0] * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return new Random().nextInt(9999);
        }

        protected void onPostExecute(Integer randomNumber) {
            Message randomNumberMessage = Message.obtain(null, RANDOM_NUMBER_MESSAGE);
            Bundle dataBundle = new Bundle();
            dataBundle.putInt(RANDOM_NUMBER_FIELD, randomNumber);
            randomNumberMessage.setData(dataBundle);

            sendMessageToClients(randomNumberMessage);
        }
    }

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
                    Thread.sleep(2000);
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
