package com.yobuligo.zeiterfassung;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.yobuligo.zeiterfassung.db.TimeDataContract;
import com.yobuligo.zeiterfassung.utils.CsvExporter;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;

public class TimeTrackingActivity extends AppCompatActivity {
    private EditText startDateTime;
    private EditText endDateTime;
    private Button startCommand;
    private Button endCommand;
    private final DateFormat dateTimeFormatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
    private final int _REQUEST_WRITE_PERMISSION_ID = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_tracking);
        startDateTime = findViewById(R.id.StartDateTime);
        endDateTime = findViewById(R.id.EndDateTime);
        startCommand = findViewById(R.id.StartCommand);
        endCommand = findViewById(R.id.EndCommand);

        //avoid keyboard input
        startDateTime.setKeyListener(null);
        endDateTime.setKeyListener(null);
    }

    @Override
    protected void onStart() {
        super.onStart();
        initFromDb();
    }

    private void initFromDb() {
        //deactive buttons
        startCommand.setEnabled(false);
        endCommand.setEnabled(false);

        //load still open entry, as far as available
        Cursor data = getContentResolver().query(
                TimeDataContract.TimeData.NOT_FINISHED_CONTENT_URI,
                new String[]{TimeDataContract.TimeData.Columns.START_TIME},
                null,
                null,
                null
        );

        //Check if data exists
        if (data.moveToFirst()) {
            Calendar startTime = null;
            try {
                startTime = TimeDataContract.Converter.parse(data.getString(0));
                startDateTime.setText(dateTimeFormatter.format(startTime.getTime()));
            } catch (ParseException e) {
                //error while converting the start time
                startDateTime.setText("Falsches Datumsformat in der Datenbank");
            }

            //activate end button
            endDateTime.setText("");
            endCommand.setEnabled(true);
        } else {
            //activate start button
            startDateTime.setText("");
            endDateTime.setText("");
            startCommand.setEnabled(true);
        }

        data.close();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startCommand.setOnClickListener(new StartButtonClicked());
        endCommand.setOnClickListener(new EndButtonClicked());
    }

    @Override
    protected void onPause() {
        super.onPause();
        startCommand.setOnClickListener(null);
        endCommand.setOnClickListener(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_time_tracking, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.ListDataMenuItem:
                /*//Implizit intent
                Intent googleIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.de"));
                startActivity(googleIntent);
                Toast.makeText(TimeTrackingActivity.this, "Auflistung aufrufen", Toast.LENGTH_LONG).show();*/

                //Expliziter Intent
                Intent listDataIntent = new Intent(this, ListDataActivity.class);
                startActivity(listDataIntent);
                return true;
            case R.id.menu_item_test:
                Toast.makeText(TimeTrackingActivity.this, "Test", Toast.LENGTH_LONG).show();
                return true;
            case R.id.menu_item_export:
                Toast.makeText(this, "Test Export", Toast.LENGTH_SHORT).show();

                //Abfrage der Berechtigung (geprüft wird, ob die benötigte Berechtigung gegeben ist
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    CsvExporter csvExporter = new CsvExporter(this);
                    csvExporter.execute();
                } else {
                    //Berechtigung anfragen,
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, _REQUEST_WRITE_PERMISSION_ID);
                }

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        //Prüfen, von welcher Anfrage die Antwort kommt
        if (requestCode == _REQUEST_WRITE_PERMISSION_ID) {
            //prüfen, ob die Berechtigung erteilt wurde
            if (Manifest.permission.WRITE_EXTERNAL_STORAGE.equals(permissions[0])
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Berechtigung erteilt, Export durchgeführen
                CsvExporter csvExporter = new CsvExporter(this);
                csvExporter.execute();
            } else {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }

    class StartButtonClicked implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            //get current time
            Calendar calendar = Calendar.getInstance();

            //Convert time for database
            String dbTime = TimeDataContract.Converter.format(calendar);

            //Time for database
            ContentValues contentValues = new ContentValues();
            contentValues.put(TimeDataContract.TimeData.Columns.START_TIME, dbTime);

            //Save to database
            v.getContext().getContentResolver().insert(TimeDataContract.TimeData.CONTENT_URI, contentValues);

            //output for URI
            startDateTime.setText(dateTimeFormatter.format(calendar.getTime()));
        }
    }

    class EndButtonClicked implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            //get current time
            Calendar calendar = Calendar.getInstance();

            //Convert time for database
            String dbTime = TimeDataContract.Converter.format(calendar);

            //Time for database
            ContentValues contentValues = new ContentValues();
            contentValues.put(TimeDataContract.TimeData.Columns.END_TIME, dbTime);

            //Save to database
            v.getContext().getContentResolver().update(TimeDataContract.TimeData.NOT_FINISHED_CONTENT_URI, contentValues, null, null);

            //output for URI
            endDateTime.setText(dateTimeFormatter.format(calendar.getTime()));
        }
    }
}