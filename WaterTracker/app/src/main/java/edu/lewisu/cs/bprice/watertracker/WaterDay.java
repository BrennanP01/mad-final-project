package edu.lewisu.cs.bprice.watertracker;

import android.util.Log;

import java.util.Calendar;
import java.util.Date;

public class WaterDay {
    private int date;
    private int baseWater;
    private int totalWater;
    private int remainingWater;
    private int drunkWater;
    private int heatWater;
    private int temp;

    public WaterDay(int base, int heat){
        setDate();
        setBaseWater(base);
        setHeatWater(calculateHeatWater(heat));
        setDrunkWater(0);
        setTotalWater(this.baseWater + this.heatWater);
        setRemainingWater(this.totalWater);
        setTemp(heat);
    }

    public WaterDay(){
        setDate(0);
        setBaseWater(0);
        setHeatWater(calculateHeatWater(0));
        setDrunkWater(0);
        setTotalWater(this.baseWater + this.heatWater);
        setRemainingWater(this.totalWater);
        setTemp(0);
    }

    /**
     * maps the heat of the day to amount of extra water to drink
     * @param heat the temperature in fahrenheit
     * @return amount of extra water to drink
     */
    public int calculateHeatWater(int heat){
        double heatWater;
        // 70 - 110 degrees
        // 0  - 500 ml
        heatWater = (heat - 70);
        heatWater = (heatWater / 40);
        heatWater = heatWater * (500);
        if(heatWater < 0){
            heatWater = 0;
        }
        return (int) heatWater;
    }

    /**
     * adds to the water drunk and subtracts from remaining
     * @param water amount of water drank
     */
    public void addWaterDrunk(int water){
        int currentDrunkWater = getDrunkWater();
        int currentRemainingWater = getRemainingWater();
        setDrunkWater(currentDrunkWater + water);
        setRemainingWater(currentRemainingWater - water);
    }

    public int getBaseWater() {
        return baseWater;
    }

    public void setBaseWater(int baseWater) {
        this.baseWater = baseWater;
    }

    public int getTotalWater() {
        return totalWater;
    }

    public void setTotalWater(int totalWater) {
        this.totalWater = totalWater;
    }

    public int getRemainingWater() {
        return remainingWater;
    }

    public void setRemainingWater(int remainingWater) {
        if(remainingWater >= 0){
            this.remainingWater = remainingWater;
        }else{
            this.remainingWater = 0;
        }
    }

    public int getDrunkWater() {
        return drunkWater;
    }

    public void setDrunkWater(int drunkWater) {
        this.drunkWater = drunkWater;
    }

    public int getHeatWater() {
        return heatWater;
    }

    public void setHeatWater(int heatWater) {
        this.heatWater = heatWater;
    }

    public int getDate() {
        return date;
    }

    public void setDate() {
        String todaysDate = Calendar.getInstance().getTime().toString();
        String[] dateParts = todaysDate.split(" ");
        // yearMonthDay
        // allows for sorting by greatest num to get the most recent date
        this.date = Integer.parseInt(dateParts[5] + monthToNum(dateParts[1]) + dateParts[2]);
    }

    public void setDate(int date) {
        this.date = date;
    }

    public int getTemp() {
        return temp;
    }

    public void setTemp(int temp) {
        this.temp = temp;
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
    @Override
    public String toString() {
        return "WaterDayAdapter{" +
                "Total=" + totalWater +
                "Remaining=" + remainingWater +
                "Drunk=" + drunkWater +
                "Heat=" + temp +
                "Date=" + date +
                '}';
    }
}
