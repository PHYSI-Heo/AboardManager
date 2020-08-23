package com.physis.aboard.monitor.manager.http;

public class HttpPacket {

    //    public static final String BASEURL = "http://192.168.1.12:3000";
    public static final String BASEURL = "http://192.168.219.106:3000";


    public static final String URL_CLIENT_REGISTER = BASEURL + "/register/client";
    public static final String URL_CLIENT_GET_INFO = BASEURL + "/get/client";
    public static final String URL_CLIENT_UPDATE_INFO = BASEURL + "/update/client";
    public static final String URL_GET_BEACONs = BASEURL + "/get/beacon/list";
    public static final String URL_UPDATE_ABOARD_STATE = BASEURL + "/update/aboard/state";



    public static final String PARAMS_CLIENT_NO = "no";
    public static final String PARAMS_CLIENT_NAME = "name";
    public static final String PARAMS_CLIENT_ADDRESS = "address";
    public static final String PARAMS_CLIENT_PHONE = "phone";
    public static final String PARAMS_CLIENT_ABOARD_TIME = "time";
    public static final String PARAMS_CLIENT_ABOARD_STATUS = "status";
    public static final String PARAMS_CLIENT_BEACON_ADDR = "beacon";
    public static final String PARAMS_CLIENT_ABOARD_TOKEN = "token";
    public static final String PARAMS_CLIENT_ABOARD_LAT = "lat";
    public static final String PARAMS_CLIENT_ABOARD_LON = "lon";
    public static final String PARAMS_MAX_NO = "max(no)";

    public static final String KEY_RES_CODE = "resCode";
    public static final String KEY_RES_ROWS = "rows";

    public static final String REQ_SUCCESS  = "1001";
}
