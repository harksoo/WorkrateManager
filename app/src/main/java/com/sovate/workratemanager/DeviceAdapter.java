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

        BluetoothDevice device = devices.get(position).getDevice();
        ActivityDeviceStudentInfo deviceStudentInfo = devices.get(position).getDeviceStudentInfo();
        ActivityWorkRate workRate =  devices.get(position).getWorkRate();

        final TextView tvuserName = ((TextView) vg.findViewById(R.id.userName));
        final TextView tvadd = ((TextView) vg.findViewById(R.id.address));
        final TextView tvname = ((TextView) vg.findViewById(R.id.name));

        final TextView tvCollectDate = ((TextView) vg.findViewById(R.id.collectDate));
        final TextView tvCalorie = ((TextView) vg.findViewById(R.id.calorie));
        final TextView tvSteps = ((TextView) vg.findViewById(R.id.steps));
        final TextView tvDistance = ((TextView) vg.findViewById(R.id.distance));

        //final TextView tvpaired = (TextView) vg.findViewById(R.id.paired);
        //final TextView tvrssi = (TextView) vg.findViewById(R.id.rssi);

        //tvrssi.setVisibility(View.VISIBLE);

        //String deviceAddress = device.getAddress();

        //byte rssival = (byte) devRssiValues.get(device.getAddress()).intValue();
//        byte rssival = 1;
//        if (rssival != 0) {
//            tvrssi.setText("Rssi = " + String.valueOf(rssival));
//        }


        tvuserName.setText(deviceStudentInfo.getUserName());
        tvname.setText(device.getName());
        tvadd.setText(device.getAddress());

        if(workRate.getCalorie().length() > 0){
            tvCollectDate.setText("T:" + workRate.getCollectDt().substring(0, 12));
            tvCalorie.setText("C:" + workRate.getCalorie());
            tvSteps.setText("S:" + workRate.getSteps());
            tvDistance.setText("D:" + workRate.getDistance());
        } else {
            tvCollectDate.setText("");
            tvCalorie.setText("");
            tvSteps.setText("");
            tvDistance.setText("");
        }


        if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
            Log.i(TAG, "device::" + device.getName());
//            tvname.setTextColor(Color.WHITE);
//            tvadd.setTextColor(Color.WHITE);
//            tvpaired.setTextColor(Color.GRAY);
//            tvpaired.setVisibility(View.VISIBLE);
//            tvpaired.setText(R.string.paired);
//            tvrssi.setVisibility(View.VISIBLE);
//            tvrssi.setTextColor(Color.WHITE);

            tvuserName.setTextColor(Color.BLACK);
            tvname.setTextColor(Color.BLACK);
            tvadd.setTextColor(Color.BLACK);

//            tvCalorie.setTextColor(Color.BLACK);
//            tvSteps.setTextColor(Color.BLACK);
//            tvDistance.setTextColor(Color.BLACK);


        } else {
            tvuserName.setTextColor(Color.BLACK);
            tvname.setTextColor(Color.BLACK);
            tvadd.setTextColor(Color.BLACK);
//            tvpaired.setVisibility(View.GONE);
//            tvrssi.setVisibility(View.VISIBLE);
//            tvrssi.setTextColor(Color.BLACK);
        }
        return vg;
    }
}