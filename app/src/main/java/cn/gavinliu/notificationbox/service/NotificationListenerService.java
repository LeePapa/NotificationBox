package cn.gavinliu.notificationbox.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.service.notification.StatusBarNotification;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RemoteViews;
import android.widget.TextView;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import cn.gavinliu.notificationbox.R;
import cn.gavinliu.notificationbox.baidutts.ttsProxy;
import cn.gavinliu.notificationbox.model.AppInfo;
import cn.gavinliu.notificationbox.model.NotificationInfo;
import cn.gavinliu.notificationbox.ui.detail.DetailActivity;
import cn.gavinliu.notificationbox.ui.main.MainContract;
import cn.gavinliu.notificationbox.utils.DbUtils;
import cn.gavinliu.notificationbox.utils.SettingUtils;

/**
 * Created by Gavin on 2016/10/11.
 */

public class NotificationListenerService extends android.service.notification.NotificationListenerService {

    private static final String TAG = "NLS";
    int flag = 0;// flag是匹配消息的结果；0代表未匹配到，1 包名匹配但消息内容不匹配；2通过包名屏蔽；3通过消息内容屏蔽

    private boolean mode_read = true;
    private int msg_count = 0;

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

    ttsProxy ttsProxy;

    @Override
    public void onCreate() {
        super.onCreate();
        //  toggleNotificationListenerService();
        ttsProxy = new ttsProxy(getApplicationContext());
        Log.i(TAG, "onCreate");
    }

    private void toggleNotificationListenerService() {
        PackageManager pm = getPackageManager();
        pm.setComponentEnabledSetting(new ComponentName(this, cn.gavinliu.notificationbox.service.NotificationListenerService.class),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        pm.setComponentEnabledSetting(new ComponentName(this, cn.gavinliu.notificationbox.service.NotificationListenerService.class),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");
/*        String app=intent.getPackage();
//        if(app.contains("com.tumuyan.notification") ){ }
            int cmd_read=intent.getIntExtra("cmd_read",0);
            if (cmd_read==1){
                mode_read=true;
            }else if(cmd_read==2){
                mode_read=false;
            }*/
        msg_count = 0;
        return super.onStartCommand(intent, flags, startId);
    }


    private void genRemoteViewsDecoder(RemoteViews remoteViews) {
        if (null == filedmActions || null == filedValue || null == filedMethod) try {

            filedmActions = RemoteViews.class.getDeclaredField("mActions");
            filedmActions.setAccessible(true);

            Log.w("" + msg_count + " any 0b", "get mActions - " + filedmActions.get(remoteViews).getClass().getSimpleName());

            ArrayList<?> mActions = (ArrayList<?>) filedmActions.get(remoteViews);

            Class innerClazz[] = RemoteViews.class.getDeclaredClasses();
            for (Class cls : innerClazz) {
                if ("ReflectionAction".equals(cls.getSimpleName())) {
                    classAction = cls;
                    Log.i("" + msg_count + " any 0c", "get private class - ReflectionAction()");

                    filedMethod = cls.getDeclaredField("methodName");
                    filedMethod.setAccessible(true);

                    filedValue = cls.getDeclaredField("value");
                    filedValue.setAccessible(true);

                    break;
                }
            }


            for (Object o : mActions) {
                Log.i("" + msg_count + " any 0d", "cycle0 for mActions" + o.getClass().getSimpleName());

                try {
                    String methord = "" + filedMethod.get(o);
                    String value = "" + filedValue.get(o);
                    Log.i("" + msg_count + " any 0e" + (methord.equals("setText")), methord + " " + value);
                    break;

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Field filedValue, filedMethod, filedmActions;
    private Class classAction;


    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);

        msg_count++;
        Log.i(TAG, "onNotificationPosted");

        Notification notification = sbn.getNotification();
        String packageName = sbn.getPackageName();
        RemoteViews notificationView = notification.contentView;
        genRemoteViewsDecoder(notificationView);

        ArrayList<String> list_nText = new ArrayList<>();
        String string_nText = "";
        String s;
        if (null != notification) {

            try {

                ArrayList<?> mActions = (ArrayList<?>) filedmActions.get(notificationView);
                for (Object o : mActions) {
                    try {
                        String value =  (String)(filedValue.get(o));
                        if ( "setText".equals((String)(filedMethod.get(o))) && null!=value) {
                            string_nText += value + "\n";
                            list_nText.add(value);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                Log.i("" + msg_count + " any 0e", string_nText);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        long time = sbn.getPostTime();
        String title = notification.extras.getString(Notification.EXTRA_TITLE);
        String text = notification.extras.getString(Notification.EXTRA_TEXT);

//        DbUtils.saveNotification(new NotificationInfo(packageName, title, text, time));

        String com_msg = (title + "\n" + text).replaceAll("\n", "");
        if (com_msg.replaceAll("\\s", "").length() > 0 && !packageName.contains("com.tumuyan.notification")) {
            switch (matchWhiteList(com_msg)) {
                case 2:
                    break;
                case 1:
                    flag = 2;
                    if (null == text) text = "";
                    if (text.replaceAll("\\s", "").length() == 0 && title.length() > 14)
                        text = "> " + title;
                    DbUtils.saveNotification(new NotificationInfo(packageName, title, text, time, flag));
                    break;
                case 0:
                    flag = 3;
                {
                    if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
                        cancelNotification(sbn.getKey());
                    } else {
                        cancelNotification(sbn.getPackageName(), sbn.getTag(), sbn.getId());
                    }
                    if (null == text) text = "";
                    if (text.replaceAll("\\s", "").length() == 0 && title.length() > 14)
                        text = "> " + title;
                }
                DbUtils.saveNotification(new NotificationInfo(packageName, title, text, time, flag));
                break;
                default: {
                    List<AppInfo> blackList = DbUtils.getApp();

                    for (AppInfo app : blackList) {

                        if (packageName.equals(app.getPackageName())) {
                            if (mode_read) {
                                ttsProxy.read(com_msg);
                            }

                            flag = 1;
                            Log.w(TAG, packageName + " Package命中：" + title + ": " + text);

                            if (matchsMessage(packageName, com_msg)) {
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

                    if (null == text) text = "";
                    if (null == title) title = "";
                    if (text.replaceAll("\\s", "").length() == 0 && title.length() > 14)
                        text = "> " + title;

                    DbUtils.saveNotification(new NotificationInfo(packageName, title, text, time, flag));
                    break;
                }
            }
        }
        flag = 0;
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);
    }


    private void block() {

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

    public int matchWhiteList(String message) {
        try {
            String MessageBlackList;
            SharedPreferences read = getSharedPreferences("setting", MODE_MULTI_PROCESS);
            //更新朗读模式
            mode_read = read.getBoolean("mode_read", true);
            Log.w("get readmode", "" + mode_read);

            // 匹配超级白名单
            MessageBlackList = read.getString(".2", "");
            String[] blacklist = splitRulls(MessageBlackList);

            if (null == blacklist) {
                Log.i("matchWhiteList", "Null white list2");
            } else {
                for (String black : blacklist) {
                    Log.w("black,message", black + ":" + message);
                    if (Pattern.matches(black, message)) {
                        return 2;
                    }
                }
            }
            // 匹配全局白名单
            MessageBlackList = read.getString(".1", "");
            blacklist = splitRulls(MessageBlackList);

            if (null == blacklist) {
                Log.i("matchWhiteList", "Null white list1");
            } else {
                for (String black : blacklist) {
                    if (Pattern.matches(black, message)) {
                        return 1;
                    }
                }
            }
            // 匹配全局黑名单
            MessageBlackList = read.getString(".0", "");
            blacklist = splitRulls(MessageBlackList);

            if (null == blacklist) {
                Log.i("matchWhiteList", "Null black list");
            } else {
                for (String black : blacklist) {
                    if (Pattern.matches(black, message)) {
                        return 0;
                    }
                }
            }
        } catch (Exception e) {
            Log.i("Servce getSetting()", "error");
            e.printStackTrace();
        }

        return -1;

    }


    public void getSetting() {
        try {
            String MessageBlackList;
            SharedPreferences read = getSharedPreferences("setting", MODE_MULTI_PROCESS);

            MessageBlackList = read.getString(".0", "");
            String BlackListF = (MessageBlackList);
            MessageBlackList = read.getString(".1", "");
            String WhiteList1 = (MessageBlackList);
            MessageBlackList = read.getString(".2", "");
            String WhiteList2 = (MessageBlackList);

        } catch (Exception e) {
            Log.i("Servce getSetting()", "error");
            e.printStackTrace();
        }
    }


    public Boolean matchsMessage(String packageName, String message) {
        Boolean result = false;
        SharedPreferences read = getSharedPreferences("setting", MODE_MULTI_PROCESS);
        int mode = read.getInt("mode", 0);


        if (mode < 0) {
            return false;
        } else {
            String MessageBlackList = read.getString(packageName, "");

            if (mode > 0) {
                MessageBlackList = MessageBlackList + "\n" + read.getString(packageName + "." + mode, "");
            }
            Log.i(packageName + " rules:", MessageBlackList);
            String[] blacklist = splitRulls(MessageBlackList);

            if (null == blacklist) {
                flag = 2;
                Log.i("MatchMessage", "Null list");
                result = true;
            } else {
                for (String black : blacklist) {
                    if (Pattern.matches(black, message)) {
                        //matches的规则是用正则表达式，必须完全匹配在字符串上；
                        result = true;
                        flag = 3;
                    }
                    Log.i("MatchMessage" + black, message + result);
                }
            }

        }


        return result;

    }


    private String[] splitRulls(String MessageBlackList) {
        try {

            MessageBlackList = MessageBlackList.replaceAll("\n+", "\n");

            if (MessageBlackList.length() == 0 || MessageBlackList == "\n") {
                return null;
            } else {

                String[] result = MessageBlackList.split("\n");
                List<String> rule = new ArrayList<>();

                for (String cache : result) {
                    if (cache.length() != 0 && cache != "\n") {
                        if (cache.replaceAll("[\\u0021-\\u0024\\u0026-\\u002b\\u002d\\u002f\\u003a-\\u003f\\u005b-\\u0061\\u007b-\\u007e\\\\]+", "").length() == cache.length()) {
                            //         减少了匹配的字符，%和.,
                            //         if(cache.replaceAll("[\\u0021-\\u002f\\u003a-\\u003f\\u005b-\\u0061\\u007b-\\u007e\\\\]+","").length()==cache.length()){
                            rule.add(".*" + cache.replaceAll("\\s", ".*") + ".*");
                        } else {
                            rule.add(cache);
                        }
                    }
                }

                return rule.toArray(new String[rule.size()]);
            }
        } catch (Exception e) {
            Log.i("Split", "error");
            e.printStackTrace();
        }
        return null;
    }


}
