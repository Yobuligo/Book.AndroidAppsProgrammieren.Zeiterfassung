package com.yobuligo.zeiterfassung.adapter;


import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yobuligo.zeiterfassung.R;
import com.yobuligo.zeiterfassung.db.TimeDataContract;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;

public class TimeDataAdapter extends RecyclerView.Adapter<TimeDataAdapter.TimeDataViewHolder> {

    private Context context;
    private Cursor data = null;
    private DateFormat _dateTimeFormatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);

    public TimeDataAdapter(Context context, Cursor data) {
        this.context = context;
        this.data = data;
    }

    @NonNull
    @Override
    public TimeDataViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.item_time_data, parent, false);
        return new TimeDataViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TimeDataViewHolder timeDataViewHolder, int position) {
        //keine Daten vorhanden
        if (data == null) {
            return;
        }

        //keine Daten an der Position
        if (!data.moveToPosition(position)) {
            return;
        }

        //Daten auslesen
        int columnIndex = data.getColumnIndex(TimeDataContract.TimeData.Columns.START_TIME);
        String startTimeString = data.getString(columnIndex);

        try {
            Calendar start = TimeDataContract.Converter.parse(startTimeString);
            startTimeString = _dateTimeFormatter.format(start.getTime());
        } catch (ParseException e) {
            startTimeString = "PARSE ERROR";
        }

        String endTimeString = "---";
        columnIndex = data.getColumnIndex(TimeDataContract.TimeData.Columns.END_TIME);
        if (!data.isNull(columnIndex)) {
            endTimeString = data.getString(columnIndex);
        }

        try {
            Calendar end = TimeDataContract.Converter.parse(endTimeString);
            endTimeString = _dateTimeFormatter.format(end.getTime());
        } catch (ParseException e) {
            endTimeString = "PARSE ERROR";
        }

        //Daten ins View schreiben
        timeDataViewHolder.startTime.setText(startTimeString);
        timeDataViewHolder.endTime.setText(endTimeString);
    }

    @Override
    public int getItemCount() {
        if (data == null) {
            return 0;
        }

        return data.getCount();
    }

    public void swapCursor(Cursor newData) {
        if (data != null) {
            data.close();
        }

        this.data = newData;
        notifyDataSetChanged();
    }

    class TimeDataViewHolder extends RecyclerView.ViewHolder {
        final TextView startTime;
        final TextView endTime;

        public TimeDataViewHolder(@NonNull View itemView) {
            super(itemView);
            startTime = itemView.findViewById(R.id.StartTimeValue);
            endTime = itemView.findViewById(R.id.EndTimeValue);
        }
    }

}
