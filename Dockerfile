FROM public.ecr.aws/docker/library/openjdk:17-oracle
WORKDIR /app

#RUN \
## Update
#apt-get update -y && \
## Install Java
#apt-get install default-jre -y

COPY src ./src 
COPY libs ./libs
COPY resources ./resources
COPY build ./build
COPY cl-star.jar ./cl-star.jar
RUN javac -d build -cp "libs/*" -Xlint:unchecked src/*.java
WORKDIR /app/build
RUN jar cvf ../cl-star.jar *.class
WORKDIR /app
