package com.xibei.networklibrary;

/**
 * Created by Xibei on 2019/5/23.
 * Github: https://github.com/jiezongnewstar
 * Email: ibossjia@gmail.com
 * Deeclaration:
 */
public interface NetChangeObserver {

    void onConnect(NetState state);

    void disConnect();
}
