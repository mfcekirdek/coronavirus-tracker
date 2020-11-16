package com.mfc.coronavirustracker.services;

import com.mfc.coronavirustracker.models.LocationStats;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@Service
//@Component("coronaVirusDataService")
public class CoronaVirusDataService {

    private static final String VIRUS_DATA_URL = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_confirmed_global.csv";
    private List<LocationStats> allStats = new ArrayList<LocationStats>();

    @PostConstruct // Uygulama ayağa kalktıktan sonra çalıştır.
    @Scheduled(cron = "* * 1 * * *") // second min hour day month year      s:m:h:d:m:y // Hergün saat 1de çalıştır.
    public void fetchVirusData() throws IOException, InterruptedException {
        List<LocationStats> newStats = new ArrayList<LocationStats>();
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(VIRUS_DATA_URL))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String virusData = response.body();
        try {
            StringReader csvBodyReader = new StringReader(virusData);
            Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(csvBodyReader);
            for (CSVRecord record : records) {
                LocationStats locationStats = new LocationStats();
                locationStats.setState(record.get("Province/State"));
                locationStats.setCountry(record.get("Country/Region"));
                int latestCases = Integer.parseInt(record.get(record.size() - 1));
                int prevDayCases = Integer.parseInt(record.get(record.size() - 2));
                locationStats.setLatestTotalCases(latestCases);
                locationStats.setDiffFromPrevDay(latestCases - prevDayCases);
                newStats.add(locationStats);
            }
            this.allStats = newStats;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("FileNotFoundException");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("IOException");
        }
    }

    public List<LocationStats> getAllStats() {
        return allStats;
    }

}
