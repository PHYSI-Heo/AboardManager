package com.physis.aboard.monitor.manager.data;

import android.os.Parcel;
import android.os.Parcelable;

public class ClientInfo {
    private String no, name, address, phone, beaconAddress, token, state, time;
    private String latitude, longitude;

    public ClientInfo(String no, String name, String address, String phone,
                      String beaconAddress, String token, String state, String time){
        this.no = no;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.beaconAddress = beaconAddress;
        this.token = token;
        this.state = state;
        this.time = time;
    }

    public String getAddress() {
        return address;
    }

    public String getBeaconAddress() {
        return beaconAddress;
    }

    public String getName() {
        return name;
    }

    public String getNo() {
        return no;
    }

    public String getPhone() {
        return phone;
    }

    public String getToken() {
        return token;
    }

    public String getState() {
        return state;
    }

    public String getTime() {
        return time;
    }

    public void changeStatus(String state, String time){
        this.state = state;
        this.time = time;
    }

    public void setLatLng(String lat, String lon){
        latitude = lat;
        longitude = lon;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }
}
