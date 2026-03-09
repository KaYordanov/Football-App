package com.sirma.pair_players_app.service;

import com.sirma.pair_players_app.entity.Record;
import com.sirma.pair_players_app.repository.RecordRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class RecordService {
    private final RecordRepository recordRepository;

    public RecordService(RecordRepository recordRepository) {
        this.recordRepository = recordRepository;
    }

    public List<Record> loadAllRecords() {
        return recordRepository.findAll();
    }
}
