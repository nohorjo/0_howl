package nohorjo.remote.howlserver;

import android.util.Base64;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import nohorjo.application.App;
import nohorjo.crypto.AESEncryptor;
import nohorjo.crypto.EncryptionException;
import nohorjo.crypto.TimeBasedEncryptor;
import nohorjo.delegation.Action;
import nohorjo.gps.Tracker;
import nohorjo.output.FileOut;
import nohorjo.remote.wordpress.IPHandler;
import nohorjo.settings.SettingsException;
import nohorjo.settings.SettingsManager;
import nohorjo.socket.SocketClient;

public abstract class HowlComms {

    private static final byte EOT = '\4';

    private static SocketClient client;
    private static TimeBasedEncryptor tbe = new TimeBasedEncryptor();
    protected static boolean lock = false;

    public static void init() throws Exception {
        FileOut.println("Initializing comms");
        tbe.setAes(new AESEncryptor() {
            @Override
            protected String encodeB64(byte[] input) {
                return new String(Base64.encode(input, 0));
            }

            @Override
            protected byte[] decodeB64(byte[] input) {
                return Base64.decode(input, 0);
            }
        });
        for (int i = 0; i < Integer
                .parseInt(SettingsManager.getSetting(SettingsManager.RETRY_LIMIT, App.RETRY_LIMIT)); i++) {
            try {
                FileOut.println("Connecting to remote server: " + IPHandler.getIPAddress() + ":" + App.port());
                client = new SocketClient(IPHandler.getIPAddress(), App.port());
                client.setActions(new Action() {
                    String buffer = "";

                    @Override
                    public Object run(Object... args) {
                        byte b = (byte) args[0];
                        if (b == EOT) {
                            processMessage(buffer);
                            buffer = "";
                        } else {
                            buffer += (char) b;
                        }
                        return null;
                    }
                }, new Action() {

                    @Override
                    public Object run(Object... args) {
                        try {
                            init();
                        } catch (Exception e) {
                            FileOut.println("Failed to reinitialise comms\n\t" + e.getMessage());
                        }
                        return null;
                    }
                });
                client.connect();
                FileOut.println("Connection established");
                return;
            } catch (IOException e) {
                IPHandler.reloadIPAddress();
            }
        }
        FileOut.println("Failed to initialise comms. Scheduling to retry");
        new Timer().schedule(new TimerTask() {

            @Override
            public void run() {
                try {
                    init();
                } catch (Exception e) {
                    FileOut.printStackTrace(e);
                }
            }
        }, Integer.parseInt(SettingsManager.getSetting(SettingsManager.REMOTE_RETRY_DELAY, App.REMOTE_RETRY_DELAY))
                * 1000);
    }

    protected static void processMessage(String message) {
        try {
            String command = tbe.decrypt(App.key(), message);
            FileOut.println("Received command: " + command);
            String key = command.substring(0, 1);
            try {
                command = command.substring(1);
            } catch (Exception e) {
            }
            switch (key) {
                case "h":
                    lock = false;
                    break;
                default:
                    Tracker.checkLocation(true);
                    break;
            }
        } catch (EncryptionException | SettingsException e) {
            try {
                Thread.sleep(1000);
                client.send(tbe.encrypt(App.key(), "RETRY:" + message).getBytes());
                client.send((byte) '\4');
            } catch (IOException | InterruptedException | EncryptionException | SettingsException e1) {
                FileOut.printStackTrace(e1);
            }
        }
    }

    public static void postLocation(double[] latlong) throws IOException {
        String location = String.format("LOCATION:%s %s", latlong[0], latlong[1]);
        try {
            client.send(tbe.encrypt(App.key(), location).getBytes());
            client.send((byte) '\4');
        } catch (EncryptionException | SettingsException e) {
            FileOut.printStackTrace(e);
        }
    }

    public static boolean isAlive() {
        lock = true;
        while (lock) {
            try {
                client.send(tbe.encrypt(App.key(), "H:B").getBytes());
                client.send((byte) '\4');
                Thread.sleep(1000);
            } catch (IOException | EncryptionException | SettingsException | InterruptedException e) {
                FileOut.println("Comms dropped");
                return false;
            }
        }
        return true;
    }

}
