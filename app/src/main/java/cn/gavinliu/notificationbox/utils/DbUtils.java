package cn.gavinliu.notificationbox.utils;

import android.util.Log;

import com.litesuits.orm.db.assit.QueryBuilder;
import com.litesuits.orm.db.assit.WhereBuilder;

import java.util.List;

import cn.gavinliu.notificationbox.NotificationBoxApp;
import cn.gavinliu.notificationbox.model.AppInfo;
import cn.gavinliu.notificationbox.model.NotificationInfo;

/**
 * Created by Gavin on 16-10-15.
 */

public class DbUtils {

    public static void saveNotification(NotificationInfo info) {
        if (info.getTitle() == null || info.getText() == null) return;
        NotificationBoxApp.getLiteOrm().save(info);
    }
/*

    public static List<NotificationInfo> getNotification(String packageName) {
       //如果输入的packageName为空，则返回全部的数据
        // Log.i("getNotification","runing"+packageName+"end");
*//*        if(null==packageName){
            Log.i("package","space1");
            return NotificationBoxApp.getLiteOrm().query(new QueryBuilder<NotificationInfo>(NotificationInfo.class).orderBy("time desc"));
        }else *//*if(4>packageName.length()){
            Log.i("package","space2");
            return NotificationBoxApp.getLiteOrm().query(new QueryBuilder<NotificationInfo>(NotificationInfo.class).orderBy("time desc"));
        }else{
            Log.i("package",""+packageName.length());
            return NotificationBoxApp.getLiteOrm().query(new QueryBuilder<NotificationInfo>(NotificationInfo.class)
                    .where("packageName = ?", packageName).orderBy("time desc"));
        }
    }

 */   public static List<NotificationInfo> getNotification(String packageName,String qurey1,String qurey2) {



        if(4>packageName.length()){

            return NotificationBoxApp.getLiteOrm().query(new QueryBuilder<NotificationInfo>(NotificationInfo.class)
                    .where("title LIKE ?", "%"+qurey1+"%")
                    .whereAppendAnd()
                    .whereAppend("text LIKE ?", "%"+qurey2+"%")
                            .orderBy("time desc"));
        }else{

            return NotificationBoxApp.getLiteOrm().query(new QueryBuilder<NotificationInfo>(NotificationInfo.class)
                    .where("packageName = ?", packageName)
                    .whereAppendAnd()
                    .whereAppend("title LIKE ?", "%"+qurey1+"%")
                    .whereAppendAnd()
                    .whereAppend("text LIKE ?", "%"+qurey2+"%")
                    .orderBy("time desc"));
        }
    }

    public static int removeRedord(String packageName,String qurey1,String qurey2) {
        if(4>packageName.length()){

            return NotificationBoxApp.getLiteOrm().delete(new WhereBuilder(NotificationInfo.class)
                    .where("title LIKE ?", "%"+qurey1+"%")
                    .and()
                    .where("text LIKE ?", "%"+qurey2+"%")
            );
        }else{

            return NotificationBoxApp.getLiteOrm().delete(new WhereBuilder(NotificationInfo.class)
                    .where("packageName = ?", packageName)
                    .and()
                    .where("title LIKE ?", "%"+qurey1+"%")
                    .and()
                    .where("text LIKE ?", "%"+qurey2+"%")
            );
        }
    }
    public static int removeRedord(int count) {
            return  NotificationBoxApp.getLiteOrm().delete(NotificationInfo.class,1,count,"_id");
    }

    public static List<AppInfo> getApp() {
        return NotificationBoxApp.getLiteOrm().query(AppInfo.class);
    }

    public static void saveApp(AppInfo info) {
        NotificationBoxApp.getLiteOrm().save(info);
    }

    public static void deleteApp(AppInfo info) {
        NotificationBoxApp.getLiteOrm().delete(new WhereBuilder(AppInfo.class)
                .where("packageName = ?", info.getPackageName()));
    }

    public static void rebuildRedord(){
     NotificationBoxApp.getLiteOrm().deleteAll(NotificationInfo.class);
    }

}
