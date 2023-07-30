FROM ubuntu:16.04 as build

WORKDIR /workspace/app

ARG JAR_NAME=message-processing-application-0.0.1.jar
ENV DEPENDENCY_PATH=build/libs/dependency

COPY src src
COPY gradle gradle
COPY gradlew gradlew
COPY build.gradle build.gradle
COPY settings.gradle settings.gradle

RUN apt-get update -y

RUN apt-get install -y wget apt-transport-https

RUN wget -O - https://packages.adoptium.net/artifactory/api/gpg/key/public | tee /usr/share/keyrings/adoptium.asc

RUN echo "deb [signed-by=/usr/share/keyrings/adoptium.asc] https://packages.adoptium.net/artifactory/deb $(awk -F= '/^VERSION_CODENAME/{print$2}' /etc/os-release) main" | tee /etc/apt/sources.list.d/adoptium.list

RUN apt-get update -y

RUN apt-get install -y temurin-17-jdk --allow-unauthenticated

RUN ./gradlew wrapper

RUN ./gradlew build

RUN mkdir -p $DEPENDENCY_PATH

RUN (cd $DEPENDENCY_PATH; jar -xf ../$JAR_NAME)

FROM ubuntu:16.04

ARG DEPENDENCY=/workspace/app/build/libs/dependency
ENV SPRING_SERVER_PORT=8083
ENV JAVA_TOOL_OPTIONS="-XX:MaxRamPercentage=80"
RUN apt-get update -y

RUN apt-get install -y wget apt-transport-https curl software-properties-common

RUN wget -O - https://packages.adoptium.net/artifactory/api/gpg/key/public | tee /usr/share/keyrings/adoptium.asc

RUN echo "deb [signed-by=/usr/share/keyrings/adoptium.asc] https://packages.adoptium.net/artifactory/deb $(awk -F= '/^VERSION_CODENAME/{print$2}' /etc/os-release) main" | tee /etc/apt/sources.list.d/adoptium.list

RUN apt-get update -y && apt-get install -y temurin-17-jdk --allow-unauthenticated

RUN groupadd -r spring && useradd -r spring -g spring

COPY --chown=spring:spring bin/start.sh /app/start.sh
COPY --chown=spring:spring --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --chown=spring:spring --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --chown=spring:spring --from=build ${DEPENDENCY}/BOOT-INF/classes /app

EXPOSE ${SPRING_SERVER_PORT}
RUN chmod 700 /app/start.sh
USER spring:spring

ENTRYPOINT ["./app/start.sh"]
