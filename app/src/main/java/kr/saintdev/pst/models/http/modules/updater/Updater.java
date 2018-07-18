package kr.saintdev.pst.models.http.modules.updater;

import android.content.Context;
import android.content.DialogInterface;

import org.json.JSONObject;

import java.util.HashMap;

import kr.saintdev.pst.R;
import kr.saintdev.pst.models.consts.version.Versions;
import kr.saintdev.pst.models.consts.HostKt;
import kr.saintdev.pst.models.http.HttpRequester;
import kr.saintdev.pst.models.http.HttpResponseObject;
import kr.saintdev.pst.models.libs.OpenPreparedActivity;
import kr.saintdev.pst.models.libs.async.BackgroundWork;
import kr.saintdev.pst.models.libs.async.OnBackgroundWorkListener;
import kr.saintdev.pst.vnc.activity.CommonActivity;
import kr.saintdev.pst.vnc.dialog.message.DialogManager;
import kr.saintdev.pst.vnc.dialog.message.OnDialogButtonClickListener;


/**
 * Copyright (c) 2015-2018 Saint software All rights reserved.
 *
 * @Date 2018-06-15
 */

public class Updater {
    private String versionName = null;
    private int versionCode = 0;
    private boolean isNeedUpdate = false;

    public Updater(String versionName, int versionCode, boolean isNeedUpdate) {
        this.versionName = versionName;
        this.versionCode = versionCode;
        this.isNeedUpdate = isNeedUpdate;
    }

    public String getVersionName() {
        return versionName;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public boolean isNeedUpdate() {
        return isNeedUpdate;
    }

    public static void checkUpdate(UpdateListener listener, Context context) {
        HashMap<String, Object> args = new HashMap<>();
        args.put("now-version", Versions.getVersionCode());

        HttpRequester requester = new HttpRequester(HostKt.HTTP_UPDATE_CHECK, args, 0, new OnBackgroundCallback(listener));
        requester.execute();
    }

    public static void openUpdateDialog(final CommonActivity activity, int newVersion) {
        DialogManager dm = new DialogManager(activity);
        dm.setTitle(activity.getString(R.string.updater_need_update_title));
        dm.setDescription(activity.getString(R.string.updater_need_update_content) + "\nNew SDK code : " + newVersion);
        dm.setDialogButtonClickListener(new OnDialogButtonClickListener() {
            @Override
            public void onPositiveClick(DialogInterface dialog, int reqId) {
                OpenPreparedActivity.INSTANCE.openPlayStore(activity);
                dialog.dismiss();
            }

            @Override
            public void onNegativeClick(DialogInterface dialog, int reqId) {
                dialog.dismiss();
            }
        });

        dm.setOnYesButtonClickListener(activity.getString(R.string.common_positive_ok));
        dm.setOnNoButtonClickListener(activity.getString(R.string.common_negative_ignore));
        dm.show();
    }

    private static class OnBackgroundCallback implements OnBackgroundWorkListener {
        UpdateListener listener = null;

        public OnBackgroundCallback(UpdateListener listener) {
            this.listener = listener;
        }

        @Override
        public void onSuccess(int requestCode, BackgroundWork worker) {
            HttpResponseObject response = (HttpResponseObject) worker.getResult();
            JSONObject message = response.getMessage();

            Updater update = null;
            if(response.isSuccess()) {
                try {
                    update = new Updater(
                            message.getString("last-version-name"),
                            message.getInt("last-version-code"),
                            message.getBoolean("need-update")
                    );
                } catch(Exception ex) {
                    ex.printStackTrace();
                }
            }

            listener.onResponseUpdate(update);
        }

        @Override
        public void onFailed(int requestCode, Exception ex) {
            listener.onResponseUpdate(null);
        }
    }
}
