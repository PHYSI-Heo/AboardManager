package com.physis.aboard.monitor.manager;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.physis.aboard.monitor.manager.data.ClientInfo;
import com.physis.aboard.monitor.manager.http.HttpPacket;
import com.physis.aboard.monitor.manager.http.HttpRequester;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class IntroActivity extends AppCompatActivity {

    private static final String TAG = IntroActivity.class.getSimpleName();

    private Handler introHandler = new Handler();

    // region   Application Check Permission
    private static final long INTRO_DELAY = 1500;
    private static final int REQ_APP_PERMISSION = 1500;

    private String[] appPermissions = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
    };


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
            startActivity(new Intent(IntroActivity.this, MainActivity.class));
            finish();
        }
    };
    // endregion

    private void nextActivity(){
        tvCheckMessage.setText("Start Aboard Monitoring...");
        introHandler.postDelayed(introRunnable, INTRO_DELAY);
    }

    private TextView tvCheckMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        initWidget();
        checkPermissions();
    }


    @Override
    public void onBackPressed() {
        introHandler.removeCallbacks(introRunnable);
        super.onBackPressed();
    }


    private void initWidget() {
        AnimationDrawable animDrawable = (AnimationDrawable) findViewById(R.id.layout_frame).getBackground();
        animDrawable.setEnterFadeDuration(4500);
        animDrawable.setExitFadeDuration(4500);
        animDrawable.start();

        tvCheckMessage = findViewById(R.id.tv_check_message);
        tvCheckMessage.setText("Checking Permissions...");
    }
}
