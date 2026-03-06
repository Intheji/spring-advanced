package org.example.expert.client;

import org.example.expert.client.dto.WeatherDto;
import org.example.expert.domain.common.exception.ServerException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class WeatherClientTest {

    @Test
    @DisplayName("오늘 날짜에 해당하는 날씨 데이터가 있으면 날씨를 반환한다")
    void getTodayWeather_success() {
        // given
        RestTemplateBuilder builder = mock(RestTemplateBuilder.class);
        RestTemplate restTemplate = mock(RestTemplate.class);

        given(builder.build()).willReturn(restTemplate);

        WeatherClient weatherClient = new WeatherClient(builder);

        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("MM-dd"));

        WeatherDto todayWeather = new WeatherDto(today, "맑음");
        WeatherDto anotherWeather = new WeatherDto("12-31", "흐림");

        ResponseEntity<WeatherDto[]> response =
                new ResponseEntity<>(new WeatherDto[]{anotherWeather, todayWeather}, HttpStatus.OK);

        given(restTemplate.getForEntity(any(URI.class), eq(WeatherDto[].class)))
                .willReturn(response);

        // when
        String result = weatherClient.getTodayWeather();

        // then
        assertThat(result).isEqualTo("맑음");
    }

    @Test
    @DisplayName("응답 상태코드가 200 OK가 아니면 예외가 발생한다")
    void getTodayWeather_fail_when_status_is_not_ok() {
        // given
        RestTemplateBuilder builder = mock(RestTemplateBuilder.class);
        RestTemplate restTemplate = mock(RestTemplate.class);
        given(builder.build()).willReturn(restTemplate);

        WeatherClient weatherClient = new WeatherClient(builder);

        ResponseEntity<WeatherDto[]> response =
                new ResponseEntity<>(new WeatherDto[]{}, HttpStatus.INTERNAL_SERVER_ERROR);

        given(restTemplate.getForEntity(any(URI.class), eq(WeatherDto[].class)))
                .willReturn(response);

        // when & then
        assertThatThrownBy(() -> weatherClient.getTodayWeather())
                .isInstanceOf(ServerException.class)
                .hasMessageContaining("날씨 데이터를 가져오는데 실패했습니다.");
    }

    @Test
    @DisplayName("응답 바디가 null 이면 예외가 발생한다")
    void getTodayWeather_fail_when_body_is_null() {
        // given
        RestTemplateBuilder builder = mock(RestTemplateBuilder.class);
        RestTemplate restTemplate = mock(RestTemplate.class);
        given(builder.build()).willReturn(restTemplate);

        WeatherClient weatherClient = new WeatherClient(builder);

        ResponseEntity<WeatherDto[]> response =
                new ResponseEntity<>(null, HttpStatus.OK);

        given(restTemplate.getForEntity(any(URI.class), eq(WeatherDto[].class)))
                .willReturn(response);

        // when & then
        assertThatThrownBy(() -> weatherClient.getTodayWeather())
                .isInstanceOf(ServerException.class)
                .hasMessage("날씨 데이터가 없습니다.");
    }

    @Test
    @DisplayName("응답 바디가 빈 배열이면 예외가 발생한다")
    void getTodayWeather_fail_when_body_is_empty() {
        // given
        RestTemplateBuilder builder = mock(RestTemplateBuilder.class);
        RestTemplate restTemplate = mock(RestTemplate.class);
        given(builder.build()).willReturn(restTemplate);

        WeatherClient weatherClient = new WeatherClient(builder);

        ResponseEntity<WeatherDto[]> response =
                new ResponseEntity<>(new WeatherDto[]{}, HttpStatus.OK);

        given(restTemplate.getForEntity(any(URI.class), eq(WeatherDto[].class)))
                .willReturn(response);

        // when & then
        assertThatThrownBy(() -> weatherClient.getTodayWeather())
                .isInstanceOf(ServerException.class)
                .hasMessage("날씨 데이터가 없습니다.");
    }

    @Test
    @DisplayName("오늘 날짜에 해당하는 날씨 데이터가 없으면 예외가 발생한다")
    void getTodayWeather_fail_when_today_weather_not_found() {
        // given
        RestTemplateBuilder builder = mock(RestTemplateBuilder.class);
        RestTemplate restTemplate = mock(RestTemplate.class);
        given(builder.build()).willReturn(restTemplate);

        WeatherClient weatherClient = new WeatherClient(builder);

        WeatherDto weather1 = new WeatherDto("01-01", "눈");
        WeatherDto weather2 = new WeatherDto("12-31", "흐림");

        ResponseEntity<WeatherDto[]> response =
                new ResponseEntity<>(new WeatherDto[]{weather1, weather2}, HttpStatus.OK);

        given(restTemplate.getForEntity(any(URI.class), eq(WeatherDto[].class)))
                .willReturn(response);

        // when & then
        assertThatThrownBy(() -> weatherClient.getTodayWeather())
                .isInstanceOf(ServerException.class)
                .hasMessage("오늘에 해당하는 날씨 데이터를 찾을 수 없습니다.");
    }
}