package com.weatherless.weatherproject.services;

import com.weatherless.weatherproject.data.Measure;
import com.weatherless.weatherproject.data.Station;
import com.weatherless.weatherproject.repository.StationRepository;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.InternalDateHistogram;
import org.elasticsearch.search.aggregations.bucket.nested.Nested;
import org.elasticsearch.search.aggregations.metrics.avg.Avg;
import org.elasticsearch.search.aggregations.metrics.max.Max;
import org.elasticsearch.search.aggregations.metrics.min.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ResultsExtractor;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class ElasticStationService implements StationService {

    private final StationRepository stationRepository;

    private ElasticsearchOperations elasticsearchOperations;

    @Autowired
    public ElasticStationService(StationRepository stationRepository, @Qualifier("elasticsearchOperations") ElasticsearchOperations elasticsearchOperations) {
        this.stationRepository = stationRepository;
        this.elasticsearchOperations = elasticsearchOperations;
    }

    @Override
    public List<Station> findAll() {
        Iterable<Station> repoResult = stationRepository.findAll();
        List<Station> resultList = new ArrayList<>();
        repoResult.forEach(f -> resultList.add(f));
        return resultList;
    }

    @Override
    public Station save(Station station) {
        return stationRepository.save(station);
    }

    @Override
    public Station read(String id) {
        return stationRepository.findById(id).orElse(null);
    }

    @Override
    public void delete(String id) {
        if (stationRepository.findById(id).isPresent()) {
            stationRepository.delete(stationRepository.findById(id).get());
        }
    }

    @Override
    public void addMeasure(String id, Measure measure) {
        Station station = read(id);
        if (station == null) {
            return;
        }
        station.getMeasures().add(measure);
        save(station);
    }

    @Override
    public Station findStation(String stationName) {
        return stationRepository.findByStationName(stationName);
    }

    @Override
        public List<Measure> findMeasures(String id, Date startingDate, Date endingDate) {
        long cStarting = startingDate.getTime();
        long cEnding = endingDate.getTime();

        Station station = read(id);
        if (station == null) {
            return null;
        }

        List<Measure> measures = new ArrayList<>();
        for (Measure measure : station.getMeasures()) {
            long cTime = measure.getDate().getTime();
            if (cTime >= cStarting && cTime <= cEnding) {
                measures.add(measure);
            }
        }

        return measures;
    }

    @Override
    public String findStats(String stationName) {
        SearchQuery query = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.matchQuery("stationName", stationName))
                .addAggregation(AggregationBuilders.nested("measures", "measures").subAggregation(AggregationBuilders.dateHistogram("monthly").field("measures.date").dateHistogramInterval(DateHistogramInterval.MONTH)
                .subAggregation(AggregationBuilders.avg("avg").field("measures.temperature")).subAggregation(AggregationBuilders.min("min").field("measures.temperature")).subAggregation(AggregationBuilders.max("max").field("measures.temperature"))))
                .build();

        Aggregations aggregations = elasticsearchOperations.query(query, new ResultsExtractor<Aggregations>() {
            @Override
            public Aggregations extract(SearchResponse searchResponse) {
                return searchResponse.getAggregations();
            }
        });

        // Formatting response as JSON String
        StringBuilder str = new StringBuilder();
        str.append("[");
        Nested agg = aggregations.get("measures");
        InternalDateHistogram month = agg.getAggregations().get("monthly");
        int c = 0;
        for (InternalDateHistogram.Bucket bucket : month.getBuckets()) {
            c++;
            String date = "\""+bucket.getKeyAsString()+"\"";
            Avg avg = bucket.getAggregations().get("avg");
            Min min = bucket.getAggregations().get("min");
            Max max = bucket.getAggregations().get("max");
            str.append("{\"date\":").append(date).append(",\"min\":").append(min.getValue()).append(",\"max\":").append(max.getValue()).append(",\"avg\":").append(avg.getValue()).append("}");
            if (c != month.getBuckets().size()) {
                str.append(",");
            }
        }
        str.append("]");
        //return aggregations.get("measures").toString(); // returns whole aggregation
        return str.toString();
    }
}
