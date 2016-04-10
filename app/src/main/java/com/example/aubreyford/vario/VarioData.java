package com.example.aubreyford.vario;

import java.util.ArrayList;

public class VarioData {
    ArrayList<AltitudeEntry> varioData;


    public VarioData(){
        this.varioData = new ArrayList<>();
    }

    public ArrayList<AltitudeEntry> getAltitudeEntries() {
        return varioData;
    }

    public void addEntry(AltitudeEntry entry) {
        varioData.add(entry);
    }

    public double getCurrentMpS(){

        if(varioData.size() < 3) {

            return 0;

        }else{

            double lastThreeSum = 0;
            double lastThreeTimeSum = 0;
            double lastTwoTimeDifference = varioData.get(varioData.size()-1).timestamp - varioData.get(varioData.size()-2).timestamp;

            for(int i = varioData.size() - 3 ; i < varioData.size() ; i++ ){

                AltitudeEntry current = varioData.get(i);
                AltitudeEntry prev = varioData.get(i-1);

                double timeDifference = current.timestamp - prev.timestamp;
                double altitudeReading = current.altitude;

                lastThreeSum += timeDifference * altitudeReading;
                lastThreeTimeSum += timeDifference;

            }
            return (lastThreeSum/lastThreeTimeSum) / lastTwoTimeDifference;
        }
    }








}
