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

import java.util.ArrayList;
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
    int flag=0;// flag是匹配消息的结果；0代表未匹配到，1 包名匹配但消息内容不匹配；2通过包名屏蔽；3通过消息内容屏蔽

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

//        DbUtils.saveNotification(new NotificationInfo(packageName, title, text, time));


                List<AppInfo> blackList = DbUtils.getApp();

                for (AppInfo app : blackList) {

                    if (packageName.equals(app.getPackageName())) {
                        flag=1;
                        Log.w(TAG, packageName + " Package命中：" + title + ": " + text);

                        if(matchsMessage(packageName,(title+"\n"+text).replaceAll("\n",""))) {
                            // flag=2或3;
                            if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
                                cancelNotification(sbn.getKey());
                            } else {
                                cancelNotification(sbn.getPackageName(), sbn.getTag(), sbn.getId());
                            }

                            if (SettingUtils.getInstance().isNotify()) {
                                createNotification(app.getAppName(), packageName, title, text);
                            }
                        }
                    }
                }

                if(null==text){text="";}

                if( text.replaceAll("\\s", "").length() == 0 && title.length()>14) {
                    text = "> " + title;
                }

        DbUtils.saveNotification(new NotificationInfo(packageName, title, text, time,flag));
                flag=0;
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
        SharedPreferences read = getSharedPreferences("setting",MODE_MULTI_PROCESS);
        int mode=read.getInt("mode",0);

        if(mode<0){
            return false;
        }else{
            String MessageBlackList = read.getString(packageName, "");

            if(mode>0){
                MessageBlackList=MessageBlackList+"\n"+read.getString(packageName+"."+mode, "");
            }
            Log.i(packageName+" rules:",MessageBlackList);
            String[] blacklist=splitRulls(MessageBlackList);

            if(null==blacklist){
                flag=2;
                Log.i("MatchMessage","Null list");
                result=true;
            }else {
                for(String black:blacklist){
                    if(Pattern.matches(black, message)){
                        //matches的规则是用正则表达式，必须完全匹配在字符串上；
                        result=true;
                        flag=3;
                    }
                    Log.i("MatchMessage"+black,message+result);
                }
            }

        }



    return  result;

    }


    private String[]  splitRulls(String MessageBlackList) {
        try{

            MessageBlackList=MessageBlackList. replaceAll("\n+","\n");

            if(MessageBlackList.length()==0 || MessageBlackList=="\n"){
                return null;
            }else{

                String[] result=MessageBlackList.split("\n");
                List<String> rule=new ArrayList<>();

                    for(String cache:result){
                        if(cache.length()!=0 && cache!="\n"){

                            if(cache.replaceAll("[\\u0021-\\u002f\\u003a-\\u003f\\u005b-\\u0061\\u007b-\\u007e\\\\]+","").length()==cache.length()){
                                    rule.add(".*"+cache.replaceAll("\\s",".*")+".*");
                            }else{
                                rule.add(cache);
                            }
                        }
                    }

                return rule.toArray(new String[rule.size()]);
            }
        }catch(Exception e) {
            Log.i("Split","error");
            e.printStackTrace();
        }
        return null;
    }
}
