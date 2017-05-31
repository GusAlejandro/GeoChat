package com.example.geo.geochat;
import com.example.geo.geochat.Point;
/**
 * Created by gustavo on 5/2/17.
 */

public class BinManager {
    // structure would be (x1,y1), (x2,y2)....(x4,y4) as defined in docs bL,tL,tR,bR
    static Point[] mIV = {
            new Point(34.407896,-119.874907), // bottom left
            new Point(34.420908,  -119.874907),//Top Left
            new Point(34.420908,-119.853688), // Top right
            new Point(34.407896,-119.853688) // bottom right
    };

    static Point[] mUCSB = {
            new Point(34.404263,-119.853000), // bottom left
            new Point(34.419551,-119.853000), // top left
            new Point(34.419551,-119.839203), // top right
            new Point(34.403920,-119.838795) // bottom right
    };

    public static boolean isInBin(Point[] bin, Point location){
        // this is horrible and should never see the light of day, I'll fix it soon
        // logic to determine if its in a given bin
        double x = location.getX();
        double y = location.getY();

        // first check x min value
        if (x > bin[0].getX()){
            // check against max x value
            if (x < bin[1].getX()){
                // check against min y value
                if (y > bin[1].getY()){
                    // check against max y value
                    if (y < bin[2].getY()){
                        return true;
                    }else {
                        return false;
                    }
                }else{
                    return false;
                }
            }else {
                return false;
            }
        }else {
            return false;
        }
    }

}
