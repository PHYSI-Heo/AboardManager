package com.physis.aboard.monitor.manager.utils;

import android.os.AsyncTask;
import android.util.Log;

import com.physis.aboard.monitor.manager.data.ClientInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class FCMMessageTransmitter extends AsyncTask<Void, Void, Void> {

    private static final String TAG = "PushMessage";

    private static final String FCM_MESSAGE_URL = "https://fcm.googleapis.com/fcm/send";
    private static final String SERVER_KEY = "AAAAnsUVYEI:APA91bGp1LH57Xw-Ib-P5pBGCUEX_K8b0Nan37hTClokbhhGsIouoXQePEyRkCzydTn1RJfzrcuwRrZDAaTl_qvDdPA8pyRBBchxIiGUeStdPZaHrzFjV6vYpRP9zRxd7vxrZeY0j_oV";

    private ClientInfo clientInfo;

    public FCMMessageTransmitter(ClientInfo info){
        this.clientInfo = info;
    }

    private JSONObject setPushMessage(){

        JSONObject root = new JSONObject();
        try {
            JSONObject bodyObj = new JSONObject();
            String msgProtocol = clientInfo.getState() + "," + clientInfo.getTime() + ","
                    + clientInfo.getLatitude() + "," + clientInfo.getLongitude();
            bodyObj.put("body", msgProtocol);
            bodyObj.put("title", "승하차 알림");

//            JSONObject notification = new JSONObject();
//            notification.put("body", bodyObj);
//            notification.put("title", "Boarding Notification");
//            root.put("notification", notification);

            root.put("data", bodyObj);
            root.put("to", clientInfo.getToken());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return root;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            URL url = new URL(FCM_MESSAGE_URL);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();

            if(conn == null)
                return null;

            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(5000);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.addRequestProperty("Authorization", "key=" + SERVER_KEY);
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Content-type", "application/json");

            OutputStreamWriter wr= new OutputStreamWriter(conn.getOutputStream());
            wr.write(setPushMessage().toString());
            wr.flush();

            int resCode = conn.getResponseCode();

            if(resCode != HttpURLConnection.HTTP_OK)
                return null;
            InputStream is = conn.getInputStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] byteBuffer = new byte[1536];
            byte[] byteData;
            int nLength;
            while((nLength = is.read(byteBuffer, 0, byteBuffer.length)) != -1) {
                baos.write(byteBuffer, 0, nLength);
            }

            byteData = baos.toByteArray();
            String resData = new String(byteData);
            Log.e(TAG, " # HTTP Response : " +  resData);

            baos.close();
            is.close();
            conn.disconnect();

        } catch(Exception e) {
            Log.e(TAG, " # HTTP Error : " +  e);
            e.printStackTrace();
        }
        return null;
    }
}
