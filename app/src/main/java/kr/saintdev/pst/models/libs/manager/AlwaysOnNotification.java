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

import static android.content.Context.NOTIFICATION_SERVICE;
import static kr.saintdev.pst.models.libs.LibKt.addBroadcastReceiverFilter;
import static kr.saintdev.pst.models.libs.LibKt.checkAPILevel;

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

    private BroadcastReceiver receiver = null;
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

        if(checkAPILevel(Build.VERSION_CODES.O)) {
            initAPI26();
            addBroadcastReceiverFilter(new ProcedureBroadcastRecv(), context, "kr.saintdev.psct.aos.settings", "kr.saintdev.psct.aos.switch");
        } else {
            initDefault();
        }

        this.notificationView = new RemoteViews(context.getPackageName(), R.layout.notification_aos);
        this.notificationView.setTextViewText(R.id.notifi_aos_title, context.getString(R.string.notifi_prepared_start_title));
        this.notificationView.setOnClickPendingIntent(R.id.notifi_aos_rightbtn_text, getPendingIntent(context, "kr.saintdev.psct.aos.settings"));
        this.notificationView.setOnClickPendingIntent(R.id.notifi_aos_leftbtn_text, getPendingIntent(context, "kr.saintdev.psct.aos.switch"));

        this.builder
                .setSmallIcon(R.drawable.app_icon)
                .setContent(this.notificationView)
                .setDefaults(Notification.DEFAULT_LIGHTS)
                .setAutoCancel(true);
    }

    @TargetApi(26)
    private void initAPI26() {
        NotificationChannel notifiChannel = new NotificationChannel(
                NOTIFI_CHN_TAG + "_ID", NOTIFI_CHN_TAG + "_NAME", android.app.NotificationManager.IMPORTANCE_DEFAULT);

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

    public void setStatus(NotificationStatus status) {
        switch(status) {
            case RUNNING:
                this.notificationView.setTextViewText(R.id.notifi_aos_title, context.getString(R.string.notifi_running_title));
                this.notificationView.setTextViewText(R.id.notifi_aos_leftbtn_text, context.getString(R.string.notifi_running_button));
                this.notificationView.setImageViewBitmap(R.id.notifi_aos_leftbtn_icon, BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_stop_black));
                break;
            case PREPARED:
                this.notificationView.setTextViewText(R.id.notifi_aos_title, context.getString(R.string.notifi_prepared_start_title));
                this.notificationView.setTextViewText(R.id.notifi_aos_leftbtn_text, context.getString(R.string.notifi_prepared_start_button));
                this.notificationView.setImageViewBitmap(R.id.notifi_aos_leftbtn_icon, BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_play_black));
                break;
        }
    }

    private PendingIntent getPendingIntent(Context context, String action) {
        Intent intent = new Intent(action);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public enum NotificationStatus {
        PREPARED,   // 준비 됨
        RUNNING
    }
}
