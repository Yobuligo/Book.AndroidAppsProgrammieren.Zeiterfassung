package com.yobuligo.zeiterfassung;

import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.yobuligo.zeiterfassung.adapter.TimeDataAdapter;
import com.yobuligo.zeiterfassung.db.TimeDataContract;
import com.yobuligo.zeiterfassung.dialogs.DeleteTimeDataDialog;
import com.yobuligo.zeiterfassung.dialogs.IConfirmDeleteListener;
import com.yobuligo.zeiterfassung.dialogs.IDeleteItemListener;

public class ListDataActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks, IConfirmDeleteListener, IDeleteItemListener {

    private TimeDataAdapter timeDataAdapter = null;
    private static final int _LOADER_ID = 100;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_data);
        RecyclerView recyclerView = findViewById(R.id.DataList);
        Cursor data = getContentResolver().query(TimeDataContract.TimeData.CONTENT_URI, null, null, null, null);
        timeDataAdapter = new TimeDataAdapter(this, data);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(timeDataAdapter);
    }

    @NonNull
    @Override
    public Loader onCreateLoader(int id, @Nullable Bundle args) {
        CursorLoader loader = null;

        switch (id) {
            case _LOADER_ID:
                loader = new CursorLoader(
                        this, //context
                        TimeDataContract.TimeData.CONTENT_URI, //Daten URI
                        null, //alle Spalten
                        null, //Filter
                        null, //Filter Argumente
                        TimeDataContract.TimeData.Columns.START_TIME + " DESC" //Sortierung
                );
                break;
        }
        return loader;
    }

    @Override
    protected void onStart() {
        super.onStart();
        getSupportLoaderManager().restartLoader(_LOADER_ID, null, this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        getSupportLoaderManager().destroyLoader(_LOADER_ID);
    }

    @Override
    public void onLoadFinished(@NonNull Loader loader, Object data) {
        Cursor cursor;

        switch (loader.getId()) {
            case _LOADER_ID:
                cursor = (Cursor) data;
                timeDataAdapter.swapCursor(cursor);
                break;
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader loader) {
        switch (loader.getId()) {
            case _LOADER_ID:
                timeDataAdapter.swapCursor(null);
                break;
        }
    }

    @Override
    public void confirmDelete(long id, int position) {
        //Argumente initialisieren
        Bundle arguments = new Bundle();
        arguments.putLong(DeleteTimeDataDialog.ID_KEY, id);
        arguments.putInt(DeleteTimeDataDialog.POSITION_KEY, position);

        //Dialog initialisieren
        DeleteTimeDataDialog deleteTimeDataDialog = new DeleteTimeDataDialog();
        deleteTimeDataDialog.setArguments(arguments);

        //Dialog anzeigen
        deleteTimeDataDialog.show(getSupportFragmentManager(), "DeleteDialog");
    }

    @Override
    public void deleteItem(long id, int position) {
        //Uri zum Löschen erzeugen
        Uri deleteUri = ContentUris.withAppendedId(TimeDataContract.TimeData.CONTENT_URI, id);

        //Löschen
        getContentResolver().delete(deleteUri, null, null);

        //Benachrichtigung, dass ein Datensatz gelöscht wurde (Recycler View)
        timeDataAdapter.notifyItemRemoved(position);
    }
}
