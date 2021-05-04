package edu.lewisu.cs.bprice.watertracker;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

public class WaterDayAdapter extends FirebaseRecyclerAdapter<WaterDay, WaterDayAdapter.InfoHolder> {
    private Context context;

    public WaterDayAdapter(@NonNull FirebaseRecyclerOptions<WaterDay> options, Context context) {
        super(options);
        this.context = context;
    }


    @Override
    protected void onBindViewHolder(@NonNull InfoHolder holder, int position, @NonNull WaterDay model) {
        String date = String.valueOf(model.getDate());
        Log.d("WaterDayAdapter:", date);
        String year = String.valueOf(date.charAt(0)) + String.valueOf(date.charAt(1)) + String.valueOf(date.charAt(2)) + String.valueOf(date.charAt(3));
        String month = String.valueOf(date.charAt(4)) + String.valueOf(date.charAt(5));
        String day = String.valueOf(date.charAt(6)) + String.valueOf(date.charAt(7));
        Log.d("WaterDayAdapter:", year);
        Log.d("WaterDayAdapter:", month);
        Log.d("WaterDayAdapter:", day);
        holder.dateTextView.setText(month + "/" + day);
        holder.waterGoalTextView.setText(String.valueOf(model.getDrunkWater()) + "/" + String.valueOf(model.getTotalWater()) + " ml");
    }

    @NonNull
    @Override
    public InfoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.list_item, parent, false);
        return new InfoHolder(view);
    }


    class InfoHolder extends RecyclerView.ViewHolder {
        private final TextView waterGoalTextView;
        private final TextView dateTextView;

        InfoHolder(View itemView){
            super(itemView);
            waterGoalTextView = itemView.findViewById(R.id.waterGoalTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
        }
    }


}
