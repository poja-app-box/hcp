# The jar is built here rather than on the host so that `sam build` and the CI
# produce the same worker artifact.
FROM amazoncorretto:21 AS builder
# gradlew shells out to xargs, which the corretto image does not ship.
RUN yum install -y findutils && yum clean all
WORKDIR /build
COPY gradle gradle
COPY gradlew settings.gradle build.gradle lombok.config ./
RUN ./gradlew --no-daemon dependencies > /dev/null 2>&1 || true
COPY . .
RUN ./gradlew --no-daemon clean bootJar collectPlaywrightDriverBundle -x test

# A Spring Boot bootJar nests its dependencies under BOOT-INF/lib, which `java -cp`
# cannot load, so flatten it. The driver bundle is grafted onto that same classpath:
# it is excluded from the build so the frontal's zip stays under Lambda's 250 MB
# unzipped quota, and only the worker actually drives a browser.
RUN mkdir -p /out && cd /out \
 && jar xf /build/build/libs/hcp-37762aa0.jar \
 && cp /build/build/playwright-driver/driver-bundle-*.jar BOOT-INF/lib/

# Carries Chromium and its system libraries under /ms-playwright. Its tag must stay
# in step with the playwright version in build.gradle: the Java driver refuses
# browsers it was not built against.
FROM mcr.microsoft.com/playwright/java:v1.62.0-noble

# Only /tmp is writable on Lambda, and the Playwright driver wants a home. Browsers
# are pinned explicitly because HOME=/tmp would otherwise send the driver looking in
# /tmp/.cache/ms-playwright instead of the path the base image populated.
ENV HOME=/tmp \
    XDG_CACHE_HOME=/tmp/.cache \
    PLAYWRIGHT_BROWSERS_PATH=/ms-playwright

COPY --from=builder /out /app

# This image is not a Lambda base image, so the Runtime Interface Client plays the
# part of the runtime and the handler is its argument.
# --enable-native-access: the client loads a JNI library, which the image's JDK only
# warns about today but will refuse in a later release.
ENTRYPOINT [ "java", "--enable-native-access=ALL-UNNAMED", \
             "-cp", "/app/BOOT-INF/classes:/app/BOOT-INF/lib/*", \
             "com.amazonaws.services.lambda.runtime.api.client.AWSLambda" ]
CMD [ "io.poja.health.handler.MailboxEventHandler::handleRequest" ]
