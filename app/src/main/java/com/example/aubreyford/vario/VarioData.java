package com.example.aubreyford.vario;

import java.util.ArrayList;
import java.util.Arrays;

public class VarioData {
    ArrayList<AltitudeEntry> varioData;
    double[] lastAltPoint = {0.0, 0.0};
    boolean glitchPresent;


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

        double FC = 4.3;
        double SM = .87;

        if(varioData.size() < 6) {
                return 0;
        }else{

                if(lastAltPoint[1] == 0.0){
                    lastAltPoint[0] = varioData.get(varioData.size()-2).altitude;
                    lastAltPoint[1] = varioData.get(varioData.size()-2).timestamp / 1000.0;
                }

                double[] currentPoint = {0.0, varioData.get(varioData.size()-1).timestamp / 1000.0};

                double sum = 0.0;
                double netTime = (varioData.get(varioData.size()-2).timestamp - varioData.get(varioData.size()-5).timestamp) / 1000.0;

                for(int i = varioData.size() - 4 ; i <= varioData.size() - 2 ; i++ ){

                    AltitudeEntry current = varioData.get(i);
                    AltitudeEntry prev = varioData.get(i-1);

                    double timeDifference = (current.timestamp - prev.timestamp) / 1000.0;
                    double altitudeReading = current.altitude;

                    if(i >= 3){
                        double plusOne = varioData.get(i+1).altitude;
                        double minusOne = prev.altitude;
                        double minusTwo = varioData.get(i-2).altitude;
                        double minusThree = varioData.get(i-3).altitude;

                        double[] glitchMedianSort = {plusOne, minusOne, minusTwo, minusThree};
                        Arrays.sort(glitchMedianSort);
//                        Log.i("***glitchMedianSort***", String.valueOf(glitchMedianSort[0]) + "  " + String.valueOf(glitchMedianSort[1]) + "  " + String.valueOf(glitchMedianSort[2]) + "   " + String.valueOf(glitchMedianSort[3]));

                        double glitchAvg = (plusOne + minusOne + minusTwo + minusThree) / 4;
//                        Log.i("*****glitchAvg*****", String.valueOf(glitchAvg));

                        glitchPresent = Math.abs(altitudeReading - glitchAvg) > (FC *  Math.abs(glitchMedianSort[2] - glitchAvg));
                        if(glitchPresent){

                            altitudeReading -= (altitudeReading - glitchAvg) * SM ;

                        }
                    }
                        sum += (timeDifference * altitudeReading);
                }

                currentPoint[0] = sum / netTime;


                double MpS = (currentPoint[0] - lastAltPoint[0]) / (currentPoint[1] - lastAltPoint[1]);

                lastAltPoint=currentPoint;

                return (float)MpS;
            }
        }

}




