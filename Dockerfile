FROM adoptopenjdk/openjdk11:alpine-jre
ENV USER_UID=1001 \
    USER_NAME=user-process-management
RUN addgroup --gid ${USER_UID} ${USER_NAME} \
    && adduser --disabled-password --uid ${USER_UID} --ingroup ${USER_NAME} ${USER_NAME}
COPY target/*.jar app.jar
USER user-process-management
ENTRYPOINT ["/bin/sh", "-c", "java $JAVA_OPTS -jar /app.jar"]
