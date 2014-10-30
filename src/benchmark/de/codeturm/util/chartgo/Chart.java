package de.codeturm.util.chartgo;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Chart {

  public static enum Background {

    BRUSHGRAY, GRADIENTBLUE, GRADIENTGRAY, GRADIENTGREEN, GRADIENTPURPLE, GRADIENTRED, GRADIENTSILVER, WHITE;

    public String toString() {
      return name().toLowerCase();
    }

  }

  public static enum ChartType {

    AREA, BAR, LINE, PIE;

    public String toString() {
      return name().toLowerCase();
    }

  }
  
  public static class Group {

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
  
  public static class Point {

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

  private Background background;
  private ChartType chartType;
  private Boolean colorplotonly;
  private Boolean gradient;
  private Boolean gridlines;
  private List<Group> groups;
  private Integer height;
  private String hostname;
  private Boolean labels;
  private Boolean legend;
  private Double maxY;
  private Double minY;
  private String pathname;
  private Boolean roundedge;
  private String scheme;
  private Boolean shadow;
  private Boolean show3D;
  private Double threshold;
  private String title;
  private String titleSub;
  private String titleX;
  private String titleY;
  private Boolean transparency;
  private Integer width;

  private Boolean lineChartShape;
  private Boolean lineChartTrendLine;
  private Boolean lineChartCurve;

  public Chart() {
    setScheme("http");
    setHostname("www.chartgo.com");
    setPathname("/create.do");
    setGroups(new LinkedList<>());
  }

  public Chart(Number... points) {
    this(null, points);

  }

  public Chart(String title, Number... points) {
    this();
    setTitle(title);
    if (points != null && points.length >= 2) {
      if (points.length % 2 != 0)
        throw new IllegalArgumentException("Points array length is not even: " + points.length);
      Point[] array = new Point[points.length >> 1];
      for (int i = 0; i < array.length; i++) {
        array[i] = new Point(points[(i << 1)], points[(i << 1) + 1]);
      }
      getGroups().add(new Group(null, array));
    }
  }

  public Background getBackground() {
    return background;
  }

  public ChartType getChartType() {
    return chartType;
  }

  public Boolean getColorplotonly() {
    return colorplotonly;
  }

  public Boolean getGradient() {
    return gradient;
  }

  public Boolean getGridlines() {
    return gridlines;
  }

  public List<Group> getGroups() {
    return groups;
  }

  public Integer getHeight() {
    return height;
  }

  public String getHostname() {
    return hostname;
  }

  public Boolean getLabels() {
    return labels;
  }

  public Boolean getLegend() {
    return legend;
  }

  public Double getMaxY() {
    return maxY;
  }

  public Double getMinY() {
    return minY;
  }

  public String getPathname() {
    return pathname;
  }

  public Boolean getRoundedge() {
    return roundedge;
  }

  public String getScheme() {
    return scheme;
  }

  public Boolean getShadow() {
    return shadow;
  }

  public Boolean getShow3D() {
    return show3D;
  }

  public Double getThreshold() {
    return threshold;
  }

  public String getTitle() {
    return title;
  }

  public String getTitleSub() {
    return titleSub;
  }

  public String getTitleX() {
    return titleX;
  }

  public String getTitleY() {
    return titleY;
  }

  public Boolean getTransparency() {
    return transparency;
  }

  public Integer getWidth() {
    return width;
  }

  public void go() {

    if (!System.getProperty("os.name").startsWith("Windows")) {
      return;
    }

    try {
      String url = toURI().toURL().toExternalForm();
      if (url.length() > 2048)
        System.err.println("URL too long! " + url.length());
      else
        Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
    } catch (Exception e) {
      // ignore
    }

  }

  public void setBackground(Background background) {
    this.background = background;
  }

  public void setChartType(ChartType chartType) {
    this.chartType = chartType;
  }

  /**
   * If true, color is only displayed on the plot background instead of the whole image background.
   */
  public void setColorplotonly(Boolean colorplotonly) {
    this.colorplotonly = colorplotonly;
  }

  public void setGradient(Boolean gradient) {
    this.gradient = gradient;
  }

  public void setGridlines(Boolean gridlines) {
    this.gridlines = gridlines;
  }

  public void setGroups(List<Group> groups) {
    this.groups = groups;
  }

  public void setHeight(Integer height) {
    this.height = height;
  }

  public void setHostname(String hostname) {
    this.hostname = hostname;
  }

  public void setLabels(Boolean labels) {
    this.labels = labels;
  }

  public void setLegend(Boolean legend) {
    this.legend = legend;
  }

  /**
   * Set the highest number to display on the Y Axes. Leave blank for default.
   */
  public void setMaxY(Double maxY) {
    this.maxY = maxY;
  }

  /**
   * Set the lowest number to display on the Y Axes. Leave blank for default.
   */
  public void setMinY(Double minY) {
    this.minY = minY;
  }

  public void setPathname(String pathname) {
    this.pathname = pathname;
  }

  public void setRoundedge(Boolean roundedge) {
    this.roundedge = roundedge;
  }

  public void setScheme(String scheme) {
    this.scheme = scheme;
  }

  public void setShadow(Boolean shadow) {
    this.shadow = shadow;
  }

  public void setShow3D(Boolean show3d) {
    this.show3D = show3d;
  }

  /**
   * Displays a dotted line on the Y axis. Requires a number between the lowest and highest Y axis values.
   * 
   * @param threshold
   *          Y axis value to highlight or <code>null</code> if no line should be drawn.
   */
  public void setThreshold(Double threshold) {
    this.threshold = threshold;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public void setTitleSub(String titleSub) {
    this.titleSub = titleSub;
  }

  public void setTitleX(String titleX) {
    this.titleX = titleX;
  }

  public void setTitleY(String titleY) {
    this.titleY = titleY;
  }

  public void setTransparency(Boolean transparency) {
    this.transparency = transparency;
  }

  public void setWidth(Integer width) {
    this.width = width;
  }

  public URI toURI() {
    StringBuilder query = new StringBuilder();

    toURIQueryParameter(query, "chart", getChartType());
    toURIQueryParameter(query, "width", getWidth());
    toURIQueryParameter(query, "height", getHeight());
    toURIQueryParameter(query, "title", getTitle());
    toURIQueryParameter(query, "subtitle", getTitleSub());
    toURIQueryParameter(query, "xtitle", getTitleX());
    toURIQueryParameter(query, "ytitle", getTitleY());
    toURIQueryParameter(query, "show3d", getShow3D());
    toURIQueryParameter(query, "threshold", getThreshold());
    toURIQueryParameter(query, "legend", getLegend());
    toURIQueryParameter(query, "labels", getLabels());
    toURIQueryParameter(query, "gridlines", getGridlines());
    toURIQueryParameter(query, "transparency", getTransparency());
    toURIQueryParameter(query, "chrtbkgndcolor", getBackground());
    toURIQueryParameter(query, "colorplotonly", getColorplotonly());
    toURIQueryParameter(query, "max_yaxis", getMaxY());
    toURIQueryParameter(query, "min_yaxis", getMinY());
    toURIQueryParameter(query, "gradient", getGradient());
    toURIQueryParameter(query, "roundedge", getRoundedge());
    toURIQueryParameter(query, "shadow", getShadow());

    if (getChartType() == ChartType.LINE) {
      toURIQueryParameter(query, "shape", getLineChartShape());
      toURIQueryParameter(query, "trendline", getLineChartTrendLine());
      toURIQueryParameter(query, "curve", getLineChartCurve());
    }

    for (int i = 0; i < groups.size(); i++) {
      Group group = groups.get(i);
      toURIQueryParameter(query, "group" + (i + 1), group.name);
      query.append("&xaxis").append(i + 1).append("=").append(group.toParameterValue(true));
      query.append("&yaxis").append(i + 1).append("=").append(group.toParameterValue(false));
    }

    String queryString = query.charAt(0) == '&' ? query.substring(1) : query.toString();

    try {
      String userInfo = null;
      int port = -1;
      String fragment = null;
      return new URI(scheme, userInfo, hostname, port, pathname, queryString, fragment);

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void toURIQueryParameter(StringBuilder builder, String name, Object value) {
    if (value == null)
      return;
    if (value instanceof Boolean)
      value = Boolean.TRUE == value ? "1" : "0";
    toURIQueryParameter(builder, name, value.toString());
  }

  public void toURIQueryParameter(StringBuilder builder, String name, String value) {
    if (value == null || value.isEmpty())
      return;
    builder.append("&").append(name).append("=").append(value);
  }

  public Boolean getLineChartShape() {
    return lineChartShape;
  }

  public void setLineChartShape(Boolean lineChartShape) {
    this.lineChartShape = lineChartShape;
  }

  public Boolean getLineChartTrendLine() {
    return lineChartTrendLine;
  }

  public void setLineChartTrendLine(Boolean lineChartTrendLine) {
    this.lineChartTrendLine = lineChartTrendLine;
  }

  public Boolean getLineChartCurve() {
    return lineChartCurve;
  }

  public void setLineChartCurve(Boolean lineChartCurve) {
    this.lineChartCurve = lineChartCurve;
  }

}
