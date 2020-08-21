package com.physis.aboard.monitor.parent;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.physis.aboard.monitor.parent.http.HttpPacket;
import com.physis.aboard.monitor.parent.http.HttpRequester;
import com.physis.aboard.monitor.parent.storage.ClientPreference;

import org.json.JSONException;
import org.json.JSONObject;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener, HttpRequester.OnResponseListener {

    private static final String TAG = RegisterActivity.class.getSimpleName();
    private static final int REQ_SCAN_BEACON = 3011;

    private EditText etName, etAddress, etPhoneNum, etBeaconAddr, etPushToken;

    private String clientID = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initWidget();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(clientID != null)
            requestClientInfo();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQ_SCAN_BEACON && resultCode == RESULT_OK){
            assert data != null;
            etBeaconAddr.setText(data.getStringExtra(HttpPacket.PARAMS_CLIENT_BEACON_ADDR));
        }
    }


    //  region  Event Listener & Callback
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_scan:
                startActivityForResult(new Intent(RegisterActivity.this, BeaconDialog.class), REQ_SCAN_BEACON);
                break;
            case R.id.btn_get_token:
                etPushToken.setText(ClientPreference.getPushToken(getApplicationContext()));
                break;
            case R.id.btn_cancel:
                finish();
                break;
            case R.id.btn_setup:
                requestRegisterClient(clientID == null);
                break;
        }
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
                    if(url.equals(HttpPacket.URL_CLIENT_REGISTER)){
                        String clientNo = resObj.getJSONArray(HttpPacket.KEY_RES_ROWS).getJSONObject(0)
                                .getString(HttpPacket.PARAMS_MAX_NO);
                        ClientPreference.setClientID(getApplicationContext(), clientNo);
                        startActivity(new Intent(RegisterActivity.this, AboardActivity.class)
                                .putExtra(HttpPacket.PARAMS_CLIENT_NO, clientNo));
                        finish();
                    }else if(url.equals(HttpPacket.URL_CLIENT_GET_INFO)){
                        showClientInfo(resObj.getJSONArray(HttpPacket.KEY_RES_ROWS).getJSONObject(0));
                    }else{
                        Toast.makeText(getApplicationContext(), "Update successful.", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(getApplicationContext(), "정보 등록/갱신에 실패하였습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    //  endregion


    private void showClientInfo(JSONObject resObj){
        try {
            etName.setText(resObj.getString(HttpPacket.PARAMS_CLIENT_NAME));
            etAddress.setText(resObj.getString(HttpPacket.PARAMS_CLIENT_ADDRESS));
            etPhoneNum.setText(resObj.getString(HttpPacket.PARAMS_CLIENT_PHONE));
            etBeaconAddr.setText(resObj.getString(HttpPacket.PARAMS_CLIENT_BEACON_ADDR));
            etPushToken.setText(resObj.getString(HttpPacket.PARAMS_CLIENT_ABOARD_TOKEN));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void requestClientInfo(){
        JSONObject params = new JSONObject();
        try {
            params.put(HttpPacket.PARAMS_CLIENT_NO, clientID);
            HttpRequester requester = new HttpRequester("POST", HttpPacket.URL_CLIENT_GET_INFO, params);
            requester.execute();
            requester.setOnResponseListener(this);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void requestRegisterClient(boolean isRegister){
        String url = isRegister ? HttpPacket.URL_CLIENT_REGISTER : HttpPacket.URL_CLIENT_UPDATE_INFO;

        String name = etName.getText().toString();
        String addr = etAddress.getText().toString();
        String phone = etPhoneNum.getText().toString();
        String beaconAddr = etBeaconAddr.getText().toString();
        String token = etPushToken.getText().toString();

        if(name.length() == 0 || addr.length() == 0 ||  phone.length() == 0 ||
                beaconAddr.length() == 0 ||token.length() == 0) {
            Toast.makeText(getApplicationContext(), "Check input data.", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject params = new JSONObject();
        try {
            params.put(HttpPacket.PARAMS_CLIENT_NAME, name);
            params.put(HttpPacket.PARAMS_CLIENT_ADDRESS, addr);
            params.put(HttpPacket.PARAMS_CLIENT_PHONE, phone);
            params.put(HttpPacket.PARAMS_CLIENT_BEACON_ADDR, beaconAddr);
            params.put(HttpPacket.PARAMS_CLIENT_ABOARD_TOKEN, token);
            if(url.equals(HttpPacket.URL_CLIENT_UPDATE_INFO)){
                params.put(HttpPacket.PARAMS_CLIENT_NO, clientID);
            }
            HttpRequester requester = new HttpRequester("POST", url, params);
            requester.execute();
            requester.setOnResponseListener(this);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void initWidget() {
        AnimationDrawable animDrawable = (AnimationDrawable) findViewById(R.id.layout_frame).getBackground();
        animDrawable.setEnterFadeDuration(4500);
        animDrawable.setExitFadeDuration(4500);
        animDrawable.start();

        etName = findViewById(R.id.et_name);
        etAddress = findViewById(R.id.et_address);
        etPhoneNum = findViewById(R.id.et_phone_num);
        etBeaconAddr = findViewById(R.id.et_beacon);
        etPushToken = findViewById(R.id.et_push_token);

        Button btnScan = findViewById(R.id.btn_scan);
        btnScan.setOnClickListener(this);
        Button btnGetToken = findViewById(R.id.btn_get_token);
        btnGetToken.setOnClickListener(this);
        Button btnCancel = findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(this);
        Button btnSetup = findViewById(R.id.btn_setup);
        btnSetup.setOnClickListener(this);

        clientID = getIntent().getStringExtra(HttpPacket.PARAMS_CLIENT_NO);
        if(clientID == null){
            btnCancel.setEnabled(false);
            btnSetup.setText("Register");
        }
    }

}
