package com.cashmallow;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;
import org.junit.jupiter.api.Test;

public class GoogleMapApiTest {

    @Test
    public void testPlaceAutocompleteRequest() throws Exception {
        GeoApiContext context = new GeoApiContext.Builder()
                .apiKey("AIzaSyDGDfc7zY5-g5NpcGwVv5wmpyInW8PmfyM")
                .build();
        GeocodingResult[] results = GeocodingApi.geocode(context,
                "中国 上海市 闵行区 七莘路 3333弄 万科城市花园 8区 15号 1101").await();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        System.out.println(gson.toJson(results));

        // Invoke .shutdown() after your application is done making requests
        context.shutdown();
    }
}
