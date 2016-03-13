package com.sovate.workratemanager;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.sovate.workratemanager.bean.ActivityBaseInfo;
import com.sovate.workratemanager.bean.ActivityDevice;
import com.sovate.workratemanager.bean.ActivityDeviceStudentInfo;
import com.sovate.workratemanager.bean.DeviceInfo;
import com.sovate.workratemanager.bundle.SettingData;
import com.sovate.workratemanager.common.Singleton;
import com.sovate.workratemanager.network.HttpApi;
import com.sovate.workratemanager.network.HttpResponseCode;

import java.lang.reflect.Method;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {


    // Constants
    private static final String TAG = "MainActivity";
    private static final int REQUEST_SELECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int UART_PROFILE_CONNECTED = 20;
    private static final int UART_PROFILE_DISCONNECTED = 21;
    private static final int RESULT_SETTINGS = 22;

    // UART 시나리오 코드
    private static final int OPCODE_DEVICEDISCOVER = 200;
    private static final int OPCODE_COLLECTDATA = 201;
    private static final int OPCODE_DEVICERESET = 202;

    private int deviceOperationCode = OPCODE_DEVICEDISCOVER;


    // 네트워크 업로드 확인
    private static final int NETWORK_TRANSACTION_NOTWORKING = 300;
    private static final int NETWORK_TRANSACTION_WORKING = 301;
    private static final int NETWORK_TRANSACTION_COMPLETE = 302;

    private int networkTransactionStatus = NETWORK_TRANSACTION_NOTWORKING;

    // permission constants
    private final int MY_PERMISSION_GRANTED = 100;

    // Bluetooth variables
    private int mState = UART_PROFILE_DISCONNECTED;
    private UartService mService = null;
    private BluetoothDevice mDevice = null;
    private BluetoothAdapter mBtAdapter = null;

    // UI variables
    private ListView messageListView;
    private ArrayAdapter<String> listAdapter;
    private Button btnConnectDisconnect;
    private Button btnRemove;
    private final Handler handler = new Handler();
    private TextView txtviewDeviceStatus;


    private Spinner spinSchool;
    private Spinner spinGrade;
    private Spinner spinClass;
    private Spinner spinSport;


    // Paired Device List
    private ArrayList<DeviceInfo> deviceList;
    private DeviceAdapter deviceAdapter;
    private Button btnGetDeviceStudentInfo;
    private ListView pairedDeviceListView;


    // band interface ui
    private Button btnSendAI;
    private Button btnSendAE;
    private Button btnSendUB;




    // Connection variables
    public final static String deviceName = "InBodyBand";
    private Boolean isBonded = false;
    private byte[] resultString = new byte[2048]; // Receiving buffer
    private byte[] lastBuf = null; // Re-sending Frame in case transfer fails
    private int offset = 0; // Received Data offset
    private int waitCnt = 0; // Connection status monitoring variables


    // network result data
    List<ActivityDevice> listActivityDevice;
    List<ActivityDeviceStudentInfo> listActivityDeviceStudentInfo;

    ActivityBaseInfo baseInfo;

    // 어플이름
    String appName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        appName = getString(R.string.app_name);



        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        // spinner 설정
        Spinner spinSchool = (Spinner) findViewById(R.id.spinSchool);


        checkPermission();

        init();

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onPause() {

        super.onPause();

        // 설정 Activity로 갈때 OnPause의 상태가 된다.
        SharedPreferences pref = getSharedPreferences("Setting", 0);
        SharedPreferences.Editor edit = pref.edit();
        edit.putString("ServerUrl", HttpApi.BASE_URL);
        edit.commit();
    }

    /*
     * Kill the service when app terminated
     *
     * @see android.app.Activity#onDestroy()
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");

        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(UARTStatusChangeReceiver);
        } catch (Exception ignore) {
            Log.e(TAG, ignore.toString());
        }
        unbindService(mServiceConnection);
        mService.stopSelf();
        mService = null;

    }


    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        if (!mBtAdapter.isEnabled()) {
            Log.i(TAG, "onResume - BT not enabled yet");
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    /*
     * Receive from device list & Bluetooth Setting
     */

    // Activity Result function
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        // TODO 시나리오에 맞게 다른 용도로 변경 요망.

        // result code를 이용하지 않음.
        switch (requestCode) {

            // Case where found InBodyBAND is selected
            case REQUEST_SELECT_DEVICE:
                // When the DeviceListActivity return, with the selected device
                // address
                if (resultCode == Activity.RESULT_OK && data != null) {
                    String deviceAddress = data.getStringExtra(BluetoothDevice.EXTRA_DEVICE);
                    mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);

                    Log.d(TAG, "... onActivityResultdevice.address==" + mDevice + "mserviceValue" + mService);
                    ((TextView) findViewById(R.id.deviceName)).setText(mDevice.getName() + " - connecting");
                    mService.connect(deviceAddress);

                }
                break;

            // Case where Bluetooth turned on/off in the smartphone setting
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, "Bluetooth has turned on ", Toast.LENGTH_SHORT).show();

                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, "Problem in BT Turning ON ", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;

            // 설정화면 결과
            case RESULT_SETTINGS:
                Log.d(TAG, "RESULT_SETTINGS");
                loadPref();
                //Toast.makeText(this, "Setting Result ", Toast.LENGTH_SHORT).show();
                break;

            default:
                Log.e(TAG, "wrong request code");
                break;
        }
    }

    //============================================================================================
    // OnItemSelectedListener interface의 구현 코드
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        Toast.makeText(parent.getContext(),
                "OnItemSelectedListener : " + parent.getItemAtPosition(position).toString(),
                Toast.LENGTH_SHORT).show();

        Spinner spinner = (Spinner) parent;
        if(spinner.getId() == R.id.spinSchool){

            String schoolName = (String)spinner.getSelectedItem();
            if(schoolName != null && schoolName.length() > 0) {

                String[] arrGrad = Singleton.getInstance().getGradeNames(schoolName);

                // TODO 왜 null로 호출이 되는지 확인이 안됌....^^;
                if(arrGrad != null) {
                    ArrayAdapter adapter = new ArrayAdapter(this, R.layout.spin_center, arrGrad);
                    adapter.setDropDownViewResource(R.layout.spin_dropdown_center);
                    spinGrade.setAdapter(adapter);
                }

            }

        }
        else if(spinner.getId() == R.id.spinGrade){

            String schoolName = (String)spinSchool.getSelectedItem();
            String gradeName = (String)spinGrade.getSelectedItem();
            if(schoolName != null && schoolName.length() > 0
                && gradeName != null && gradeName.length() > 0) {

                String[] arrClass = Singleton.getInstance().getClassNames(schoolName, gradeName);

                // TODO 왜 null로 호출이 되는지 확인이 안됌....^^;
                if(arrClass != null) {
                    ArrayAdapter adapter = new ArrayAdapter(this, R.layout.spin_center, arrClass);
                    adapter.setDropDownViewResource(R.layout.spin_dropdown_center);
                    spinClass.setAdapter(adapter);
                }

            }
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    //============================================================================================

    private void loadPref(){


        SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String serverUrl = mySharedPreferences.getString("serverUrl", "");

        if(serverUrl != null && serverUrl.length() > 0){
            setServerUrl(serverUrl);
        }


    }

    private void setServerUrl(String serverUrl){
        HttpApi.BASE_URL = serverUrl;

        // Display title
        setTitle(appName + String.format(" ( %s ) ", HttpApi.BASE_URL));
    }


    @Override
    public void onBackPressed() {
        if (mState == UART_PROFILE_CONNECTED) {
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
            showMessage("운동량관리자가 백그라운드로 실행 중 입니다.\n             어플 종료를 위해 서비스를 종료하세요.");
        } else {
            new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle(R.string.popup_title)
                    .setMessage(R.string.popup_message)
                    .setPositiveButton(R.string.popup_yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    }).setNegativeButton(R.string.popup_no, null).show();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            SettingData data = new SettingData();

            data.setServerUrl(HttpApi.BASE_URL);

            // PreferenceSetting
            Intent i = new Intent(this, PreferenceSetting.class);

            Bundle extra = new Bundle();
            extra.putSerializable("settingdata", data);
            i.putExtras(extra);

            startActivityForResult(i, RESULT_SETTINGS);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    private void init() {

        loadPref();

        HttpApi.setMainActivity(this);

        // API의 기초 정보를 제공 받는다.
        HttpApi.getBaseInfoRequest();


        // Check Bluetooth available to use
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // device interface UI
        messageListView = (ListView) findViewById(R.id.listMessage);
        listAdapter = new ArrayAdapter<String>(this, R.layout.message_detail);
        messageListView.setAdapter(listAdapter);
        messageListView.setDivider(null);
        btnConnectDisconnect = (Button) findViewById(R.id.btn_select);
        btnRemove = (Button) findViewById(R.id.btn_remove);

        btnSendAI = (Button) findViewById(R.id.btn_sendAI);
        btnSendAE = (Button) findViewById(R.id.btn_sendAE);
        btnSendUB = (Button) findViewById(R.id.btn_sendUB);




        // 상단의 조회 UI
        btnGetDeviceStudentInfo = (Button) findViewById(R.id.btn_getDeviceStudentInfo);
        txtviewDeviceStatus = (TextView) findViewById(R.id.deviceName);



        spinSchool = (Spinner) findViewById(R.id.spinSchool);
        spinSchool.setOnItemSelectedListener(this);
        spinGrade = (Spinner) findViewById(R.id.spinGrade);
        spinGrade.setOnItemSelectedListener(this);
        spinClass = (Spinner) findViewById(R.id.spinClass);
        spinClass.setOnItemSelectedListener(this);

        spinSport = (Spinner) findViewById(R.id.spinSport);

        pairedDeviceListView = (ListView) findViewById(R.id.new_devices);
        deviceList = new ArrayList<DeviceInfo>();
        deviceAdapter = new DeviceAdapter(this, deviceList);

        pairedDeviceListView.setAdapter(deviceAdapter);
        pairedDeviceListView.setOnItemClickListener(mDeviceClickListener);


        service_init();

        // Handler Remove button
        btnRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // unpair는 사용하지 않도록 구성
                // unpairDevice(GetPairedBLEDevice(deviceName));
                //Toast.makeText(v, "not support", Toast.LENGTH_SHORT).show();
                //Toast.makeText(this, "Bluetooth has turned on ", Toast.LENGTH_SHORT).show();
                Toast.makeText(getApplicationContext(), "not support", Toast.LENGTH_SHORT).show();
            }
        });

        // Handler Disconnect & Connect button
        btnConnectDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mBtAdapter.isEnabled()) {
                    Log.i(TAG, "onClick - BT not enabled yet");
                    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                } else {
                    if (btnConnectDisconnect.getText().equals("Connect")) {

                        if(mDevice == null || mDevice.getName() == null || mDevice.getName().length() == 0 ) {
                            Toast.makeText(getApplicationContext(), "선택된 디바이이스가 없습니다.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // 기존 로그를 모두 삭제 처리 : 개별 처리를 할때 삭제늘 할지 않도록 구성해야 할듯..
                        listAdapter.clear();

                        // Start searching if no InBodyBAND connected

                        // TODO 연결이 되는 상황에서는 모두 삭제가 됨.
                        isBonded = true;
                        Log.d(TAG, "... onActivityResultdevice.address==" + mDevice + "mserviceValue" + mService);
                        ((TextView) findViewById(R.id.deviceName)).setText(mDevice.getName() + " - connecting");
                        mService.connect(mDevice.getAddress());

                    } else {
                        // Disconnect button pressed
                        if (mDevice != null) {
                            mService.disconnect();
                        }
                    }
                }
            }
        });


        btnSendAI.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO 연결상태와 데이터가 처리 가능한 상태인지 모두 체크를 해야 하는데 가능한지 모르겠음..
                // AK의 처리 방식도 필요 아마도 헬스체크일 것으로 보임....

                deviceOperationCode = OPCODE_DEVICEDISCOVER;
                btnConnectDisconnect.performClick();
            }
        });

        btnSendAE.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO 연결상태와 데이터가 처리 가능한 상태인지 모두 체크를 해야 하는데 가능한지 모르겠음..
                // AK의 처리 방식도 필요 아마도 헬스체크일 것으로 보임....

                deviceOperationCode = OPCODE_COLLECTDATA;
                btnConnectDisconnect.performClick();
            }
        });

        btnSendUB.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO 연결상태와 데이터가 처리 가능한 상태인지 모두 체크를 해야 하는데 가능한지 모르겠음..
                // AK의 처리 방식도 필요 아마도 헬스체크일 것으로 보임....

                deviceOperationCode = OPCODE_DEVICERESET;
                btnConnectDisconnect.performClick();
            }
        });



        // 학교, 학년, 반을 기준으로 기기의 등록정보를 조회함.
        // 등록된 기기을 기준으로 구성된 학생 정보를 가져온다.
        btnGetDeviceStudentInfo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                v.setEnabled(false);

                String schoolName = (String)spinSchool.getSelectedItem();
                String grade = (String)spinGrade.getSelectedItem();
                String classNumber = (String)spinClass.getSelectedItem();

                String[] schoolGradeId =  Singleton.getInstance().getSchoolGradeId(schoolName, grade);

                if(schoolGradeId != null) {
                    HttpApi.getDevicesStudentMapRequest(schoolGradeId[0], schoolGradeId[1], classNumber);
                    //HttpApi.getDevicesStudentMapRequest("16", "7", "1");
                }
                else {
                    Toast.makeText(getApplicationContext(), "올바른 데이터가 없어, 조회할 수 없습니다.", Toast.LENGTH_SHORT).show();
                }

            }
        });

        // Set initial UI state
    }

    //==============================================================================================
    // http 통신 데이터를 처리 함수

    public void setListActivityDevice(List<ActivityDevice> list)
    {
        listActivityDevice = list;
    }

    public void responseGetBaseInfo(ActivityBaseInfo baseInfo, Throwable t)
    {
        if(t != null){
            // 요청 실패임.
            Log.e(TAG, "onFailure");
            Log.e(TAG, t.getMessage());
            return;
        }

        // TODO spinner controll의 값의 설정

        Singleton.getInstance().setActivityBaseInfo(baseInfo);

        // 학교 spinner의 설정 처리
        String[] arrSchool = Singleton.getInstance().getSchoolNames();
        ArrayAdapter adapter = new ArrayAdapter(this, R.layout.spin, arrSchool);
        adapter.setDropDownViewResource(R.layout.spin_dropdown);
        spinSchool.setAdapter(adapter);
        spinSchool.setSelection(13);

        // 스포츠 항목 추가
        String[] arrSport = Singleton.getInstance().getSportNames();
        ArrayAdapter adapterSport = new ArrayAdapter(this, R.layout.spin, arrSport);
        adapterSport.setDropDownViewResource(R.layout.spin_dropdown);
        spinSport.setAdapter(adapterSport);





    }

    public void responseGetDevicesStudentMap(List<ActivityDeviceStudentInfo> list, Throwable t)
    {

        // button enable
        btnGetDeviceStudentInfo.setEnabled(true);

        if(t != null){
            // 요청 실패임.
            Log.e(TAG, "onFailure");
            Log.e(TAG, t.getMessage());
            return;
        }

        listActivityDeviceStudentInfo = list;

        // get device list
        GetPairedBLEDevices(MainActivity.deviceName);

    }

    public void responsePostWorkrate(int responseCode, Throwable t)
    {
        // trasaction complete 처리를 한다.
        networkTransactionStatus = NETWORK_TRANSACTION_COMPLETE;
        Log.i(TAG, "responsePostWorkrate");

        if(t != null){
            // 요청 실패임.
            Log.e(TAG, "onFailure");
            Log.e(TAG, t.getMessage());
            return;
        }

        // TODO workRate의 등록 완료 메시지 처리
        // 향후 배치 처리로 한방에 올리도록 구성
        // 500 create
        if(responseCode == HttpResponseCode.SC_CREATED){
            Toast.makeText(this, "생성 되었습니다.", Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(this, "생성이 실패했습니다. (response code : " + responseCode + ")", Toast.LENGTH_SHORT).show();
        }


    }


    //==============================================================================================
    // bean 데이터를 처리하는 함수
    private ActivityDeviceStudentInfo getDeviceStudentInfo(String mac){

        // TODO : listActivityDeviceStudentInfo 의 null 체크 필요
        for (ActivityDeviceStudentInfo info: listActivityDeviceStudentInfo) {
            if(info.getMac().equalsIgnoreCase(mac))
                return info;
        }

        ActivityDeviceStudentInfo empty = new ActivityDeviceStudentInfo();

        return empty;
    }

    //===========================================================
    // Device Commucation Function


    // Connect to UART service
    private void service_init() {
        Intent bindIntent = new Intent(this, UartService.class);
        bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

        LocalBroadcastManager.getInstance(this).registerReceiver(UARTStatusChangeReceiver,
                makeGattUpdateIntentFilter());
    }


    // UART service event filter
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UartService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(UartService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(UartService.DEVICE_DOES_NOT_SUPPORT_UART);
        return intentFilter;
    }

    // TODO UART 서비스의 중요 핸들러임......
    // Event handler function received from UART service
    private final BroadcastReceiver UARTStatusChangeReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(UartService.ACTION_GATT_CONNECTED)) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        Log.d(TAG, "UART_CONNECT_MSG");
                        btnConnectDisconnect.setText("Disconnect");

                        ((TextView) findViewById(R.id.deviceName)).setText(mDevice.getName() + " - ready");
                        listAdapter.add("[" + currentDateTimeString + "] Connected to: " + mDevice.getName());
                        messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                        mState = UART_PROFILE_CONNECTED;

                        String _deviceAddress = "";
                        if (null != mDevice)
                            _deviceAddress = mDevice.getAddress(); // null 가능성
                        // null
                        // possibility

                        Log.d(TAG, "UART_CONNECT_MSG[UART_PROFILE_CONNECTED==" + mState + ", DeviceAddress:"
                                + _deviceAddress + "]");
                    }
                });
            }

            if (action.equals(UartService.ACTION_GATT_DISCONNECTED)) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        Log.d(TAG, "UART_DISCONNECT_MSG");
                        btnConnectDisconnect.setText("Connect");

                        ((TextView) findViewById(R.id.deviceName)).setText("Not Connected");
                        listAdapter.add("[" + currentDateTimeString + "] Disconnected to: " + mDevice.getName());
                        mState = UART_PROFILE_DISCONNECTED;
                        mService.close();
                    }
                });
            }

            if (action.equals(UartService.ACTION_GATT_SERVICES_DISCOVERED)) {
                mService.enableTXNotification();

                // Device bonding when service connected
                if (!isBonded)
                    pairDevice(mDevice);

                // set AK frame
                lastBuf = new byte[] { 0x02, 0x48, 0x0C, 0x0A, 0x41, 0x4B, 0x34, 0x03 };
                checkComm();
            }

            if (action.equals(UartService.ACTION_DATA_AVAILABLE)) {

                // Start reading data after connected
                if (isBonded) {
                    ReadData(intent);
                }
            }

            if (action.equals(UartService.DEVICE_DOES_NOT_SUPPORT_UART)) {
                showMessage("Device doesn't support UART. Disconnecting");
                mService.disconnect();
            }

        }
    };

    // TODO pairing의 절차 문제를 확이니 요망.
    // Bonding event receiver.
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);

                switch (state) {
                    case BluetoothDevice.BOND_BONDING:
                        break;
                    case BluetoothDevice.BOND_BONDED:
                        isBonded = true;
                        unregisterReceiver(mReceiver);
                        fncSendCommand((byte) 'A', (byte) 'K', null);
                        break;

                    case BluetoothDevice.BOND_NONE:
                        break;
                }
            }
        }
    };


    // Bonding Bluetooth device
    private void pairDevice(final BluetoothDevice device) {
        showMessage("Wearable Device Bonding...");
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
        try {
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //===========================================================

    private void GetPairedBLEDevices(String DeviceName) {

        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

        if (pairedDevices.size() != 0) {

            deviceList.clear();
            for (BluetoothDevice device : pairedDevices) {
                if (device.getName() == null)
                    continue;
                if (DeviceName.equals(device.getName()))
                {
                    // add list

                    DeviceInfo info = new DeviceInfo();
                    info.setDevice(device);
                    info.setDeviceStudentInfo(getDeviceStudentInfo(device.getAddress()));
                    deviceList.add(info);
                }
            }
            deviceAdapter.notifyDataSetChanged();
        }
    }

    // UART service connected/disconnected
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            mService = ((UartService.LocalBinder) rawBinder).getService();
            Log.d(TAG, "onServiceConnected mService= " + mService);
            if (!mService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
        }

        public void onServiceDisconnected(ComponentName classname) {
            mService = null;
        }
    };

    // Item click의 처리 함수
    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            // TODO 기존 연결 제거 및 정보 초기화 처리

            if(mService != null && mDevice != null){
                // 초기화 처리
                mService.disconnect();
                mDevice = null;
            }

            String deviceAddress = deviceList.get(position).getDevice().getAddress();

            mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);

            //mDevice = deviceList.get(position).getDevice();

            // select text view에 해당 내용을 전달 하도록 구성
            txtviewDeviceStatus.setText(mDevice.getAddress());

//            mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);
//
//            Log.d(TAG, "... onActivityResultdevice.address==" + mDevice + "mserviceValue" + mService);


//            Bundle b = new Bundle();
//            b.putString(BluetoothDevice.EXTRA_DEVICE, deviceList.get(position).getAddress());
//
//            Intent result = new Intent();
//            result.putExtras(b);
//            setResult(Activity.RESULT_OK, result);
//            finish();
        }
    };

    //============================================================
    // Utility 함수

    // Toast messege
    private void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }


    //============================================================
    // UART 로직 함수

    // No response check
    private void checkComm() {
        handler.postDelayed(new Runnable() {
            public void run() {
                if (lastBuf != null && mService != null && mService.mConnectionState == UartService.STATE_CONNECTED
                        && mState == UART_PROFILE_CONNECTED) {
                    waitCnt++;
                    if (waitCnt > 4) {
                        if (mDevice != null) {
                            Log.e(TAG, "TIMEOUT");
                            // 요청사항이 느릴수도 있음.... 확인 요망.
                            //mService.disconnect();
                        }
                    } else if (waitCnt == 3) {
                        listAdapter.add("Resend buffer");
                        SendData(lastBuf);
                        checkComm();
                    } else {
                        checkComm();
                    }
                }
            }
        }, 1000);
    }


    // garbage data trim.
    private void Trim() {
        if (resultString[0] == 0x02)
            return;
        int cnt = 0;
        for (int i = 0; i < offset; i++)
            if (resultString[i] == 0x02) {
                cnt = i;
                break;
            }
        for (int j = 0; j < resultString.length; j++) {
            if (j < offset)
                resultString[j] = resultString[j + cnt];
            else
                resultString[j] = 0x00;
        }

        offset = offset - cnt;
    }


    // Make data frame
    private void fncSendCommand(byte cmd1, byte cmd2, byte[] data) {
        if (data == null) {
            data = new byte[] {};
        }
        byte[] Buf = new byte[8 + data.length];
        Buf[0] = 0x02;
        Buf[1] = 'W';
        Buf[2] = (byte) (((Buf.length - 6) & 0x3f) + 0x0a);
        Buf[3] = (byte) ((((Buf.length - 6) >> 6) & 0x3f) + 0x0a);
        Buf[4] = cmd1;
        Buf[5] = cmd2;

        for (int i = 0; i < data.length; i++)
            Buf[6 + i] = data[i];

        // checksum
        Buf[Buf.length - 2] = 0x00;
        for (int i = 1; i < Buf.length - 2; i++) {
            Buf[Buf.length - 2] += Buf[i];
        }
        Buf[Buf.length - 2] = (byte) ((Buf[Buf.length - 2] & 0x3F) + 0X0A);

        Buf[Buf.length - 1] = 0x03;

        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
        }

        String log = "";
        for (int i = 0; i < Buf.length; i++)
            log += String.format("%02X ", Buf[i]);

        listAdapter.add("Snd : " + log);
        messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
        lastBuf = Buf;
        SendData(Buf);
    }

    // Command 처리
    // AI command maker
    private byte[] MakeAI() {
        Calendar c = Calendar.getInstance();
        int yyyy = c.get(Calendar.YEAR);
        int mm = c.get(Calendar.MONTH) + 1;
        int dd = c.get(Calendar.DAY_OF_MONTH);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int min = c.get(Calendar.MINUTE);
        int sec = c.get(Calendar.SECOND);
        int weekDay = c.get(Calendar.DAY_OF_WEEK) - 1;

        double weight = 60;
        double height = 175;

        byte[] Buf = new byte[13];

        Buf[0] = (byte) (yyyy - 2000);
        Buf[1] = (byte) (mm);
        Buf[2] = (byte) (dd);
        Buf[3] = (byte) (weekDay);

        Buf[4] = (byte) (hour);
        Buf[5] = (byte) (min);
        Buf[6] = (byte) (sec);

        Buf[7] = (byte) ((int) ((weight * 10.0) / 256));
        Buf[8] = (byte) ((int) (weight * 10.0) % 256);
        Buf[9] = (byte) ((int) ((height * 10.0) / 256));
        Buf[10] = (byte) ((int) (height * 10.0) % 256);
        Buf[11] = 0x01;
        Buf[12] = (byte) (35); // Age
        return Buf;
    }



    // Send Data
    private void SendData(byte[] value) {
        //
        if (value.length > 20) {
            byte[] temp = new byte[20];
            for (int i = 0; i < value.length / 20 + 1; i++) {
                int j = 0;
                for (j = 0; j < 20 && value.length > j + 20 * i; j++) {
                    temp[j] = value[j + 20 * i];
                }

                if (j != 19) {
                    temp = new byte[j];
                    for (int k = 0; k < j; k++)
                        temp[k] = value[k + 20 * i];
                }

                if (mService != null)
                    mService.writeRXCharacteristic(temp);
            }
        } else {
            if (mService != null)
                mService.writeRXCharacteristic(value);
        }
    }


    // Read Data
    private void ReadData(final Intent intent) {
        runOnUiThread(new Runnable() {
            public void run() {
                byte[] readBuf = intent.getByteArrayExtra(UartService.EXTRA_DATA);
                int byteCnt = readBuf.length;

                try {

                    for (int i = 0; i < byteCnt; i++) {
                        resultString[offset + i] = readBuf[i];
                    }
                    offset += byteCnt;
                    Trim();

                    int nbyte = 0;
                    byte[] frame = null;
                    while (true) {
                        frame = new byte[offset];
                        for (byteCnt = 0; byteCnt < offset; byteCnt++) {
                            frame[byteCnt] = resultString[byteCnt];
                            if (frame[0] == 0x02 && byteCnt > 2) {
                                if (nbyte == 0) {
                                    nbyte = ((frame[2] - 0x0a) & 0x3f) + (((frame[3] - 0x0a) & 0x3f) << 6) + 4;
                                }
                                if (byteCnt > nbyte && resultString[byteCnt] == 0x03)
                                    break;
                            }
                        }
                        if (offset == byteCnt) {
                            break;
                        }

                        for (int j = 0; j < resultString.length; j++) {
                            if (j < byteCnt + 1) // Frame Shift
                                resultString[j] = resultString[j + byteCnt + 1];
                            else
                                resultString[j] = 0x0;
                        }
                        offset -= (byteCnt + 1);

                        String text = "";
                        for (int i = 0; i < frame.length; i++){
                            text += String.format("%02X ", frame[i]);
                        }

                        listAdapter.add("Rcv : " + text);
                        messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);

                        waitCnt = 0;
                        lastBuf = null;

                        // 0x41 0x4B : 초기 연결 시 데이터 전송 : 인바디 데이터 전달
                        // 0x41 0x57 : 인바디 검사 대기 상태일 때 주고 받는 메시지
                        // 0x41 0x49 // 기기 설정 정보 전달 (시간 및 체중, 키 등..)가 진동하는 효과가 있음. --> 기기 확인시에 필요함.

                        if(deviceOperationCode == OPCODE_DEVICEDISCOVER){
                            if (frame[4] == 'A' && frame[5] == 'K') {
                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                }
                                fncSendCommand((byte) 'A', (byte) 'W', null);
                            } else if (frame[4] == 'A' && frame[5] == 'W') {
                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                }

                                fncSendCommand((byte) 'A', (byte) 'I', MakeAI());
                            } else if (frame[4] == 'A' && frame[5] == 'I') {
                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                }

                                fncSendCommand((byte) 'A', (byte) 'W', null);
                            }
                        }
                        else if(deviceOperationCode == OPCODE_COLLECTDATA){

                            if (frame[4] == 'A' && frame[5] == 'K') {
                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                }

                                networkTransactionStatus = NETWORK_TRANSACTION_NOTWORKING;

                                fncSendCommand((byte) 'A', (byte) 'W', null);
                            } else if (frame[4] == 'A' && frame[5] == 'W') {
                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                }

                                fncSendCommand((byte) 'A', (byte) 'I', MakeAI());
                            } else if (frame[4] == 'A' && frame[5] == 'I') {
                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                }

                                Log.e(TAG, "==================== networkTransactionStatus : " + networkTransactionStatus);
                                if(networkTransactionStatus == NETWORK_TRANSACTION_NOTWORKING){
                                    fncSendCommand((byte) 'A', (byte) 'E', null);
                                } if(networkTransactionStatus == NETWORK_TRANSACTION_COMPLETE){
                                    // TODO 리셋처리를 한다.
                                    networkTransactionStatus = NETWORK_TRANSACTION_NOTWORKING;
                                    if (mDevice != null) {
                                        mService.disconnect();
                                    }
                                }
                                else {
                                    fncSendCommand((byte) 'A', (byte) 'W', null);
                                }

                            } else if (frame[4] == 'A' && frame[5] == 'E') { // 0x41 0x45 : 최종 할동량 전송 처리

                                int startPosData = 6;

                                int year = (int)frame[startPosData + 0] + 2000;
                                int month = (int)frame[startPosData + 1];
                                int day = (int)frame[startPosData + 2];
                                int hour = (int)frame[startPosData + 3];

                                int steps = ((frame[startPosData + 4] & 0xff) << 8) | (frame[startPosData + 5] & 0xff);
                                int runCount = ((frame[startPosData + 6] & 0xff) << 8) | (frame[startPosData + 7] & 0xff);

                                int stepMinute = ((frame[startPosData + 8] & 0xff) << 8) | (frame[startPosData + 9] & 0xff);
                                int runMinute = ((frame[startPosData + 10] & 0xff) << 8) | (frame[startPosData + 11] & 0xff);

                                int stepCalorie = ((frame[startPosData + 12] & 0xff) << 8) | (frame[startPosData + 13] & 0xff);
                                int runCalorie = ((frame[startPosData + 14] & 0xff) << 8) | (frame[startPosData + 15] & 0xff);

                                int stepDistance = ((frame[startPosData + 16] & 0xff) << 8) | (frame[startPosData + 17] & 0xff);
                                int runDistance = ((frame[startPosData + 18] & 0xff) << 8) | (frame[startPosData + 19] & 0xff);


                                String collectDate = String.format("%d-%02d-%02d %02d:00:00", year, month, day, hour);
                                String log = String.format("날짜 : %s\n", collectDate);
                                log += String.format("걸음수 : %d, 뜀수 : %d, 걸은시간 : %d, 뛴시간  : %d \n", steps, runCount, stepMinute, runMinute);
                                log += String.format("걸음 칼로리 : %d, 뜀 칼로리 : %d, 걸은 거리 : %d, 뛴 거리  : %d \n", stepCalorie, runCalorie, stepDistance, runDistance);

                                Log.e(TAG, log);


                                // device apapter 변경 처리 함.
                                String sportName = (String)spinSport.getSelectedItem();
                                String sportId = Singleton.getInstance().getSportId(sportName);

                                // TODO 요청 처리를 변경 요망.
                                boolean result =  deviceAdapter.setWorkrate(mDevice.getAddress(),
                                        collectDate,
                                        stepCalorie+runCalorie, steps+runCount, stepDistance + runDistance,
                                        sportId);

                                if(result){
                                    networkTransactionStatus = NETWORK_TRANSACTION_WORKING;
                                }

                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                }

                                // TODO 종료코드로 리셋 처리도 가능할 수 있게 구성 요망.
                                fncSendCommand((byte) 'A', (byte) 'W', null);
                            }
                        }
                        else if(deviceOperationCode == OPCODE_DEVICERESET){

                            if (frame[4] == 'A' && frame[5] == 'K') {
                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                }
                                fncSendCommand((byte) 'A', (byte) 'W', null);
                            } else if (frame[4] == 'A' && frame[5] == 'W') {
                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                }

                                fncSendCommand((byte) 'U', (byte) 'B', null);
                            }  else if (frame[4] == 'U' && frame[5] == 'B') { // 0x55 0x42
                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                }
                                // connection 종료 처리 : 초기화가 된다.
                                //fncSendCommand((byte) 'M', (byte) 'E', null);
                            } else if (frame[4] == 'U' && frame[5] == 'E') {
                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                }
                                // 클라이언트에서 연결 종료 처리 함.
                            }
                        }


                    }
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                }
            }
        });
    }

    //=================================================================
    // 권한 획득 함수
    // 권한확인 코드
    private void checkPermission() {
        Log.i("", "!!!!! CheckPermission : " + ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION));
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //사용권한이 없을경우

            //최초권한 요청인지 , 사용자에 의한 재요청인지 확인
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                Log.e("", "@@@@@@@@ permission  재요청");
            }

            //최초로 권한을 요청하는경우(처음실행)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,}, MY_PERMISSION_GRANTED);


        } else {
            //사용 권한이 있는경우
            Log.e("", "@@@@@@@@@@@@@ ermission deny");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_GRANTED:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "권한 획득", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "권한 허용을 선택하지않은경우 정상동작을 보장할수없습니다.", Toast.LENGTH_LONG).show();
                }
                break;
        }


    }

}
