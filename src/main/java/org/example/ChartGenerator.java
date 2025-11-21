package org.example;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisState;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTick;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.Tick;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ChartGenerator {

    public void generateChart(List<WaterLevelData> data, String title, String filePath) throws IOException {
        if (data == null || data.isEmpty()) {
            System.out.println("No data available to generate chart for " + title);
            return;
        }

        XYSeries series = new XYSeries("");
        double maxWaterLevel = 0.0;
        double minWaterLevel = Double.MAX_VALUE; // Initialize with a very large value

        for (int i = 0; i < data.size(); i++) {
            WaterLevelData d = data.get(i);
            series.add(i, d.getWaterLevel());
            if (d.getWaterLevel() > maxWaterLevel) {
                maxWaterLevel = d.getWaterLevel();
            }
            if (d.getWaterLevel() < minWaterLevel) {
                minWaterLevel = d.getWaterLevel();
            }
        }

        XYSeriesCollection dataset = new XYSeriesCollection(series);
        JFreeChart chart = ChartFactory.createXYLineChart(
                title,
                null, // No X-axis label
                null, // No Y-axis label
                dataset
        );

        // Customize chart
        chart.setBackgroundPaint(Color.WHITE);

        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinesVisible(false);
        plot.setRangeGridlinePaint(Color.DARK_GRAY);
        plot.getRenderer().setSeriesPaint(0, new Color(50, 150, 255));
        plot.setOutlineVisible(false);

        // Customize Y-axis (Range Axis)
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        // Dynamically set lower bound
        if (minWaterLevel < 6.0) {
            rangeAxis.setLowerBound(minWaterLevel);
        } else {
            rangeAxis.setLowerBound(6.0);
        }
        
        if (maxWaterLevel > 20.0) {
            rangeAxis.setUpperBound(50.0);
            rangeAxis.setTickUnit(new NumberTickUnit(5.0));
        } else {
            rangeAxis.setUpperBound(20.0);
            rangeAxis.setTickUnit(new NumberTickUnit(2.0));
        }

        // Set custom domain axis for precise hourly ticks
        plot.setDomainAxis(new HourlyNumberAxis(data));

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

    private static class HourlyNumberAxis extends NumberAxis {
        private final List<WaterLevelData> data;

        public HourlyNumberAxis(List<WaterLevelData> data) {
            super();
            this.data = data;
        }

        @Override
        public List<Tick> refreshTicks(Graphics2D g2, AxisState state, Rectangle2D dataArea, RectangleEdge edge) {
            List<Tick> ticks = new ArrayList<>();
            if (data == null || data.isEmpty()) {
                return ticks;
            }

            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

            // Simpler, more robust logic: find the first data point for each new hour.
            int lastHour = -1;
            for (int i = 0; i < data.size(); i++) {
                LocalTime time = data.get(i).getTime();
                int currentHour = time.getHour();
                if (currentHour != lastHour) {
                    // For the DST fallback, 1am -> 1am, this will trigger twice, which is correct.
                    ticks.add(new NumberTick(i, LocalTime.of(currentHour, 0).format(timeFormatter),
                            TextAnchor.TOP_CENTER, TextAnchor.CENTER, 0.0));
                    lastHour = currentHour;
                }
            }
            return ticks;
        }
    }
}
