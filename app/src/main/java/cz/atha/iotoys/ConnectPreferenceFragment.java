package cz.atha.iotoys;


import android.Manifest;


import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;

import androidx.core.location.LocationManagerCompat;
import androidx.preference.PreferenceGroup;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;


import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothDeviceDecorator;
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothService;

import java.util.Arrays;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class ConnectPreferenceFragment extends PreferenceFragmentCompat implements BluetoothService.OnBluetoothScanCallback {

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

    // dynamic UI Views we need to look up (so we'll keep them for later)
    private Preference initiateScanDevices;
    private PreferenceGroup availableDevices;

    private BluetoothService mService;
    private boolean mScanning;

    final static String LOG_TAG = "PrefFrag";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.prefs_devices, rootKey);


        initiateScanDevices = findPreference(getString(R.string.key_scan_btn));
        availableDevices = (PreferenceGroup) findPreference(getString(R.string.key_devices_list));

//        availableDevices.removeAll();
//        initiateScanDevices.setOnPreferenceClickListener(
//                new Preference.OnPreferenceClickListener() {
//                    @Override
//                    public boolean onPreferenceClick(Preference preference) {
//                        availableDevices.setLayoutResource(R.layout.preference_group_no_title);
//                        availableDevices.removeAll();
//                        scanLeDevice(true);
//                        return true;
//                    }
//                });

        Context ctx = this.getContext();
        LocationManager lm = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
 //       if(!LocationManagerCompat.isLocationEnabled(lm)){
//            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
//        }
        if(lm.isLocationEnabled()){
            Log.d(LOG_TAG, "Location enabled.");
        } else {
            Log.w(LOG_TAG, "Location not enabled");
        }
        mService = BluetoothService.getDefaultInstance();
        mService.setOnScanCallback(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        availableDevices.setLayoutResource(R.layout.preference_group_no_title);
        availableDevices.removeAll();

        // start scanning
        Log.d(LOG_TAG, "starting scan");
//        scanLeDevice(true);

        mService.startScan();
    }


    @Override
    public void onDeviceDiscovered(BluetoothDevice device, int rssi) {
        Log.d(LOG_TAG, "onDeviceDiscovered: " + device.getName() + " - " + device.getAddress() + " - " + Arrays.toString(device.getUuids()));

        Preference pref = (Preference)new LeDevicePreference(getContext(), device);
        if(availableDevices.getPreferenceManager().findPreference(pref.getKey()) == null) {
            availableDevices.addPreference(pref);
        }
/*
        BluetoothDeviceDecorator dv = new BluetoothDeviceDecorator(device, rssi);
        int index = mAdapter.getDevices().indexOf(dv);
        if (index < 0) {
            mAdapter.getDevices().add(dv);
            mAdapter.notifyItemInserted(mAdapter.getDevices().size() - 1);
        } else {
            mAdapter.getDevices().get(index).setDevice(device);
            mAdapter.getDevices().get(index).setRSSI(rssi);
            mAdapter.notifyItemChanged(index);
        }

 */
    }

    @Override
    public void onStartScan() {
        Log.d(LOG_TAG, "onStartScan");
        mScanning = true;
//        pgBar.setVisibility(View.VISIBLE);
//        mMenu.findItem(R.id.action_scan).setTitle(R.string.action_stop)
    }

    @Override
    public void onStopScan() {
        Log.d(LOG_TAG, "onStopScan");
        mScanning = false;
//        pgBar.setVisibility(View.GONE);
//        mMenu.findItem(R.id.action_scan).setTitle(R.string.action_scan);
    }


    class LeDevicePreference extends Preference {

        private BluetoothDevice device;

        LeDevicePreference(Context context, final BluetoothDevice device) {
            super(context);

            this.device = device;


            if(device.getName() != null)
                setTitle(device.getName());
            else
                setTitle(device.getAddress());

            setKey(device.getAddress());

            notifyChanged();
        }

        @Override
        protected void onClick() {
            super.onClick();

            // stop scanning
//            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//            bluetoothAdapter.getBluetoothLeScanner().stopScan(leScanCallback);
            mService.stopScan();

            // we were going to put a check next to the device before, now we're just saving it and exiting
            //            if (preferredDevicePreference != null)
//                preferredDevicePreference.setIcon(null);
//            setIcon(R.drawable.ic_check_black);

            // saved here, we look up what device we want from the main activity
            SharedPreferences sharedPref = getActivity()
                    .getSharedPreferences(getString(R.string.shared_prefs_application), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(getString(R.string.key_device_to_connect), device.getAddress());
            editor.commit();

            // exit preferences
            getActivity().finish();

        }

    }




}
