package cz.atha.iotoys;

import android.bluetooth.BluetoothDevice;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.os.Bundle;

import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothClassicService;
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothConfiguration;
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothService;
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothStatus;
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothWriter;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import java.util.UUID;


public class MainActivity extends AppCompatActivity implements BluetoothService.OnBluetoothEventCallback
{

    final static String LOG_TAG = "MainActivity";

    private BluetoothService mService;
    private BluetoothWriter mWriter;

    private JoystickView.JoystickListener listener;

    public JoystickView.JoystickListener getListener() {
        return listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        listener = new JoystickView.JoystickListener(){
            int lastValue = 0;
            public boolean onJoystickPositionChanged(byte x, byte y){

                // do this to avoid sending duplicate values
                int val = ((x & 0xff) << 8) | (y & 0xff);
                if (val == lastValue)
                    return true;
                lastValue = val;

                System.out.println(String.format("x: %d, y: %d", x, y));
                if(mService.getStatus() == BluetoothStatus.CONNECTED) {
//                    mWriter.write("x- " + x + ", y- " + y + "\n");
                    // easiest packet ever
                    byte[] packet = { x, y };
                    mWriter.write(packet);
                }
                return true;
            }
        };

        JoystickView joystickView = findViewById(R.id.joystick);
        joystickView.setJoystickListener(listener);

        BluetoothConfiguration config = new BluetoothConfiguration();
        // use below for low energy
//        BluetoothConfiguration config = new BluetoothLeConfiguration();

        config.context = getApplicationContext();
        config.bluetoothServiceClass = BluetoothClassicService.class;
        config.bufferSize = 1024;
        config.characterDelimiter = '\n';
        config.deviceName = "Your App Name";
        config.callListenersInMainThread = true;

        config.uuidService = UUID.fromString("e7810a71-73ae-499d-8c15-faa9aef0c3f2"); // Required
        config.uuidCharacteristic = UUID.fromString("bef8d6c9-9c21-4c9e-b632-bd58c1009f9f"); // Required
        config.transport = BluetoothDevice.TRANSPORT_LE; // Required for dual-mode devices
        config.uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); // Used to filter found devices. Set null to find all devices.

        BluetoothService.init(config);
        mService = BluetoothService.getDefaultInstance();
        mService.setOnEventCallback(this);

        mWriter = new BluetoothWriter(mService);
    }

    @Override
    protected void onResume(){
        super.onResume();

        SharedPreferences sharedPref = MainActivity.this
                .getSharedPreferences(getString(R.string.shared_prefs_application), Context.MODE_PRIVATE);

        String connectAddress = sharedPref.getString(getString((R.string.key_device_to_connect)),null);


        if (connectAddress != null) {
            Log.i(LOG_TAG, "device to connect-"+ connectAddress);

            try {
                mService.connect(connectAddress);
            } catch (IllegalArgumentException e) {
                Log.e(LOG_TAG, "Bad argument to Service.connect: '" + connectAddress + "'");
                throw e;
            }
            mService.setOnEventCallback(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mService.disconnect();
    }

    @Override
    public void onPause(){
        super.onPause();
//        stopAdvertising();
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
        if (id == R.id.action_connect) {
            startActivity (new Intent(this, ConnectPreferenceActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDataRead(byte[] buffer, int length) {
        Log.d(LOG_TAG, "onDataRead: " + new String(buffer, 0, length));
//        mEdRead.append("< " + new String(buffer, 0, length) + "\n");
    }

    @Override
    public void onStatusChange(BluetoothStatus status) {
        Log.d(LOG_TAG, "onStatusChange: " + status);
    }

    @Override
    public void onDeviceName(String deviceName) {
        Log.d(LOG_TAG, "onDeviceName: " + deviceName);
    }

    @Override
    public void onToast(String message) {
        Log.d(LOG_TAG, "onToast: " + message);
    }

    @Override
    public void onDataWrite(byte[] buffer) {

//        Log.d(LOG_TAG, "onDataWrite: "+ new String(buffer));
    }

}
