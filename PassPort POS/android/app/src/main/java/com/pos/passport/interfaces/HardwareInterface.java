package com.pos.passport.interfaces;

import com.pos.passport.model.Device;

import org.json.JSONObject;

/**
 * Created by karim on 10/12/15.
 */
public interface HardwareInterface {

    void onAdd(JSONObject insertdata,JSONObject data,int checkId);
    void onDelete(int rowid,String type);
    void onAddCardReader(JSONObject data, Device device);
}
