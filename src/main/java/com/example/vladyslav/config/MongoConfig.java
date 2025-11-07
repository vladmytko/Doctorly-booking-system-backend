package com.example.vladyslav.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.index.*;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Configuration
public class MongoConfig {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ISO_LOCAL_TIME;

    @WritingConverter
    static class LocalTimeToString implements Converter<LocalTime, String> {
        @Override public String convert(LocalTime source) { return source.format(FMT); }
    }

    @ReadingConverter
    static class StringToLocalTime implements Converter<String, LocalTime> {
        @Override public LocalTime convert(String source) { return LocalTime.parse(source, FMT); }
    }

    @Bean
    public MongoCustomConversions mongoCustomConversions() {
        return new MongoCustomConversions(List.of(new LocalTimeToString(), new StringToLocalTime()));
    }

    @Bean
    public ApplicationRunner ensureClinicIndexes(MongoTemplate mongoTemplate){
        return args -> {
            IndexOperations indexOps = mongoTemplate.indexOps("clinics"); // default collection name for Clinic.class

            // Ensure 2dsphere index on "location" field;
            GeospatialIndex geoIndex = new GeospatialIndex("location").typed(GeoSpatialIndexType.GEO_2DSPHERE);

            indexOps.createIndex(geoIndex);

            // Ensure text index on selected fields
            TextIndexDefinition textIndex = new TextIndexDefinition.TextIndexDefinitionBuilder()
                    .onField("name", 2.0f)  // higher weight for name
                    .onField("address", 1.0f)
                    .onField("bio", 1.0f)
                    .build();
            indexOps.createIndex(textIndex);
        };
    }

    @Bean
    public ApplicationRunner ensureDoctorIndexes(MongoTemplate mongoTemplate){
        return args -> {
            IndexOperations indexOps = mongoTemplate.indexOps("doctors");

            TextIndexDefinition textIndex = new TextIndexDefinition.TextIndexDefinitionBuilder()
                    .onField("firstName", 2.0f)
                    .onField("lastName", 2.0f)
                    .onField("bio", 1.0f)
                    .build();
            indexOps.createIndex(textIndex);
            indexOps.createIndex(new Index().on("clinic", Sort.Direction.ASC));
        };
    }
}