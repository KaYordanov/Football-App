package com.sirma.pair_players_app.service.file;

import com.sirma.pair_players_app.entity.Match;
import com.sirma.pair_players_app.entity.Player;
import com.sirma.pair_players_app.entity.Record;
import com.sirma.pair_players_app.entity.Team;
import com.sirma.pair_players_app.enums.TeamGroup;
import com.sirma.pair_players_app.repository.MatchRepository;
import com.sirma.pair_players_app.repository.PlayerRepository;
import com.sirma.pair_players_app.repository.RecordRepository;
import com.sirma.pair_players_app.repository.TeamRepository;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CsvFileService {
    private final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;
    private final MatchRepository matchRepository;
    private final RecordRepository recordRepository;

    public CsvFileService(TeamRepository teamRepository, PlayerRepository playerRepository, MatchRepository matchRepository, RecordRepository recordRepository) {
        this.teamRepository = teamRepository;
        this.playerRepository = playerRepository;
        this.matchRepository = matchRepository;
        this.recordRepository = recordRepository;
    }


    public void loadData() {
        Resource teams = resolver.getResource("classpath:files/teams.csv");
        Resource players = resolver.getResource("classpath:files/players.csv");
        Resource matches = resolver.getResource("classpath:files/matches.csv");
        Resource records = resolver.getResource("classpath:files/records.csv");

        Map<Long, Team> teamCache;
        Map<Long, Player> playerCache;
        Map<Long, Match> matchCache;

        teamCache = persistTeams(teams);

        playerCache = persistPlayers(players, teamCache);

        matchCache = persistMatches(matches, teamCache);

        persistRecords(records, playerCache, matchCache);
    }

    private Map<Long, Team> persistTeams(Resource teams) {
        Map<Long, Team> teamsCache = new HashMap<>();

        try(BufferedReader bf = new BufferedReader(new InputStreamReader(teams.getInputStream()))){
            String line;

            bf.readLine(); // skipping the first line with column names

            while ((line = bf.readLine()) != null){
                String[] data = line.split(",");
                Long id = Long.valueOf(data[0]);

                Team team = new Team();
                team.setName(data[1]);
                team.setManagerFullName(data[2]);
                team.setTeamGroup(TeamGroup.valueOf(data[3]));

                teamRepository.save(team);

                teamsCache.put(id, team);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return teamsCache;
    }

    private Map<Long, Player> persistPlayers(Resource players, Map<Long, Team> teamCache) {
        Map<Long, Player> playersCache = new HashMap<>();

        try(BufferedReader bf = new BufferedReader(new InputStreamReader(players.getInputStream()))){
            String line;

            bf.readLine();

            while ((line = bf.readLine()) != null){
                String[] data = line.split(",");
                Long id = Long.valueOf(data[0]);

                Player player = new Player();
                player.setTeamNumber(Integer.valueOf(data[1]));
                player.setPosition(data[2]);
                player.setFullName(data[3]);
                player.setTeam(teamCache.get(Long.valueOf(data[4])));


                playerRepository.save(player);

                playersCache.put(id, player);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return playersCache;
    }

    private Map<Long, Match> persistMatches(Resource matches, Map<Long, Team> teamCache) {
        Map<Long, Match> matchesCache = new HashMap<>();

        try(BufferedReader bf = new BufferedReader(new InputStreamReader(matches.getInputStream()))){
            String line;

            bf.readLine();

            while ((line = bf.readLine()) != null){
                String[] data = line.split(",");
                Long id = Long.valueOf(data[0]);

                Match match = new Match();
                match.setaTeam(teamCache.get(Long.valueOf(data[1])));
                match.setbTeam(teamCache.get(Long.valueOf(data[2])));
                match.setDate(dateParser(data[3]));
                match.setScore(data[4]);

                matchRepository.save(match);

                matchesCache.put(id, match);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return matchesCache;
    }

    private LocalDate dateParser(String date) {
        List<DateTimeFormatter> formats = List.of(
                DateTimeFormatter.ofPattern("M/d/yyyy"),
                DateTimeFormatter.ofPattern("MM/dd/yyyy"),
                DateTimeFormatter.ofPattern("d/M/yyyy"),
                DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                DateTimeFormatter.ofPattern("yyyy-M-d"),
                DateTimeFormatter.ofPattern("yyyy/MM/dd"),
                DateTimeFormatter.ISO_LOCAL_DATE
        );

        for(var format : formats){
            try{
                return LocalDate.parse(date, format);
            } catch (DateTimeParseException e) {
                continue;
            }
        }

        return null;
    }

    private void persistRecords(Resource records, Map<Long, Player> playerCache, Map<Long, Match> matchCache) {
        try(BufferedReader bf = new BufferedReader(new InputStreamReader(records.getInputStream()))){
            String line;

            bf.readLine();

            while ((line = bf.readLine()) != null){
                String[] data = line.split(",");
                Long id = Long.valueOf(data[0]);

                Record record = new Record();
                record.setPlayer(playerCache.get(Long.valueOf(data[1])));
                record.setMatch(matchCache.get(Long.valueOf(data[2])));
                record.setFromMinutes(Integer.valueOf(data[3]));
                if(data[4].equals("NULL")){
                    record.setToMinutes(90);
                } else {
                    record.setToMinutes(Integer.valueOf(data[4]));
                }

                recordRepository.save(record);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    // първо пълня отобрите (teams)
    // след това ми трябват отбор за да напълня играчите (playeres),
    // след това ми трябват по два отбора за да напълня мачовете (matches),
    // след това ми трябват мач и играч за да напълня записите (records)
}
