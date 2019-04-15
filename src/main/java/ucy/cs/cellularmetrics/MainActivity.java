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
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
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

public class MainActivity extends Activity {
    int counter = 0;
    int changes = 0;
    TelephonyManager mTelephonyManager;
    TextView tvNetworkInfo;
    TextView txtList;
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
        txtList = (TextView) findViewById(R.id.tvNetworkInfo2);
        txtList.setMovementMethod(new ScrollingMovementMethod());

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

    /**
     * Listener that waits the button to be clicked
     *
     * @param view, the view of the app
     */
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

    /**
     * This is the listener that waits the signal to change, or the cells to change
     * so that it can save the cell info
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
        /**
         * Gets the cell info
         * @param cellInfoList, the list the contains all cells with their info
         */
        public void onCellInfoChanged(List<CellInfo> cellInfoList) {

            int signal = 0, cid = 0, mnc = 0, mcc = 0, lac = 0, cells = 0;
            String type = "";
            StringBuilder data = new StringBuilder();
            //StringBuilder error = new StringBuilder();

            Long tsLong = System.currentTimeMillis() / 1000;
            String ts = tsLong.toString();


            cellInfoList = mTelephonyManager.getAllCellInfo();
            if (mTelephonyManager.getAllCellInfo() != null) {
                int cellids[] = new int[cellInfoList.size()];
                for (int i = 0; i < cellInfoList.size(); i++) {
                    CellInfo info = cellInfoList.get(i);
                    if (info instanceof CellInfoGsm) {
                        signal = ((CellInfoGsm) info).getCellSignalStrength().getDbm();
                        type = "GSM";
                        cellids[i] = cid;
                        cid = ((CellInfoGsm) info).getCellIdentity().getCid();
                        mnc = ((CellInfoGsm) info).getCellIdentity().getMnc();
                        lac = ((CellInfoGsm) info).getCellIdentity().getLac();
                        mcc = ((CellInfoGsm) info).getCellIdentity().getMcc();
                    } else if (info instanceof CellInfoWcdma) {
                        cellids[i] = cid;
                        signal = ((CellInfoWcdma) info).getCellSignalStrength().getDbm();
                        type = "WCDMA";
                        cid = ((CellInfoWcdma) info).getCellIdentity().getCid();
                        mnc = ((CellInfoWcdma) info).getCellIdentity().getMnc();
                        lac = ((CellInfoWcdma) info).getCellIdentity().getLac();
                        mcc = ((CellInfoWcdma) info).getCellIdentity().getMcc();
                    } else if (info instanceof CellInfoCdma) {
                        cellids[i] = cid;
                        signal = ((CellInfoCdma) info).getCellSignalStrength().getDbm();
                        type = "CDMA";
                        cid = ((CellInfoCdma) info).getCellIdentity().getBasestationId();
                        mnc = ((CellInfoCdma) info).getCellIdentity().getSystemId();
                        lac = ((CellInfoCdma) info).getCellIdentity().getNetworkId();

                        if (mnc != 0) mcc = 280;
                    } else {
                        cellids[i] = cid;
                        signal = ((CellInfoLte) info).getCellSignalStrength().getDbm();
                        type = "LTE";
                        cid = ((CellInfoLte) info).getCellIdentity().getCi();
                        mnc = ((CellInfoLte) info).getCellIdentity().getMnc();
                        lac = ((CellInfoLte) info).getCellIdentity().getTac();
                        mcc = ((CellInfoLte) info).getCellIdentity().getMcc();

                    }
                    //if ((cid == 2147483647) || (cid == -1))
                    //  error.append(ts + " " + latitude + " " + longitude + " " + type + " ");
                    //else {
                    boolean flag = false;
                    if (cid != 2147483647 && mcc == 280) {
                        for (int j = 0; j < i; j++)
                            if (cellids[j] == cid) {
                                flag = true;
                                break;
                            }
                        if (!flag)
                            data.append(ts + "," + latitude + "," + longitude + "," + type + "," + signal + "," + cid + "," + mnc + "," + lac + "," + "\n");
                        cells++;
                    }
                    txtList.append(" \n Timestamp: " + ts + "\n Lat: " + latitude + "\n Lon: " + longitude +
                            "\n Type: " + type + "\n Signal: " + signal + "\n CellID: " + cid + "\n MNC: " + mnc + "\n LAC: " + lac + "\n");
                }
            } else Log.i("info", "null cellinfo");
            if (data.length() > 0) writeToFile(data, 0);
            //if (error.length() > 0) writeToFile(error, 1);
            tvNetworkInfo.setText("Changes in Cell info: " + (changes++) + " Current Cells: " + cells);
        }
    };

    @Override
    /**
     * When the 'Start' button is pressed, starts getting cell info
     */
    public void onStart() {
        super.onStart();

        // Code below register your mPhoneStateListener will start to be called everytime cell info changed.
        // If you update any UI element, be sure that they were created already (since they are created during "onCreate".. and not at onStart)
        // I added LISTEN_CELL_LOCATION.. But I think that PhoneStateListener.LISTEN_CELL_INFO is enough
        mTelephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CELL_LOCATION | PhoneStateListener.LISTEN_CELL_INFO | PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
    }

    @Override
    /**
     *  When the 'Stop' button is pressed, stops getting cell info
     */
    public void onStop() {
        // Code below unregister your listener... You will not receive any cell info change notification anymore
        TelephonyManager mTelephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
        super.onStop();
    }

    /**
     * This method writes into a file the cell measurements and errors if there are any kind of errors
     *
     * @param data, the data the will be written into the file.
     * @param type, 0 for measurements, 1 for errors
     */
    private void writeToFile(StringBuilder data, int type) {

        File file = new File(Environment.getExternalStorageDirectory(), "/mymetrics");
        if (!file.exists()) {
            file.mkdir();
        }

        try {
            File gpxfile;
            if (type == 0)
                gpxfile = new File(file, "measurements.csv");
            else gpxfile = new File(file, "errors.csv");

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
                Log.e("Error", "Writing in file");
            }

            FileWriter writer = new FileWriter(gpxfile);
            writer.append(String.valueOf(text) + String.valueOf(data));

            writer.flush();
            writer.close();


        } catch (Exception e) {
            Log.e("Exception", "File write failed: " + e.toString());

        }
    }

    /**
     * This method gets the permissions for everything that the application uses
     */
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
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
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
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
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
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    result);

            fineLocation = result;
        } else {
            // Permission has already been granted
            fineLocation = 1;
        }
        //return (coarseLocation == 1 && fineLocation == 1 && read == 1 && write == 1);
        return true;
    }
}
