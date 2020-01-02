package com.weatherless.weatherproject.controller;

import com.weatherless.weatherproject.data.Measure;
import com.weatherless.weatherproject.data.Station;
import com.weatherless.weatherproject.services.StationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Date;
import java.util.List;

@RestController
public class StationController {

    private final StationService stationService;

    @Autowired
    public StationController(StationService stationService) {
        this.stationService = stationService;
    }

    @GetMapping("/api/station")
    public List<Station> findAll() {
        return stationService.findAll();
    }

    @PostMapping("/api/station")
    public Station createStation(@RequestBody Station station) {
        return stationService.save(station);
    }

    @GetMapping("/api/station/{id}")
    public ResponseEntity<Station> findStation(@PathVariable("id") String id) {
        Station station = stationService.read(id);
        if (station != null) {
            return ResponseEntity.ok(station);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/api/station/{id}")
    public ResponseEntity<Station> deleteStation(@PathVariable("id") String id) {
        Station station = stationService.read(id);
        if (station != null) {
            stationService.delete(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/api/station/{id}/addMeasure")
    public void addMeasure(@PathVariable("id") String id, @Valid @RequestBody Measure measure) {
        stationService.addMeasure(id, measure);
    }

    @GetMapping("/api/station/")
    public Station findStationByName(@RequestParam(name = "stationName") String stationName) {
        return stationService.findStation(stationName);
    }

    @GetMapping("/api/station/{id}/measure")
    public List<Measure> findMeasures(@PathVariable("id") String id, @RequestParam(name = "start") @DateTimeFormat(pattern = "yyyyMMdd") Date start, @RequestParam(name = "end") @DateTimeFormat(pattern = "yyyyMMdd") Date end) {
        return stationService.findMeasures(id, start, end);
    }

    @GetMapping("/api/station/stats")
    public String findStats(@RequestParam(name = "stationName") String stationName) {
        return stationService.findStats(stationName);
    }
}
