package com.android.iris.bleperipheral_test;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.charset.Charset;
import java.util.UUID;

public class AdvertiseActivity extends AppCompatActivity {
    private Button btn_Advertise;
    private TextView txt_Data;
    private BluetoothLeAdvertiser mBleAdvertiser;
    private BluetoothGattServer mBluetoothGattServer;
    private BluetoothAdapter mBluetoothAdapter;
    private final  String TAG = "TAG_Advertise";
    String SERVICE_HEART_RATE = "0000180D-0000-1000-8000-00805F9B34FB";
    String CHAR_BODY_SENSOR_LOCATION_READ = "00002A38-0000-1000-8000-00805F9B34FB";

//    String strRandomService = "0000e7f7-0000-1000-8000-00805F9B34FB";
//    String strRandomChar = UUID.randomUUID().toString();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advertise);

        //get components from layout
        btn_Advertise = (Button) findViewById(R.id.btn_Advertise);
        txt_Data = (TextView) findViewById(R.id.txt_Data);

        //check phone to support peripheral mode
        if(!isSupportPeripheral()){
            Toast.makeText(this, "Device dose't support Peripheral Mode.", Toast.LENGTH_SHORT).show();
            return;
        }

        //get bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothAdapter.setName("zen");

        //set btn on click action
        btn_Advertise.setOnClickListener(btn_Advertise_Listener);
    }

    /**
     * check the device support peripheral mode*/
    public boolean isSupportPeripheral(){
        if(!BluetoothAdapter.getDefaultAdapter().isMultipleAdvertisementSupported()) {
            return  false;
        }
        return true;
    }

    /**
     * prepare to advertise*/
    Button.OnClickListener btn_Advertise_Listener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            String strData;

            //start to advertise
            strData = startAdvertise();
            txt_Data.setText(strData);
        }
    };

    /**
     * start to advertise*/
    public String startAdvertise(){
        String strData = "TestData";

        //get advertiser
        mBleAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();

        //set gatt service
        setService();

        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                .setConnectable(true)
//                .setTimeout(100000)
                .build();

        ParcelUuid pUuid = new ParcelUuid(UUID.fromString(SERVICE_HEART_RATE));

        AdvertiseData data = new AdvertiseData.Builder()
                .setIncludeDeviceName(true)
                .setIncludeTxPowerLevel(false)
                .addServiceUuid(pUuid)
                .addServiceData(pUuid, strData.getBytes(Charset.forName("UTF-8")))
                .build();

        mBleAdvertiser.startAdvertising(settings, data, mAdvertiseCallback);

        return data.toString();
    }

    /**
     * stop to advertise*/
    public void stopAdvertise() {
        if (mBleAdvertiser != null) {
            mBleAdvertiser.stopAdvertising(mAdvertiseCallback);
            mBleAdvertiser = null;
        }
    }

    /**
     *set service of the device*/
    public void setService(){
        //get Gatt Server
        mBluetoothGattServer = ((BluetoothManager)getSystemService(this.BLUETOOTH_SERVICE)).openGattServer(this, mBluetoothGattServerCallback);

        //char's setting
//        BluetoothGattCharacteristic char_BodySensorLocation = new BluetoothGattCharacteristic(
//                UUID.fromString(CHAR_BODY_SENSOR_LOCATION_READ),
//                BluetoothGattCharacteristic.PROPERTY_READ,
//                BluetoothGattCharacteristic.PERMISSION_READ);
//
//        char_BodySensorLocation.setValue("HELLOHELLO");
//
//        BluetoothGattService  service_Heart_rate = new BluetoothGattService(
//                UUID.fromString(SERVICE_HEART_RATE),
//                BluetoothGattService.SERVICE_TYPE_PRIMARY);
//
//        service_Heart_rate.addCharacteristic(char_BodySensorLocation);
//        mBluetoothGattServer.addService(service_Heart_rate);
//        Toast.makeText(getApplicationContext(),"setService success", Toast.LENGTH_LONG).show();


        BluetoothGattService  service_HeartRate = new BluetoothGattService(
                UUID.fromString(SERVICE_HEART_RATE),
                BluetoothGattService.SERVICE_TYPE_PRIMARY);

        BluetoothGattCharacteristic char_BodySensorLocation = new BluetoothGattCharacteristic(
                UUID.fromString(CHAR_BODY_SENSOR_LOCATION_READ),
                BluetoothGattCharacteristic.PROPERTY_READ,
                BluetoothGattCharacteristic.PERMISSION_READ);

        char_BodySensorLocation.setValue("HELLOHELLO");
        service_HeartRate.addCharacteristic(char_BodySensorLocation);
        mBluetoothGattServer.addService(service_HeartRate);

        Toast.makeText(getApplicationContext(),"setService success", Toast.LENGTH_SHORT).show();
    }

    AdvertiseCallback mAdvertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
            Log.d(TAG, "AdvertiseCallback Success");
            Toast.makeText(getApplicationContext(), "Advertise success", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);
            String strError = "";
            if(errorCode == AdvertiseCallback.ADVERTISE_FAILED_INTERNAL_ERROR){
                strError = "ADVERTISE_FAILED_INTERNAL_ERROR";
            }
            if(errorCode == AdvertiseCallback.ADVERTISE_FAILED_ALREADY_STARTED){
                strError = "ADVERTISE_FAILED_ALREADY_STARTED";
            }
            if(errorCode == AdvertiseCallback.ADVERTISE_FAILED_DATA_TOO_LARGE){
                strError = "ADVERTISE_FAILED_DATA_TOO_LARGE";
            }
            if(errorCode == AdvertiseCallback.ADVERTISE_FAILED_FEATURE_UNSUPPORTED){
                strError = "ADVERTISE_FAILED_FEATURE_UNSUPPORTED";
            }
            if(errorCode == AdvertiseCallback.ADVERTISE_FAILED_TOO_MANY_ADVERTISERS){
                strError = "ADVERTISE_FAILED_TOO_MANY_ADVERTISERS";
            }
            Log.d(TAG, "Advertising onStartFailure: " + errorCode + "-" + strError);
            Toast.makeText(getApplicationContext(), "Advertising onStartFailure: " + errorCode + "-" + strError, Toast.LENGTH_SHORT).show();
        }
    };

    BluetoothGattServerCallback mBluetoothGattServerCallback = new BluetoothGattServerCallback() {

        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            super.onConnectionStateChange(device, status, newState);
            Log.d(TAG, "servercallback onConnectionStateChange:");
            if(status == BluetoothGatt.GATT_SUCCESS){
                if(newState == BluetoothProfile.STATE_CONNECTED){
                    Log.d(TAG, "servercallback connected: "+ device.getAddress());
                }else{
                    Log.d(TAG, "servercallback disconnected: "+ newState);
                }
            }else{
                Log.d(TAG, "servercallback BluetoothGatt status: "+ status + "newState: " + newState);
            }

        }

        @Override
        public void onServiceAdded(int status, BluetoothGattService service) {
            super.onServiceAdded(status, service);
            Log.d(TAG, "Our gatt server service was added.");
            Log.d(TAG, service.getUuid().toString());


        }

        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
            Log.d(TAG, "Our gatt characteristic was read.");
            mBluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, characteristic.getValue());
        }
    };

}
