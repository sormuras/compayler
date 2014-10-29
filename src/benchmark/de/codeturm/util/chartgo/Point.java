package de.codeturm.util.chartgo;

public class Point {

  public Double x;
  public Double y;

  public Point(double x, double y) {
    this.x = Double.valueOf(x);
    this.y = Double.valueOf(y);
  }

  public Point(Double x, Double y) {
    this.x = x;
    this.y = y;
  }

  public Point(Number x, Number y) {
    this.x = x.doubleValue();
    this.y = y.doubleValue();
  }

  public Point(String x, String y) {
    this.x = Double.valueOf(x);
    this.y = Double.valueOf(y);
  }

}
