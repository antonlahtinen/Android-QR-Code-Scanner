package com.example.finalassignmentandroid;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class MainActivity extends AppCompatActivity implements View.OnClickListener
{

    Button scanButton;
    private TextView textLatitude;

    private TextView textLongitude;

    private TextView qrCodeLatitude;

    private TextView qrCodeLongitude;

    private TextView distanceBetween;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textLatitude = findViewById(R.id.myLatitude);
        textLongitude = findViewById(R.id.myLongitude);
        qrCodeLatitude = findViewById(R.id.qrCodeLatitude);
        qrCodeLongitude = findViewById(R.id.qrCodeLongitude);
        distanceBetween = findViewById(R.id.distanceBetween);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        initViews();
    }

    @Override
    protected void onStart() {
        super.onStart();

        textLatitude.setText("61.50");
        textLongitude.setText("23.75");

        qrCodeLatitude.setText("N/A");
        qrCodeLongitude.setText("N/A");

        if (ActivityCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            System.out.println("Coarse and fine location permission not granted!");

            String[] permissions = {android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION};

            ActivityCompat.requestPermissions(this, permissions, 42);
            return;

        } else {
            System.out.println("Coarse and fine location permission granted!");
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();

                            String formattedLatitude = String.format("%.2f", latitude);
                            String formattedLongitude = String.format("%.2f", longitude);

                            textLatitude.setText(formattedLatitude);
                            textLongitude.setText(formattedLongitude);
                        }
                    }
                });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        System.out.println("onRequestPermissionsResult: requestCode = " + requestCode);
        for (int i = 0; i < permissions.length; i++) {
            System.out.println(permissions[i] + " = " + Integer.toString(grantResults[i]));
        }
    }

    private void initViews()
    {
        scanButton = findViewById(R.id.scanButton);
        scanButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v)
    {
        startActivityForResult(new Intent(MainActivity.this, ScanActivity.class), 1001);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            double latitude = data.getDoubleExtra("latitude", 0);
            double longitude = data.getDoubleExtra("longitude", 0);

            qrCodeLatitude.setText((String.format("%.2f", latitude)));
            qrCodeLongitude.setText(String.format("%.2f", longitude));

            calculateDistance();
        }
    }

    private void calculateDistance() {
        try {
            // Extract latitude and longitude from TextViews
            double myLatitude = Double.parseDouble(textLatitude.getText().toString().replaceAll("[^0-9.]", ""));
            double myLongitude = Double.parseDouble(textLongitude.getText().toString().replaceAll("[^0-9.]", ""));
            double qrLatitude = Double.parseDouble(qrCodeLatitude.getText().toString().replaceAll("[^0-9.]", ""));
            double qrLongitude = Double.parseDouble(qrCodeLongitude.getText().toString().replaceAll("[^0-9.]", ""));

            // Calculate distance
            float[] results = new float[1];
            Location.distanceBetween(myLatitude, myLongitude, qrLatitude, qrLongitude, results);
            float distanceInMeters = results[0];

            // Convert distance to kilometers for better readability
            float distanceInKm = distanceInMeters / 1000;

            // Update the distanceBetween TextView
            distanceBetween.setText(String.format("%.2f km", distanceInKm));

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid latitude or longitude format.", Toast.LENGTH_SHORT).show();
        }
    }

}