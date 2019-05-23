package com.xibei.networklistener;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.xibei.networklibrary.NetChangeObserver;
import com.xibei.networklibrary.NetState;
import com.xibei.networklibrary.NetworkManager;

import static com.xibei.networklibrary.Constants.NETWORK_TAG;

public class MainActivity extends Activity implements NetChangeObserver {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //注册监听
        NetworkManager.getInstance().setObserver(this);

    }

    @Override
    public void onConnect(NetState state) {
        Log.e(NETWORK_TAG,MainActivity.this.getClass().getName()+"网络连接"+state);
    }

    @Override
    public void disConnect() {
        Log.e(NETWORK_TAG,MainActivity.this.getClass().getName()+"网络连接断开");

    }
}
