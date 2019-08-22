FROM adoptopenjdk/openjdk11
RUN mkdir /app
COPY build/libs/radius-server-*-all.jar /app/radius-server.jar
WORKDIR /app

EXPOSE 1812:1812/udp
EXPOSE 1813:1813/udp

ENTRYPOINT ["java", "-jar", "radius-server.jar"]
