package com.weatherless.weatherproject.services;

import com.weatherless.weatherproject.data.Measure;
import com.weatherless.weatherproject.data.Station;

import java.util.Date;
import java.util.List;

public interface StationService {
    /**
     * Gets all Stations registered
     * @return List of Station object
     */
    List<Station> findAll();

    /**
     * Proceed to a register in the DB
     * @param station
     * @return Passed object to ensure it is registered
     */
    Station save(Station station);

    /**
     * Gets a Station object registered in DB from Id
     * @param id
     * @return
     */
    Station read(String id);

    /**
     * Removes an existing Station from the DB depending of given Id
     * @param id station's Id to delete from DB
     */
    void delete(String id);

    /**
     * Adds a Measure object to the nested array field of a given Station (found by id)
     * @param id the Station id of the station to add the Measure object
     * @param measure
     */
    void addMeasure(String id, Measure measure);

    /**
     * Gets a Station object registered in DB from stationName property
     * @param stationName
     * @return
     */
    Station findStation(String stationName);

    /**
     * Gets all measures for a Station (found by station id) between two dates
     * @param id the station id
     * @param startingDate the starting date of the interval
     * @param endingDate the ending date of the interval
     * @return a List of Measure objects which match the interval
     */
    List<Measure> findMeasures(String id, Date startingDate, Date endingDate);

    /**
     * Gets statistics (min, max, avg) for each month for a Station found by its stationName
     * @param stationName
     * @return Json formatted String representing the stats (keys = date, min, max, avg)
     */
    String findStats(String stationName);
}
