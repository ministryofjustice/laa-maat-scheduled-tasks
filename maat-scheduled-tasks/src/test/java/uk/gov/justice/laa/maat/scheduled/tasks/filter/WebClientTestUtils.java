package uk.gov.justice.laa.maat.scheduled.tasks.filter;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClientResponseException;

public class WebClientTestUtils {

    public static WebClientResponseException getWebClientResponseException(HttpStatus status) {
        return WebClientResponseException.create(
                status.value(),
                status.getReasonPhrase(),
                new HttpHeaders(),
                new byte[0],
                null
        );
    }
}
