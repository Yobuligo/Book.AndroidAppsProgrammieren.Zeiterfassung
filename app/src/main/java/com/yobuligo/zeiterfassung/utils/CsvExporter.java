package com.yobuligo.zeiterfassung.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.Toast;

import com.yobuligo.zeiterfassung.R;
import com.yobuligo.zeiterfassung.db.TimeDataContract;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CsvExporter extends AsyncTask<Void, Integer, Void> {

    private Context context;
    private ProgressDialog progressDialog = null;

    public CsvExporter(Context context) {
        this.context = context;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Cursor data = null;

        try {
            data = context.getContentResolver()
                    .query(TimeDataContract.TimeData.CONTENT_URI,
                            null,
                            null,
                            null,
                            null);

            int dataCount = data == null ? 0 : data.getCount();

            if (dataCount == 0) {
                return null;
            }

            //maximalen Wert in ProgressDialog schreiben
            if (progressDialog != null){
                progressDialog.setMax(dataCount + 1); //+1, da noch die Spaltenzeilen hinzugefügt werden
            }

            File externalStorage = Environment.getExternalStorageDirectory();
            if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                return null;
            }

            File exportPath = new File(externalStorage, "export");
            File exportFile = new File(exportPath, "TimeDataLog.csv");
            if (!exportPath.exists()) {
                exportPath.mkdirs();
            }

            BufferedWriter writer = null;
            StringBuilder stringBuilder = new StringBuilder();
            String[] columnList = data.getColumnNames();

            try {
                writer = new BufferedWriter(new FileWriter(exportFile));

                for (String columnName : columnList) {
                    if (stringBuilder.length() > 0) {
                        stringBuilder.append(";");
                    }

                    stringBuilder.append(columnName);
                }

                writer.append(stringBuilder);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }

            try {
                while (data.moveToNext()) {
                    writer.newLine();
                    stringBuilder.delete(0, stringBuilder.length());
                    for (int columnIndex = 0; columnIndex < columnList.length; columnIndex++) {
                        if (stringBuilder.length() > 0) {
                            stringBuilder.append(";");
                        }

                        if (data.isNull(columnIndex)) {
                            stringBuilder.append("<NULL>");
                        } else {
                            stringBuilder.append(data.getString(columnIndex));
                        }

                        Thread.sleep(250);
                        writer.append(stringBuilder);

                        //Fortschritt melden
                        publishProgress(data.getPosition() + 2); //+1 für 0 basierte Position +1 für Überschriften
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } finally {
                if (writer != null) {
                    try {
                        writer.flush();
                        writer.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (data != null) {
                data.close();
            }
        }

        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle(R.string.DialogTitleExport);
        progressDialog.setMessage(context.getString(R.string.DialogMessageExport));
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(true);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.show();
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        Toast.makeText(context, R.string.export_completed, Toast.LENGTH_LONG).show();

        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        if (progressDialog != null && values != null && values.length == 1){
            //Weitergabe des aktuellen Standes an den Dialog
            progressDialog.setProgress(values[0]);
        }
    }
}
