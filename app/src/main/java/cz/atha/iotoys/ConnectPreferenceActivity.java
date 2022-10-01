package cz.atha.iotoys;


import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;


public class ConnectPreferenceActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, new ConnectPreferenceFragment())
                .commit();


    }


}
