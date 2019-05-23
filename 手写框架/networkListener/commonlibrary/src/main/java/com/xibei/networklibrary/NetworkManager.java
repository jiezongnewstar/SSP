package com.xibei.networklibrary;

import android.app.Application;
import android.content.IntentFilter;

import static com.xibei.networklibrary.Constants.NETWORK_ACTION;

/**
 * Created by Xibei on 2019/5/23.
 * Github: https://github.com/jiezongnewstar
 * Email: ibossjia@gmail.com
 * Deeclaration: 网路管理类
 */
public class NetworkManager {

    // 采用比synchronized 轻量级的 volatile
    private static volatile NetworkManager instance;

    private NetStateReceiver reciver;

    private Application application;

    private NetworkManager(){
        reciver = new NetStateReceiver();
    }

    public void setObserver(NetChangeObserver observer){
        reciver.setObserver(observer);
    }


    public static NetworkManager getInstance(){
        if (instance ==null){
            synchronized (NetworkManager.class){
                if (instance ==null){
                    instance =  new NetworkManager();
                }
            }
        }

        return instance;
    }

    public Application getApplication() {
        return application;
    }


    public void init(Application application){

        this.application = application;

        //动态注册广播
        IntentFilter filter = new IntentFilter();
        filter.addAction(NETWORK_ACTION);
        application.registerReceiver(reciver,filter);
    }

}
