package cn.gavinliu.notificationbox.ui.detail;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import cn.gavinliu.notificationbox.R;

/**
 * Created by Gavin on 2016/10/11.
 */

public class DetailActivity extends AppCompatActivity {
   public String appName;
    public    String MessageBlackList;
    public    Button SaveMessageBlackList;
    public EditText EditMessageBlackList;
    View ViewMessageBlackList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_applist);

        DetailFragment detailFragment = (DetailFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_applist);

        if (detailFragment == null) {
            Intent intent = getIntent();
            Bundle bundle = intent.getExtras();
            appName=bundle.getString("packageName");

            detailFragment = DetailFragment.newInstance(bundle.getString("appName"), bundle.getString("packageName"));

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.content, detailFragment)
                    .commit();
        }

        new DetailPresenter(detailFragment);

        EditMessageBlackList=(EditText)findViewById(R.id.editMessageBlackList);
        SaveMessageBlackList=(Button)findViewById(R.id.saveMessageBlackList);

        getSetting();

        SaveMessageBlackList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                putSetting();
                Toast.makeText(DetailActivity.this,"Saved",Toast.LENGTH_SHORT).show();
            }
        });

    }


    public  void getSetting() {
        try{
            Log.i("get",""+appName);
        SharedPreferences read = getSharedPreferences("setting",MODE_PRIVATE);
        MessageBlackList = read.getString(appName, "");
        EditMessageBlackList.setText(MessageBlackList);
        Log.i(appName,MessageBlackList);
        }catch(Exception e) {
            Log.i(appName,"error");
            e.printStackTrace();

        }
    }

    public void putSetting(){
        SharedPreferences.Editor editor = getSharedPreferences("setting", MODE_PRIVATE).edit();
        MessageBlackList=EditMessageBlackList.getText().toString();
        editor.putString(appName, MessageBlackList);
        editor.commit();
        getSetting();
    }
}
