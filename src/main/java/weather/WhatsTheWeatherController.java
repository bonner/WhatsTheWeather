/*
Author: Michael Bonner 
Date: 20190626 
*/ 

package weather;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ApiParam;

@SpringBootApplication
@RestController
public class WhatsTheWeatherController {

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder.build();
	}
	
	@Value("${openWeatherMapAPIKey}")
	private String apiKey;	
	private static final String resourceUrl = "https://api.openweathermap.org/data/2.5/weather";
	
    // Map of payment schedule strings to number of payments per year.
	private static HashMap<String, String> cityCodeMap = new HashMap<String,String>();
	
	 
	static {
	    cityCodeMap.put("London", "2643741");
	    cityCodeMap.put("Hong Kong", "1819729");
	}
	/*
    private static Map<String, String> cityCodeMap = Stream.of(
        new AbstractMap.SimpleEntry<>("London", "2643741"), 
        new AbstractMap.SimpleEntry<>("Hong Kong", "1819729"))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    */
	
    protected static ResponseEntity<Map<?, ?>> resp(HttpStatus status, Object... keyValues) {
        assert (keyValues.length % 2 == 0);
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            map.put((String) keyValues[i], keyValues[i + 1]);
        }
        return new ResponseEntity<Map<?, ?>>(map, status);
    }
    protected static ResponseEntity<Map<?, ?>> resp(HttpStatus status, Map<?, ?> map) {
        return new ResponseEntity<Map<?, ?>>(map, status);
    }
 
    @RequestMapping(path = "/", method = RequestMethod.GET)
    String home() {
      return "<head><meta http-equiv=\"refresh\" content=\"0; URL='index.html'\" /></head>";
    }
    
    @ApiOperation(value = "Get the weather for the requested city", response = Map.class)
    @ApiResponses(value = { 
            @ApiResponse(code = 200, message = "Successfully obtained the weather for the requested city."),
            @ApiResponse(code = 400, message = "Unexpected request data")})
    @RequestMapping(path = "/weather", method = RequestMethod.GET, produces = "application/json")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<Map<?, ?>> whatsTheWeather(
            @ApiParam(defaultValue = "london", allowableValues = "london, hongkong") @RequestParam(name = "city", required = true) String city) {
        
    	System.out.printf("CITY: %s\n", city);
    	System.out.printf("API KEY: %s\n", apiKey);    	
    	
        if (cityCodeMap.containsKey(city)) {
        	
        	RestTemplate restTemplate = new RestTemplate();
        	String url = String.format("%s?appid=%s&id=%s", resourceUrl, apiKey, cityCodeMap.get(city));
            
            System.out.printf("URL: %s\n", url);
            
        	ResponseEntity<String> response
        	  = restTemplate.getForEntity(url, String.class);
        	
        	HttpStatus status = response.getStatusCode();
        	if (status.value() / 100 == 2) {

        		try {
            	    ObjectMapper mapper = new ObjectMapper();
            	    JsonNode rootNode = mapper.readTree(response.getBody()); 
            	    
            	    String currDateTime = rootNode.path("dt").asText(); // just need date.
            	    String cityName = rootNode.path("name").asText();
            	    String timeZone = rootNode.path("timezone").asText();
            	    
            	    // get specific required values, return html or somehow use a template!
            	    // example
            	    JsonNode weatherNode = rootNode.path("weather");
            	    // ! weather is an array
            	    // need to look at a schema for this response too! this might change!
            	    String weatherMainStr = weatherNode.path("main").asText();
            	    String weatherDescriptionStr = weatherNode.path("description").asText();
            	    
            	    JsonNode mainNode = rootNode.path("main");
            	    String tempStr = mainNode.path("temp").asText();
            	    String pressureStr = mainNode.path("pressure").asText();
            	    String humidityStr = mainNode.path("humidity").asText();
            	    String minTempStr = mainNode.path("temp_min").asText();
            	    String maxTempStr = mainNode.path("temp_max").asText();
            	   
            	    JsonNode sysNode = rootNode.path("sys");
            	    String sunriseStr = sysNode.path("sunrise").asText();
            	    String sunsetStr = sysNode.path("sunset").asText();
            	    /*
            	    today’s date
            	    o the city name
            	    o overall description of the weather (e.g. "Light rain", "Clear sky", etc.)
            	    o temperature in Fahrenheit and Celsius
            	    o sunrise and sunset times in 12 hour format (e.g. 9:35am; 11:47pm)     
            	    */   
            	    return resp(HttpStatus.OK, "code", status, "city", cityName, "timezone", timeZone, 
            	    		"datetime", currDateTime, 
            	    		"weather", String.format("%s - %s", weatherMainStr, weatherDescriptionStr),
            	    		"temperature", tempStr);
        		}
                catch (IOException e) {
                	return resp(HttpStatus.BAD_REQUEST, "error", e.getMessage());
                }
        	    
        		//return resp(HttpStatus.OK, "city", city, "code", status,  "body", response.getBody());
        	}
        	else {
        		return resp(status, "body", response.getBody());
        	}
        }
        else {
        	// raise exception/return 4xx.
        	return resp(HttpStatus.BAD_REQUEST, "error", "Unsupported city.");
        }
    }
    
    public static void main(String[] args) {
        
        String ENV_PORT = System.getenv().get("PORT");
        String ENV_DYNO = System.getenv().get("DYNO");
        if(ENV_PORT != null && ENV_DYNO != null) {
            System.getProperties().put("server.port", ENV_PORT);
        }
        SpringApplication.run(WhatsTheWeatherController.class, args);
    }

}
