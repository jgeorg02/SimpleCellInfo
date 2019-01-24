package ucy.cs.cellularmetrics;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.app.Activity;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellLocation;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import static android.telephony.TelephonyManager.NETWORK_TYPE_GPRS;
import static android.telephony.TelephonyManager.NETWORK_TYPE_GSM;
import static android.telephony.TelephonyManager.NETWORK_TYPE_HSDPA;
import static android.telephony.TelephonyManager.NETWORK_TYPE_HSPA;
import static android.telephony.TelephonyManager.NETWORK_TYPE_LTE;
import static android.telephony.TelephonyManager.NETWORK_TYPE_UMTS;
import static android.telephony.TelephonyManager.PHONE_TYPE_CDMA;

public class MainActivity extends Activity {
    int counter = 0;
    int changes = 0;
    TelephonyManager mTelephonyManager;
    TextView tvNetworkInfo;
    // get lat, lon, fix this
    Location location; // location
    double latitude = 0.0; // latitude
    double longitude = 0.0; // longitude
    LocationManager locationManager;
    // getting GPS status
    boolean isGPSEnabled;
    boolean permissions = false;

    private final LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            longitude = location.getLongitude();
            latitude = location.getLatitude();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    @SuppressLint("MissingPermission")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvNetworkInfo = (TextView) findViewById(R.id.tvNetworkInfo);

        if (getPermissions()) {
            permissions = true;
            locationManager = (LocationManager) MainActivity.this.getSystemService(LOCATION_SERVICE);
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            if (locationManager != null) {
                location = locationManager
                        .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (location != null) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                }
            }

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, locationListener);
        } else {
            Toast toast = Toast.makeText(getApplicationContext(), "You have to give the right permissions", Toast.LENGTH_LONG);
            toast.show();
        }


    }

    public void clicked(View view) {
        Button st = findViewById(R.id.button_start);
        if (permissions) {
            if (counter % 2 == 0) {
                st.setText("Stop");
                onStart();
            } else {
                st.setText("Start");
                onStop();
            }
            counter++;
        }
    }

    /*

    final Handler ha=new Handler();
ha.postDelayed(new Runnable() {

    @Override
    public void run() {
        //call function

        ha.postDelayed(this, 10000);
    }
}, 10000);
     */

    private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {

        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            onCellInfoChanged(null);
        }

        @Override
        public void onCellLocationChanged(CellLocation location) {
            onCellInfoChanged(null);

        }


        @SuppressLint({"NewApi", "MissingPermission"})
        @Override
        public void onCellInfoChanged(List<CellInfo> cellInfoList) {

            int signal = 0;
            String type = "";
            int cid = 0;
            int mnc = 0;
            int lac = 0;
            int cells = 0;
            StringBuilder data = new StringBuilder();
            StringBuilder error = new StringBuilder();


            //Log.i("lat", String.valueOf(latitude));
            //Log.i("lon", String.valueOf(longitude));
            Long tsLong = System.currentTimeMillis() / 1000;
            String ts = tsLong.toString();

            // This callback method will be called automatically by Android OS
            // Every time a cell info changed (if you are registered)
            // Here, you will receive a cellInfoList....

            cellInfoList = mTelephonyManager.getAllCellInfo();
            if (mTelephonyManager.getAllCellInfo() != null)
                for (int i = 0; i < cellInfoList.size(); i++) {
                    CellInfo info = cellInfoList.get(i);
                    if (info instanceof CellInfoGsm) {
                        signal = ((CellInfoGsm) info).getCellSignalStrength().getDbm();
                        type = "GSM";
                        cid = ((CellInfoGsm) info).getCellIdentity().getCid();
                        mnc = ((CellInfoGsm) info).getCellIdentity().getMnc();
                        lac = ((CellInfoGsm) info).getCellIdentity().getLac();
                    } else if (info instanceof CellInfoWcdma) {
                        signal = ((CellInfoWcdma) info).getCellSignalStrength().getDbm();
                        type = "WCDMA";
                        cid = ((CellInfoWcdma) info).getCellIdentity().getCid();
                        mnc = ((CellInfoWcdma) info).getCellIdentity().getMnc();
                        lac = ((CellInfoWcdma) info).getCellIdentity().getLac();
                    } else if (info instanceof CellInfoCdma) {
                        signal = ((CellInfoCdma) info).getCellSignalStrength().getDbm();
                        type = "CDMA";
                        cid = ((CellInfoCdma) info).getCellIdentity().getBasestationId();
                        mnc = ((CellInfoCdma) info).getCellIdentity().getSystemId();
                        lac = ((CellInfoCdma) info).getCellIdentity().getNetworkId();

                    } else {
                        signal = ((CellInfoLte) info).getCellSignalStrength().getDbm();
                        type = "LTE";
                        cid = ((CellInfoLte) info).getCellIdentity().getCi();
                        mnc = ((CellInfoLte) info).getCellIdentity().getMnc();
                        lac = ((CellInfoLte) info).getCellIdentity().getTac();

                    }
                    if (cid == 2147483647)
                        error.append(ts + " " + latitude + " " + longitude + " " + type + " ");
                    else {
                        data.append(ts + "," + latitude + "," + longitude + "," + type + "," + signal + "," + cid + "," + mnc + "," + lac + "," + "\n");
                        cells++;
                    }//Log.i("data", data);

                }
            else Log.i("info", "null cellinfo");
            if (data.length() > 0) writeToFile(data, 0);
            if (error.length() > 0) writeToFile(error, 1);
            tvNetworkInfo.setText("Changes in Cell info: " + (changes++) + " cells: " + cells + "\n timestamp, lat, lon, type, signal, cid, mnc, lac\n" + data);
        }
    };

    @Override
    public void onStart() {
        super.onStart();

        // Code below register your mPhoneStateListener will start to be called everytime cell info changed.
        // If you update any UI element, be sure that they were created already (since they are created during "onCreate".. and not at onStart)
        // I added LISTEN_CELL_LOCATION.. But I think that PhoneStateListener.LISTEN_CELL_INFO is enough
        mTelephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CELL_LOCATION | PhoneStateListener.LISTEN_CELL_INFO | PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
    }

    @Override
    public void onStop() {
        // Code below unregister your listener... You will not receive any cell info change notification anymore
        TelephonyManager mTelephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
        super.onStop();
    }

    private void writeToFile(StringBuilder data, int type) {

        File file = new File(Environment.getExternalStorageDirectory(), "/mymetrics");
        if (!file.exists()) {
            file.mkdir();
        }

        try {
            File gpxfile;
            if (type == 0)
                gpxfile = new File(file, "metrics.txt");
            else gpxfile = new File(file, "errors.txt");
            //Read text from file
            StringBuilder text = new StringBuilder();
            try {
                BufferedReader br = new BufferedReader(new FileReader(gpxfile));
                String line;

                while ((line = br.readLine()) != null) {
                    text.append(line);
                    text.append('\n');
                }
                br.close();
            } catch (IOException e) {
                //You'll need to add proper error handling here
            }

            FileWriter writer = new FileWriter(gpxfile);
            writer.append(String.valueOf(text) + String.valueOf(data));
            //Log.i("write", data);
            //Log.i("read", String.valueOf(text));
            writer.flush();
            writer.close();

            //Log.i("file", gpxfile.getParent());

        } catch (Exception e) {
            Log.e("Exception", "File write failed: " + e.toString());

        }
    }

    public boolean getPermissions() {

        int result = 0, coarseLocation, write, read, fineLocation;

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {


            // No explanation needed; request the permission
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    result);

            coarseLocation = result;
        } else {
            // Permission has already been granted
            coarseLocation = 1;
        }

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // No explanation needed; request the permission
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    result);

            write = result;

        } else {
            // Permission has already been granted
            write = 1;
        }

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // No explanation needed; request the permission
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    result);

            read = result;

        } else {
            // Permission has already been granted
            read = 1;
        }

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // No explanation needed; request the permission
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    result);

            fineLocation = result;
        } else {
            // Permission has already been granted
            fineLocation = 1;
        }
        return (coarseLocation == 1 && fineLocation == 1 && read == 1 && write == 1);
    }
}
