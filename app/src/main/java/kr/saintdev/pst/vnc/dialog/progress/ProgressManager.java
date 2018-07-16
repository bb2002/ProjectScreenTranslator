package kr.saintdev.pst.vnc.dialog.progress;

import android.app.ProgressDialog;
import android.content.Context;

/**
 * Created by 5252b on 2017-07-04.
 * Progress Dialog 를 띄워주는 클래스
 */

public class ProgressManager {
    private ProgressDialog dialog = null;

    public ProgressManager(Context context) {
        this.dialog = new ProgressDialog(context);
        this.dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        this.dialog.setCancelable(false);
    }

    public void setMessage(String msg) {
        this.dialog.setMessage(msg);
    }

    public void enable(){
        try {
            this.dialog.show();
        } catch(Exception ex){}
    }

    public void disable() {
        if(!this.dialog.isShowing()) return;

        this.dialog.dismiss();
    }
}
