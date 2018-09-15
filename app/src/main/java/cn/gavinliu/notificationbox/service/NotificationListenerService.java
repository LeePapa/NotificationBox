package cn.gavinliu.notificationbox.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.widget.Toast;

import java.util.List;
import java.util.regex.Pattern;

import cn.gavinliu.notificationbox.NotificationBoxApp;
import cn.gavinliu.notificationbox.R;
import cn.gavinliu.notificationbox.model.AppInfo;
import cn.gavinliu.notificationbox.model.NotificationInfo;
import cn.gavinliu.notificationbox.ui.detail.DetailActivity;
import cn.gavinliu.notificationbox.utils.DbUtils;
import cn.gavinliu.notificationbox.utils.SettingUtils;

/**
 * Created by Gavin on 2016/10/11.
 */

public class NotificationListenerService extends android.service.notification.NotificationListenerService {

    private static final String TAG = "NLS";

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind");
        return super.onBind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);
        Log.i(TAG, "onNotificationPosted");

        Notification notification = sbn.getNotification();

        String packageName = sbn.getPackageName();
        long time = sbn.getPostTime();
        String title = notification.extras.getString(Notification.EXTRA_TITLE);
        String text = notification.extras.getString(Notification.EXTRA_TEXT);

        DbUtils.saveNotification(new NotificationInfo(packageName, title, text, time));
        List<AppInfo> blackList = DbUtils.getApp();

        for (AppInfo app : blackList) {
            if (packageName.equals(app.getPackageName())) {
                Log.w(TAG, packageName + " 拦截：" + title + ": " + text);

                if(matchsMessage(packageName,(title+"\n"+text).replaceAll("\n",""))) {
                    Log.e("Hide",packageName);
                    if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
                        cancelNotification(sbn.getKey());
                    } else {
                        cancelNotification(sbn.getPackageName(), sbn.getTag(), sbn.getId());
                    }

                    if (SettingUtils.getInstance().isNotify()) {
                        createNotification(app.getAppName(), packageName, title, text);
                    }
                }else{
                    Log.e("Exit",packageName);
                }

            //    Log.w("End",packageName);

            }
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);
    }

    private void createNotification(String appName, String packageName, String title, String text) {

        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra("appName", appName);
        intent.putExtra("packageName", packageName);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        Notification.Builder notifyBuilder = new Notification.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(getResources().getString(R.string.notify_blocking, appName))
                .setContentText(title + ": " + text)
                .setContentIntent(contentIntent)
                .setDefaults(Notification.DEFAULT_LIGHTS);

        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Notification notification = notifyBuilder.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        nm.notify(R.string.app_name, notification);
    }


    public Boolean matchsMessage(String packageName,String message){
        Boolean result=false;
        String[] blacklist=getSetting(packageName);
        if(null!=blacklist){
            for(String black:blacklist){
                if(Pattern.matches(black, message)){
                    //matches的规则是用正则表达式，必须完全匹配在字符串上；
                    result=true;
                }
                Log.i(black,message+result);
            }
        }
    return  result;

    }


    public String[]  getSetting(String appName) {
        try{
            Log.i("get",""+appName);
            SharedPreferences read = getSharedPreferences("setting",MODE_PRIVATE);
            String MessageBlackList = read.getString(appName, "");

            Log.i(appName+" rules:",MessageBlackList);
            String[] result=MessageBlackList.split("\n");
            return result;
        }catch(Exception e) {
            Log.i(appName,"error");
            e.printStackTrace();

        }
        return null;
    }
}
