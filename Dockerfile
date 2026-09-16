FROM eclipse-temurin:25-jre-alpine

ARG APP_UID=10001
ARG APP_PORT=8092

RUN addgroup -S aft && adduser -S -u ${APP_UID} -G aft aft
WORKDIR /app

COPY --chown=aft:aft *.jar /app/app.jar

USER aft

ENV TZ=Europe/Istanbul \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=50 -XX:+ExitOnOutOfMemoryError"

EXPOSE ${APP_PORT}

ENTRYPOINT ["java", "-jar", "/app/app.jar"]