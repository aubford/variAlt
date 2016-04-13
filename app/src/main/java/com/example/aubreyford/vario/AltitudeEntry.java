package com.example.aubreyford.vario;

import java.io.Serializable;

public class AltitudeEntry implements Serializable{
    float altitude;
    double timestamp;
    double lattitude;
    double longitude;



    public AltitudeEntry(float altitude, double timestamp, double lat, double lng) {
        this.altitude = altitude;
        this.timestamp = timestamp;
        this.lattitude = lat;
        this.longitude = lng;
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

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLattitude() {
        return lattitude;
    }

    public void setLattitude(double lattitude) {
        this.lattitude = lattitude;
    }






    public void setTimestamp(Double timestamp) {
        this.timestamp = timestamp;
    }

}
