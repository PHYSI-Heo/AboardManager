package com.physis.aboard.monitor.manager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.physis.aboard.monitor.manager.data.ClientInfo;
import com.physis.aboard.monitor.manager.utils.NotifyDialog;
import com.physis.aboard.monitor.manager.http.HttpPacket;
import com.physis.aboard.monitor.manager.http.HttpRequester;
import com.physis.aboard.monitor.manager.list.ClientAdapter;
import com.physis.aboard.monitor.manager.utils.FCMMessageTransmitter;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ClientAdapter.OnClientSelectedListener,
        View.OnClickListener, BeaconConsumer, HttpRequester.OnResponseListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    @SuppressLint("SimpleDateFormat")
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd HH:mm");

    private static final int ABOARD_START_SCOPE = -45;
    private static final int ABOARD_END_SCOPE = -78;

    private ClientAdapter clientAdapter;
    private List<ClientInfo> clients = new LinkedList<>();
    private String currentLatitude, currentLongitude;

    private BeaconManager beaconManager;

    private TextView tvScanBlink;
    private boolean isBlink = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        initWidget();
        bindAltBeacon();
        startLocationTracking();
        requestClientInfos();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
    }

    // region   Event Listener & Callback
    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.iv_btn_reset){
            requestClientInfos();
        }
    }

    @Override
    public void onClientPosition(int position) {
        LayoutInflater inflater = (LayoutInflater)getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_detail_info, null);
        ClientInfo info = clients.get(position);
        ((EditText)view.findViewById(R.id.et_name)).setText(info.getName());
        ((EditText)view.findViewById(R.id.et_phone_num)).setText(info.getPhone());
        ((EditText)view.findViewById(R.id.et_address)).setText(info.getAddress());
        new NotifyDialog().show(MainActivity.this, null, view);
    }


    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            currentLatitude = String.valueOf(location.getLatitude());
            currentLongitude = String.valueOf(location.getLongitude());
            Log.e(TAG, "onLocationChanged : " + currentLatitude + ", " + currentLongitude);
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

    @Override
    public void onResponseListener(String url, String responseData) {
        try{
            if(responseData == null){
                Toast.makeText(getApplicationContext(), "서버와 통신이 불안정합니다.", Toast.LENGTH_SHORT).show();
                throw new Exception("http server response - null");
            }else{
                JSONObject resObj = new JSONObject(responseData);
                if(resObj.getString(HttpPacket.KEY_RES_CODE).equals(HttpPacket.REQ_SUCCESS)){
                    setClientList(resObj.getJSONArray(HttpPacket.KEY_RES_ROWS));
                }else {
                    Toast.makeText(getApplicationContext(), "정보 조회에 실패하였습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onBeaconServiceConnect() {
        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if (beacons.size() > 0) {
                    for (Beacon beacon : beacons) {
                        Log.e(TAG, "Address : " + beacon.getBluetoothAddress() + ", Rssi : " + beacon.getRssi());
                        checkAboardStatus(beacon.getBluetoothAddress(), beacon.getRssi());
                    }
                    isBlink = !isBlink;
                    tvScanBlink.setBackgroundResource(isBlink ? R.color.colorScanBlink : R.color.colorAccent);
                }
            }
        });
        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myMonitoringUniqueId", null, null, null));
        } catch (RemoteException ignored) {    }
    }

    // endregion

    private void checkAboardStatus(String addr, double rssi){
        int pos = -1;
        for(int i = 0; i < clients.size(); i++){
            if(clients.get(i).getBeaconAddress().equals(addr)) {
                pos = i;
                break;
            }
        }
        if(pos == -1)   return;

//        String state = rssi > ABOARD_START_SCOPE ? "1" : (rssi < ABOARD_END_SCOPE ? "2" : "3");
        String state = rssi > ABOARD_END_SCOPE ? "1" : "2";

        if(clients.get(pos).getState().equals(state))
            return;

        Log.e(TAG, "Update State :" + state);
        String currentTime = dateFormat.format(new Date());
        clients.get(pos).changeStatus(state, currentTime);
        clients.get(pos).setLatLng(currentLatitude, currentLongitude);
        clientAdapter.setItems(clients);
        requestUpdateClient(clients.get(pos).getNo(), state, currentTime);
        new FCMMessageTransmitter(clients.get(pos)).execute();
    }


    private void setClientList(JSONArray array) {
        clients.clear();
        for(int i = 0; i < array.length(); i++){
            try {
                JSONObject obj = array.getJSONObject(i);
                clients.add(new ClientInfo(
                        obj.getString(HttpPacket.PARAMS_CLIENT_NO),
                        obj.getString(HttpPacket.PARAMS_CLIENT_NAME),
                        obj.getString(HttpPacket.PARAMS_CLIENT_ADDRESS),
                        obj.getString(HttpPacket.PARAMS_CLIENT_PHONE),
                        obj.getString(HttpPacket.PARAMS_CLIENT_BEACON_ADDR),
                        obj.getString(HttpPacket.PARAMS_CLIENT_ABOARD_TOKEN),
                        obj.getString(HttpPacket.PARAMS_CLIENT_ABOARD_STATUS),
                        obj.getString(HttpPacket.PARAMS_CLIENT_ABOARD_TIME)
                ));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        clientAdapter.setItems(clients);
    }

    private void requestUpdateClient(String no, String state, String time){
        JSONObject params = new JSONObject();
        try {
            params.put(HttpPacket.PARAMS_CLIENT_NO, no);
            params.put(HttpPacket.PARAMS_CLIENT_ABOARD_STATUS, state);
            params.put(HttpPacket.PARAMS_CLIENT_ABOARD_TIME, time);
            params.put(HttpPacket.PARAMS_CLIENT_ABOARD_LAT, currentLatitude);
            params.put(HttpPacket.PARAMS_CLIENT_ABOARD_LON, currentLongitude);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        HttpRequester requester = new HttpRequester("POST", HttpPacket.URL_UPDATE_ABOARD_STATE, params);
        requester.execute();
    }

    private void requestClientInfos(){
        HttpRequester requester = new HttpRequester("POST", HttpPacket.URL_CLIENT_GET_INFO, null);
        requester.execute();
        requester.setOnResponseListener(this);
    }

    private void bindAltBeacon(){
        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        beaconManager.bind(this);
    }

    @SuppressLint("MissingPermission")
    private void startLocationTracking() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        assert locationManager != null;
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                5000, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                5000, 0, locationListener);
    }

    private void initWidget() {
        AnimationDrawable animDrawable = (AnimationDrawable) findViewById(R.id.layout_frame).getBackground();
        animDrawable.setEnterFadeDuration(4500);
        animDrawable.setExitFadeDuration(4500);
        animDrawable.start();

        ImageView ivBtnReset = findViewById(R.id.iv_btn_reset);
        ivBtnReset.setOnClickListener(this);

        RecyclerView rvClients = findViewById(R.id.rv_clients);
        LinearLayoutManager itemLayoutManager = new LinearLayoutManager(getApplicationContext());
        itemLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        itemLayoutManager.setItemPrefetchEnabled(true);
        rvClients.setLayoutManager(itemLayoutManager);
        rvClients.setAdapter(clientAdapter = new ClientAdapter());
        clientAdapter.setOnSelectedIndexListener(this);

        tvScanBlink = findViewById(R.id.tv_scan_blink);
    }

}
