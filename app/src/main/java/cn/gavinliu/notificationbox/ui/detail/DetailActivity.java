package cn.gavinliu.notificationbox.ui.detail;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import cn.gavinliu.notificationbox.R;
import cn.gavinliu.notificationbox.ui.setting.SettingActivity;
import cn.gavinliu.notificationbox.utils.DbUtils;

/**
 * Created by Gavin on 2016/10/11.
 *
 */

// detailActivity即点开应用就看到的明细列表界面
public class DetailActivity extends AppCompatActivity {
   public String appName="";
   public String packageName="";

    public    Button SaveMessageBlackList;
    public EditText EditMessageBlackList;
    public EditText EditMessageBlackList2;
    public EditText EditMessageBlackList1;
    public ImageButton ImageButtonSave;

    public EditText Edit1;
    public EditText Edit2;
    public ImageButton ImageButtonQuery;
    public  ImageButton ImageButtonQuery2;
    public  ImageButton ImageButtonSave2;

    public EditText EditMessageBlackListF;
    public EditText EditMessageWhiteList2;
    public EditText EditMessageWhiteList1;

    public ImageButton ImageButtonSave3;
    public ImageButton ImageButtonQuery3;
    public ImageButton ImageButtonSaveF;

    View ViewMessageBlackList;
    View ViewMessageQuery;
    View ViewMessageList;

    public String query1="";
    public String query2="";

    public Boolean FOR_FULL_APP=false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_list_new);

        DetailFragment detailFragment = (DetailFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_applist);

        if (detailFragment == null) {
            Intent intent = getIntent();
            Bundle bundle = intent.getExtras();
            appName=bundle.getString("appName");
            packageName=bundle.getString("packageName");
            if(packageName.length()<4) FOR_FULL_APP =true;

            detailFragment = DetailFragment.newInstance( appName,packageName);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.content, detailFragment)
                    .commit();
        }
        new DetailPresenter(detailFragment,query1,query2);
     //   new DetailPresenter(detailFragment);

        ViewMessageQuery=(View)findViewById(R.id.viewMessageQurey);
        ViewMessageBlackList=(View)findViewById(R.id.viewMessageBlackList);
        ViewMessageList=(View)findViewById(R.id.viewMessageList);

        Edit1=(EditText)findViewById(R.id.editMessageQuery1);
        Edit2=(EditText)findViewById(R.id.editMessageQuery2);
        ImageButtonQuery=(ImageButton)findViewById(R.id.imageButtonQuery);

        ImageButtonQuery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                query1=Edit1.getText().toString();
                query2=Edit2.getText().toString();
                reload();
                Toast.makeText(DetailActivity.this,"Finished",Toast.LENGTH_SHORT).show();
//                query1="";
//                query2="";
            }
        });

        EditMessageBlackList=(EditText)findViewById(R.id.editMessageBlackList);
        EditMessageBlackList1=(EditText)findViewById(R.id.editMessageBlackList1);
        EditMessageBlackList2=(EditText)findViewById(R.id.editMessageBlackList2);

        EditMessageBlackListF=(EditText)findViewById(R.id.editMessageBlackListF);
        EditMessageWhiteList1=(EditText)findViewById(R.id.editMessageWhiteListF1);
        EditMessageWhiteList2=(EditText)findViewById(R.id.editMessageWhiteListF2);
        getSetting();

        ImageButtonSave=(ImageButton)findViewById(R.id.imageButtonSave);
        ImageButtonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                putSetting();
                Toast.makeText(DetailActivity.this,"Saved",Toast.LENGTH_SHORT).show();
            }
        });

        ImageButtonSaveF=(ImageButton)findViewById(R.id.imageButtonSaveF);
        ImageButtonSaveF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                putSetting();
                Toast.makeText(DetailActivity.this,"Saved",Toast.LENGTH_SHORT).show();
            }
        });

        ImageButtonQuery2=(ImageButton)findViewById(R.id.imageButtonQuery2);
        ImageButtonSave2=(ImageButton)findViewById(R.id.imageButtonSave2);

        ImageButtonQuery2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ViewMessageBlackList.setVisibility(View.GONE);
                ViewMessageQuery.setVisibility(View.VISIBLE);
            }
        });

        ImageButtonSave2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ViewMessageBlackList.setVisibility(View.VISIBLE);
                ViewMessageQuery.setVisibility(View.GONE);
            }
        });

        ImageButtonQuery3=(ImageButton)findViewById(R.id.imageButtonQuery3);
        ImageButtonSave3=(ImageButton)findViewById(R.id.imageButtonSave3);

        ImageButtonQuery3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ViewMessageList.setVisibility(View.GONE);
                ViewMessageQuery.setVisibility(View.VISIBLE);
            }
        });


        ImageButtonSave3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ViewMessageList.setVisibility(View.VISIBLE);
                ViewMessageQuery.setVisibility(View.GONE);
            }
        });

        if(FOR_FULL_APP){
            ViewMessageBlackList.setVisibility(View.GONE);
            ViewMessageList.setVisibility(View.GONE);
            ViewMessageQuery.setVisibility(View.VISIBLE);
                ImageButtonSave2.setVisibility(View.GONE);
                ImageButtonSave3.setVisibility(View.VISIBLE);

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_record, menu);
        menu.findItem(R.id.menu_rules).setVisible(false);
        menu.findItem(R.id.menu_query).setVisible(false);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case R.id.menu_delete:
            {
                query1=Edit1.getText().toString();
                query2=Edit2.getText().toString();
                reload();
                delete();
                return true;
            }
            case R.id.menu_query:{

                return true;
            }
            case R.id.menu_rules :{

                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public void delete(){
        AlertDialog.Builder d=new AlertDialog.Builder(this);
        d.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                DbUtils.removeRedord(packageName,query1,query2);
                reload();
                Toast.makeText(DetailActivity.this,"Finished",Toast.LENGTH_SHORT).show();

            }
        });
        d.setNegativeButton(R.string.channel,null);
//        d.setCancelable(true);
            d.setMessage(R.string.delete_message);
            d.show();
    }


    public  void getSetting() {
        try{
            String MessageBlackList;
            SharedPreferences read = getSharedPreferences("setting",MODE_MULTI_PROCESS);
            MessageBlackList = read.getString(packageName, "");
            EditMessageBlackList.setText(MessageBlackList);

            MessageBlackList = read.getString(packageName+".1", "");
            EditMessageBlackList1.setText(MessageBlackList);

            MessageBlackList = read.getString(packageName+".2", "");
            EditMessageBlackList2.setText(MessageBlackList);


            MessageBlackList = read.getString(".0", "");
            EditMessageBlackListF.setText(MessageBlackList);
            MessageBlackList = read.getString(".1", "");
            EditMessageWhiteList1.setText(MessageBlackList);
            MessageBlackList = read.getString(".2", "");
            EditMessageWhiteList2.setText(MessageBlackList);

            }catch(Exception e) {
                Log.i(packageName,"error");
                e.printStackTrace();

            }
    }

    public void putSetting(){
  //      SharedPreferences.Editor editor = getSharedPreferences("setting", MODE_PRIVATE).edit();
        String MessageBlackList;
        SharedPreferences.Editor editor = getSharedPreferences("setting", MODE_MULTI_PROCESS).edit();
        MessageBlackList=EditMessageBlackList.getText().toString();
        editor.putString(packageName, MessageBlackList);

        MessageBlackList=EditMessageBlackList1.getText().toString();
        editor.putString(packageName+".1", MessageBlackList);

        MessageBlackList=EditMessageBlackList2.getText().toString();
        editor.putString(packageName+".2", MessageBlackList);

        MessageBlackList=EditMessageBlackListF.getText().toString();
        editor.putString(".0", MessageBlackList);

        MessageBlackList=EditMessageWhiteList1.getText().toString();
        editor.putString(".1", MessageBlackList);

        MessageBlackList=EditMessageWhiteList2.getText().toString();
        editor.putString(".2", MessageBlackList);

        editor.commit();
        getSetting();
    }

    public void reload(){
        DetailFragment detailFragment = (DetailFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_applist);
        detailFragment = DetailFragment.newInstance( appName,packageName);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content, detailFragment)
                    .commit();
        Log.w("query1"+query1,"query2"+query2);
        new DetailPresenter(detailFragment,query1,query2);

    }
}
