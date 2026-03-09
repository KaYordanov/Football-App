package com.sirma.pair_players_app.service.file;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataLoaderService implements CommandLineRunner {
    private final CsvFileService csvFileService;

    public DataLoaderService(CsvFileService csvFileService) {
        this.csvFileService = csvFileService;
    }

    @Override
    public void run(String... args) throws Exception {
        csvFileService.loadData();
    }
}
