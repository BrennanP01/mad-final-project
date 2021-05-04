package edu.lewisu.cs.bprice.watertracker;

public class WeatherDay {
    private String location;
    private String date;
    private int minTemp;
    private int maxTemp;
    private boolean rain;

    public WeatherDay(String location, String date, int minTemp, int maxTemp, boolean rain) {
        this.location = location;
        this.date = date;
        this.minTemp = minTemp;
        this.maxTemp = maxTemp;
        this.rain = rain;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getMinTemp() {
        return minTemp;
    }

    public void setMinTemp(int minTemp) {
        this.minTemp = minTemp;
    }

    public int getMaxTemp() {
        return maxTemp;
    }

    public void setMaxTemp(int maxTemp) {
        this.maxTemp = maxTemp;
    }

    public boolean getRain() {
        return rain;
    }

    public void setRain(boolean rain) {
        this.rain = rain;
    }



}
