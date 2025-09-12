FROM amazoncorretto:21-alpine
RUN mkdir -p /opt/laa-maat-scheduled-tasks/
WORKDIR /opt/laa-maat-scheduled-tasks/
COPY ./build/libs/maat-scheduled-tasks.jar /opt/laa-maat-scheduled-tasks/app.jar
RUN addgroup -S appgroup && adduser -u 1001 -S appuser -G appgroup
USER 1001
EXPOSE 8499 8510
CMD java -jar app.jar
