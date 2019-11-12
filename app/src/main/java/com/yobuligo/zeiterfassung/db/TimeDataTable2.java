package com.yobuligo.zeiterfassung.db;

import android.database.sqlite.SQLiteDatabase;

public class TimeDataTable2 {
    private static final String _CREATE_TABLE = "CREATE TABLE \"time_data2\" (\n" +
            "\t\"_id\"\tINTEGER PRIMARY KEY AUTOINCREMENT,\n" +
            "\t\"start_time\"\tTEXT NOT NULL,\n" +
            "\t\"end_time\"\tTEXT\n" +
            ")";

    public static void createTable(SQLiteDatabase db){
        db.execSQL(_CREATE_TABLE);
    }
}
