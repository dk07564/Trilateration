package com.example.trilateration;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PackageManagerCompat;

import com.opencsv.CSVWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class RunService extends Service {
    List<ScanResult> wifiList = new ArrayList<>();
    ArrayList<String> wifiFormatList = new ArrayList<String>();
    IntentFilter intentFilter = new IntentFilter();
    WifiManager wifiManager;
    int ap1, ap2, ap3 = 0;
    int freq1, freq2, freq3 = 0;

    Date date;
    DateFormat dateFormat;
    String time;

    public static final String ACTION_STOP_FOREGROUND_SERVICE = "ACTION_STOP_FOREGROUND_SERVICE";

    final CSVWriter[] csvWriter = new CSVWriter[1];

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        String CHANNEL_ID = "channel";
        NotificationChannel channel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            channel = new NotificationChannel(CHANNEL_ID, "TEST", NotificationManager.IMPORTANCE_DEFAULT);
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);


            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID).setContentTitle("").setContentText("").build();
            startForeground(2, notification);
        }

        ArrayList<String[]> csv = new ArrayList<String[]>();

        date = new Date();
        dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        time = dateFormat.format(date);

        String fileName = "WIFI_" + time + ".csv";

        csv.add(new String[]{"date", "AP1", "freq1", "AP2", "freq2", "AP3", "freq3"});

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        Timer timer = new Timer();

        permission();

        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = new Date();
                String now = simpleDateFormat.format(date);

                try {
                    String filePath = getApplicationContext().getFilesDir().getAbsolutePath() + fileName;

                    getWiFi();

                    csvWriter[0] = new CSVWriter(new FileWriter(filePath));
                    csv.add(new String[]{String.valueOf(now), String.valueOf(ap1), String.valueOf(freq1), String.valueOf(ap2), String.valueOf(freq2), String.valueOf(ap3), String.valueOf(freq3)});

                    csvWriter[0].writeAll(csv);
                    csvWriter[0].close();

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        };

        timer.schedule(timerTask, 1000, 1000);


        return Service.START_STICKY;
    }

    public void permission() {
//        final String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
    }

    public void getWiFi() {
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        wifiList = wifiManager.getScanResults();

        List<ScanResult> scanResults = wifiManager.getScanResults();

        Log.e("LIST", String.valueOf(scanResults));


        for (int i = 0; i < scanResults.size(); i++) {
            switch (scanResults.get(i).BSSID) {
                case (""):
//                    ap1 = (float) Math.pow(10, (27.55 - (20 * Math.log10(scanResults.get(i).frequency)) + scanResults.get(i).level) / 20);
                    ap1 = scanResults.get(i).level;
                    freq1 = scanResults.get(i).frequency;
                    break;
                case (""):
//                    ap2 = (float) Math.pow(10, (27.55 - (20 * Math.log10(scanResults.get(i).frequency)) + scanResults.get(i).level) / 20);
                    ap2 = scanResults.get(i).level;
                    freq2 = scanResults.get(i).frequency;
                    break;
                case (""):
//                    ap3 = (float) Math.pow(10, (27.55 - (20 * Math.log10(scanResults.get(i).frequency)) + scanResults.get(i).level) / 20);
                    ap3 = scanResults.get(i).level;
                    freq3 = scanResults.get(i).frequency;
                    break;
            }
        }

        Log.e("AP1", String.valueOf(ap1) + " " + String.valueOf(freq1));
        Log.e("AP2", String.valueOf(ap2) + " " + String.valueOf(freq2));
        Log.e("AP3", String.valueOf(ap3) + " " + String.valueOf(freq3));
    }


}
