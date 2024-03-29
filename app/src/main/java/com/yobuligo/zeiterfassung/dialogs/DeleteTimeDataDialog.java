package com.yobuligo.zeiterfassung.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.yobuligo.zeiterfassung.R;

public class DeleteTimeDataDialog extends AppCompatDialogFragment {
    public static final String ID_KEY = "Key_TimeDataId";
    public static final String POSITION_KEY = "Key_TimeDataPosition";
    private long id = -1L;
    private int position = -1;

    @Override
    public void setArguments(@Nullable Bundle args) {
        super.setArguments(args);
        //ID aus Argumenten auslesen
        id = args.getLong(ID_KEY, -1L);
        position = args.getInt(POSITION_KEY, -1);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        //ID für die Drehung zwischenspeichern
        outState.putLong(ID_KEY, id);
        outState.putInt(POSITION_KEY, position);
        super.onSaveInstanceState(outState);
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        //ID wiederherstellen, falls Drehung passiert ist
        if (savedInstanceState != null && savedInstanceState.containsKey(ID_KEY)) {
            id = savedInstanceState.getLong(ID_KEY);
        }

        if (savedInstanceState != null && savedInstanceState.containsKey(POSITION_KEY)) {
            position = savedInstanceState.getInt(POSITION_KEY);
        }

        //Fehlermeldung ausgeben, falls ID nicht gesetzt werden wurde
        if (id == -1L) {
            throw new IllegalArgumentException("Please set id with 'setArguments' method and key '" + ID_KEY + "'");
        }

        if (position == -1) {
            throw new IllegalArgumentException("Please set position with 'setArguments' methods and key '" + POSITION_KEY + "'");
        }

        return new AlertDialog.Builder(getContext())
                .setTitle(R.string.DialogTitleDeleteItem)
                .setMessage(R.string.DialogMessageDeleteItem)
                .setNegativeButton(R.string.ButtonCancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Dialog schließen
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(R.string.ButtonDelete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Löschfunktion in der Activity aufrufen
                        if (getActivity() instanceof IDeleteItemListener) {
                            ((IDeleteItemListener) getActivity()).deleteItem(id, position);
                        }

                        //Dialog schließen
                        dialog.dismiss();
                    }
                }).create();
    }
}
