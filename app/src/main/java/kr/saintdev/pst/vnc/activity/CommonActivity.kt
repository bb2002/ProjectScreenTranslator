package kr.saintdev.pst.vnc.activity

import android.content.DialogInterface
import android.support.v7.app.AppCompatActivity
import cn.pedant.SweetAlert.SweetAlertDialog
import kr.saintdev.pst.R
import kr.saintdev.pst.vnc.dialog.message.DialogManager
import kr.saintdev.pst.vnc.dialog.message.OnDialogButtonClickListener
import kr.saintdev.pst.vnc.dialog.progress.ProgressManager
import libs.mjn.prettydialog.PrettyDialog
import libs.mjn.prettydialog.PrettyDialogCallback


/**
 * Copyright (c) 2015-2018 Saint software All rights reserved.
 * @Date 2018-07-01
 */
open class CommonActivity : AppCompatActivity() {

    fun openMessageDialog(
            title: String = R.string.info_msg_title.str(),
            content: String) {
        val dialog = DialogManager(context=this)
        dialog.setTitle(title)
        dialog.setDescription(content)
        dialog.setDialogButtonClickListener(object : OnDialogButtonClickListener {
            override fun onPositiveClick(dialog: DialogInterface, reqId: Int) {
                dialog.dismiss()
            }

            override fun onNegativeClick(dialog: DialogInterface, reqId: Int) {}
        })

        dialog.setOnYesButtonClickListener(getString(R.string.common_positive_ok))
        dialog.show()
    }

    var pm: ProgressManager? = null
    fun openProgressDialog(msg: String? = R.string.common_loading.str()) {
        pm = ProgressManager(this)
        pm!!.setMessage(msg)
        pm!!.enable()
    }

    fun closeProgressDialog() {
        if(pm != null) {
            pm!!.disable()
        }
    }

    // Int 형 리소스 아이디를 String 으로 바꾸어 줍니다.
    fun Int.str() = getString(this)

    // Int 형 리소스 아이디를 Color 값으로 바꾸어 줍니다.
    fun Int.color() = resources.getColor(this)

    // Int 형 리소스를 Drawable 으로 바꾸어줍니다.
    fun Int.image() = resources.getDrawable(this)

    fun Int.array() = resources.getStringArray(this)

    // ActionBar 를 제어합니다.
    fun actionBar(v: Boolean) {
        val bar = supportActionBar
        if (bar != null) {
            if (v) bar.show() else bar.hide()
        }
    }

    /**
     * Sweet dialog Functions
     */
    fun openPrettyDialog(title: Int, content: Int, type: DialogType) {
        val set = getDialogType(type)

        PrettyDialog(this)
                .setTitle(title.str())
                .setMessage(content.str())
                .setIconTint(set[1])
                .setIcon(set[0])
                .show()
    }

    fun openPrettyConfirmDialog(title: Int, content: Int, type: DialogType, listener: PrettyDialogCallback): PrettyDialog {
        val set = getDialogType(type)
        val dialog = PrettyDialog(this)


        dialog
                .setTitle(title.str())
                .setMessage(content.str())
                .setIcon(set[0])
                .setIconTint(set[1])
                .addButton(R.string.common_positive_ok.str(), R.color.pdlg_color_white, R.color.pdlg_color_green, listener)
                .addButton(R.string.common_negative_no.str(), R.color.pdlg_color_white, R.color.pdlg_color_red) {
                    dialog.dismiss()
                }
        dialog.show()

        return dialog
    }

    private fun getDialogType(type: DialogType)
        = when(type) {
            DialogType.ERROR -> arrayOf(R.drawable.pdlg_icon_close, R.color.colorRed)
            DialogType.WARNING -> arrayOf(R.drawable.pdlg_icon_info, R.color.ll_aqours_chika_hard)
            DialogType.SUCCESS -> arrayOf(R.drawable.pdlg_icon_success, R.color.colorGreen)
        }
}

enum class DialogType {
    ERROR, WARNING, SUCCESS
}