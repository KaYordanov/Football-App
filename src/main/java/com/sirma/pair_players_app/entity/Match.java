package com.sirma.pair_players_app.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "matches")
public class Match {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "a_team_id", nullable = false)
    private Team aTeam;

    @ManyToOne
    @JoinColumn(name = "b_team_id", nullable = false)
    private Team bTeam;

    @Column(name = "match_date", nullable = true)
    private LocalDate date;

    @Column(name = "score", nullable = false)
    private String score;

    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL)
    private List<Record> records;

    public Match() {
    }

    public Match(Long id, Team aTeam, Team bTeam, LocalDate date, String score, List<Record> records) {
        this.id = id;
        this.aTeam = aTeam;
        this.bTeam = bTeam;
        this.date = date;
        this.score = score;
        this.records = records;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Team getaTeam() {
        return aTeam;
    }

    public void setaTeam(Team aTeam) {
        this.aTeam = aTeam;
    }

    public Team getbTeam() {
        return bTeam;
    }

    public void setbTeam(Team bTeam) {
        this.bTeam = bTeam;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public List<Record> getRecords() {
        return records;
    }

    public void setRecords(List<Record> records) {
        this.records = records;
    }
}
