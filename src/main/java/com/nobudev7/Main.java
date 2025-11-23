package com.nobudev7;

import org.apache.commons.cli.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class Main {
    public static void main(String[] args) {
        Options options = new Options();

        options.addOption(Option.builder("i")
                .longOpt("inputDir")
                .hasArg()
                .required()
                .desc("Input directory containing waterlevel CSV files")
                .build());

        options.addOption(Option.builder("o")
                .longOpt("outputDir")
                .hasArg()
                .required()
                .desc("Output directory for generated chart PNGs")
                .build());

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            formatter.printHelp("SumpDataVisualizer", options);
            System.exit(1);
            return;
        }

        String inputDir = cmd.getOptionValue("i");
        String outputDir = cmd.getOptionValue("o");

        Pattern pattern = Pattern.compile("waterlevel-(\\d{8})\\.csv");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        DateTimeFormatter titleDateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");

        try (Stream<Path> paths = Files.walk(Paths.get(inputDir))) {
            paths.filter(Files::isRegularFile)
                    .forEach(path -> {
                        Matcher matcher = pattern.matcher(path.getFileName().toString());
                        if (matcher.matches()) {
                            String dateString = matcher.group(1);
                            LocalDate chartDate = LocalDate.parse(dateString, dateFormatter);
                            String year = chartDate.format(DateTimeFormatter.ofPattern("yyyy"));
                            String month = chartDate.format(DateTimeFormatter.ofPattern("MM"));

                            Path outputSubDir = Paths.get(outputDir, year, month);
                            try {
                                Files.createDirectories(outputSubDir);

                                CsvReader reader = new CsvReader();
                                List<WaterLevelData> data = reader.readWaterLevelData(path.toString());

                                ChartGenerator chartGenerator = new ChartGenerator();
                                String chartTitle = "Water Level on " + chartDate.format(titleDateFormatter);
                                String outputFilePath = outputSubDir.resolve("waterlevel-" + dateString + ".png").toString();
                                System.out.println("Start generating chart for " + chartDate.format(titleDateFormatter) + " at " + outputFilePath);
                                chartGenerator.generateChart(data, chartTitle, outputFilePath);
                                System.out.println("Generated chart for " + chartDate.format(titleDateFormatter) + " at " + outputFilePath);

                            } catch (IOException e) {
                                // Show the error and go on to the next file.
                                System.err.println("File IO error: " + path.getFileName());
                            }
                        }
                    });
        } catch (IOException e) {
            System.err.println("Walk failed: " + inputDir);
        }
    }
}