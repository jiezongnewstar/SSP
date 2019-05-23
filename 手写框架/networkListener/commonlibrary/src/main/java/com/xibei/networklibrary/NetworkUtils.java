package com.xibei.networklibrary;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by Xibei on 2019/5/23.
 * Github: https://github.com/jiezongnewstar
 * Email: ibossjia@gmail.com
 * Deeclaration: 网络校验类
 */
public class NetworkUtils {

    //网络是否可用
    @SuppressLint("MissingPermission")
    public static boolean isNetworkAvailable(){

        ConnectivityManager connMgr =
                (ConnectivityManager) NetworkManager.getInstance().getApplication()
                        .getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connMgr == null) {
            return false;
        }

        NetworkInfo [] infos = connMgr.getAllNetworkInfo();
        if (infos != null){
            for (NetworkInfo info :infos){
                if (info.getState() == NetworkInfo.State.CONNECTED){
                    return true;
                }
            }
        }

        return false;
    }


    //网络类型
    @SuppressLint("MissingPermission")
    public static NetState getNetState(){
        ConnectivityManager connMgr =
                (ConnectivityManager) NetworkManager.getInstance().getApplication()
                        .getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connMgr == null) {
            return NetState.NONE;
        }

        //获取当前激活网络信息
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo == null){
            return NetState.NONE;
        }


        int mState = networkInfo.getType();

        if (mState == ConnectivityManager.TYPE_MOBILE){
            if (networkInfo.getExtraInfo().toLowerCase().equals("cmnet")){
                return NetState.CMNET;
            }else {
                return NetState.CMWAP;
            }
        }else if (mState == ConnectivityManager.TYPE_WIFI){
            return NetState.WIFI;
        }

        return NetState.NONE;

    }


}
