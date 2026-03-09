package com.sirma.pair_players_app.service;


import com.sirma.pair_players_app.entity.Record;
import com.sirma.pair_players_app.repository.PlayerRepository;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PlayerService {
    private final PlayerRepository playerRepository;
    private final RecordService recordService;

    public PlayerService(PlayerRepository playerRepository, RecordService recordService) {
        this.playerRepository = playerRepository;
        this.recordService = recordService;
    }


    public List<Integer> pairPlayer(){
        List<Record> records = recordService.loadAllRecords();
        Map<Long, List<Record>> matchRecordsList = records          // групирам записите по даден мач
                .stream()
                .collect(Collectors.groupingBy(r -> r.getMatch().getId()));

        Map<String, Integer> pairMinutes = new HashMap<>();           // тук пазя двойката и обшото им врме за всички мачове
        Map<String, List<Integer>> pairMatchesTime = new HashMap<>(); // String e двойката 1-3 и лист със обшото време на двамата за всеки мач

        for (Map.Entry<Long, List<Record>> entry : matchRecordsList.entrySet()){
            List<Record> matchRecords = entry.getValue();

            for (int i = 0; i < matchRecords.size(); i++){
                Long currentPlayerId = matchRecords.get(i).getPlayer().getId();
                Integer currentPlayerFromMinutes = matchRecords.get(i).getFromMinutes();
                Integer currentPlayerToMinutes = matchRecords.get(i).getToMinutes();

                for (int j = i + 1; j < matchRecords.size(); j++){
                    Long nextPlayerId = matchRecords.get(j).getPlayer().getId();
                    Integer nextPlayerFromMinutes = matchRecords.get(j).getFromMinutes();
                    Integer nextPlayerToMinutes = matchRecords.get(j).getToMinutes();

                    Integer overlapStart = Math.max(currentPlayerFromMinutes, nextPlayerFromMinutes);
                    Integer overlapEnd = Math.min(currentPlayerToMinutes, nextPlayerToMinutes);
                    Integer totalMinTogether = Math.max(0, overlapEnd - overlapStart);

                    String key = generatePairKey(currentPlayerId, nextPlayerId);  // логика за създаване на ключа на всяка двойка

                    if(totalMinTogether > 0){
                        pairMinutes.put(key, pairMinutes.getOrDefault(key, 0) + totalMinTogether); // подавам в Map-а двойка и общо време на терена през всичките мачове
                        pairMatchesTime
                                .computeIfAbsent(key, m -> new ArrayList<>())
                                .add(totalMinTogether);  // добавям времето на двойката по време на следващия им открит мач
                    }
                }
            }
        }

        // намирам от Map-а двойката с най-голямо value, като го сортирам по value и я слагам първа
        Map<String, Integer> sortedDesc = pairMinutes.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));


        Map.Entry<String, Integer> highestPair = pairMinutes
                .entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .orElseThrow();

        return pairMatchesTime.get(highestPair.getKey());
    }

    private String generatePairKey(Long currentPlayerId, Long nextPlayerId) {
        if(currentPlayerId < nextPlayerId){
            return currentPlayerId + "-" + nextPlayerId;
        } else {
            return nextPlayerId + "-" + currentPlayerId;
        }
    }
}
