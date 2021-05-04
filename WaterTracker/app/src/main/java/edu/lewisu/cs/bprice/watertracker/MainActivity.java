package edu.lewisu.cs.bprice.watertracker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.VolleyError;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private static final int ADD_INTENT_RESULT = 1;
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 1234;


    private TextView mAmountLeftForToday;
    private TextView mAmountHadToday;
    private Button mAddButton;
    private TextView mAddedToday;
    private TextView mAmountAddedToday;

    private static Context mContext;

    private WaterDay todaysWater;
    private int temperature = 90;
    private int baseWater;
    private String todaysDate;
    private boolean isNewDay;

    private LocationCallback mLocationCallback;
    private FusedLocationProviderClient mFusedLocationProviderClient;

    private RecyclerView recyclerView;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private Query query;
    private FirebaseRecyclerAdapter<WaterDay, WaterDayAdapter.InfoHolder> mAdapter;
    private FirebaseUser user;

    private Boolean mDeniedPermissions;

    private static final int RC_SIGN_IN = 111;

    private final WeatherFetcher.OnWeatherReceivedListener mFetchWeatherListener = new WeatherFetcher.OnWeatherReceivedListener(){

        @Override
        public void onWeatherReceived(WeatherDay weatherDay) {
            int temp = weatherDay.getMaxTemp();
            baseWater = 2000;

            todaysWater = new WaterDay(baseWater, temp);

            mDatabaseReference.child(mFirebaseAuth.getCurrentUser().getUid()).child(todaysDate).setValue(todaysWater);

            isNewDay = false;

            Intent reload = new Intent(mContext, MainActivity.class);
            startActivity(reload);
            finish();
        }

        @Override
        public void onErrorResponse(VolleyError error) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Views
        mAmountLeftForToday = findViewById(R.id.amountLeftForToday);
        mAmountHadToday = findViewById(R.id.amountHadToday);
        mAddedToday = findViewById(R.id.addedToday);
        mAmountAddedToday = findViewById(R.id.amountAddedToday);
        mAddButton = findViewById(R.id.addButton);
        recyclerView = findViewById(R.id.recyclerView);

        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addIntent = new Intent(getApplicationContext(), AddWaterActivity.class);
                addIntent.putExtra("todays_date", todaysDate);
                addIntent.putExtra("uid", mFirebaseAuth.getCurrentUser().getUid());
                startActivityForResult(addIntent, ADD_INTENT_RESULT);
            }
        });
        mAddedToday.setText("Amount Added Due to Heat (" + "..." + "°F)");

        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        // Logic
        mDeniedPermissions = false;
        isNewDay = false;
        mContext = this.getApplicationContext();
        todaysDate = Calendar.getInstance().getTime().toString();
        String[] dateParts = todaysDate.split(" ");
        todaysDate = dateParts[5] + monthToNum(dateParts[1]) + dateParts[2];
        Log.d("MainActivity:", todaysDate);

        // Firebase
        FirebaseApp.initializeApp(this);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = mFirebaseAuth.getCurrentUser();
                if(user == null){
                    startActivityForResult(AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setIsSmartLockEnabled(false)
                            .setAvailableProviders(Arrays.asList(new AuthUI.IdpConfig.EmailBuilder().build()))
                            .build(), RC_SIGN_IN);
                }
            }
        };

        // Notifications
        Intent showNotification = new Intent(this, NotificationAlertReciever.class);
        showNotification.setAction(NotificationAlertReciever.ACTION_REVIEW_REMINDER);
        PendingIntent notifyPendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                showNotification,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        AlarmManager alarmManager = (AlarmManager) this.getSystemService(ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 5000, 60000, notifyPendingIntent);

    }

    @Override
    protected void onResume() {
        super.onResume();

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setStackFromEnd(true);
        llm.setReverseLayout(true);
        recyclerView.setLayoutManager(llm);

        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
            try {
                query = mDatabaseReference.child(mFirebaseAuth.getCurrentUser().getUid());
                Log.d("MainActivity", query.toString());
                FirebaseRecyclerOptions<WaterDay> options = new FirebaseRecyclerOptions.Builder<WaterDay>().setQuery(query, WaterDay.class).build();
                mAdapter = new WaterDayAdapter(options, this);
                recyclerView.setAdapter(mAdapter);

                mDatabaseReference.child(mFirebaseAuth.getCurrentUser().getUid()).child(String.valueOf(todaysDate)).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if (task.getResult().getValue() == null) {
                            isNewDay = true;
                            Log.d("MainActivity:", "New Day");
                            // create New Day of Weather
                            if (checkPermissions()) {
                                makeTodaysWaterDay();
                            } else if (!checkPermissions() && !mDeniedPermissions) {
                                requestPermissions();
                            }
                        } else {
                            isNewDay = false;
                            todaysWater = new WaterDay();

                            int temp = Integer.parseInt(String.valueOf(task.getResult().child("temp").getValue()));
                            int base = Integer.parseInt(String.valueOf(task.getResult().child("baseWater").getValue()));
                            int total = Integer.parseInt(String.valueOf(task.getResult().child("totalWater").getValue()));
                            int remaining = Integer.parseInt(String.valueOf(task.getResult().child("remainingWater").getValue()));
                            int drunk = Integer.parseInt(String.valueOf(task.getResult().child("drunkWater").getValue()));
                            int heatWater = Integer.parseInt(String.valueOf(task.getResult().child("heatWater").getValue()));

                            todaysWater.setTemp(temp);
                            todaysWater.setBaseWater(base);
                            todaysWater.setRemainingWater(remaining);
                            todaysWater.setTotalWater(total);
                            todaysWater.setDrunkWater(drunk);
                            todaysWater.setHeatWater(heatWater);

                            mAddedToday.setText("Amount Added Due to Heat (" + todaysWater.getTemp() + "°F)");
                            mAmountLeftForToday.setText(String.valueOf(todaysWater.getRemainingWater()));
                            mAmountHadToday.setText(String.valueOf(todaysWater.getDrunkWater()));
                            mAmountAddedToday.setText(String.valueOf(todaysWater.getHeatWater()));
                        }
                    }
                });
                mAdapter.startListening();
            }catch (Exception ex){
            Log.d("MainActivity:",ex.toString());
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        try{
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
            mAdapter.stopListening();
        }catch (Exception ex){
            Log.d("MainActivity:", ex.toString());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.sign_out){
            AuthUI.getInstance().signOut(this);
        }
        return super.onOptionsItemSelected(item);
    }

    private String monthToNum(String month){
        if (month.equals("Jan") ){
            return "01";
        } else if (month.equals("Feb")){
            return "02";
        } else if (month.equals("Mar")){
            return "03";
        } else if (month.equals("Apr")){
            return "04";
        } else if (month.equals("May")){
            return "05";
        } else if (month.equals("Jun")){
            return "06";
        } else if (month.equals("Jul")){
            return "07";
        } else if (month.equals("Aug")){
            return "08";
        } else if (month.equals("Sep")){
            return "09";
        } else if (month.equals("Oct")){
            return "10";
        } else if (month.equals("Nov")){
            return "11";
        } else {
            return "12";
        }
    }

    public static Context getContext() {
        return mContext;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == ADD_INTENT_RESULT){
            Log.d("MainActivity:", "Returned from AddActivity");
        }else if(resultCode == RESULT_OK && requestCode == RC_SIGN_IN){
            Log.d("MainActivity:", FirebaseAuth.getInstance().getCurrentUser().toString());
            this.recreate();
        }else if(resultCode == RESULT_CANCELED && requestCode == RC_SIGN_IN){
            finish();
        }
    }

    @SuppressLint("MissingPermission")
    private void makeTodaysWaterDay() {
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        createLocationCallback();
        mFusedLocationProviderClient.requestLocationUpdates(locationRequest, mLocationCallback, Looper.myLooper());
    }

    private void createLocationCallback(){
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if(isNewDay){
                    Location location = locationResult.getLastLocation();
                    WeatherFetcher weatherFetcher = new WeatherFetcher(mContext, location);
                    weatherFetcher.fetchWeather(mFetchWeatherListener);
                }
            }
        };
    }
    /**********************Permissions************************/
    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        if (shouldProvideRationale) {
            Snackbar.make(findViewById(android.R.id.content),R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(
                            android.R.string.ok, new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    // Request permission
                                    ActivityCompat.requestPermissions(MainActivity.this,
                                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                            REQUEST_PERMISSIONS_REQUEST_CODE);
                                }
                            }).show();
        } else {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            Log.d("ActivityMain:", String.valueOf(grantResults[0]));
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                makeTodaysWaterDay();
            } else {
                mDeniedPermissions = true;
                Snackbar.make(findViewById(android.R.id.content),R.string.permission_denied_explanation, Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.settings, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        }).show();
            }
        }
    }
}