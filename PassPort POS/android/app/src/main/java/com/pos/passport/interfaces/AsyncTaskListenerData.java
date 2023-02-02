package com.pos.passport.interfaces;

/**
 * Created by Kareem on 2/10/2016.
 */
public interface AsyncTaskListenerData {
    //void onSuccess();
    void onFailure();
    void onSuccess(String data);
}
