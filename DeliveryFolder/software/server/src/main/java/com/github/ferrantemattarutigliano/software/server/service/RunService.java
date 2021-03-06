package com.github.ferrantemattarutigliano.software.server.service;

import com.github.ferrantemattarutigliano.software.server.constant.Message;
import com.github.ferrantemattarutigliano.software.server.model.entity.Individual;
import com.github.ferrantemattarutigliano.software.server.model.entity.Position;
import com.github.ferrantemattarutigliano.software.server.model.entity.Run;
import com.github.ferrantemattarutigliano.software.server.model.entity.User;
import com.github.ferrantemattarutigliano.software.server.repository.IndividualRepository;
import com.github.ferrantemattarutigliano.software.server.repository.RunRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;

@Service
public class RunService {

    private final IndividualRepository individualRepository;
    private final RunRepository runRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    public RunService(IndividualRepository individualRepository, RunRepository runRepository, SimpMessagingTemplate simpMessagingTemplate) {
        this.individualRepository = individualRepository;
        this.runRepository = runRepository;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }


    public Collection<Run> showRuns() {
        return runRepository.findAll();
    }

    public Collection<Run> showCreatedRuns() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user == null)
            return null;
        Individual organizer = individualRepository.findByUser(user);
        return organizer.getCreatedRuns();
    }

    public Collection<Run> showNewRuns() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user == null)
            return null;
        Individual individual = individualRepository.findByUser(user);

        /* error showRuns instead of showCreateRuns
        OLD VERSION
        Collection<Run> allRuns = showCreatedRuns();

        */
        // NEW VERSION

        Collection<Run> allRuns = showRuns();
        //update following the test phase, need of and iterator in the following loop cycle
        /*

        OLD VERSION
        for(Run run : allRuns){
            boolean isOrganizer = individual.getEnrolledRuns().contains(run);
            boolean isWatched = individual.getWatchedRuns().contains(run);
            boolean isEnrolled = individual.getEnrolledRuns().contains(run);
            if(isOrganizer || isWatched || isEnrolled){
                allRuns.remove(run);
            }
        */
        //NEW VERSION
        for (Iterator<Run> i = allRuns.iterator(); i.hasNext(); ) {
            Run run = i.next();
            boolean isOrganizer = individual.getCreatedRuns().contains(run);
            boolean isWatched = individual.getWatchedRuns().contains(run);
            boolean isEnrolled = individual.getEnrolledRuns().contains(run);
            if (isOrganizer || isWatched || isEnrolled) {
                i.remove();
            }
        }
        return allRuns;
    }

    public Collection<Run> showEnrolledRuns() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user == null)
            return null;
        Individual athlete = individualRepository.findByUser(user);
        return athlete.getEnrolledRuns();
    }

    public Collection<Run> showWatchedRuns() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user == null)
            return null;
        Individual spectator = individualRepository.findByUser(user);
        return spectator.getWatchedRuns();
    }

    public String createRun(Run run) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user == null)
            return Message.BAD_REQUEST.toString();

        Individual organizer = individualRepository.findByUser(user);
        run.setOrganizer(organizer);


        Boolean sec = compareTimeBefore(run.getTime(), getCurrentTime());
        int k = 0;





        if ((compareDate(run.getDate(), datesConversion()) && compareTimeBefore(run.getTime(), getCurrentTime()))
                || beforeDate(run.getDate(), datesConversion())) {
            return Message.RUN_NOT_ALLOWED.toString();
        }


        runRepository.save(run);
        return Message.RUN_CREATED.toString();
    }

    public String startRun(Long runId) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user == null)
            return Message.BAD_REQUEST.toString();

        if (!runRepository.findById(runId).isPresent()) {
            return Message.RUN_DOES_NOT_EXISTS.toString();
        }

        Run r = runRepository.findById(runId).get();
        Individual organizer = individualRepository.findByUser(user);

        if (!r.getOrganizer().getSsn().equals(organizer.getSsn()))
            return Message.RUN_NOT_ORGANIZER.toString() + r.getTitle();

        runRepository.startRun(runId);
        return Message.RUN_STARTED.toString();
    }

    public String editRun(Run run) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user == null)
            return Message.BAD_REQUEST.toString();

        if (!runRepository.findById(run.getId()).isPresent()) {
            return Message.RUN_DOES_NOT_EXISTS.toString();
        }

        Individual organizer = individualRepository.findByUser(user);

        Run existing = runRepository.findById(run.getId()).get();

        if (!run.getOrganizer().getSsn().equals(organizer.getSsn())) {
            return Message.RUN_NOT_ORGANIZER.toString() + run.getTitle();
        }

        if (run.getTitle() != null
                && !run.getTitle().equals(existing.getTitle())) {
            existing.setTitle(run.getTitle());
        }

        if (run.getDate() != null
                && !run.getDate().equals(existing.getDate())) {
            existing.setDate(run.getDate());
        }

        if (run.getTime() != null
                && !run.getTime().equals(existing.getTime())) {
            existing.setTime(run.getTime());
        }

        if (run.getPath() != null
                && !run.getPath().equals(existing.getPath())) {
            existing.setPath(run.getPath());
        }

        runRepository.save(existing);

        return Message.RUN_EDITED.toString();
    }

    public String deleteRun(Long runId) {

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user == null) {
            return Message.BAD_REQUEST.toString();
        }

        if (!runRepository.findById(runId).isPresent()) {
            return Message.RUN_DOES_NOT_EXISTS.toString();
        }

        Individual organizer = individualRepository.findByUser(user);

        Run run = runRepository.findById(runId).get();

        if (!run.getOrganizer().getSsn().equals(organizer.getSsn())) {
            return Message.RUN_NOT_ORGANIZER.toString() + run.getTitle();
        }

        for (Individual athlete : run.getAthletes()) {
            athlete.getEnrolledRuns().removeIf(r -> r.getId().equals(runId));
        }

        for (Individual spectator : run.getSpectators()) {
            spectator.getWatchedRuns().removeIf(r -> r.getId().equals(runId));
        }

        runRepository.delete(run);

        return Message.RUN_DELETED.toString();
    }

    public String enrollRun(Long runId) {

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user == null) {
            return Message.BAD_REQUEST.toString();
        }

        if (!runRepository.findById(runId).isPresent()) {
            return Message.RUN_DOES_NOT_EXISTS.toString();
        }

        Run run = runRepository.findById(runId).get();
        Individual athlete = individualRepository.findByUser(user);


        if (run.getAthletes().stream().anyMatch(individual -> individual.getSsn().equals(athlete.getSsn()))) {
            return Message.RUN_ALREADY_ATHLETE.toString();
        }

        run.enrollAthlete(athlete);
        runRepository.save(run);

        return Message.RUN_ENROLLED.toString() + run.getTitle();
    }

    public String unenrollRun(Long runId) {

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user == null) {
            return Message.BAD_REQUEST.toString();
        }

        if (!runRepository.findById(runId).isPresent()) {
            return Message.RUN_DOES_NOT_EXISTS.toString();
        }

        Run run = runRepository.findById(runId).get();
        Individual athlete = individualRepository.findByUser(user);


        if (!run.getAthletes().stream().anyMatch(individual -> individual.getSsn().equals(athlete.getSsn()))) {
            return Message.RUN_NOT_ATHLETE.toString();
        }

        athlete.getEnrolledRuns().removeIf(r -> r.getId().equals(runId));
        individualRepository.save(athlete);

        run.getAthletes().remove(athlete);
        runRepository.save(run);

        return Message.RUN_UNENROLLED.toString() + run.getTitle();
    }

    public String watchRun(Long runId) {

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user == null) {
            return Message.BAD_REQUEST.toString();
        }

        if (!runRepository.findById(runId).isPresent()) {
            return Message.RUN_DOES_NOT_EXISTS.toString();
        }

        Run run = runRepository.findById(runId).get();
        Individual spectator = individualRepository.findByUser(user);

        if (run.getSpectators().stream().anyMatch(individual -> individual.getSsn().equals(spectator.getSsn()))) {
            return Message.RUN_ALREADY_SPECTATOR.toString();
        }

        run.addSpectator(spectator);
        runRepository.save(run);

        return Message.RUN_WATCHED.toString() + run.getTitle();
    }

    public String unwatchRun(Long runId) {

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user == null) {
            return Message.BAD_REQUEST.toString();
        }

        if (!runRepository.findById(runId).isPresent()) {
            return Message.RUN_DOES_NOT_EXISTS.toString();
        }

        Run run = runRepository.findById(runId).get();
        Individual spectator = individualRepository.findByUser(user);

        if (!run.getSpectators().stream().anyMatch(individual -> individual.getSsn().equals(spectator.getSsn()))) {
            return Message.RUN_NOT_SPECTATOR.toString();
        }

        spectator.getWatchedRuns().removeIf(r -> r.getId().equals(runId));
        individualRepository.save(spectator);

        run.getSpectators().remove(spectator);
        runRepository.save(run);

        return Message.RUN_UNWATCHED.toString() + run.getTitle();
    }

    @Transactional
    @Scheduled(fixedDelay = 5000)
    public void startedRunSendAthletesPosition() {

        Collection<Run> runs = runRepository.findAll();

        for (Run run : runs) {
            if (run.getState().equals("started")) {

                String[] path = run.getPath().split(";");
                String firstPos = path[0];
                String lastPos = path[path.length - 1];

                Position start = new Position();

                start.setLatitude(StringUtils.substringBefore(firstPos, ":"));
                start.setLongitude(StringUtils.substringAfter(firstPos, ":"));

                Position arrival = new Position();

                arrival.setLatitude(StringUtils.substringBefore(lastPos, ":"));
                arrival.setLongitude(StringUtils.substringAfter(lastPos, ":"));

                final double MINIMUM_DISTANCE = 0.1; //minimum distance from arrival point: 10 meters
                final double MAXIMUM_DISTANCE = calculateDistance(start, arrival); //maximum run path: 30 kilometers

                Collection<Individual> enrolled = new ArrayList<>();
                enrolled.addAll(run.getAthletes());

                for (Individual athlete : run.getAthletes()) {
                    ArrayList<Position> athletePositions = new ArrayList<>();
                    athletePositions.addAll(athlete.getPosition());

                    if (athletePositions.size() == 0) {
                        enrolled.remove(athlete);
                        break;
                    }
                    Position lastPosition = athletePositions.get((athletePositions.size() - 1));

                    double distance = calculateDistance(lastPosition, arrival);
                    if (distance >= MINIMUM_DISTANCE
                            && distance < MAXIMUM_DISTANCE) {
                        for (Individual spectator : run.getSpectators()) {
                            simpMessagingTemplate.convertAndSend("/run/" + run.getId() + "/" + spectator.getUser().getUsername(),
                                    "Athlete: " + athlete.getUser().getUsername()
                                            + ". Position: "
                                            + "lat=" + lastPosition.getLatitude()
                                            + ", lon=" + lastPosition.getLongitude() + ";");
                        }
                    } else {
                        enrolled.remove(athlete);
                    }
                }
                if (enrolled.isEmpty()) {
                    run.setState("finished");
                    runRepository.save(run);
                }
            }
        }
    }

    private double calculateDistance(Position p1, Position p2) {
        double lastAthleteLongitude = Double.parseDouble(p1.getLongitude());
        double lastAthleteLatitude = Double.parseDouble(p1.getLatitude());
        double lastPositionLongitude = Double.parseDouble(p2.getLongitude());
        double lastPositionLatitude = Double.parseDouble(p2.getLatitude());

        double theta = lastAthleteLongitude - lastPositionLongitude;

        if (theta == 0.0)
            return theta;

        double rho = Math.toRadians(lastPositionLatitude)
                + Math.cos(Math.toRadians(lastAthleteLatitude))
                * Math.cos(Math.toRadians(lastPositionLatitude))
                * Math.cos(Math.toRadians(theta));

        double dist = Math.sin(Math.toRadians(lastAthleteLatitude)) * Math.sin(rho);

        return Math.toDegrees(Math.acos(dist)) * 1.609344;
    }

    public Time getCurrentTime() {
        Time time = new Time(System.currentTimeMillis());
        return time;
    }


    public Date datesConversion() {
        java.util.Date uDate = new java.util.Date();
        java.sql.Date sDate = convertUtilToSql(uDate);
        return sDate;

    }

    public java.sql.Date convertUtilToSql(java.util.Date uDate) {
        java.sql.Date sDate = new java.sql.Date(uDate.getTime());
        return sDate;
    }


    public Boolean compareDate(Date date1, Date date2) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date1);
        int month1 = cal.get(Calendar.MONTH);
        int day1 = cal.get(Calendar.DAY_OF_MONTH);
        int year1 = cal.get(Calendar.YEAR);
        cal.setTime(date2);
        int month2 = cal.get(Calendar.MONTH);
        int day2 = cal.get(Calendar.DAY_OF_MONTH);
        int year2 = cal.get(Calendar.YEAR);
        if (year1 == year2
                && month1 == month2
                && day1 == day2) {
            return true;
        } else return false;
    }

    public Boolean beforeDate(Date date1, Date date2) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date1);
        int month1 = cal.get(Calendar.MONTH);
        int day1 = cal.get(Calendar.DAY_OF_MONTH);
        int year1 = cal.get(Calendar.YEAR);
        cal.setTime(date2);
        int month2 = cal.get(Calendar.MONTH);
        int day2 = cal.get(Calendar.DAY_OF_MONTH);
        int year2 = cal.get(Calendar.YEAR);
        if (year1 < year2) {
            return true;
        } else if (year1 == year2
                && month1 < month2) {
            return true;
        } else if (year1 == year2
                && month1 == month2
                && day1 < day2) {
            return true;
        } else return false;
    }

    public Boolean compareTimeBefore(Time t1, Time t2) {

        if (toLocalTime(t1).getHour() < toLocalTime(t2).getHour()) {
            return true;
        } else if (toLocalTime(t1).getHour() == toLocalTime(t2).getHour()
                && toLocalTime(t1).getMinute() < toLocalTime(t2).getMinute()) {
            return true;
        } else return false;
    }

    public LocalTime toLocalTime(java.sql.Time time) {
        return time.toLocalTime();
    }

}




