package com.yobuligo.zeiterfassung.db;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class TimeDataContract {

    // unique name within the operation system
    public static final String AUTHORITY = "com.yobuligo.zeiterfassung.provider";

    //base URI to access the content provider
    public static final Uri AUTHORIZTY_URI = Uri.parse("content://" + AUTHORITY);

    //contract for time
    public static final class TimeData {

        //subfolder to access the data / for me its more or less the entity
        public static final String CONTENT_DIRECTORY = "time";

        //subfolder for still open entry
        public static final String NOT_FINISHED_CONTENT_DIRECTORY = CONTENT_DIRECTORY + "/not_finished";

        //URI to access the data
        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORIZTY_URI, CONTENT_DIRECTORY);

        //URI to access open entry
        public static final Uri NOT_FINISHED_CONTENT_URI = Uri.withAppendedPath(AUTHORIZTY_URI, NOT_FINISHED_CONTENT_DIRECTORY);

        //Typ for listing the data
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_DIRECTORY;

        //Typ for a single date
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_DIRECTORY;

        public interface Columns extends BaseColumns {
            //Start time
            String START_TIME = "start_time";

            //End time
            String END_TIME = "end_time";
        }
    }

    public static final class Converter {
        private static final String _ISO_8601_PATTERN = "yyyy-MM-dd'T'HH:mm";
        public static final DateFormat DB_DATE_TIME_FORMATTER = new SimpleDateFormat(_ISO_8601_PATTERN, Locale.GERMANY);

        public static Calendar parse(String dbTime) throws ParseException {
            Calendar date = Calendar.getInstance();
            date.setTime(DB_DATE_TIME_FORMATTER.parse(dbTime));
            return date;
        }

        public static String format(Calendar dateTime) {
            return DB_DATE_TIME_FORMATTER.format(dateTime.getTime());
        }
    }

}
