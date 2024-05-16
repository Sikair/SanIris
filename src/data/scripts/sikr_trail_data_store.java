package data.scripts;

import java.util.ArrayList;
import java.util.List;

public class sikr_trail_data_store {

    static List<sikr_trailData> sikr_trailDataStoreList = new ArrayList<>();

    public static void addToTrailList(sikr_trailData trail){
        sikr_trailDataStoreList.add(trail);
    }

    public static sikr_trailData getTrailWithId(String id){
        for (sikr_trailData trail : sikr_trailDataStoreList) {
            if(trail.shipId.equals(id)) return trail;
        }
        return null;
    }

    public static class sikr_trailData{
        public final String shipId;
        public final float startSpeed;
        public final float endSpeed;
        public final float startAngular;
        public final float endAngular;
        public final float startSize;
        public final float endSize; 
        public final String colorIn;
        public final String colorOut;
        public final float inDuration;
        public final float mainDuration;
        public final float outDuration;
        public final float loopLength;
        public final float loopSpeed;

        public sikr_trailData(String shipId, String startSpeed, String endSpeed, String startAngular, String endAngular, String startSize,
            String endSize, String colorIn, String colorOut, String inDuration, String mainDuration, String outDuration,
            String loopLength, String loopSpeed) {
                this.shipId = shipId;
                this.startSpeed = Float.parseFloat(startSpeed);
                this.endSpeed = Float.parseFloat(endSpeed);
                this.startAngular = Float.parseFloat(startAngular);
                this.endAngular = Float.parseFloat(endAngular);
                this.startSize = Float.parseFloat(startSize);
                this.endSize = Float.parseFloat(endSize);
                this.colorIn = colorIn;
                this.colorOut = colorOut;
                this.inDuration = Float.parseFloat(inDuration);
                this.mainDuration = Float.parseFloat(mainDuration);  
                this.outDuration = Float.parseFloat(outDuration);
                this.loopLength = Float.parseFloat(loopLength);             
                this.loopSpeed = Float.parseFloat(loopSpeed);
        }
    }
}
