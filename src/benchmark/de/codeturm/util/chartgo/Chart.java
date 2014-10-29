package de.codeturm.util.chartgo;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;

public class Chart {

  public static enum Background {

    GRADIENTGRAY, BRUSHGRAY;

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

  private Background background;
  private ChartType chartType;
  private Boolean colorplotonly;
  private Boolean gridlines;
  private List<Group> groups;
  private String hostname;
  private Boolean legend;
  private String pathname;
  private String scheme;
  private Boolean show3D;
  private Double threshold;
  private String title;
  private String titleSub;
  private String titleX;
  private String titleY;

  public Chart() {
    setScheme("http");
    setHostname("www.chartgo.com");
    setPathname("/create.do");
    setGroups(new LinkedList<>());
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

  public List<Group> getGroups() {
    return groups;
  }

  public String getHostname() {
    return hostname;
  }

  public Boolean getLegend() {
    return legend;
  }

  public String getPathname() {
    return pathname;
  }

  public String getScheme() {
    return scheme;
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

  public void setGroups(List<Group> groups) {
    this.groups = groups;
  }

  public void setHostname(String hostname) {
    this.hostname = hostname;
  }

  public void setLegend(Boolean legend) {
    this.legend = legend;
  }

  public void setPathname(String pathname) {
    this.pathname = pathname;
  }

  public void setScheme(String scheme) {
    this.scheme = scheme;
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

  public URI toURI() {

    StringBuilder query = new StringBuilder();
    toURIQueryParameter(query, "title", getTitle());
    toURIQueryParameter(query, "subtitle", getTitleSub());
    toURIQueryParameter(query, "xtitle", getTitleX());
    toURIQueryParameter(query, "ytitle", getTitleY());
    toURIQueryParameter(query, "chart", getChartType());
    toURIQueryParameter(query, "show3d", getShow3D());
    toURIQueryParameter(query, "threshold", getThreshold());
    toURIQueryParameter(query, "legend", getLegend());
    toURIQueryParameter(query, "gridlines", getGridlines());
    toURIQueryParameter(query, "chrtbkgndcolor", getBackground());
    toURIQueryParameter(query, "colorplotonly", getColorplotonly());

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
    if (value instanceof Boolean) {
      toURIQueryParameter(builder, name, Boolean.TRUE == value ? "1" : "0");
      return;
    }
    toURIQueryParameter(builder, name, String.valueOf(value));
  }

  public void toURIQueryParameter(StringBuilder builder, String name, String value) {
    if (value == null || value.isEmpty())
      return;
    builder.append("&").append(name).append("=").append(value);
  }

  public Boolean getGridlines() {
    return gridlines;
  }

  public void setGridlines(Boolean gridlines) {
    this.gridlines = gridlines;
  }

  public String getTitleX() {
    return titleX;
  }

  public void setTitleX(String titleX) {
    this.titleX = titleX;
  }

  public String getTitleY() {
    return titleY;
  }

  public void setTitleY(String titleY) {
    this.titleY = titleY;
  }

  public String getTitleSub() {
    return titleSub;
  }

  public void setTitleSub(String titleSub) {
    this.titleSub = titleSub;
  }

  public Boolean getColorplotonly() {
    return colorplotonly;
  }

  public void setColorplotonly(Boolean colorplotonly) {
    this.colorplotonly = colorplotonly;
  }

}
