package pl.globallogic.gorest;

import io.restassured.http.ContentType;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pl.globallogic.configuration.ConfigurationVerificationTest;
import pl.globallogic.gorest.dto.UserRequestDto;
import pl.globallogic.gorest.dto.UserResponseDto;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class UserManagementSmokeTest {

    private static final String HOST = System.getProperty("host") + "public/v2";
    private static final String ENDPOINT = "/users";
    private static final String ENDPOINT_WITH_ID = "/users/{userId}";
    private static final String TOKEN = System.getProperty("token");
    private static Logger logger = LoggerFactory.getLogger(ConfigurationVerificationTest.class);
    private RequestSpecification req;

    @BeforeMethod
    public void testSetUp() {
        req = given()
                .basePath(ENDPOINT)
                .baseUri(HOST)
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + TOKEN);
    }

    @AfterMethod
    public void testCleanUp () {
        logger.info("Put your test clean up");
    }

    @Test
    public void shouldListAllUsersInformation() {
        int user_per_page = 10;
        int target_page = 3;

        req.queryParam("page", user_per_page)
            .queryParam("per_page", target_page)
        .when()
            .get()
        .then()
            .statusCode(200)
            .header("x-pagination-limit", equalTo(String.valueOf(user_per_page)))
            .body("[0].id", notNullValue());
    }

    @Test
    public void shouldCreateUserAndReturnId() {
        UserRequestDto payload = getUserDataWithRandomEmail();
        UserResponseDto newUser = createUser(payload);
        logger.info("ID for new user is: {}", newUser.id());
        Assert.assertNotNull(newUser.id());
    }

    @Test
    public void shouldGetUserInformationById() {
        var user = createUser(getUserDataWithRandomEmail());
        req.basePath(ENDPOINT_WITH_ID).pathParam("userId", user.id());
        UserResponseDto userFromGet = req.get().then().extract().as(UserResponseDto.class);
        Assert.assertEquals(user.name(), userFromGet.name());
    }

    @Test
    public void shouldUpdateUserWithNewEmail() {
        var user = createUser(getUserDataWithRandomEmail());
        String newEmail = "some_email%s@gmail.com".formatted(RandomStringUtils.randomAlphanumeric(5));
        logger.info("Generated new user email: {}", newEmail);
        UserRequestDto updateUserPayload = new UserRequestDto(
                user.name(), newEmail, user.gender(), user.status()
        );
        req.basePath(ENDPOINT_WITH_ID).pathParam("userId", user.id()).body(updateUserPayload);
        UserResponseDto updateResponse = req.put().as(UserResponseDto.class);
        logger.info("User after update: {}", updateResponse);
        Assert.assertEquals(newEmail, updateResponse.email());
    }

    @Test
    public void shouldDeleteUserFromSystem() {
        var user = createUser(getUserDataWithRandomEmail());
        req.basePath(ENDPOINT_WITH_ID).pathParam("userId", user.id());
        req.delete().then().assertThat().statusCode(204);
        req.get().then().assertThat().statusCode(404);
    }

    @Test
    public void validateUserDataAgainstSchema() {
        var user = createUser(getUserDataWithRandomEmail());
        logger.info("Validating user against schema '{}'", "user_schema.json");
        req.basePath(ENDPOINT_WITH_ID).pathParam("userId", user.id());
        req.get().then()
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("user_schema.json"));
    }

    private UserResponseDto createUser(UserRequestDto userData) {
        req.body(userData);
        Response response = req.post().andReturn();
        logger.info("Created user: {}", response.prettyPrint());
        return response.then().extract().as(UserResponseDto.class);
    }

    private UserRequestDto getUserDataWithRandomEmail() {
        String randomEmailSuffix = RandomStringUtils.randomAlphanumeric(5);
        UserRequestDto payload = new UserRequestDto(
                "Ivan Paulouski",
                "ivan.paulouski%s@gmail.com".formatted(randomEmailSuffix),
                "male",
                "active");
        return payload;
    }

}
