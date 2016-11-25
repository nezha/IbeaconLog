package edu.jiangnan.ibeaconlog;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import android.renderscript.Byte2;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang3.ArrayUtils;


public class MainActivity extends Activity {
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private Handler mHandler;
    private TextView ble;

    private final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ble = (TextView)findViewById(R.id.ble);


        mHandler = new Handler();
        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)){
            Toast.makeText(this,"ble_not_supported",Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
//        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();


        if(mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()){
            Toast.makeText(this,"please open the bluetooth function",Toast.LENGTH_SHORT).show();
            Intent btIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(btIntent, 1);
        }
        checkBluetoothPermission();
//        scanLeDevice(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /*
       校验蓝牙权限
      */
    private void checkBluetoothPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            //校验是否已具有模糊定位权限
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
            } else {
                //具有权限
//                connectBluetooth();
                scanLeDevice(true);
            }
        } else {
            //系统不高于6.0直接执行
//            connectBluetooth();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        doNext(requestCode, grantResults);
    }

    private void doNext(int requestCode, int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //同意权限
                // Permission Granted
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
//                connectBluetooth();
                scanLeDevice(true);
            } else {
                // 权限拒绝
                // 下面的方法最好写一个跳转，可以直接跳转到权限设置页面，方便用户
                Toast.makeText(this, "ACCESS_COARSE_LOCATION Denied", Toast.LENGTH_SHORT).show();
//                denyPermission();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void scanLeDevice(final boolean enable){
        if(enable){
//            mBluetoothLeScanner.startScan(mScanCallback);
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        }else {
//            mBluetoothLeScanner.stopScan(mScanCallback);
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }



    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            Log.i("StartScan","BLE Record>>>>>>>"+result.getDevice().toString());
            result.getDevice().getUuids();
        }
    };

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback(){
        @Override
        public void onLeScan(final BluetoothDevice bluetoothDevice, final int i, final byte[] bytes) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ArrayUtils.reverse(bytes);// 数组反转
                    // 将Byte数组的数据以十六进制表示并拼接成字符串
                    StringBuffer str = new StringBuffer();
                    int j = 0;
                    for (byte b : bytes) {
                        j = (b & 0xff);
                        str.append(Integer.toHexString(j));
                    }
                    String struuid = str.toString();
//                    String struuid = bytes2HexString(bytes).replace("-", "").toLowerCase();
                    Log.i("搜索蓝牙设备信息", "device=" + bluetoothDevice.getName() + ";rssi=" + i + ";scanRecord=" + struuid);
                    ble.setText("device=" + bluetoothDevice.getName() + ";rssi=" + i + ";scanRecord=" + struuid);
                }
            });


        }

    };


}
