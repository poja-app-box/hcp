package io.poja.health.endpoint.rest.controller.health;

import static io.poja.health.endpoint.rest.controller.health.PingController.OK;

import io.poja.health.endpoint.event.EventProducer;
import io.poja.health.endpoint.event.model.WebsiteTitleCheckRequested;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class HealthPlaywrightController {

  private final EventProducer<WebsiteTitleCheckRequested> eventProducer;

  @GetMapping(value = "/health/playwright")
  public ResponseEntity<String> check_website_title(
      @RequestParam(defaultValue = "https://poja.io") String url) {
    eventProducer.accept(List.of(new WebsiteTitleCheckRequested(url)));
    return OK;
  }
}
