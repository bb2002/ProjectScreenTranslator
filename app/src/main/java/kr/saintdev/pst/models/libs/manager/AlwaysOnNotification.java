package kr.saintdev.pst.models.libs.manager;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import kr.saintdev.pst.R;
import kr.saintdev.pst.models.components.broadcast.ProcedureBroadcastRecv;
import kr.saintdev.pst.models.components.services.AlwaysOnService;
import kr.saintdev.pst.models.libs.DeviceControl;
import kr.saintdev.pst.vnc.activity.view.MainActivity;
import kr.saintdev.pst.vnc.activity.view.NotificationActivity;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by 5252b on 2018-04-09
 * 노티피케이션 메니저
 */

public class AlwaysOnNotification {
    public static final int NOTIFI_ID = 0x20;
    public static final String NOTIFI_CHN_TAG = "스크린 번역기";
    private static AlwaysOnNotification instance = null;

    private Context context = null;
    private NotificationCompat.Builder builder = null;
    private Notification notification = null;
    private NotificationManager notifiMgr = null;

    private RemoteViews notificationView = null;

    public static AlwaysOnNotification getInstance(Context context) {
        if(AlwaysOnNotification.instance == null) {
            AlwaysOnNotification.instance = new AlwaysOnNotification(context);
        }

        return AlwaysOnNotification.instance;
    }

    private AlwaysOnNotification(Context context) {
        this.context = context;
        this.notifiMgr = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

        if(DeviceControl.INSTANCE.checkAPILevel(Build.VERSION_CODES.O)) {
            initAPI26();
        } else {
            initDefault();
        }

        this.builder
                .setSmallIcon(R.drawable.app_icon)
                .setContent(this.notificationView)
                .setContentIntent(getPendingIntent())
                .setAutoCancel(true);

        setStatus(NotificationStatus.PREPARED);
    }

    @TargetApi(26)
    private void initAPI26() {        // api 26 이상에서는 receiver 를 등록합니다.
        NotificationChannel notifiChannel = new NotificationChannel(
                NOTIFI_CHN_TAG + "_ID", NOTIFI_CHN_TAG + "_NAME", NotificationManager.IMPORTANCE_DEFAULT);

        notifiChannel.setDescription("Screentranslate Alarm channel");
        notifiChannel.enableLights(false);
        notifiChannel.enableVibration(false);
        notifiChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        notifiMgr.createNotificationChannel(notifiChannel);

        this.builder = new NotificationCompat.Builder(context, NOTIFI_CHN_TAG + "_ID");
    }

    private void initDefault() {
        this.builder = new NotificationCompat.Builder(context);
    }

    public Notification show() {
        this.notification = this.builder.build();
        this.notification.flags = Notification.FLAG_NO_CLEAR;
        this.notifiMgr.notify(NOTIFI_ID, this.notification);

        return this.notification;
    }

    public Notification getNotification() {
        if(this.notification == null) this.notification = this.builder.build();
        return this.notification;
    }

    public Context getContext() {
        return this.context;
    }

    private PendingIntent getPendingIntent() {
        Intent intent = new Intent(context, NotificationActivity.class);
        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        return PendingIntent.getActivity(this.context, 0, intent,  PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public void setStatus(NotificationStatus status) {
        switch(status) {
            case RUNNING:
                builder.setContentTitle(this.context.getString(R.string.notifi_running_title))
                        .setContentText(this.context.getString(R.string.notifi_running_content))
                        .setLargeIcon(BitmapFactory.decodeResource(this.context.getResources(), R.drawable.icon_stop_black));
                break;
            case PREPARED:
                builder.setContentTitle(this.context.getString(R.string.notifi_prepared_start_title))
                        .setContentText(this.context.getString(R.string.notifi_prepared_start_content))
                        .setLargeIcon(BitmapFactory.decodeResource(this.context.getResources(), R.drawable.icon_start_black));
                break;
        }

        show();
    }


    public enum NotificationStatus {
        PREPARED,   // 준비 됨
        RUNNING
    }
}
