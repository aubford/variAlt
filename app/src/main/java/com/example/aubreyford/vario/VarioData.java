package com.example.aubreyford.vario;

import android.util.Log;

import java.util.ArrayList;

public class VarioData {
    ArrayList<AltitudeEntry> varioData;
    double[] lastAltPoint = {0.0, 0.0};


    public VarioData(){
        this.varioData = new ArrayList<>();
    }

    public ArrayList<AltitudeEntry> getAltitudeEntries() {
        return varioData;
    }

    public void addEntry(AltitudeEntry entry) {
        varioData.add(entry);
    }

    public float getCurrentMpS(){

        if(varioData.size() < 6) {
            return 0;
        }else{

            if(lastAltPoint[1] == 0.0){
                lastAltPoint[0] = varioData.get(varioData.size()-2).altitude;
                lastAltPoint[1] = varioData.get(varioData.size()-2).timestamp / 1000.0;
            }

            double[] currentPoint = {0.0, varioData.get(varioData.size()-1).timestamp / 1000.0};

            double sum = 0.0;
            double netTime = (varioData.get(varioData.size()-1).timestamp - varioData.get(varioData.size()-6).timestamp) / 1000.0;

            for(int i = varioData.size() - 5 ; i < varioData.size() ; i++ ){

                AltitudeEntry current = varioData.get(i);
                AltitudeEntry prev = varioData.get(i-1);

                double timeDifference = (current.timestamp - prev.timestamp) / 1000.0;

                double altitudeReading = current.altitude;
                Log.i("****READING***", String.valueOf(altitudeReading));


                sum += (timeDifference * altitudeReading);
            }

            currentPoint[0] = sum / netTime;
//            Log.i("****currentPoint****", String.valueOf(currentPoint[0]));
//            Log.i("****currentPoint****", String.valueOf(currentPoint[1]));
//            Log.i("****lastAltPoint****", String.valueOf(lastAltPoint[0]));
//            Log.i("****lastAltPoint****", String.valueOf(lastAltPoint[1]));

            double MpS = (currentPoint[0] - lastAltPoint[0]) / (currentPoint[1] - lastAltPoint[1]);
            Log.i("******MPS:", String.valueOf(MpS));

            return (float)MpS;
        }
    }



}
