package org.example;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.axis.DateTickUnitType;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

public class ChartGenerator {

    public void generateChart(List<WaterLevelData> data, String title, String filePath, LocalDate chartDate) throws IOException {
        TimeSeries series = new TimeSeries("Water Level");
        double maxWaterLevel = 0.0;

        for (WaterLevelData d : data) {
            Date date = Date.from(d.getTime().atDate(chartDate).atZone(ZoneId.systemDefault()).toInstant());
            series.add(new Second(date), d.getWaterLevel());
            if (d.getWaterLevel() > maxWaterLevel) {
                maxWaterLevel = d.getWaterLevel();
            }
        }

        TimeSeriesCollection dataset = new TimeSeriesCollection(series);
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                title,
                "Time",
                "Water Level (cm)",
                dataset,
                false,
                true,
                false
        );

        // Customize chart
        chart.setBackgroundPaint(Color.WHITE);

        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE); // Make chart background really white
        plot.setDomainGridlinesVisible(false); // No vertical grid lines
        plot.setRangeGridlinePaint(Color.DARK_GRAY); // Only horizontal grid lines
        plot.getRenderer().setSeriesPaint(0, new Color(50, 150, 255)); // Brighter blue line color
        plot.setOutlineVisible(false); // No surrounding solid line on the chart

        // Customize Y-axis (ValueAxis)
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setLowerBound(6.0); // Y axis bottom is always 6 cm.
        if (maxWaterLevel > 20.0) {
            rangeAxis.setUpperBound(50.0); // max is at 50 cm if data point more than 20 cm
            rangeAxis.setTickUnit(new NumberTickUnit(5.0)); // 5 cm interval
        } else {
            rangeAxis.setUpperBound(20.0); // Y axis max is always 20 cm
            rangeAxis.setTickUnit(new NumberTickUnit(2.0)); // 2 cm interval
        }

        // Customize X-axis (DateAxis)
        DateAxis domainAxis = (DateAxis) plot.getDomainAxis();
        domainAxis.setRange(
                Date.from(chartDate.atStartOfDay(ZoneId.systemDefault()).toInstant()),
                Date.from(chartDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant())
        ); // X axis is always from 0:00 to 0:00 for the day
        domainAxis.setTickUnit(new DateTickUnit(DateTickUnitType.HOUR, 2)); // Make time label 2 hour interval
        domainAxis.setDateFormatOverride(new SimpleDateFormat("HH:mm")); // Format time label as HH:mm

        // Create image with margin and save
        int width = 1600;
        int height = 900; // Make PNG height to 900 px.
        int margin = 20; // 20px margin

        BufferedImage image = new BufferedImage(width + margin, height + margin, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, width + margin, height + margin);

        // Draw chart with margin on top, no margin on left/bottom, and margin on right
        chart.draw(g2, new Rectangle2D.Double(0, margin, width, height));
        g2.dispose();

        ImageIO.write(image, "png", new File(filePath));
    }
}
