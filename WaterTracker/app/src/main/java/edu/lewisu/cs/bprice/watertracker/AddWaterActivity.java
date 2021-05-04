package edu.lewisu.cs.bprice.watertracker;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class AddWaterActivity extends AppCompatActivity {
    private Button mAddButton;
    private EditText mAmountToAdd;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;

    private String userID;
    private String todaysDate;
    private WaterDay todaysWater;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        userID = this.getIntent().getExtras().getString("uid");
        todaysDate = this.getIntent().getExtras().getString("todays_date");

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();

        mAmountToAdd = findViewById(R.id.waterToAdd);

        mAddButton = findViewById(R.id.addWaterButton);

        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int waterToAdd = Integer.parseInt(String.valueOf(mAmountToAdd.getText()));
                todaysWater = new WaterDay(0, 0);
                mDatabaseReference.child(userID).child(todaysDate).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        HashMap result = (HashMap) task.getResult().getValue();
                        int temp = Integer.parseInt(String.valueOf(result.get("temp")));
                        int base = Integer.parseInt(String.valueOf(result.get("baseWater")));
                        int total = Integer.parseInt(String.valueOf(result.get("totalWater")));
                        int remaining = Integer.parseInt(String.valueOf(result.get("remainingWater")));
                        int drunk = Integer.parseInt(String.valueOf(result.get("drunkWater")));
                        int heatWater = Integer.parseInt(String.valueOf(result.get("heatWater")));

                        Log.d("AddWaterActivity:", (String.valueOf(temp)));
                        Log.d("AddWaterActivity:", (String.valueOf(base)));
                        Log.d("AddWaterActivity:", (String.valueOf(total)));
                        Log.d("AddWaterActivity:", (String.valueOf(remaining)));
                        Log.d("AddWaterActivity:", (String.valueOf(drunk)));
                        Log.d("AddWaterActivity:", (String.valueOf(heatWater)));

                        todaysWater.setTemp(temp);
                        todaysWater.setBaseWater(base);
                        todaysWater.setTotalWater(total);
                        todaysWater.setRemainingWater(remaining);
                        todaysWater.setDrunkWater(drunk);
                        todaysWater.setHeatWater(heatWater);

                        todaysWater.addWaterDrunk(waterToAdd);

                        mDatabaseReference.child(userID).child(String.valueOf(todaysDate)).setValue(todaysWater);

                        Intent returnIntent = new Intent();
                        setResult(RESULT_OK, returnIntent);
                        finish();
                    }
                });
            }
        });
    }
}
