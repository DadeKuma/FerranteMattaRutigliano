package com.github.ferrantemattarutigliano.software.server.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Collection;

@Entity
@JsonIgnoreProperties({
        "organizer",
        "athletes",
        "spectators"
})
public class Run {
    @Id
    @GeneratedValue
    private Long id;
    private String title;
    private Date date;
    private Time time;
    private String path;
    private String state;

    @ManyToOne
    @JoinColumn(name = "organizerId", nullable = false)
    private Individual organizer;

    @ManyToMany
    @JoinTable(name = "runEnroll",
            joinColumns = {@JoinColumn(name = "runId")},
            inverseJoinColumns = {@JoinColumn(name = "athleteId")})
    private Collection<Individual> athletes;

    @ManyToMany
    @JoinTable(name = "runWatch",
            joinColumns = {@JoinColumn(name = "runId")},
            inverseJoinColumns = {@JoinColumn(name = "spectatorId")})
    private Collection<Individual> spectators;


    public Run() {
        this.athletes = new ArrayList<>();
        this.spectators = new ArrayList<>();
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Time getTime() {
        return time;
    }

    public void setTime(Time time) {
        this.time = time;
    }

    public Individual getOrganizer() {
        return organizer;
    }

    public void setOrganizer(Individual organizer) {
        this.organizer = organizer;
    }

    public Collection<Individual> getAthletes() {
        return athletes;
    }

    public void enrollAthlete(Individual athlete) {
        this.athletes.add(athlete);
    }

    public Collection<Individual> getSpectators() {
        return spectators;
    }

    public void addSpectator(Individual spectator) {
        this.spectators.add(spectator);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
