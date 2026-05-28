package com.orientapp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orientapp.dto.GeocodingResultDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GeocodingServiceTest {

    @Mock private HttpClient httpClient;

    private GeocodingService geocodingService;

    @BeforeEach
    void setUp() {
        geocodingService = new GeocodingService(new ObjectMapper(), httpClient);
    }

    @Test
    @SuppressWarnings("unchecked")
    void search_parsesJsonResponseIntoResults() throws Exception {
        String json = """
            [
              {"lat":"50.9044","lon":"15.7197","display_name":"Jelenia Góra, Polska"},
              {"lat":"51.1079","lon":"17.0385","display_name":"Wrocław, Polska"}
            ]
            """;
        HttpResponse<String> response = (HttpResponse<String>) org.mockito.Mockito.mock(HttpResponse.class);
        when(response.body()).thenReturn(json);
        when(httpClient.send(any(HttpRequest.class),
                org.mockito.ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(response);

        List<GeocodingResultDto> results = geocodingService.search("Jelenia Góra");

        assertThat(results).hasSize(2);
        assertThat(results.get(0).lat()).isEqualTo(50.9044);
        assertThat(results.get(0).lon()).isEqualTo(15.7197);
        assertThat(results.get(0).displayName()).isEqualTo("Jelenia Góra, Polska");
        assertThat(results.get(1).displayName()).isEqualTo("Wrocław, Polska");
    }

    @Test
    void search_whenApiUnavailable_returnsEmptyListWithoutThrowing() throws Exception {
        when(httpClient.send(any(HttpRequest.class),
                org.mockito.ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                .thenThrow(new IOException("Nominatim unavailable"));

        List<GeocodingResultDto> results = geocodingService.search("Jelenia Góra");

        assertThat(results).isEmpty();
    }
}
