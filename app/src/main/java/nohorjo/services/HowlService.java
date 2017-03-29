package nohorjo.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import nohorjo.application.App;
import nohorjo.output.FileOut;
import nohorjo.remote.howlserver.HowlComms;

import static nohorjo.settings.SettingsManager.PHONE_NUMBER;
import static nohorjo.settings.SettingsManager.isSet;

public class HowlService extends Service {
    private static boolean running;

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        Thread.setDefaultUncaughtExceptionHandler(App.getExceptionHandler());
        new Thread() {
            @Override
            public void run() {
                startService();
            }

            ;
        }.start();
    }

    private void startService() {
        if (!running || !HowlComms.isAlive()) {
            FileOut.println("Starting service...");
            App.setContext(this);
            // wait to load phone number
            new Thread() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            if (isSet(PHONE_NUMBER)) {
                                initHowler();
                                break;
                            } else {
                                FileOut.println("Phone number not set");
                            }
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            FileOut.printStackTrace(e);
                        }
                    }
                }

                ;
            }.start();
        } else {
            FileOut.println("Service already running");
        }
    }

    protected synchronized void initHowler() {
        try {
            HowlComms.init();
            running = true;
            FileOut.println("Service started!");
            while (true)
                continue;
        } catch (Exception e) {
            FileOut.printStackTrace(e);
        }
    }

    @Override
    public void onStart(Intent intent, int startId) {
        onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        onCreate();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        try {
            super.onDestroy();
            FileOut.println("Destroying service");
            running = false;
            HowlBR.schedule(this);
        } catch (Throwable e) {
            FileOut.printStackTrace(e);
        }
    }
}
