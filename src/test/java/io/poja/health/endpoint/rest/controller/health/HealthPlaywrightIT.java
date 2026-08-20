package io.poja.health.endpoint.rest.controller.health;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.springframework.http.HttpStatus.OK;

import io.poja.health.conf.FacadeIT;
import io.poja.health.endpoint.event.EventProducer;
import io.poja.health.endpoint.event.model.WebsiteTitleCheckRequested;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

class HealthPlaywrightIT extends FacadeIT {

  private static final String DEFAULT_URL = "https://poja.io";
  private static final String CUSTOM_URL = "https://example.com";

  @Autowired private TestRestTemplate restTemplate;
  @MockBean private EventProducer<WebsiteTitleCheckRequested> eventProducerMock;

  @Captor private ArgumentCaptor<Collection<WebsiteTitleCheckRequested>> eventsCaptor;

  @Test
  void check_website_title_with_default_url_ok() {
    ResponseEntity<String> response = restTemplate.getForEntity("/health/playwright", String.class);

    assertEquals(OK, response.getStatusCode());
    assertEquals("OK", response.getBody());
    assertEquals(DEFAULT_URL, capturedRequestedUrl());
  }

  @Test
  void check_website_title_with_explicit_url_ok() {
    ResponseEntity<String> response =
        restTemplate.getForEntity("/health/playwright?url=" + CUSTOM_URL, String.class);

    assertEquals(OK, response.getStatusCode());
    assertEquals("OK", response.getBody());
    assertEquals(CUSTOM_URL, capturedRequestedUrl());
  }

  private String capturedRequestedUrl() {
    verify(eventProducerMock).accept(eventsCaptor.capture());
    List<WebsiteTitleCheckRequested> events = List.copyOf(eventsCaptor.getValue());
    assertEquals(1, events.size());
    return events.getFirst().getUrl();
  }
}
