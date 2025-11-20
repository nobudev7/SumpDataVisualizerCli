package org.example;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ChartGenerator {

    public void generateChart(List<WaterLevelData> data, String title, String filePath, LocalDate chartDate) throws IOException {
        if (data == null || data.isEmpty()) {
            System.out.println("No data available to generate chart for " + title);
            return;
        }

        XYSeries series = new XYSeries("");
        double maxWaterLevel = 0.0;

        for (int i = 0; i < data.size(); i++) {
            WaterLevelData d = data.get(i);
            series.add(i, d.getWaterLevel());
            if (d.getWaterLevel() > maxWaterLevel) {
                maxWaterLevel = d.getWaterLevel();
            }
        }

        XYSeriesCollection dataset = new XYSeriesCollection(series);
        JFreeChart chart = ChartFactory.createXYLineChart(
                title,
                "Time",
                "Water Level (cm)",
                dataset
        );

        // Customize chart
        chart.setBackgroundPaint(Color.WHITE);

        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinesVisible(false); // No vertical grid lines
        plot.setRangeGridlinePaint(Color.DARK_GRAY); // Only horizontal grid lines
        plot.getRenderer().setSeriesPaint(0, new Color(50, 150, 255));
        plot.setOutlineVisible(false);

        // Customize Y-axis (Range Axis)
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setLowerBound(6.0);
        if (maxWaterLevel > 20.0) {
            rangeAxis.setUpperBound(50.0);
            rangeAxis.setTickUnit(new NumberTickUnit(5.0));
        } else {
            rangeAxis.setUpperBound(20.0);
            rangeAxis.setTickUnit(new NumberTickUnit(2.0));
        }

        // Customize X-axis (Domain Axis)
        NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
        domainAxis.setTickLabelsVisible(true); // Show tick labels
        domainAxis.setTickMarksVisible(true); // Show tick marks
        domainAxis.setAxisLineVisible(true); // Ensure the axis line itself is visible

        // Dynamically calculate tick unit based on data density
        int totalPoints = data.size();
        LocalTime startTime = data.get(0).getTime();
        LocalTime endTime = data.get(totalPoints - 1).getTime();
        long durationInSeconds = Duration.between(startTime, endTime).getSeconds();
        
        // Handle overnight or DST cases where duration might be negative
        if (durationInSeconds < 0) {
            durationInSeconds += 24 * 3600;
        }
        if (durationInSeconds == 0) durationInSeconds = 1; // Avoid division by zero

        double pointsPerSecond = (double) totalPoints / durationInSeconds;
        int pointsPerHour = (int) (pointsPerSecond * 3600);

        if (pointsPerHour > 0) {
            domainAxis.setTickUnit(new NumberTickUnit(pointsPerHour));
        } else {
            // Fallback for very sparse data
            domainAxis.setTickUnit(new NumberTickUnit(totalPoints / 24.0));
        }
        
        domainAxis.setNumberFormatOverride(new CustomTimeFormat(data));

        // Create image with margin and save
        int width = 1600;
        int height = 900;
        int margin = 20;

        BufferedImage image = new BufferedImage(width + margin, height + margin, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, width + margin, height + margin);

        chart.draw(g2, new Rectangle2D.Double(0, margin, width, height));
        g2.dispose();

        ImageIO.write(image, "png", new File(filePath));
    }

    // Inner class for custom time formatting on the NumberAxis
    private static class CustomTimeFormat extends NumberFormat {
        private final List<WaterLevelData> data;
        private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        public CustomTimeFormat(List<WaterLevelData> data) {
            this.data = data;
        }

        @Override
        public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition pos) {
            int index = (int) Math.round(number);
            if (index >= 0 && index < data.size()) {
                return toAppendTo.append(data.get(index).getTime().format(timeFormatter));
            }
            return toAppendTo.append(""); // Return empty string for out-of-bounds indices
        }

        @Override
        public StringBuffer format(long number, StringBuffer toAppendTo, FieldPosition pos) {
            return format((double) number, toAppendTo, pos); // Delegate to the double version
        }

        @Override
        public Number parse(String source, ParsePosition parsePosition) {
            return null; // Not needed for formatting
        }
    }
}
