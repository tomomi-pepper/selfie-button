package com.robot_partner.camera_button;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.cgutman.adblib.AdbBase64;
import com.cgutman.adblib.AdbConnection;
import com.cgutman.adblib.AdbCrypto;
import com.cgutman.adblib.AdbStream;
import com.robot_partner.util.L;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CameraButtonService extends Service {
    private AdbConnection adb;
    private AdbStream stream;
    MediaPlayer mp;
    MediaPlayer mp2;
    MediaPlayer mp3;
    MediaPlayer mp4;
    MediaPlayer mp5;
    MediaPlayer mp6;
    List<MediaPlayer> medias;
    Random rand = new Random();
    Handler mHandler;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mp = MediaPlayer.create(this, R.raw.say_cheese);
        mp.setLooping(false);
        mp2 = MediaPlayer.create(this, R.raw.countdown);
        mp2.setLooping(false);
        mp3 = MediaPlayer.create(this, R.raw.countdown2);
        mp3.setLooping(false);
        mp4 = MediaPlayer.create(this, R.raw.countdown3);
        mp4.setLooping(false);
        mp5 = MediaPlayer.create(this, R.raw.laugh);
        mp5.setLooping(false);
        mp6 = MediaPlayer.create(this, R.raw.pochiru);
        mp6.setLooping(false);
        medias = new ArrayList<>();
        medias.add(mp);
        medias.add(mp2);
        medias.add(mp3);
        medias.add(mp4);
        medias.add(mp5);
        medias.add(mp6);
        register();
        connect();
        mHandler = new Handler();

        return super.onStartCommand(intent, flags, startId);
    }

    private void sendCommand(final int type) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String command = null;
                    switch (type) {
                        case 0:
                            command = "/system/bin/input keyevent KEYCODE_CAMERA";
                            break;
                        case 1:
                            command = "/system/bin/input keyevent KEYCODE_CAMERA";
                            medias.get(0).start();
                            break;
                        case 2:
                            MediaPlayer current = medias.get(rand.nextInt(5)+1);
                            current.start();
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            String command = "/system/bin/input keyevent KEYCODE_CAMERA";
                                            L.e("send shell command:[".concat(command).concat("]"));
                                            try {
                                                stream.write(command.concat("\n"));
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                                return;
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                                return;
                                            }
                                        }
                                    }).start();
                                }
                            }, current.getDuration() - 1000);
                            return;
                    }
                    L.e("send shell command:[".concat(command).concat("]"));
                    if (stream != null) {
                        stream.write(command.concat("\n"));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }).start();
    }

    public void connect() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Socket sock;
                AdbCrypto crypto = null;

                File privateKeyFile = new File(getApplicationContext().getFilesDir(), "private.key");
                File publicKeyFile = new File(getApplicationContext().getFilesDir(), "pub.key");

                if (privateKeyFile.isFile() && publicKeyFile.isFile()) {
                    try {
                        crypto = AdbCrypto.loadAdbKeyPair(new AdbBase64() {
                            @Override
                            public String encodeToString(byte[] data) {
                                return android.util.Base64.encodeToString(data, 16);
                            }
                        }, privateKeyFile, publicKeyFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    } catch (InvalidKeySpecException e) {
                        e.printStackTrace();
                    }
                } else {
                    // Setup the crypto object required for the AdbConnection
                    try {
                        crypto = AdbCrypto.generateAdbKeyPair(new AdbBase64() {
                            @Override
                            public String encodeToString(byte[] data) {
                                return android.util.Base64.encodeToString(data, 16);
                            }
                        });
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                        return;
                    }
                }

                // Connect the socket to the remote host
                L.i("Socket connecting...");
                try {
                    sock = new Socket("127.0.0.1", 5555);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                    return;
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
                L.i("Socket connected");

                // Construct the AdbConnection object
                try {
                    adb = AdbConnection.create(sock, crypto);
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }

                // Start the application layer connection process
                L.i("ADB connecting...");
                try {
                    adb.connect();
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                }
                L.i("ADB connected");
                try {
                    crypto.saveAdbKeyPair(privateKeyFile, publicKeyFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // Open the shell stream of ADB
                try {
                    stream = adb.open("shell:");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    return;
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                }

                // Start the receiving thread
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        L.i("receiving thread start");
                        while (!stream.isClosed()) {
                            try {
                                // Print each thing we read from the shell stream
                                System.out.print(new String(stream.read(), "US-ASCII"));
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                                return;
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                                return;
                            } catch (IOException e) {
                                e.printStackTrace();
                                return;
                            }
                        }
                    }
                }).start();
            }
        }).start();
    }

    private void register() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(PairingConst.ACTION_NOTIFY);
        registerReceiver(localReceiver, filter);

    }

    BroadcastReceiver localReceiver = new BroadcastReceiver() {
        int buttonId = -1;

        @Override
        public void onReceive(Context context, Intent intent) {
            buttonId = intent.getIntExtra(PairingConst.EXTRA_BUTTON_ID, -1);

            L.d("buttonId:" + buttonId);
            switch (buttonId) {
                default:
                    break;
                case 2://Single Click
                    sendCommand(0);
                    break;
                case 4://Double Click
                    sendCommand(1);
                    break;
                case 7://Long Click
                    sendCommand(2);
                    break;
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(localReceiver);
        try {
            if (adb != null) {
                adb.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}