/*
Author: Michael Bonner
Date: 20190626
*/
package weather;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.util.NestedServletException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class WhatsTheWeatherControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    public void homeTest() throws Exception {
        MvcResult result = this.mvc.perform(get("/")).andExpect(status().isOk())
            .andExpect(content().contentType("text/html;charset=UTF-8"))
            .andReturn();
        String content = result.getResponse().getContentAsString();
        Assert.assertTrue(content.contains("Vancouver"));
        Assert.assertTrue(content.contains("Hong Kong"));
        Assert.assertTrue(content.contains("London"));
    }
    
    @Test
    public void processOpenWeatherMapAPIResponseTest() throws Exception {
        MvcResult result = this.mvc.perform(get("/weather?city=London")).andExpect(status().isOk())
            .andExpect(content().contentType("text/html;charset=UTF-8"))
            .andReturn();
        String content = result.getResponse().getContentAsString();
        Assert.assertTrue(content.contains("London"));
        Assert.assertTrue(content.contains("Temperature (C)"));
        
        
        Assert.assertFalse(content.contains("WhatsTheWeather Error"));
        Assert.assertFalse(content.contains("Exception"));
        Assert.assertFalse(content.contains("text-danger"));
    }

    @Test
    public void processOpenWeatherMapAPIResponseTestHK() throws Exception {
        MvcResult result = this.mvc.perform(get("/weather?city=Hong Kong")).andExpect(status().isOk())
            .andExpect(content().contentType("text/html;charset=UTF-8"))
            .andReturn();
        String content = result.getResponse().getContentAsString();
        Assert.assertTrue(content.contains("Hong Kong"));
        Assert.assertTrue(content.contains("Temperature (C)"));
        
        
        Assert.assertFalse(content.contains("WhatsTheWeather Error"));
        Assert.assertFalse(content.contains("Exception"));
        Assert.assertFalse(content.contains("text-danger"));
    }
    
    @Test(expected = NestedServletException.class)
    public void processOpenWeatherMapAPIResponseTestUnsupportedCity() throws Exception {
        
        this.mvc.perform(get("/weather?city=Los Angeles")).andExpect(status().is5xxServerError())
            .andExpect(content().contentType("text/html;charset=UTF-8"))
            .andReturn();           
    }
}
