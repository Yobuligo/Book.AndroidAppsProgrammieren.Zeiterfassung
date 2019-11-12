package com.yobuligo.zeiterfassung;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.yobuligo.zeiterfassung.db.DbHelper;
import com.yobuligo.zeiterfassung.db.TimeDataContract;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class TimeTrackingActivity extends AppCompatActivity {
    private EditText startDateTime;
    private EditText endDateTime;
    private Button startCommand;
    private Button endCommand;
    private final DateFormat dateTimeFormatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_tracking);
        startDateTime = findViewById(R.id.StartDateTime);
        endDateTime = findViewById(R.id.EndDateTime);
        startCommand = findViewById(R.id.StartCommand);
        endCommand = findViewById(R.id.EndCommand);
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