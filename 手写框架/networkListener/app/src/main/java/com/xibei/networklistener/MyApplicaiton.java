package com.xibei.networklistener;

import android.app.Application;

import com.xibei.networklibrary.NetworkManager;

/**
 * Created by Xibei on 2019/5/23.
 * Github: https://github.com/jiezongnewstar
 * Email: ibossjia@gmail.com
 * Deeclaration:
 */
public class MyApplicaiton extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        NetworkManager.getInstance().init(this);  //广播注册兼容
    }
}
