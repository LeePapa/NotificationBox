package cn.gavinliu.notificationbox.ui.main;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import cn.gavinliu.notificationbox.R;
import cn.gavinliu.notificationbox.service.NotificationListenerService;
import cn.gavinliu.notificationbox.ui.detail.DetailActivity;
import cn.gavinliu.notificationbox.ui.setting.SettingActivity;
import cn.gavinliu.notificationbox.utils.CommonUtils;

public class MainActivity extends AppCompatActivity {

    FloatingActionButton mFloatingActionButton;
    private MainPresenter mMainPresenter;
    MenuItem mode__1,mode_0,mode_1,mode_2;
    int mode=0;
    boolean mode_read=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mFloatingActionButton = (FloatingActionButton) findViewById(R.id.fab);
        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMainPresenter.addApp();
            }
        });

        MainFragment mainFragment = (MainFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_main);

        if (mainFragment == null) {
            mainFragment = MainFragment.newInstance();

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.content, mainFragment)
                    .commit();
        }

        mMainPresenter = new MainPresenter(mainFragment);
        initPermission();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!CommonUtils.checkNotificationReadPermission(this)) {
            Snackbar.make(mFloatingActionButton, getText(R.string.no_read_notification_promission), Snackbar.LENGTH_INDEFINITE)
                    .setAction(getText(R.string.action_settings), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                            startActivity(intent);
                        }
                    })
                    .show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);




        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        SharedPreferences read = getSharedPreferences("setting",MODE_MULTI_PROCESS);
         mode=read.getInt("mode",0);
         mode_read=read.getBoolean("mode_read",true);

        switch (mode){
            case -1:
                menu.findItem(R.id.mode__1).setChecked(true);
                break;
            case 0:
                menu.findItem(R.id.mode_0).setChecked(true);
                break;
            case 1:
                menu.findItem(R.id.mode_1).setChecked(true);
                break;
            case 2:
                menu.findItem(R.id.mode_2).setChecked(true);
                break;
        }

        read_off=menu.findItem(R.id.mode_read_off);
        read_on=menu.findItem(R.id.mode_read_on);

        if (mode_read){
           read_off .setVisible(false) ;
            read_on.setVisible(true) ;
        }else {
           read_off.setVisible(true) ;
            read_on.setVisible(false) ;
        }
        return true;
    }
    MenuItem read_off,read_on;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();


        switch (id){
            case R.id.action_settings:
            {
                Intent intent = new Intent(this, SettingActivity.class);
                startActivity(intent);
                return true;
            }
/*            case R.id.action_allrecords:{
                Intent intent = new Intent(this, DetailActivity.class);
                intent.putExtra("appName", "");
                intent.putExtra("packageName", "");
                startActivity(intent);
                return true;
            }*/
            case R.id.mode_0 :{
                mode=0;
                break;
            }
            case R.id.mode_1 :{
                mode=1;
                break;
            }
            case R.id.mode_2 :{
                mode=2;
                break;
            }
            case R.id.mode__1 :{
                mode=-1;
                break;
            }


            case R.id.mode_read_off:{
                read_off.setVisible(false);
                read_on.setVisible(true);
                SharedPreferences.Editor editor = getSharedPreferences("setting", MODE_MULTI_PROCESS).edit();
                editor.putBoolean("mode_read",true);
                editor.commit();

                Log.w("pkg name for this",getPackageName());

                createNotificationCMD("mode_read_on");
                break;
            }
            case R.id.mode_read_on:{
                read_off.setVisible(true);
                read_on.setVisible(false);
                SharedPreferences.Editor editor = getSharedPreferences("setting", MODE_MULTI_PROCESS).edit();
                editor.putBoolean("mode_read",false);
                editor.commit();
                createNotificationCMD("mode_read_off");
                break;
            }

            case R.id.action_stop:{
                createNotificationCMD("action_stop");
                break;
            }

        }


        if(item.getGroupId() == R.id.group_mode){

            //单选--排序组
            item.setChecked(true);
            SharedPreferences.Editor editor = getSharedPreferences("setting", MODE_MULTI_PROCESS).edit();
            editor.putInt("mode",mode);
            editor.commit();

        }



        return super.onOptionsItemSelected(item);
    }




    private void createNotificationCMD(String text) {

        Notification.Builder notifyBuilder = new Notification.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText(text)
                .setDefaults(Notification.DEFAULT_LIGHTS);

        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification notification = notifyBuilder.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        nm.notify(R.string.app_name, notification);
    }


    private void toggleNotificationListenerService() {
        PackageManager pm = getPackageManager();
        pm.setComponentEnabledSetting(new ComponentName(this, cn.gavinliu.notificationbox.service.NotificationListenerService.class),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        pm.setComponentEnabledSetting(new ComponentName(this, cn.gavinliu.notificationbox.service.NotificationListenerService.class),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }

    public static Intent getExplicitIntent(Context context, Intent implicitIntent) {
        // Retrieve all services that can match the given intent
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resolveInfo = pm.queryIntentServices(implicitIntent, 0);
     //   Make sure only one match was found
        if (resolveInfo == null || resolveInfo.size() != 1) {
            return null;
        }
        // Get component info and create ComponentName
        ResolveInfo serviceInfo = resolveInfo.get(0);
        String packageName = serviceInfo.serviceInfo.packageName;
        String className = serviceInfo.serviceInfo.name;
        ComponentName component = new ComponentName(packageName, className);
        // Create a new intent. Use the old one for extras and such reuse
        Intent explicitIntent = new Intent(implicitIntent);
        // Set the component to be explicit
        explicitIntent.setComponent(component);
        return explicitIntent;
    }



    /**
     * android 6.0 以上需要动态申请权限
     */
    private void initPermission() {
        String[] permissions = {
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.MODIFY_AUDIO_SETTINGS,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_SETTINGS,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE
        };

        ArrayList<String> toApplyList = new ArrayList<String>();

        for (String perm : permissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, perm)) {
                toApplyList.add(perm);
                // 进入到这里代表没有权限.
            }
        }
        String[] tmpList = new String[toApplyList.size()];
        if (!toApplyList.isEmpty()) {
            ActivityCompat.requestPermissions(this, toApplyList.toArray(tmpList), 123);
        }

    }
}
