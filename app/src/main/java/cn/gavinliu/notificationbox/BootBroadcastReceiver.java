package cn.gavinliu.notificationbox;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import cn.gavinliu.notificationbox.service.NotificationListenerService;
import cn.gavinliu.notificationbox.ui.main.MainActivity;

/*
public class BootBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
*/


public class BootBroadcastReceiver extends BroadcastReceiver{
    private static final String action_boot = "android.intent.action.BOOT_COMPLETED";
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(action_boot)) {
            Log.i("onReceive:", "Boot system");
            Intent startIntent = new Intent(context, NotificationListenerService.class);
            startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //    context.startActivity(startIntent);
            context.startService(startIntent);
        }
    }


}
