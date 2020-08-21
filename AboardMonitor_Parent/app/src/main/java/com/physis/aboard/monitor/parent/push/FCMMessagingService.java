package com.physis.aboard.monitor.parent.push;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.physis.aboard.monitor.parent.IntroActivity;
import com.physis.aboard.monitor.parent.R;
import com.physis.aboard.monitor.parent.storage.ClientPreference;

import org.json.JSONException;
import org.json.JSONObject;

public class FCMMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FCMMessagingService";

    public final static String RECEIVED_NOTIFICATION = "RECEIVED_NOTIFICATION";
    public final static String RECEIVED_MESSAGE = "RECEIVED_MESSAGE";

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.e(TAG, "onNewToken : " + token);
        ClientPreference.setPushToken(this, token);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        // Check if message contains a data payload.
        String title = null;
        String msg = null;
        if (remoteMessage.getData().size() > 0) {
            try {
                JSONObject obj = new JSONObject(remoteMessage.getData());
                title = obj.getString("title");
                msg = obj.getString("body");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (remoteMessage.getNotification() != null) {
            title = remoteMessage.getNotification().getTitle();
            msg = remoteMessage.getNotification().getBody();
        }
        Log.e(TAG, "onMessageReceived : " + msg);
        sendBroadcast(new Intent(RECEIVED_NOTIFICATION).putExtra(RECEIVED_MESSAGE, msg));
        sendNotification(title, msg);
    }
    // [END receive_message]

    /**
     * Handle time allotted to BroadcastReceivers.
     */
    private void handleNow() {
        Log.d(TAG, "Short lived task is done.");
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     */
    private void sendNotification(String title, String msg) {
        Intent intent = new Intent(this, IntroActivity.class);
        intent.putExtra(RECEIVED_MESSAGE, msg);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        String[] datas = msg.split(",");
        String outputMsg = datas[1] + "\n";
        if(datas[0].equals("1"))
            outputMsg += "원생이 통학 차량이 탑승하였습니다.";
        else
            outputMsg += "원생이 통학 차량에서 하차하였습니다.";

        long sec = System.currentTimeMillis();
        String channelID = getString(R.string.notification_channel_id) + sec;
        NotificationCompat.Builder nBuilder =
                new NotificationCompat.Builder(this, channelID)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(title)
                        .setContentText(outputMsg)
                        .setAutoCancel(true)
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        .setContentIntent(pendingIntent);

        NotificationManager nManager =(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create channel to show notifications.
            String channelName = getString(R.string.notification_channel_name) + sec;
            NotificationChannel channel = new NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_HIGH);
            assert nManager != null;
            nManager.createNotificationChannel(channel);
        }
        assert nManager != null;
        nManager.notify(0 /* ID of notification */, nBuilder.build());
    }
}