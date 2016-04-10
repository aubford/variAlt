package com.example.aubreyford.vario;

public class AltitudeEntry {
    float altitude;
    double timestamp;

    public AltitudeEntry(float altitude, double timestamp) {
        this.altitude = altitude;
        this.timestamp = timestamp;
    }


    public float getAltitude() {
        return altitude;
    }

    public void setAltitude(Float altitude) {
        this.altitude = altitude;
    }

    public double getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Double timestamp) {
        this.timestamp = timestamp;
    }

}
