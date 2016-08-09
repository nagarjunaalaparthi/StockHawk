package com.stockhawk;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

public class SearchDialogFragment extends DialogFragment {

    private EditText mSearchText;
    private TextView mOK;
    private OkListener mOkListener;


    public void setOkListener(OkListener okListener) {
        mOkListener = okListener;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialogue_layout, container, false);

        mSearchText = (EditText) view.findViewById(R.id.search_symbol);
        mOK = (TextView) view.findViewById(R.id.ok);


        mOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOkListener.onOk(mSearchText.getText().toString());
            }
        });

        return view;
    }


    public interface OkListener {
        void onOk(String text);
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        // request a window without the title
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }
}
