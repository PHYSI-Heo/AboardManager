package com.physis.aboard.monitor.parent.ble;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.util.Log;

import java.util.List;
import java.util.Objects;

public class BluetoothLEManager {
    private static final String TAG = BluetoothLEManager.class.getSimpleName();

    public static final int BLE_SCAN_START = 101;
    public static final int BLE_SCAN_STOP = 102;
    public static final int BLE_SCAN_DEVICE = 103;

    private static BluetoothLEManager bluetoothLEManager = null;
    private Context context;
    private Handler bleHandler = null;

    private BluetoothAdapter bluetoothAdapter = null;
    private BluetoothLeScanner bluetoothLeScanner = null;

    private long scanTime = 5000;
    private boolean isScanning = false;

    private BluetoothLEManager(Context context){
        this.context = context;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
    }

    public synchronized static BluetoothLEManager getInstance(Context context){
        if(bluetoothLEManager == null)
            bluetoothLEManager = new BluetoothLEManager(context);
        return bluetoothLEManager;
    }

    //  Check Bluetooth LE Support
    public boolean checkBleStatus() {
        return bluetoothAdapter != null && context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    public boolean getEnable(){
        return bluetoothAdapter.isEnabled();
    }

    //  Setup Scan Time
    public void setScanTime(long time){
        scanTime = time;
    }

    //  Setup Bluetooth LE Handler
    public void setHandler(Handler handler){
        bleHandler = handler;
    }


    //region    Bluetooth LE Scanner
    private void startBLEScan(){
        bluetoothLeScanner.startScan(scanCallback);
        isScanning = true;
        bleHandler.obtainMessage(BLE_SCAN_START).sendToTarget();
        Log.e(TAG, "# Start Bluetooth LE Scan..");
    }

    private void stopBLEScan(){
        bluetoothLeScanner.stopScan(scanCallback);
        isScanning = false;
        bleHandler.removeCallbacks(scanStopRunnable);
        bleHandler.obtainMessage(BLE_SCAN_STOP).sendToTarget();
        Log.e(TAG, "# Stop Bluetooth LE Scan..");
    }

    @SuppressLint("NewApi")
    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            bleHandler.obtainMessage(BLE_SCAN_DEVICE, result).sendToTarget();
        }
        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult result : results){
                Log.e(TAG, "onBatchScanResults : " + result.getDevice().getAddress());
            }
        }
        @Override
        public void onScanFailed(int errorCode) {
            Log.e(TAG, "# BLE Scan Error..[ErrorCode] : " + errorCode);
        }
    };


    public void scan(boolean isScan, boolean isTimer){
        if(isScan){
            if(isScanning)
                return;
            startBLEScan();
            if(isTimer)
                bleHandler.postDelayed(scanStopRunnable, scanTime);
        }else{
            if(isScanning) stopBLEScan();
        }
    }

    private Runnable scanStopRunnable = new Runnable() {
        @Override
        public void run() {
            stopBLEScan();
        }
    };
    //endregion

    public boolean isBeacon(ScanResult result){
        byte[] scanRecord = Objects.requireNonNull(result.getScanRecord()).getBytes();
        int startByte = 2;
        while (startByte <= 5) {
            if (((int) scanRecord[startByte + 2] & 0xff) == 0x02 &&
                    ((int) scanRecord[startByte + 3] & 0xff) == 0x15) {
                return true;
            }
            startByte++;
        }
        return false;
    }
}
