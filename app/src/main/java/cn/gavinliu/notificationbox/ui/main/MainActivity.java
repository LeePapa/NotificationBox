package cn.gavinliu.notificationbox.ui.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

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
        return true;
    }

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
            case R.id.action_allrecords:{
                Intent intent = new Intent(this, DetailActivity.class);
                intent.putExtra("appName", "");
                intent.putExtra("packageName", "");
                startActivity(intent);
                return true;
            }
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
        }


        if(item.getGroupId() == R.id.group_mode){

            //单选--排序组
            item.setChecked(true);
            SharedPreferences.Editor editor = getSharedPreferences("setting", MODE_MULTI_PROCESS).edit();
            editor.putInt("mode",mode);
            editor.commit();

        }


/*
        if (id == R.id.action_startservice) {

            Log.d("onClick:", "Start Service");
            Intent startIntent = new Intent(this,cn.gavinliu.notificationbox.service.NotificationListenerService.class);
         //   startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            this.startService(startIntent);
        }


*/



        return super.onOptionsItemSelected(item);
    }
}
