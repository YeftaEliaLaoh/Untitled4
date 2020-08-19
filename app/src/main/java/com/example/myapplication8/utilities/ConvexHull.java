package com.example.myapplication8.utilities;

import java.util.Arrays;

public class ConvexHull{

    public static double cross(Point O, Point A, Point B){
        return (A.x - O.x) * (B.y - O.y) - (A.y - O.y) * (B.x - O.x);
    }

    public static Point[] convexHull(Point[] P){

        if(P.length > 1)
        {
            int n = P.length, k = 0;
            Point[] h = new Point[2 * n];

            Arrays.sort(P);

            // Build lower hull
            for(Point point : P)
            {
                while(k >= 2 && cross(h[k - 2], h[k - 1], point) <= 0)
                    k--;
                h[k++] = point;
            }

            // Build upper hull
            for(int i = n - 2, t = k + 1; i >= 0; i--)
            {
                while(k >= t && cross(h[k - 2], h[k - 1], P[i]) <= 0)
                    k--;
                h[k++] = P[i];
            }
            if(k > 1)
            {
                h = Arrays.copyOfRange(h, 0, k - 1); // remove non-hull vertices after k; remove k - 1 which is a duplicate
            }
            return h;
        }
        else if(P.length <= 1)
        {
            return P;
        }
        else
        {
            return null;
        }
    }
}
