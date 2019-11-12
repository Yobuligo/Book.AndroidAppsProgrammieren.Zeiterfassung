package com.yobuligo.zeiterfassung;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.yobuligo.zeiterfassung.db.DbHelper;

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
        startCommand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                startDateTime.setText(dateTimeFormatter.format(calendar.getTime()));

                DateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.GERMANY);

                DbHelper dbHelper = new DbHelper(getApplicationContext());
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put("start_time", dbFormat.format(calendar.getTime()));
                db.insert("time_data", null, values);
                db.insert("time_data2", null, values);
                db.close();
                dbHelper.close();

            }
        });

        endCommand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                endDateTime.setText(dateTimeFormatter.format(calendar.getTime()));
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        startCommand.setOnClickListener(null);
        endCommand.setOnClickListener(null);
    }
}
