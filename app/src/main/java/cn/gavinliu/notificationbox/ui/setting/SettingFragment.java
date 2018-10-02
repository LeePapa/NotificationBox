package cn.gavinliu.notificationbox.ui.setting;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.widget.Toast;

import cn.gavinliu.notificationbox.R;
import cn.gavinliu.notificationbox.utils.DbUtils;

/**
 * Created by Gavin on 2016/10/25.
 */

public class SettingFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.setting);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        findPreference("permission").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                startActivity(intent);
                return true;
            }
        });

        findPreference("rebuildRecod").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                DbUtils.rebuildRedord();
                Toast.makeText(getContext(),R.string.record_rebuilded,Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        findPreference("openSource").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                intent.setData(Uri.parse("https://github.com/tumuyan/NotificationBox"));
                startActivity(intent);
                return true;
            }
        });
        findPreference("based_on").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                intent.setData(Uri.parse("https://github.com/gavinliu/NotificationBox"));
                startActivity(intent);
                return true;
            }
        });
    }
}
