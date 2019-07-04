# WhatsTheWeather

This is a simple spring boot, bootstrap, thymeleaf web application that shows you the current weather in London, Hong Kong, and Vancouver (BC CA).
It uses the open weather map API to get the current weather conditions, temperature, and time of sunrise and sunset in the user selected city.

The app is currently up and running at https://whats-the-current-weather.herokuapp.com/.
	
---

## prerequisites:
The app was built with the following tools
* Java 1.8 
* Maven 3.6.1
* Docker 18.09.2

---

## Building

Follow the steps below in sequence.

- Execute the below cmd to buid and start the application

	`$ ./mvnw package && java -jar target/whats-the-weather-0.1.0.jar`

	Note the version from the pom.xml file is part of the jar name, if the version changes, this command will need to be updated.

- Navigate to http://localhost:8080

To Containerize the WhatsTheWeather Application (see the Dockerfile for details) run the cmds below which will build a Docker image with the name weather/whats-the-weather:latest

- Execute the below cmd
	`$ ./mvnw install dockerfile:build`
 
- Run the docker image with the following cmd
	`$ docker run -p 8081:8080 -t weather/whats-the-weather`

- Navigate to http://localhost:8081
	Here 8081 is the Docker port and 8080 is the Tomcat port where is the application is running. 
	
---
## Documentation
Javadoc documentaion for the classes within the app is generated when running mvnw package.
Swagger documentation can be viewed at [swagger](http://localhost:8080/swagger-ui.html#!)

---

## Adding another city

In order to add another city to the application you need to find the openweather map id for the city (or the closest city) and the timezone identifier for your city.  
You can find the list of open weather map city ids [here](http://bulk.openweathermap.org/sample/city.list.json.gz) and the timezone identifiers [here](https://en.wikipedia.org/wiki/List_of_tz_database_time_zones).  
Once you have these values you can add them to the *cityCodeMap* and *timeZoneIds* maps within the *WhatsTheWeatherController* class. 

---

## Troubleshooting
This application has a hard coded expectation on the response from open weather map, if it changes, this 
applicaiton will likely break and generate an exception.
In this case, you will be directed to an error page with details of the exception.
The API key used by this application is a free account, if too many requests are made the request for 
weather data will be denied with a *439* response, you will see this on the error page, details can be found [here](https://openweathermap.org/appid).

---

### TODO:
* Set http response codes for exceptions (NOT 500!)
* Create cache of mappings between city names and ids, if not in cache, use get weather by city, else get by id.
* Polling/retry back off logic for getting from the openmap api, handle/support for unavailable service
* Remove dependence in controller code on response schema
* Make a modal for the response from the form, single page design
* Use proper logging rather than System.out.
* Support for input by lat, long or UTC coordinates.
* AWS deployment
* Support for day light savings time
* Move css in head to a static file.

contact bonner.mike@gmail.com for more details and queries. 
