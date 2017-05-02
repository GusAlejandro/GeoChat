package com.example.geo.geochat;
import com.example.geo.geochat.Point;
/**
 * Created by gustavo on 5/2/17.
 */

public class BinManager {
    // structure would be (x1,y1), (x2,y2)....(x4,y4) as defined in docs
    Point[] mIV = {
            new Point(34.408913,-119.869647),
            new Point(34.417436,-119.869587),
            new Point(34.417195,-119.853599),
            new Point(34.407896,-119.853688)
    };

    Point[] mUCSB = {
            new Point(34.404263,-119.852816),
            new Point(34.403920,-119.838795),
            new Point(34.419527,-119.853570),
            new Point(34.419551,-119.839203)
    };

    public boolean isInBin(Point[] bin, Point location){
        // logic to determine if its in IV or UCSB
        return true;
    }

}
