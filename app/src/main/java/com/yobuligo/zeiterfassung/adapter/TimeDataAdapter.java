package com.yobuligo.zeiterfassung.adapter;


import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yobuligo.zeiterfassung.R;
import com.yobuligo.zeiterfassung.db.TimeDataContract;
import com.yobuligo.zeiterfassung.dialogs.IConfirmDeleteListener;
import com.yobuligo.zeiterfassung.dialogs.IDeleteItemListener;

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
    public void onBindViewHolder(@NonNull final TimeDataViewHolder timeDataViewHolder, int position) {
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
        timeDataViewHolder.position = position;
        timeDataViewHolder.itemId = getItemId(position);

        timeDataViewHolder.moreIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(context, timeDataViewHolder.moreIcon);
                popupMenu.inflate(R.menu.ctx_menu_data_list);
                popupMenu.setOnMenuItemClickListener(new OnMenuItemClicked(timeDataViewHolder.itemId, timeDataViewHolder.position));
                popupMenu.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        if (data == null) {
            return 0;
        }

        return data.getCount();
    }

    @Override
    public long getItemId(int position) {
        if (data == null) {
            return -1L;
        }

        if (data.moveToPosition(position)) {
            return data.getLong(data.getColumnIndex(BaseColumns._ID));
        }

        return -1L;
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
        final TextView moreIcon;
        Long itemId;
        int position;

        public TimeDataViewHolder(@NonNull View itemView) {
            super(itemView);
            startTime = itemView.findViewById(R.id.StartTimeValue);
            endTime = itemView.findViewById(R.id.EndTimeValue);
            moreIcon = itemView.findViewById(R.id.more_icon_text);
        }
    }

    class OnMenuItemClicked implements PopupMenu.OnMenuItemClickListener {
        private final long id;
        private final int position;

        public OnMenuItemClicked(long id, int position) {
            this.id = id;
            this.position = position;
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_item_delete:
                    deleteItem(id, position);
                    return true;
                default:
                    return false;
            }
        }

        private void deleteItem(final long id, final int position) {
            //Datensatz löschen
            if (context instanceof IConfirmDeleteListener) {
                ((IConfirmDeleteListener) context).confirmDelete(id, position);
            }
        }
    }

}
