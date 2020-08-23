package com.physis.aboard.monitor.parent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.physis.aboard.monitor.parent.http.HttpPacket;
import com.physis.aboard.monitor.parent.utils.FCMMessagingService;
import com.physis.aboard.monitor.parent.utils.ClientPreference;

import java.util.LinkedList;
import java.util.List;

public class IntroActivity extends AppCompatActivity {

    private static final String TAG = IntroActivity.class.getSimpleName();

    //  region   Application Check Permission
    private static final long INTRO_DELAY = 1500;
    private static final int REQ_APP_PERMISSION = 1500;

    private String[] appPermissions = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
    };

    private Handler introHandler = new Handler();

    private void checkPermissions(){
        final List<String> reqPermissions = new LinkedList<>();
        for(String permission : appPermissions){
            if(checkSelfPermission(permission) == PackageManager.PERMISSION_DENIED){
                reqPermissions.add(permission);
            }
        }
        if(reqPermissions.size() == 0){
            nextActivity();
        }else{
            requestPermissions(reqPermissions.toArray(new String[reqPermissions.size()]), REQ_APP_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQ_APP_PERMISSION){
            boolean accessStatus = true;
            for(int grantResult : grantResults){
                if (grantResult == PackageManager.PERMISSION_DENIED) {
                    accessStatus = false;
                    break;
                }
            }

            if(!accessStatus){
                Toast.makeText(getApplicationContext(), "App Permission Denied!", Toast.LENGTH_SHORT).show();
                finish();
            }else{
                nextActivity();
            }
        }
    }

    private Runnable introRunnable = new Runnable() {
        @Override
        public void run() {
            Intent intent;
            String cid = ClientPreference.getClientID(getApplicationContext());
            if(cid == null) {
                intent = new Intent(IntroActivity.this, RegisterActivity.class);
            }else {
                String fcmMessage = getIntent().getStringExtra(FCMMessagingService.RECEIVED_MESSAGE);
                intent = new Intent(IntroActivity.this, AboardActivity.class);
                intent.putExtra(HttpPacket.PARAMS_CLIENT_NO, cid);
                if(fcmMessage != null)
                    intent.putExtra(FCMMessagingService.RECEIVED_MESSAGE, fcmMessage);
            }
            startActivity(intent);
            finish();
        }
    };
    // endregion


    private void nextActivity(){
        introHandler.postDelayed(introRunnable, INTRO_DELAY);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        checkPermissions();
    }

    @Override
    public void onBackPressed() {
        introHandler.removeCallbacks(introRunnable);
        super.onBackPressed();
    }
}
