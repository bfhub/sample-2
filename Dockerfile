FROM openjdk:8-alpine

COPY target/uberjar/sample-1-2.jar /sample-1-2/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/sample-1-2/app.jar"]
