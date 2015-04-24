package com.sdgsystems.simpleserviceinteraction;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import java.util.Random;

public class BinderService extends Service {

    private static SynchronousServiceBinder mBinderInstance;

    public BinderService() {
    }

    @Override
    public IBinder onBind(Intent intent) {

        mBinderInstance = new SynchronousServiceBinder();

        return mBinderInstance;
    }

    public class SynchronousServiceBinder extends Binder {
        public BinderService getService() {
            return BinderService.this;
        }
        public int getRandomNumber(int sleepSeconds) {
            return getRandomNumberInternal(sleepSeconds);
        }
    }

    private int getRandomNumberInternal(int sleepSeconds) {

        try {
            Thread.sleep(sleepSeconds * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return new Random().nextInt(9999);
    }
}
