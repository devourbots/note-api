FROM java:8
VOLUME /tmp
WORKDIR /tmp
COPY ./flomo-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java -Djava.security.egd=file:/dev/./urandom -jar /tmp/app.jar --spring.profiles.active=docker"]