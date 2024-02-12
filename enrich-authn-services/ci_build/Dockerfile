# Use a base image with Ubuntu 20.04
FROM ubuntu:20.04

# Install OpenJDK 11
RUN apt-get update && apt-get install -y openjdk-11-jdk
ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update && apt-get install -y tzdata

RUN ln -fs /usr/share/zoneinfo/Asia/Kolkata /etc/localtime && \
    dpkg-reconfigure --frontend noninteractive tzdata

# Set the working directory in the container
WORKDIR /app

# Copy the Spring Boot application JAR file to the container
COPY target/enrich-authn-services.jar .

# Specify the command to run the application
CMD ["java", "-jar", "enrich-authn-services.jar"]
