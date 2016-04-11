package com.example.aubreyford.vario;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

public class VarioData {
    ArrayList<AltitudeEntry> varioData;
    double[] lastAltPoint = {0.0, 0.0};
    boolean glitch;


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
        double SM = .8;

        if(varioData.size() < 6) {
                return 0;
        }else{

//                Log.i("*****currentValue*****", String.valueOf(varioData.get(varioData.size()-1).altitude));

                if(lastAltPoint[1] == 0.0){
                    lastAltPoint[0] = varioData.get(varioData.size()-2).altitude;
                    lastAltPoint[1] = varioData.get(varioData.size()-2).timestamp / 1000.0;
                }

                double[] currentPoint = {0.0, varioData.get(varioData.size()-1).timestamp / 1000.0};

                double sum = 0.0;
                double netTime = (varioData.get(varioData.size()-2).timestamp - varioData.get(varioData.size()-6).timestamp) / 1000.0;

                for(int i = varioData.size() - 5 ; i <= varioData.size() - 2 ; i++ ){

                    AltitudeEntry current = varioData.get(i);
                    AltitudeEntry prev = varioData.get(i-1);

                    double timeDifference = (current.timestamp - prev.timestamp) / 1000.0;
                    double altitudeReading = current.altitude;
//                    Log.i("****READING***", String.valueOf(altitudeReading));



                    if(i >= 3){
                        double plusOne = varioData.get(i+1).altitude;
                        double minusOne = prev.altitude;
                        double minusTwo = varioData.get(i-2).altitude;
                        double minusThree = varioData.get(i-3).altitude;
//                        Log.i("***PLUSONE****", String.valueOf(plusOne));
//                        Log.i("***MINUSONE****", String.valueOf(minusOne));
//                        Log.i("***MINUSTWO****", String.valueOf(minusTwo));
//                        Log.i("***MINUSTHREE****", String.valueOf(minusThree));

                        double[] glitchMedianSort = {plusOne, minusOne, minusTwo, minusThree};
                        Arrays.sort(glitchMedianSort);
//                        Log.i("***glitchMedianSort***", String.valueOf(glitchMedianSort[0]) + "  " + String.valueOf(glitchMedianSort[1]) + "  " + String.valueOf(glitchMedianSort[2]) + "   " + String.valueOf(glitchMedianSort[3]));

                        double glitchAvg = (plusOne + minusOne + minusTwo + minusThree) / 4;
//                        Log.i("*****glitchAvg*****", String.valueOf(glitchAvg));



                        if( Math.abs(altitudeReading - glitchAvg) > FC *  Math.abs(glitchMedianSort[1] - glitchAvg) ){

                            altitudeReading -= (altitudeReading - glitchAvg) * SM ;

                            Log.i("***BOOL****", String.valueOf(    Math.abs(altitudeReading - glitchAvg) > (FC * Math.abs(glitchMedianSort[1] - glitchAvg)) ));
                            Log.i("****READING***", String.valueOf(altitudeReading));
                            Log.i("*****ALTERED*****", String.valueOf(altitudeReading));
                            Log.i("****FIRSTOP*****", String.valueOf(Math.abs(altitudeReading - glitchAvg)));
                            Log.i("****SECONDOP*****", String.valueOf(FC *  Math.abs(glitchMedianSort[1] - glitchAvg)));

                        }


                    }




                        sum += (timeDifference * altitudeReading);

                }

                currentPoint[0] = sum / netTime;

//            Log.i("****currentPoint****", String.valueOf(currentPoint[0]));
//            Log.i("****currentPoint****", String.valueOf(currentPoint[1]));
//            Log.i("****lastAltPoint****", String.valueOf(lastAltPoint[0]));
//            Log.i("****lastAltPoint****", String.valueOf(lastAltPoint[1]));

                double MpS = (currentPoint[0] - lastAltPoint[0]) / (currentPoint[1] - lastAltPoint[1]);

//            Log.i("******MPS:", String.valueOf(MpS));

                lastAltPoint=currentPoint;

                return (float)MpS;
            }
        }

}




