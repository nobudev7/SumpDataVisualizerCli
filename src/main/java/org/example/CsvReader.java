package org.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CsvReader {

    public List<WaterLevelData> readWaterLevelData(String filePath) throws IOException {
        List<WaterLevelData> data = new ArrayList<>();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length == 2) {
                    try {
                        LocalTime time = LocalTime.parse(values[0], timeFormatter);
                        double waterLevel = Double.parseDouble(values[1]);
                        data.add(new WaterLevelData(time, waterLevel));
                    } catch (Exception e) {
                        // Ignore lines that cannot be parsed
                    }
                }
            }
        }
        return data;
    }
}
