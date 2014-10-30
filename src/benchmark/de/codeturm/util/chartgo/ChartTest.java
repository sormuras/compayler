package de.codeturm.util.chartgo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.codeturm.util.chartgo.Chart.Background;
import de.codeturm.util.chartgo.Chart.ChartType;

public class ChartTest {

  @Test
  public void testChart() {
    Chart chart = new Chart(11, 11, 12, 12, 13, 10);
    // chart.go();
    assertEquals("http://www.chartgo.com/create.do?xaxis1=11%0A12%0A13&yaxis1=11%0A12%0A10", chart.toURI().toString());
  }

  @Test
  public void testLineChart() {
    Chart chart = new Chart("LineChart", 11, 11, 12, 12, 13, 10);
    chart.setChartType(ChartType.LINE);
    chart.setBackground(Background.GRADIENTGRAY);
    chart.setThreshold(1.5d);
    chart.setLineChartShape(true);
    chart.setLineChartTrendLine(true);
    chart.setLineChartCurve(true);
    chart.getGroups().add(new Chart.Group("2point", new Chart.Point(11, 1), new Chart.Point(12, 2), new Chart.Point(13, 4)));
    // chart.go();
    
    assertEquals(2, chart.getGroups().size());
    assertTrue(chart.toURI().toString().contains("shape"));
    assertTrue(chart.toURI().toString().contains("trendline"));
    assertTrue(chart.toURI().toString().contains("curve"));    
  }

}
