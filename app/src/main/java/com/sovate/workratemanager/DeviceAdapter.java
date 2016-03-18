package com.sovate.workratemanager;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.sovate.workratemanager.bean.ActivityDeviceStudentInfo;
import com.sovate.workratemanager.bean.ActivityWorkRate;
import com.sovate.workratemanager.bean.DeviceInfo;
import com.sovate.workratemanager.common.UploadStatus;
import com.sovate.workratemanager.network.HttpApi;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by harks on 2016-02-27.
 */
public class DeviceAdapter extends BaseAdapter {
    public static final String TAG = "UartService";
    Context context;
    List<DeviceInfo> devices;
    LayoutInflater inflater;

    public DeviceAdapter(Context context, List<DeviceInfo> devices) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.devices = devices;
    }

    public boolean setWorkrate(String mac, String collectDate, int Calorie, int steps, int distance, String sportId){
        for(DeviceInfo item : devices){
            if(item.getDevice().getAddress().equals(mac)){

                item.getWorkRate().setUserId(item.getDeviceStudentInfo().getUserId());
                item.getWorkRate().setMac(mac);
                item.getWorkRate().setCollectDt(collectDate);
                item.getWorkRate().setCalorie(Integer.toString(Calorie));
                item.getWorkRate().setSteps(Integer.toString(steps));
                item.getWorkRate().setDistance(Integer.toString(distance));
                item.getWorkRate().setSportId(sportId);

                notifyDataSetChanged();

                HttpApi.postWorkrate(item.getWorkRate());

                return  true;
            }
        }

        return false;
    }

    public boolean setUpdateStatus(String mac, UploadStatus status){
        for(DeviceInfo item : devices){
            if(item.getDevice().getAddress().equals(mac)){

                item.setUploadStatus(status);
                notifyDataSetChanged();

                return  true;
            }
        }

        return false;
    }

    @Override
    public int getCount() {
        return devices.size();
    }

    @Override
    public Object getItem(int position) {
        return devices.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewGroup vg;

        if (convertView != null) {
            vg = (ViewGroup) convertView;
        } else {
            vg = (ViewGroup) inflater.inflate(R.layout.device_element, null);
        }

        UploadStatus status = devices.get(position).getUploadStatus();
        BluetoothDevice device = devices.get(position).getDevice();
        ActivityDeviceStudentInfo deviceStudentInfo = devices.get(position).getDeviceStudentInfo();
        ActivityWorkRate workRate =  devices.get(position).getWorkRate();


        final TextView tvDeviceAlias = ((TextView) vg.findViewById(R.id.deviceAlias));
        final TextView tvDeviceMac = ((TextView) vg.findViewById(R.id.deviceMac));

        final TextView tvuserName = ((TextView) vg.findViewById(R.id.userName));
        final TextView tvWearableData = ((TextView) vg.findViewById(R.id.wearbleData));
        final TextView tvUploadStatus = ((TextView) vg.findViewById(R.id.uploadStatus));



        tvDeviceAlias.setText(deviceStudentInfo.getName());
        tvuserName.setText(deviceStudentInfo.getUserName());
        tvDeviceMac.setText(device.getAddress());

        if(workRate.getCalorie().length() > 0){

            String s = String.format("T:%s, C:%s, S:%s, D:%s"
                    , workRate.getCollectDt().substring(0, 13)
                    , workRate.getCalorie()
                    , workRate.getSteps()
                    , workRate.getDistance()
                    );
            tvWearableData.setText(s);

        } else {
            tvWearableData.setText("");
        }

        if(status == UploadStatus.SUCCESS){
            tvUploadStatus.setText("성공");
            tvUploadStatus.setTextColor(Color.BLUE);
        } else if(status == UploadStatus.FAIL){
            tvUploadStatus.setText("실패");
            tvUploadStatus.setTextColor(Color.RED);
        }
        else {
            tvUploadStatus.setText("준비");
            tvUploadStatus.setTextColor(Color.GRAY);
        }


        if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
            Log.i(TAG, "device::" + device.getName());
            tvDeviceAlias.setTextColor(Color.BLACK);
            tvuserName.setTextColor(Color.BLACK);


            //tvDeviceMac.setTextColor(Color.BLACK);

//            tvCalorie.setTextColor(Color.BLACK);
//            tvSteps.setTextColor(Color.BLACK);
//            tvDistance.setTextColor(Color.BLACK);


        } else {
            tvuserName.setTextColor(Color.BLACK);
            //tvDeviceMac.setTextColor(Color.BLACK);
//            tvpaired.setVisibility(View.GONE);
//            tvrssi.setVisibility(View.VISIBLE);
//            tvrssi.setTextColor(Color.BLACK);
        }
        return vg;
    }
}