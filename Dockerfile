ARG BUILDER_JDK_VERSION=haeseung/amazoncorretto:17-al2023-headless
FROM ${BUILDER_JDK_VERSION} AS builder
WORKDIR /application
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} application.jar
RUN java -Djarmode=layertools -jar application.jar extract

FROM haeseung/curl-opentelemetry:v1.32.0 AS download

ARG BUILDER_JDK_VERSION
FROM ${BUILDER_JDK_VERSION}
WORKDIR /application
COPY --from=builder application/dependencies/ ./
COPY --from=builder application/spring-boot-loader/ ./
COPY --from=builder application/snapshot-dependencies/ ./
COPY --from=download /home/curl_user/opentelemetry-javaagent.jar /opentelemetry-javaagent.jar
COPY --from=builder application/application/ ./
ENTRYPOINT ["java", "-javaagent:/opentelemetry-javaagent.jar", "org.springframework.boot.loader.JarLauncher"]