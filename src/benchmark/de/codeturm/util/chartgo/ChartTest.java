package de.codeturm.util.chartgo;

import org.junit.Test;

import de.codeturm.util.chartgo.Chart.ChartType;

public class ChartTest {

  @Test
  public void testSimpleChart() {
    Chart chart = new Chart("€ $+²", 11, 11, 12, 12, 13, 10);

    chart.setChartType(ChartType.LINE);
    chart.setShow3D(true);
    chart.setThreshold(1.5d);
    chart.getGroups().add(new Group("2point", new Point(11, 1), new Point(12, 2), new Point(13, 4)));
    // assertEquals("http://www.chartgo.com/create.do?xaxis1=1%0A2&yaxis1=1%0A2", chart.toURI().toString());
    // chart.go();
  }

}
