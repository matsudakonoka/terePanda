FROM 11.11.174.85:28086/cnpcrd/java_base:201906-pinpoint-filebeat_v1

MAINTAINER blueapple <baidongying@cnpc.com.cn>
LABEL version="2.0 safe"
LABEL description="This image base on alpine 3.9 include pinpoint-agent 1.7.3,curl,openjdk 1.8.0_212,glibc 2.29 dumb-init gosu"

ARG JAVA_APP_JAR_NAME="${JAVA_APP_JAR_NAME:-app.jar}"
ENV SPRING_OUTPUT_ANSI_ENABLED=ALWAYS \
    APP_SLEEP=0 \
    JAVA_OPTS="" \
    RUN_ARGS="" \
    APP_NAME_ID=""

# Add a user admin to run our application so that it doesn't need to run as root
RUN adduser -D -s /bin/sh admin

USER root
WORKDIR /deployments

COPY target/${JAVA_APP_JAR_NAME:-app.jar} \
     entrypoint.sh \
     /deployments/
COPY filebeat.yml /deployments/filebeat-6.8.0/filebeat.yml
RUN chmod +x /deployments/entrypoint.sh && \ 
    chmod go-w /deployments/filebeat-6.8.0/filebeat.yml && \
    echo '. ~/.bashrc' > /home/admin/.bash_profile && \
    echo '. /etc/profile' > /home/admin/.bashrc && \
    chown -R admin:admin /home/admin \
                         /deployments \
                         /var/log
USER admin
EXPOSE 8080 8301
