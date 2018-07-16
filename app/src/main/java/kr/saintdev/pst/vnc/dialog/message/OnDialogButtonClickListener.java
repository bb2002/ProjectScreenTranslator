package kr.saintdev.pst.vnc.dialog.message;

import android.content.DialogInterface;

public interface OnDialogButtonClickListener {
    void onPositiveClick(DialogInterface dialog, int reqId);
    void onNegativeClick(DialogInterface dialog, int reqId);
}
