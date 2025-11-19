package org.example;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ui.RectangleAnchor;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ChartGenerator {

    public void generateChart(List<WaterLevelData> data, String title, String filePath, LocalDate chartDate) throws IOException {
        XYSeries series = new XYSeries("");
        double maxWaterLevel = 0.0;

        List<ValueMarker> markers = new ArrayList<>();
        Set<Integer> labeledHours = new HashSet<>();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        for (int i = 0; i < data.size(); i++) {
            WaterLevelData d = data.get(i);
            series.add(i, d.getWaterLevel());
            if (d.getWaterLevel() > maxWaterLevel) {
                maxWaterLevel = d.getWaterLevel();
            }

            // Logic to find the first index for each 2-hour label
            int currentHour = d.getTime().getHour();
            if (currentHour % 2 == 0 && !labeledHours.contains(currentHour)) {
                ValueMarker marker = new ValueMarker(i);
                marker.setLabel(d.getTime().format(timeFormatter));
                marker.setLabelFont(new Font("SansSerif", Font.PLAIN, 12));
                marker.setLabelTextAnchor(TextAnchor.TOP_CENTER);
                marker.setLabelAnchor(RectangleAnchor.BOTTOM);
                marker.setPaint(Color.DARK_GRAY);
                markers.add(marker);
                labeledHours.add(currentHour);
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
        plot.setDomainGridlinesVisible(false);
        plot.setRangeGridlinePaint(Color.DARK_GRAY);
        plot.getRenderer().setSeriesPaint(0, new Color(50, 150, 255));
        plot.setOutlineVisible(false);

        // Add the custom time labels as markers
        for (ValueMarker marker : markers) {
            plot.addDomainMarker(marker);
        }

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
        domainAxis.setTickLabelsVisible(false); // Hide default numerical labels
        domainAxis.setTickMarksVisible(false); // Hide tick marks
        domainAxis.setAxisLineVisible(true); // Ensure the axis line itself is visible

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
}
