Whats the weather - a simple spring boot web application that shows you the current weather in London and Hong Kong.

prerequisites: 
1. Java 1.8 or above
2. Latest version of Maven
3. Latest version of Docker

Follow the below steps in sequence.

- Execute the below cmd to buid and start the application-

$ ./mvnw package && java -jar target/whats-the-weather-0.1.0.jar

Note the version from the pom.xml file is part of the jar name, if the version changes, this command will need to be updated.

- Nevigate to http://localhost:8080

- To Containerize the WhatsTheWeather Application (see the Dockerfile for details) run the cmds below which will build a Docker image with the name weather/whats-the-weather:latest

- Execute the below cmd - 

$ ./mvnw install dockerfile:build
 
- Run the docker image with the following cmd
$ docker run -p 8081:8080 -t weather/whats-the-weather

- Navigate to http://localhost:8081

- Here 8081 is the Docker port and 8080 is the Tomcat port where is the application is running. 

### TODO:
* Create cache of mappings between city names and ids, if not in cache, use get weather by city, else get by id.
* Polling/retry back off logic for getting from the openmap api, handle/support for unavailable service
* Remove dependence in controller code on response schema

contact bonner.mike@gmail.com for more details and queries. 
