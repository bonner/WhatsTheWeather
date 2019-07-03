/*
Author: Michael Bonner 
Date: 20190626 
*/ 

package weather;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;
//import java.util.SimpleTimeZone;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ApiParam;

/*
@GetMapping("/age")
ResponseEntity<String> age(@RequestParam("yearOfBirth") int yearOfBirth) {
    if (isInFuture(yearOfBirth)) {
        return ResponseEntity.badRequest()
            .body("Year of birth cannot be in the future");
    }
 
    return ResponseEntity.status(HttpStatus.OK)
        .body("Your age is " + calculateAge(yearOfBirth));
}


@GetMapping("/mymethod")
public ResponseEntity<Response> myMethod() {
    ResponseEntity responseEntity = null;

    if(string) {
        responseEntity = new ResponseEntity(getString(), HttpStatus.OK);
    } else {
        responseEntity = new ResponseEntity(getHtml(), HttpStatus.OK);
    }

    return responseEntity;
}

public RestModel create(@RequestBody String data, HttpServletResponse response) {
    // code ommitted..
    response.setStatus(HttpServletResponse.SC_ACCEPTED);
}

todo tonight: 
    * unit tests
    * set http response codes for exceptions, debug exception variable not being set.
    * clean up, refactor, way too much functionality in controller, how could I..
    *     push conversions to view, or make open api specific bit its own thing! ie parsing the response, populate a more generic structure, ie a model.
    * documentation, swagger and source code
    * readme
    * deploy on heroku and aws
 */


/**
 * Controller for the WhatsTheWeather application.
 * 
 * @author Michael Bonner
 * @since 20190626
 */
@Controller
public class WhatsTheWeatherController {

    @Value("${openWeatherMapAPIKey}")
    private String apiKey;    
    private static final String resourceUrl = "https://api.openweathermap.org/data/2.5/weather";
    
    private static HashMap<String, String> cityCodeMap = new HashMap<String,String>();
    private static HashMap<String, String> timeZoneIds = new HashMap<String,String>();
    static {
        cityCodeMap.put("London", "2643741");
        cityCodeMap.put("Hong Kong", "1819729");
        cityCodeMap.put("Vancouver", "6173331");
        timeZoneIds.put("London", "Europe/London");
        timeZoneIds.put("Hong Kong", "Asia/Hong_Kong");
        timeZoneIds.put("Vancouver", "America/Vancouver");
    }  
    
    /**
     * Process the JsonNode from the open weather map api response into a map consumable by 
     * the populateModel method.  
     * 
     * The model will contain the following attributes: 
     *     date: today’s date
     *     city_name: the city name
     *     weather_: overall description of the weather (e.g. "Light rain", "Clear sky", etc.)
     *     weather_description:
     *     temperature_celcius: temperature in Celsius
     *     temperature_fahrenheit: temperature in Fahrenheit
     *     sunrise: Time in local timezone of the sunrise in 12hr format
     *     sunset: Time in local timezone of the sunset in 12hr format
     * 
     * @param city User input city, must be one of cityCodeMap.keySet()
     * @param rootNode Jackson JsonNode instance containing the contents of a 200 response from the 
     *          api.openweathermap.org/data/2.5/weather api.
     * @param model The model to populate with attributes used by the view weather.html template.
     * 
     * @return Map containing all values consumable by the view.
     */
    private void processOpenWeatherMapAPIResponse(String city, JsonNode rootNode, Model model) {
                       
        // Response is in seconds, convert to milliseconds 
        long dt = 1000 * rootNode.path("dt").longValue();

        JsonNode sysNode = rootNode.path("sys");
        // response is in seconds, convert to milliseconds
        long sunrise = 1000 * Long.parseLong(sysNode.path("sunrise").asText()); 
        long sunset = 1000 * Long.parseLong(sysNode.path("sunset").asText());
        
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        DateFormat timeFormat = new SimpleDateFormat("hh:mm a"); 

        // Create TimeZone instance based on the timezone offset in the response, this is more generic and
        // makes it easier to add new cities, but many timezones have the same offset, so requiring new cities
        // to be added to the timeZoneIds map is reasonable and provides a better user experience.
        //int tzOffsetms = 1000 * rootNode.path("timezone").intValue();
        //int tzOffsetHrs = tzOffsetms / (1000 * 60 * 60);        
        //String[] tzIds = TimeZone.getAvailableIDs(tzOffsetms);                    
        //System.out.printf("timezone ids: %s\n", Arrays.toString(tzIds));                    
        //SimpleTimeZone tz = new SimpleTimeZone(tzOffsetHrs, tzIds[0]);
        
        TimeZone tz = TimeZone.getTimeZone(timeZoneIds.get(city));
        dateFormat.setTimeZone(tz);
        timeFormat.setTimeZone(tz);
        
        String dateStr = dateFormat.format(new Date(dt));
        String sunriseStr = timeFormat.format(new Date(sunrise));
        String sunsetStr = timeFormat.format(new Date(sunset));
        
        String cityName = rootNode.path("name").asText();

        // Create the weather description string from the weather.main and weather.description fields in the
        // response.
        // TODO: update to use string builder...
        ArrayList<String> weatherDescriptions = new ArrayList<String>();
        for (JsonNode weatherNode : rootNode.path("weather")) {     
            weatherDescriptions.add(String.format("%s - %s", weatherNode.path("main").asText(), 
                    weatherNode.path("description")));
        }
        String weatherStr = String.join(",", weatherDescriptions); 

        // Response temp value is in Kelvin, convert to celsius and fahrenheit (move to view?)
        JsonNode mainNode = rootNode.path("main");
        double temp = Double.parseDouble(mainNode.path("temp").asText());
        double tempCelc = temp - 273.15;
        double tempFahr = tempCelc * 9./5. + 32.;  
        
        model.addAttribute("date", dateStr);
        model.addAttribute("city_name", cityName);
        model.addAttribute("weather_description", weatherStr);
        model.addAttribute("temperature_celsius", String.format("%.3f", tempCelc));
        model.addAttribute("temperature_fahrenheit", String.format("%.3f", tempFahr));
        model.addAttribute("sunrise", sunriseStr);
        model.addAttribute("sunset", sunsetStr);
        model.addAttribute("time_zone", tz.getID());        
    }
 
    /**
     * Simple method to render the input form for the WhatsTheWeather application.
     * 
     * @param model The model to populate (contains the list of supported cities).
     * @return The name of the template to render.
     */
    @ApiOperation(value = "Get city input form for getting the weather for the selected city", 
            response = String.class)
    @ApiResponses(value = { 
            @ApiResponse(code = 200, message = "Successfully obtained the html for the city input form."),
            @ApiResponse(code = 500, message = "An exception occurred.")}
    )
    @GetMapping(path = "/", produces = "text/html")
    public String home(Model model) {
    	model.addAttribute("cities", cityCodeMap.keySet());
        return "index";
    }
    
    /**
     * Populates the input model instance with the expected values from the open weather map api response.
     * Raises or passes exceptions when the response is not received, the schema is different than expected, 
     * or the values in the response cannot be parsed into the expected types.
     * 
     * @param city The user input city, must be one of cityCodeMap.keyValues().
     * @param model The model to populate
     * @return The name of the thymeleaf template to render.
     * 
     * @throws IOException Raised by JsonNode instances when the response does not meet the expected schema.
     */
    @ApiOperation(value = "Get the weather for the requested city", response = String.class)
    @ApiResponses(value = { 
            @ApiResponse(code = 200, message = "Successfully obtained the weather for the requested city."),
            @ApiResponse(code = 500, message = "An exception occurred.")}
    )
    @GetMapping(path = "/weather", produces = "text/html")
    public String whatsTheWeather(
            @ApiParam(defaultValue = "London", allowableValues = "London, Hong Kong, Vancouver") @RequestParam(name = "city", required = true) String city,
            Model model) throws IOException {
        
        System.out.printf("CITY: %s\n", city);
        //System.out.printf("API KEY: %s\n", apiKey);  commented out for security 
        
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
                    processOpenWeatherMapAPIResponse(city, mapper.readTree(response.getBody()), model);           
                }
                catch (NumberFormatException e) {
                    model.addAttribute("message", "");
                    System.out.println(
                            "One or more of the expected values could not be parsed into the expected data type.");
                    throw e;
                }
                catch (IOException e) { // exceptions from parsing json response
                	System.out.println("One of the values in the expected schema was not present.");
                	throw e;
                }
            }
            else {
            	String message = String.format(
            	        "A non-200 response was received from %s, status: %s body: %s", 
            	        resourceUrl);
            	System.out.println(message);
            	throw new HttpClientErrorException(status, message);
            }
        }
        else {
        	String message = String.format("Unsupported city, must be one of: %s", 
        	        Arrays.toString(cityCodeMap.keySet().toArray()));
        	throw new IllegalArgumentException(message);
        }
        return "weather";
    }
}
