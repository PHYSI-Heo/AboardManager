package com.physis.aboard.monitor.parent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.physis.aboard.monitor.parent.ble.BluetoothLEManager;
import com.physis.aboard.monitor.parent.http.HttpPacket;
import com.physis.aboard.monitor.parent.http.HttpRequester;
import com.physis.aboard.monitor.parent.list.BeaconAdapter;
import com.physis.aboard.monitor.parent.storage.ClientPreference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

public class BeaconDialog extends AppCompatActivity implements View.OnClickListener, BeaconAdapter.OnSelectedDeviceListener, HttpRequester.OnResponseListener {

    private static final String TAG = BeaconDialog.class.getSimpleName();

    private Button btnScan;
    private RecyclerView rcBeacons;
    private ProgressBar pbScanning;

    private BeaconAdapter beaconAdapter;
    private BluetoothLEManager bleManager;

    private List<String> registerBeacons = new LinkedList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFinishOnTouchOutside(false);
        setContentView(R.layout.dialog_beacon);

        init();
    }

    @Override
    protected void onStart() {
        super.onStart();
        requestBeacons();
    }

    @Override
    protected void onStop() {
        super.onStop();
        bleManager.scan(false, false);
    }

    @Override
    public void onClick(View view) {
        beaconAdapter.clearItem();
        bleManager.scan(true, true);
    }

    @Override
    public void onSelected(BluetoothDevice device) {
        if(registerBeacons.contains(device.getAddress())){
            Toast.makeText(getApplicationContext(), "이미 등록된 Beacon 입니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = getIntent();
        intent.putExtra(HttpPacket.PARAMS_CLIENT_BEACON_ADDR, device.getAddress());
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onResponseListener(String url, String responseData) {
        try{
            if(responseData == null){
                Toast.makeText(getApplicationContext(), "서버와 통신이 불안정합니다.", Toast.LENGTH_SHORT).show();
                throw new Exception("http server response - null");
            }else{
                JSONObject resObj = new JSONObject(responseData);
                if(resObj.getString(HttpPacket.KEY_RES_CODE).equals(HttpPacket.REQ_SUCCESS)){
                    setRegisterBeacons(resObj.getJSONArray(HttpPacket.KEY_RES_ROWS));
                }else {
                    Toast.makeText(getApplicationContext(), "정보 조회에 실패하였습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what){
                case BluetoothLEManager.BLE_SCAN_START:
                    btnScan.setEnabled(false);
                    rcBeacons.setEnabled(false);
                    pbScanning.setVisibility(View.VISIBLE);
                    break;
                case BluetoothLEManager.BLE_SCAN_STOP:
                    btnScan.setEnabled(true);
                    rcBeacons.setEnabled(true);
                    pbScanning.setVisibility(View.INVISIBLE);
                    break;
                case BluetoothLEManager.BLE_SCAN_DEVICE:
                    ScanResult result = (ScanResult) msg.obj;
                    beaconAdapter.addItem(result.getDevice());
                    break;
            }
        }
    };


    private void setRegisterBeacons(JSONArray array){
        registerBeacons.clear();
        for(int i = 0; i < array.length(); i++){
            try {
                registerBeacons.add(array.getJSONObject(i).getString(HttpPacket.PARAMS_CLIENT_BEACON_ADDR));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void requestBeacons(){
        HttpRequester requester = new HttpRequester("POST", HttpPacket.URL_GET_BEACONs, null);
        requester.execute();
        requester.setOnResponseListener(this);
    }

    private void init() {
        btnScan = findViewById(R.id.btn_scan);
        btnScan.setOnClickListener(this);

        rcBeacons = findViewById(R.id.rv_beacons);
        DividerItemDecoration decoration
                = new DividerItemDecoration(getApplicationContext(), LinearLayoutManager.VERTICAL);
        decoration.setDrawable(getResources().getDrawable(R.drawable.list_division_line));
        rcBeacons.addItemDecoration(decoration);
        LinearLayoutManager itemLayoutManager = new LinearLayoutManager(getApplicationContext());
        itemLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        itemLayoutManager.setItemPrefetchEnabled(true);
        rcBeacons.setLayoutManager(itemLayoutManager);
        rcBeacons.setAdapter(beaconAdapter = new BeaconAdapter());
        beaconAdapter.setOnSelectedDeviceListener(this);

        pbScanning = findViewById(R.id.pb_scanning);
        pbScanning.setVisibility(View.INVISIBLE);

        bleManager = BluetoothLEManager.getInstance(getApplicationContext());
        bleManager.setHandler(handler);
    }

}
