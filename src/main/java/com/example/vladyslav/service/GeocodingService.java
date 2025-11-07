package com.example.vladyslav.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class GeocodingService {

    public GeoJsonPoint geocodeAddress(String address, String city, String postCode) {
        try {
            // Build the query string by concatenating address components
            StringBuilder queryBuilder = new StringBuilder();

            if (address != null && !address.isBlank()) {
                queryBuilder.append(address).append(", ");
            }
            if (city != null && !city.isBlank()) {
                queryBuilder.append(city).append(", ");
            }
            if (postCode != null && !postCode.isBlank()) {
                queryBuilder.append(postCode).append(", ");
            }
            queryBuilder.append("UK");

            // Encode the query string to be URL-safe
            String encodedQuery = URLEncoder.encode(queryBuilder.toString(), StandardCharsets.UTF_8);
            // Construct the full URL for the OpenStreetMap Nominatim API request
            String url = "https://nominatim.openstreetmap.org/search?format=json&limit=1&q=" + encodedQuery;

            // Use RestTemplate to send a GET request to the API
            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.getForObject(url, String.class);
            // If response is empty or null, return null indicating no location found
            if (response == null || response.isBlank()) {
                return null;
            }

            // Parse the JSON response using Jackson ObjectMapper
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);
            // Check if the response array contains at least one result
            if (root.isArray() && root.size() > 0) {
                JsonNode first = root.get(0);
                // Extract latitude and longitude values from the JSON node
                double lat = first.get("lat").asDouble();
                double lon = first.get("lon").asDouble();
                // Create and return a GeoJsonPoint with longitude and latitude
                return new GeoJsonPoint(lon, lat);
            }
        } catch (Exception e) {
            // In a real application, log this exception properly
        }
        // Return null if geocoding fails or no valid location found
        return null;
    }
}
