package com.xibei.networklibrary;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import static com.xibei.networklibrary.Constants.NETWORK_ACTION;
import static com.xibei.networklibrary.Constants.NETWORK_TAG;

/**
 * Created by Xibei on 2019/5/23.
 * Github: https://github.com/jiezongnewstar
 * Email: ibossjia@gmail.com
 * Deeclaration:
 */
public class NetStateReceiver extends BroadcastReceiver {

    private NetState netState;

    private NetChangeObserver observer;

    public NetStateReceiver(){
        //初始化 、重置
        netState = NetState.NONE;
    }


    public void setObserver(NetChangeObserver observer){
        this.observer = observer;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent ==null || intent.getAction() == null){
            Log.e(NETWORK_TAG,"异常....");
            return;
        }

        //处理广播
        if (intent.getAction().equalsIgnoreCase(NETWORK_ACTION)){
            Log.e(NETWORK_TAG,"网络发生改变....");
            if (NetworkUtils.isNetworkAvailable()){  //网络是否可用判断
                netState = NetworkUtils.getNetState();
                Log.e(NETWORK_TAG,"网络可用....");
                if (observer != null){
                    observer.onConnect(netState); //回调当前网络连接类型
                }

            }else {
                Log.e(NETWORK_TAG,"网络不可用....");
                if (observer != null){
                    observer.disConnect();
                }
            }
        }
    }
}
