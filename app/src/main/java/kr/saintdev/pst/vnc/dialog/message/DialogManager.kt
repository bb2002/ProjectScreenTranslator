package kr.saintdev.pst.vnc.dialog.message

import android.content.Context
import android.content.DialogInterface
import android.support.v7.app.AlertDialog

/**
 * Created by 5252b on 2017-08-01.
 * Update 06.16 / 2018
 */

class DialogManager(context: Context) {
    private var builder: AlertDialog.Builder? = null
    private var listener: OnDialogButtonClickListener? = null
    private var requestId = 0

    init {
        this.builder = AlertDialog.Builder(context)
    }

    fun setTitle(title: String) {
        this.builder!!.setTitle(title)
    }

    fun setDescription(desc: String) {
        this.builder!!.setMessage(desc)
    }

    fun setDialogButtonClickListener(listener: OnDialogButtonClickListener) {
        this.listener = listener
    }

    fun setOnYesButtonClickListener(text: String) {
        this.builder!!.setPositiveButton(text) { dialog, _ -> listener!!.onPositiveClick(dialog, requestId) }
    }

    fun setOnNoButtonClickListener(text: String) {
        this.builder!!.setNegativeButton(text) { dialog, _ -> listener!!.onNegativeClick(dialog, requestId) }
    }

    fun setRequestId(id: Int) {
        this.requestId = id
    }

    fun show() {
        this.builder!!.create().show()
    }
}
