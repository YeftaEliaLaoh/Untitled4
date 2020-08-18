package com.example.myapplication8.controllers;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.myapplication8.R;


public class DialogProgressController
{
    private ProgressBar progressBar;
    private TextView textProgress;

    private Context context;
    private Dialog dialog;
    private int max;
    private int progress = -1;
    private int label;

    public DialogProgressController( Context context, int label, int max){
        this.context = context;
        this.label = label;
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_import_loading);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        initLayout();
        setProgressMax(max);
        updateProgressDialog();
    }

    private void initLayout(){

        progressBar = dialog.findViewById(R.id.dialog_progress);
        textProgress = dialog.findViewById(R.id.dialog_txt_progress);
        TextView headerTitle = dialog.findViewById(R.id.deviceInfo_text_header_title);

        if(label != R.string.label_exporting)
        {
            headerTitle.setText(context.getString(R.string.label_header_importing));
        }
        else
        {
            headerTitle.setText(context.getString(R.string.label_header_exporting));
        }

    }

    public void showDialog(){
        dialog.show();
    }

    public void updateProgressDialog(){

        String extraMessage;

        if(label != R.string.label_exporting)
        {
            progress++;
            progressBar.setProgress(progress);
            extraMessage = " " + progress + " / " + max + " session(s)";
        }
        else
        {
            extraMessage = " " + max + " session(s)";
        }

        textProgress.setText(context.getString(label) + extraMessage);

    }

    public void setProgressMax(int max){
        progressBar.setMax(max);
        this.max = max;
    }

    public void dismissProgress(){
        dialog.dismiss();
    }
}
