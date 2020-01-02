package com.weatherless.weatherproject.repository;

import com.weatherless.weatherproject.data.Station;
import org.springframework.data.elasticsearch.repository.ElasticsearchCrudRepository;

public interface StationRepository extends ElasticsearchCrudRepository<Station, String> {
    Station findByStationName(String stationName);
}
