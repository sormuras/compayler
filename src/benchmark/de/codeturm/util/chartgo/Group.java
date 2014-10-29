package de.codeturm.util.chartgo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Group {

  public String name = "";

  public List<Point> points = new LinkedList<>();

  public Group(String name, Point... points) {
    this.name = name;
    this.points.addAll(Arrays.asList(points));
  }

  public String toParameterValue(boolean x) {
    List<String> values = new ArrayList<>(points.size());
    points.forEach(p -> values.add(String.format("%d", x ? p.x.intValue() : p.y.intValue())));
    return String.join("\n", values);
  }

}
