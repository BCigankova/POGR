package utils;

import java.awt.*;

public class Line {
    private final double slope;
    private final double intercept;
    private Point start;
    private Point end;

    public Line(Point start, Point end) {
        this.slope = computeSlope(start, end);
        this.intercept = computeIntercept(start, slope);
        this.start = start;
        this.end = end;
    }

    public Line(double slope, double intercept) {
        this.slope = slope;
        this.intercept = intercept;
    }

    public double computeSlope(Point A, Point B) {
        return (double) (A.y - B.y) / (A.x - B.x);
    }

    public double computeIntercept(Point A, double slope) {
        return A.y - slope * A.x;
    }

    public boolean isOnLine(Point A) {
        return A.y == (int) (slope * A.x + intercept);
    }

    public boolean isOnLineWithBoundary(Point A) {
        if (!isOnLine(A)) {
            return false;
        }
        int minX = Math.min(start.x, end.x);
        int maxX = Math.max(start.x, end.x);
        int minY = Math.min(start.y, end.y);
        int maxY = Math.max(start.y, end.y);

        return (A.x >= minX && A.x <= maxX) && (A.y >= minY && A.y <= maxY);
    }

    public String toString() {
        return "y = " + slope + " * x + " + intercept;
    }

    public Point getStart() {
        return start;
    }

    public Point getEnd() {
        return end;
    }

    public double getSlope() {
        return slope;
    }

    public double getIntercept() {
        return intercept;
    }
}
