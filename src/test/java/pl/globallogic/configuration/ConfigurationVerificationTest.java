package pl.globallogic.configuration;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ConfigurationVerificationTest {

    private Logger logger = LoggerFactory.getLogger(ConfigurationVerificationTest.class);

    @Test
    void shouldSendRequestTOServerAndGetStatusOk() {
        logger.info("Verifying project dependencies set up");
        RestAssured.useRelaxedHTTPSValidation();
        Response response = RestAssured.get("https://gorest.co.in/public/v2/users");
        logger.info("Response body: {}", response.getBody().prettyPeek());
        Assert.assertEquals(200, response.getStatusCode());
    }

}
