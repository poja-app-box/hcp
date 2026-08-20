package io.poja.health.service.event;

import com.microsoft.playwright.BrowserType.LaunchOptions;
import com.microsoft.playwright.Playwright;
import io.poja.health.endpoint.event.model.WebsiteTitleCheckRequested;
import java.util.List;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Opens the requested website with a real Chromium driven by Playwright, and logs the document
 * title. It only reads the page, it submits nothing.
 */
@Service
@AllArgsConstructor
@Slf4j
public class WebsiteTitleCheckRequestedService implements Consumer<WebsiteTitleCheckRequested> {

  private static final List<String> CHROMIUM_LAMBDA_ARGS =
      List.of(
          // Lambda already is the sandbox, and the kernel features Chromium's own
          // sandbox needs are not there.
          "--no-sandbox",
          // /dev/shm is 64 MB on Lambda, far below what Chromium assumes.
          "--disable-dev-shm-usage",
          "--disable-gpu");

  @Override
  public void accept(WebsiteTitleCheckRequested event) {
    var url = event.getUrl();
    log.info("Checking website title, url={}, attemptNb={}", url, event.getAttemptNb());

    try (var playwright = Playwright.create()) {
      try (var browser =
          playwright
              .chromium()
              .launch(new LaunchOptions().setHeadless(true).setArgs(CHROMIUM_LAMBDA_ARGS))) {
        var page = browser.newPage();
        page.navigate(url);
        log.info("Opened {}: title={}", url, page.title());
      }
    }
  }
}
