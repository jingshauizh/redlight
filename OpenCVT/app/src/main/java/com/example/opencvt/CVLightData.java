package com.example.opencvt;

public class CVLightData {
    public static int CV_STATUS_RED = 1001;
    public static int CV_STATUS_GREEN = 1002;
    public static int CV_STATUS_No_light = 1003;

    private int redCount = 0;
    private int greenCount = 0;

    public int getRedCount() {
        return redCount;
    }

    public void setRedCount(int redCount) {
        this.redCount = redCount;
    }

    public int getGreenCount() {
        return greenCount;
    }

    public void setGreenCount(int greenCount) {
        this.greenCount = greenCount;
    }

    public int checkLightType(){
        if(redCount == greenCount){
            return CV_STATUS_No_light;
        }
        else if(redCount > greenCount){
            return CV_STATUS_RED;
        }
        else{
            return CV_STATUS_GREEN;
        }
    }
}
