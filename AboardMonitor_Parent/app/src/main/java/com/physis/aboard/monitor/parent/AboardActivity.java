package com.physis.aboard.monitor.parent;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.physis.aboard.monitor.parent.dialog.NotifyDialog;
import com.physis.aboard.monitor.parent.http.HttpPacket;
import com.physis.aboard.monitor.parent.http.HttpRequester;
import com.physis.aboard.monitor.parent.push.FCMMessagingService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class AboardActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener {

    private static final String TAG = AboardActivity.class.getSimpleName();

    private EditText etStatus, etTime;

    private GoogleMap map;
    private LocationManager locationManager;

    private String aboardMessage;
    private String clientID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aboard);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        initWidget();
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(FCMMessagingService.RECEIVED_NOTIFICATION);
        registerReceiver(notificationReceiver, intentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(notificationReceiver);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        showNewAboardMessage();
    }

    //  region  Event Listener & Callback
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_btn_edit:
                startActivity(new Intent(AboardActivity.this, RegisterActivity.class)
                        .putExtra(HttpPacket.PARAMS_CLIENT_NO, clientID));
                break;
            case R.id.iv_btn_refresh:
                showNewAboardMessage();
                break;
        }
    }

    private HttpRequester.OnResponseListener onResponseListener = new HttpRequester.OnResponseListener() {
        @Override
        public void onResponseListener(String url, String responseData) {
            try{
                if(responseData == null){
                    Toast.makeText(getApplicationContext(), "서버와 통신이 불안정합니다.", Toast.LENGTH_SHORT).show();
                    throw new Exception("http server response - null");
                }else{
                    JSONObject resObj = new JSONObject(responseData);
                    if(resObj.getString(HttpPacket.KEY_RES_CODE).equals(HttpPacket.REQ_SUCCESS)){
                        showLastAboardInfo(resObj.getJSONArray(HttpPacket.KEY_RES_ROWS));
                    }else {
                        Toast.makeText(getApplicationContext(), "정보 조회에 실패하였습니다.", Toast.LENGTH_SHORT).show();
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };

    private BroadcastReceiver notificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            assert action != null;
            if(action.equals(FCMMessagingService.RECEIVED_NOTIFICATION)){
                aboardMessage = intent.getStringExtra(FCMMessagingService.RECEIVED_MESSAGE);
            }
        }
    };


    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 14f));
            locationManager.removeUpdates(locationListener);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };
    //  endregion


    private void showLastAboardInfo(JSONArray array){
        try {
            if(array.length() == 0)
                return;

            JSONObject obj = array.getJSONObject(0);
            String status = obj.getString(HttpPacket.PARAMS_CLIENT_ABOARD_STATUS);
            String time = obj.getString(HttpPacket.PARAMS_CLIENT_ABOARD_TIME);

            etStatus.setText(setAboardStateText(status));
            etTime.setText(time.equals("null") ? "정보없음" : time);
            showAboardPosition(obj.getString(HttpPacket.PARAMS_CLIENT_ABOARD_LAT), obj.getString(HttpPacket.PARAMS_CLIENT_ABOARD_LON));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void showAboardPosition(String lat, String lon){
        LatLng latLng = new LatLng(Float.parseFloat(lat), Float.parseFloat(lon));
        MarkerOptions options = new MarkerOptions();
        options.position(latLng);
        map.addMarker(options);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14f));
    }

    private void showNewAboardMessage() {
        aboardMessage = getIntent().getStringExtra(FCMMessagingService.RECEIVED_MESSAGE);
        if(aboardMessage == null){
            requestCurrentLocation();
            new NotifyDialog().show(AboardActivity.this, null,
                    "푸쉬 메시지 수신상태가 아닙니다.\n최신정보를 가져오시겠습니까?",
                    "가져오기", new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            requestLastAboardData();
                        }
                    });
        }else{
            String[] infos = aboardMessage.split(",");
            etStatus.setText(setAboardStateText(infos[0]));
            etTime.setText(infos[1]);
            showAboardPosition(infos[2], infos[3]);
        }
    }


    private String setAboardStateText(String state){
        if(state.equals("0")){
            return "정보없음";
        }else if(state.equals("1")){
            return "승차";
        }else{
            return "하차";
        }
    }

    private void requestLastAboardData(){
        JSONObject params = new JSONObject();
        try {
            params.put(HttpPacket.PARAMS_CLIENT_NO, clientID);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        HttpRequester requester = new HttpRequester("POST", HttpPacket.URL_CLIENT_GET_INFO, params);
        requester.execute();
        requester.setOnResponseListener(onResponseListener);
    }


    @SuppressLint("MissingPermission")
    private void requestCurrentLocation() {
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        assert locationManager != null;
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
    }


    private void initWidget() {
        clientID = getIntent().getStringExtra(HttpPacket.PARAMS_CLIENT_NO);
        Log.e(TAG, "Monitoring client id : " + clientID);

        AnimationDrawable animDrawable = (AnimationDrawable) findViewById(R.id.layout_frame).getBackground();
        animDrawable.setEnterFadeDuration(4500);
        animDrawable.setExitFadeDuration(4500);
        animDrawable.start();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_map);
        Objects.requireNonNull(mapFragment).getMapAsync(this);

        etStatus = findViewById(R.id.et_state);
        etTime = findViewById(R.id.et_time);

        ImageView ivBtnEdit = findViewById(R.id.iv_btn_edit);
        ImageView ivBtnRefresh = findViewById(R.id.iv_btn_refresh);

        ivBtnRefresh.setOnClickListener(this);
        ivBtnEdit.setOnClickListener(this);
    }

}
