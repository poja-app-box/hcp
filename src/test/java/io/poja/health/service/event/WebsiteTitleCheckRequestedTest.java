package io.poja.health.service.event;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.poja.health.endpoint.event.model.WebsiteTitleCheckRequested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

@ExtendWith(OutputCaptureExtension.class)
class WebsiteTitleCheckRequestedTest {

  private static final String URL = "https://poja.io";
  private static final String EXPECTED_TITLE = "Deploy Spring Boot in minutes |\u00A0Poja";

  private final WebsiteTitleCheckRequestedService subject = new WebsiteTitleCheckRequestedService();

  @Test
  void check_ok(CapturedOutput logs) {
    subject.accept(new WebsiteTitleCheckRequested(URL));

    var expectedLog = "Opened %s: title=%s".formatted(URL, EXPECTED_TITLE);
    assertTrue(
        logs.getAll().contains(expectedLog),
        "expected log \"%s\", got: %s".formatted(expectedLog, logs.getAll()));
  }
}
