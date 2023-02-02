package com.pos.passport.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Spinner;

import com.pos.passport.R;
import com.pos.passport.data.ProductDatabase;
import com.pos.passport.interfaces.HardwareInterface;
import com.pos.passport.model.Device;
import com.pos.passport.util.Consts;
import com.pos.passport.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Kareem on 10/11/2016.
 */
public class AddHardwareFragment extends DialogFragment {

    private ProductDatabase mDb;
    private Spinner mTypeSpinner;
    private EditText mIpAddressEditText;
    private EditText mPortEditText;
    private EditText mNameEditTest;
    private int mId;
    public HardwareInterface mCallback;
    public static AddHardwareFragment newInstance() {
        AddHardwareFragment fragment = new AddHardwareFragment();
        fragment.setCancelable(false);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.txt_add)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton)
                            {
                                saveDeviceInfo(mId);
                                getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, getActivity().getIntent());
                                dialog.dismiss();
                            }
                        }
                )
                .setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                            }
                        }
                );

        if (getArguments() != null)
            mId = getArguments().getInt(Consts.BUNDLE_ID, 0);
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View view = inflater.inflate(R.layout.fragment_add_hardware, null, false);
        Utils.setTypeFace(Typeface.createFromAsset(getActivity().getAssets(), "fonts/NotoSans-Regular.ttf"), (ViewGroup) view);
        mDb = ProductDatabase.getInstance(getActivity());
       /* mPermissionIntent = PendingIntent.getBroadcast(getActivity(), 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        getActivity().registerReceiver(mUsbReceiver, filter);*/
        bindUIElements(view);
        //setUpUI();
        //setUpListeners();
        builder.setView(view);
        return builder.create();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallback = (HardwareInterface) context;
    }
    private void setUpUI(){

    }

    private void bindUIElements(View view){
        mTypeSpinner = (Spinner) view.findViewById(R.id.type_spinner);
        mIpAddressEditText = (EditText) view.findViewById(R.id.ip_address_edit_text);
        mPortEditText = (EditText) view.findViewById(R.id.port_edit_text);
        mNameEditTest = (EditText) view.findViewById(R.id.name_edit_text);

    }

    private void setUpListeners(){

    }

    private void saveDeviceInfo(int id)
    {
        try
        {
        Device device = new Device();
        device.setDeviceName(mNameEditTest.getText().toString().trim());
        device.setDeviceType(mTypeSpinner.getSelectedItem().toString());
        device.setIpaddress(mIpAddressEditText.getText().toString().trim());
        device.setPort(mPortEditText.getText().toString().trim());
        //device.setId(id);

            //mDb.saveHardwareDevice(device, id);

        JSONObject jsonsend = new JSONObject();
        jsonsend.put("cardReader", mTypeSpinner.getSelectedItem().toString());
        jsonsend.put("name", mNameEditTest.getText().toString().trim());
        jsonsend.put("port", mPortEditText.getText().toString().trim());
        jsonsend.put("ipAddress", mIpAddressEditText.getText().toString().trim());
            mCallback.onAddCardReader(jsonsend,device);
            //AddPrinterSync(jsonsend);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
