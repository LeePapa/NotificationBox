package cn.gavinliu.notificationbox.ui.main;

import android.content.Intent;
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
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingActivity.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.action_allrecords) {

            Intent intent = new Intent(this, DetailActivity.class);
            intent.putExtra("appName", "");
            intent.putExtra("packageName", "");
            startActivity(intent);

 /*           Intent intent = new Intent(this, DetailActivity.class);
            startActivity(intent);
            return true;*/
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
