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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.gavinliu.notificationbox.R;
import cn.gavinliu.notificationbox.baidutts.ttsProxy;
import cn.gavinliu.notificationbox.model.AppInfo;
import cn.gavinliu.notificationbox.model.NotificationInfo;
import cn.gavinliu.notificationbox.msg.TextBook;
import cn.gavinliu.notificationbox.msg.imTextBook;
import cn.gavinliu.notificationbox.msg.musicTextBook;
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
    boolean ttsInit=true;

    @Override
    public void onCreate() {
        super.onCreate();
        //  toggleNotificationListenerService();
        ttsProxy = new ttsProxy(getApplicationContext());
        //   init.start();
        Log.i(TAG, "onCreate");
    }

    Thread init=new Thread(new Runnable() {
        @Override
        public void run() {
            ttsProxy = new ttsProxy(getApplicationContext());
            ttsInit=false;
        }
    });


    @Override
    public void onDestroy() {
        super.onDestroy();
        ttsProxy.release();
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
       // if(ttsInit) return;

        msg_count++;
        Log.i(TAG, "onNotificationPosted");

        Notification notification = sbn.getNotification();
        String packageName = sbn.getPackageName();

        long time = sbn.getPostTime();
        String title = notification.extras.getString(Notification.EXTRA_TITLE);
        String text = notification.extras.getString(Notification.EXTRA_TEXT);

        boolean null_text = false;

        if (null == title) title = "";

        if (null == text) text = "";

        if ((title + text).replaceAll("\\s", "").length() < 1)
            null_text = true;

        if (null_text) {

            RemoteViews notificationView = notification.contentView;
            genRemoteViewsDecoder(notificationView);

            LinkedList<String> list_nText = new LinkedList<>();
            String string_nText = "";

            if (null != notification) {

                try {
                    ArrayList<?> mActions = (ArrayList<?>) filedmActions.get(notificationView);
                    for (Object o : mActions) {
                      //  if(o instanceof android.widget.RemoteViews$ReflectionAction )
                        if(!"ReflectionAction".equals(o.getClass().getSimpleName()) ) continue;
                        try {
                            if ("setText".equals((String) (filedMethod.get(o)))) {
                            //    String value = (String) (filedValue.get(o));
                                //  由于收到短信时，获取的是spannableString 造成错误。故暂时使用如下方法
                                String value = ""+ (filedValue.get(o));
                                if (null == value) continue;
                                if (value.matches("null")) continue;
                                if(value.matches("\\s+")) continue;
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

            if (list_nText.size() > 0) {
                null_text = false;
                title = list_nText.remove();
                for (String s : list_nText) {
                    text += s + "\n";
                }
            }
        }

//        DbUtils.saveNotification(new NotificationInfo(packageName, title, text, time));

        if (!null_text && !packageName.contains("com.tumuyan.notification")) {
            title = title.trim();
            text = text.trim();
            go_reader(packageName,title,text);
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);
    }

    ArrayList<String> list_OldBook=new ArrayList<>();

    private void go_reader(String packageName,String title,String text) {
        SharedPreferences read = getSharedPreferences("setting", MODE_MULTI_PROCESS);
        //更新朗读模式
        mode_read = read.getBoolean("mode_read", true);
        Log.w("get readmode", "" + mode_read);
        if(!mode_read) return;

        if(text.matches("[^\n]{1,50}正在后台运行"))
            return ;

        if(text.equals("触摸即可了解详情或停止应用。"))
            return ;

        long time=System.currentTimeMillis();

        List<AppInfo> blackList = DbUtils.getApp();

        for (AppInfo app : blackList) {

            if (packageName.equals(app.getPackageName())) {
                Log.w(TAG, packageName + " Package命中：" + title + ": " + text);

                String com_msg;
                if(title.length()>0)
                     com_msg=title;
                else
                     com_msg=text.substring(0,text.indexOf("\n"));

                if (matchsMessage(packageName, com_msg)) {
                    ttsProxy.read(getTextBook(title, text, packageName),new_speaker);
                    DbUtils.saveNotification(new NotificationInfo(packageName, title, text, time, 1));
                }else {
                    DbUtils.saveNotification(new NotificationInfo(packageName, title, text, time, -1));
                }

                return;
            }
        }
        DbUtils.saveNotification(new NotificationInfo(packageName, title, text, time, 0));

    }


    private String TextTool_num2words(String input){
        // 从文本数字混合转换为汉字。通过这种方式纠正数字读音，避免ED2 读为ED two 支持前后缀，但是如果出现两段数字，无法正确解析
        String number=input.replaceAll("[^0-9]*","");
        String words="";
        switch (number){
            case "0":
                words="零";
                break;
            case "1":
                words="一";
                break;
            case "2":
                words="二";
                break;
            case "3":
                words="三";
                break;
            case "4":
                words="四";
                break;
            case "5":
                words="五";
                break;
            case "6":
                words="六";
                break;
            case "7":
                words="七";
                break;
            case "8":
                words="八";
                break;
            case "9":
                words="九";
                break;
            default:
                Log.w("TextTool_num2words","oped error -"+number+"-"+words);

        }

        return input.replace(number,words);

    }

    private Pattern patterMusicOriginal=Pattern.compile("《[^《》]+》(\\s)?(第[0-9一二三四五六七八九十零壹贰叁肆伍陆柒]+季)?(\\s)?(OP|ED|片头|片头曲|主题曲|角色歌|OST|片尾|片尾曲|插入歌)?(\\d+)?");
    private Pattern patternMusicOPED=Pattern.compile("(OP|ED)(\\d+)");


    private TextBook old_im_book,old_music_book;
    private boolean new_speaker;

    private boolean remove_repeat(String s){
        if(list_OldBook.contains(s)){
            return true;
        }
        list_OldBook.add(0,s);
        return false;
    }

    private String getTextBook(String title, String text, String pkg) {

        new_speaker=false;

        //避免解锁时重新绘制ui导致的短信重复播报。其他app待添加
        if(pkg.matches(".*mms")){
            if(remove_repeat(title+text)){
                return "";
            }
        }

        new_speaker=true;
        if(pkg.matches(".*music")){
            if(pkg.equals("com.netease.cloudmusic"))
                text=text.substring(0, text.indexOf(" - "));
            musicTextBook mu=new musicTextBook(title,text,pkg);
            old_music_book=mu.getTextBook(old_music_book);
            return mu.getString();
        }

        switch (pkg) {
  /*             case "com.netease.cloudmusic":
                musicTextBook mu=new musicTextBook(title,text.substring(0, text.indexOf(" - ")),pkg);
                old_music_book=mu.getTextBook(old_music_book);
                return mu.getString();

             Matcher m=patterMusicOriginal.matcher(title);
                String original="";

                if(m.find()){
                    original=m.group(0);
                    title=title.replace(original,"").replaceFirst("\\(\\s\\)","");
                    String OPED=original.replaceFirst("[^(OP)(ED)]+","");
                    original=original.replace(OPED,"");
                    OPED=TextTool_num2words(OPED);
                    Log.w("music suffix OPED",original+"=>"+OPED);
                    original+="，"+OPED;
                }


                String[] song_tag=title.split("[()~]");
                String new_title="";
                for(String tag:song_tag){
                    String s=tag.replaceAll("(\\s|_)+"," ");
                    if(s.contains(" "))
                    {
                        if(s.replaceAll("\\s","").matches("[A-Z]+")){
                            //如果全部大写了，那么有可能是错误的拼写，需要转换小写以免误读
                            new_title+=s.toLowerCase();
                            continue;
                        }
                    }
                    if(s.replace(" ","").length()>0)
                        new_title+="，"+s;
                }
                                return new_title + original +"。"+  text.substring(0, text.indexOf(" - "));
                */



/*

                // 先把（）括号全部替换为西文半角
                title= title.replaceAll("(\\(|（|（|（)([^(|（|（|（]*)(\\)|）|）|）)", "($2)")
                     //   .replaceFirst("(OP|ED)(\\d+)",TextBookTool_num2words($1 $2))
                        + "。  " + text.substring(0, text.indexOf(" - "));
*/


            case "com.tencent.mm":{
                imTextBook im=new imTextBook(title,text.replaceFirst("^\\[\\d+条\\]",""),":");
                new_speaker=im.isDifferentSpeaker(old_im_book);
                old_im_book=im.getTextBook();
                return  im.getString();
            }


            case "com.tencent.mobileqq":{
                imTextBook im=new imTextBook(title.replaceFirst("\\(\\d+条新消息\\)$",""),text,":");
                new_speaker=im.isDifferentSpeaker(old_im_book);
                old_im_book=im.getTextBook();
                return  im.getString();
            }

        }


        return title+"，"+text;
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
