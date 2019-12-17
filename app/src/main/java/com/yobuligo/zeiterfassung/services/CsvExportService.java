package com.yobuligo.zeiterfassung.services;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Environment;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.yobuligo.zeiterfassung.R;
import com.yobuligo.zeiterfassung.db.TimeDataContract;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CsvExportService extends IntentService {

    private static final String _NOTIFICATION_CHANNEL = "Export";
    private static final int _NOTIFICATION_ID = 500;

    public CsvExportService() {
        super("CsvExporter");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        //System Service für Benachrichtigung abfragen
        NotificationManagerCompat notifyManager = NotificationManagerCompat.from(getApplicationContext());

        //Gruppe(Channel) anlegen
        createChannel();

        //Benachrichtigung vorbefüllen
        NotificationCompat.Builder notificationBuilder = createNotification();

        Cursor data = null;

        getBaseContext();

        try {
            data = getBaseContext().getContentResolver()
                    .query(TimeDataContract.TimeData.CONTENT_URI,
                            null,
                            null,
                            null,
                            null);

            int dataCount = data == null ? 0 : data.getCount();

            if (dataCount == 0) {
                //Nicht weiter machen, wenn keine Daten vorhanden sind
                return;
            }

            //Export starten, mit richtigen Max-Wert
            notificationBuilder.setProgress(dataCount + 1, 0, false);

            //Benachrichtigung veröffentlichen
            notifyManager.notify(_NOTIFICATION_ID, notificationBuilder.build());

            //Ordner für externe Daten
            File externalStorage = Environment.getExternalStorageDirectory();

            //Prüfen, ob externe Daten geschrieben werden können (SD-Karte nur Read Only oder voll)
            if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                return;
            }

            //Unterordner für unseren Export
            File exportPath = new File(externalStorage, "export");

            //Dateinamen für Export
            File exportFile = new File(exportPath, "TimeDataLog.csv");

            //Erzeugen der Ordner, falls noch nicht vorhanden
            if (!exportPath.exists()) {
                exportPath.mkdirs();
            }

            //Klasse zum Schreiben der Daten
            BufferedWriter writer = null;
            StringBuilder stringBuilder = new StringBuilder();

            //Auslesen der Spaltennamen
            String[] columnList = data.getColumnNames();

            try {
                writer = new BufferedWriter(new FileWriter(exportFile));

                //Befüllen der ersten Zeile mit Spaltennamen
                for (String columnName : columnList) {
                    if (stringBuilder.length() > 0) {
                        stringBuilder.append(";");
                    }

                    stringBuilder.append(columnName);
                }

                writer.append(stringBuilder);

                //Fortschritt für Ausgabe Spaltennamen veröffentlichen
                notificationBuilder.setProgress(dataCount + 1, 1, false);
                notifyManager.notify(_NOTIFICATION_ID, notificationBuilder.build());

            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            try {
                //Zeilen mit Daten ausgeben
                while (data.moveToNext()) {
                    //Neue Zeile
                    writer.newLine();

                    //Zeilenvariable leeren
                    stringBuilder.delete(0, stringBuilder.length());

                    //Ausgabe aller Spaltenwerte
                    for (int columnIndex = 0; columnIndex < columnList.length; columnIndex++) {
                        if (stringBuilder.length() > 0) {
                            stringBuilder.append(";");
                        }

                        //Prüfen auf NULL (Datenbank) des Spalteninhaltes
                        if (data.isNull(columnIndex)) {
                            stringBuilder.append("<NULL>");
                        } else {
                            stringBuilder.append(data.getString(columnIndex));
                        }

                        Thread.sleep(1000);
                        writer.append(stringBuilder);

                        //Fortschritt für Ausgabe Eintrag veröffentlichen
                        notificationBuilder.setProgress(dataCount, data.getPosition() + 2, false);
                        notifyManager.notify(_NOTIFICATION_ID, notificationBuilder.build());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                return;
            } finally {
                if (writer != null) {
                    try {
                        writer.flush();
                        writer.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                        return;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        } finally {
            if (data != null) {
                data.close();
            }

            //Fortschritte für Abschluss veröffentlichen
            notificationBuilder
                    .setProgress(0, 0, false)
                    .setContentText(getString(R.string.ExportNotificationFinishMessage));
            notifyManager.notify(_NOTIFICATION_ID, notificationBuilder.build());

        }

        return;
    }

    private void createChannel() {
        //Versionsweiche
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //OS Service für Benachrichtigung holen
            NotificationManager manager = (NotificationManager) getSystemService(getBaseContext().NOTIFICATION_SERVICE);

            //Gruppe definieren
            NotificationChannel channel = new NotificationChannel(
                    _NOTIFICATION_CHANNEL, //Name der Gruppe
                    getString(R.string.ExportNotificationChannel), //Titel der Gruppe
                    NotificationManager.IMPORTANCE_DEFAULT // Wichtigkeit
            );

            //Beschreibung für die Grüße
            channel.setDescription(getString(R.string.ExportNotificationChannelDescription));

            //Sichtbarkeit der Gruppe auf dem Sperrbildschirm
            channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PRIVATE);

            //Gruppe erzeugen
            manager.createNotificationChannel(channel);
        }
    }

    private NotificationCompat.Builder createNotification() {
        //Notification erzeugen
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), _NOTIFICATION_CHANNEL)
                .setContentTitle(getString(R.string.ExportNotificationTitle))
                .setContentText(getString(R.string.ExportNotificationMessage))
                .setSmallIcon(R.drawable.ic_file_download_black_24dp)
                .setAutoCancel(true);

        return builder;
    }
}