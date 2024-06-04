package pl.globallogic.gorest;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pl.globallogic.configuration.ConfigurationVerificationTest;
import pl.globallogic.gorest.dto.UserRequestDto;
import pl.globallogic.gorest.dto.UserResponseDto;

import java.util.Map;

import static io.restassured.RestAssured.given;

public class UserManagementSmokeTest {

    private static final String HOST = "https://gorest.co.in/public/v2";
    private static final String ENDPOINT = "/users";
    private static final String ENDPOINT_WITH_ID = "/users/{userId}";
    private static final String TOKEN = "edb4afe94ffa5dd5ee817c3eb2ff3a0408777f6781098e96969cd9cb742050d4";
    private static Logger logger = LoggerFactory.getLogger(ConfigurationVerificationTest.class);
    private RequestSpecification req;

    @BeforeMethod
    public void testSetup() {
        req = given()
                .basePath(ENDPOINT_WITH_ID)
                .baseUri(HOST)
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + TOKEN);
    }

    @Test
    public void shouldListAllUsersInformation() {
        given()  //  Arrange
                .baseUri(HOST)
                .basePath(ENDPOINT)
                .queryParam("page", 3)
                .queryParam("per_page", 10)
                .log()
                .parameters()
        .when()  //  Act
                .get()
        .then()  //  Assert
                .log().headers()
                .statusCode(200);
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
        RequestSpecification req = given()
                .basePath(ENDPOINT_WITH_ID)
                .baseUri(HOST)
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + TOKEN)
                .pathParam("userId", user.id());
        UserResponseDto userFromGet = req.get().then().extract().as(UserResponseDto.class);
        Assert.assertEquals(user.name(), userFromGet.name());
    }

    @Test
    public void shouldUpdateUserWithNewEmail() {
        var user = createUser(getUserDataWithRandomEmail());
        String newEmail = "some_email%s@gmail.com".formatted(RandomStringUtils.randomAlphanumeric(5));
        logger.info("Generated new user email: {}", newEmail);
        RequestSpecification req = given()
                .basePath(ENDPOINT_WITH_ID)
                .baseUri(HOST)
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + TOKEN)
                .pathParam("userId", user.id());
        UserRequestDto updateUserPayload = new UserRequestDto(
                user.name(), newEmail, user.gender(), user.status()
        );
        UserResponseDto updateResponse = req.body(updateUserPayload)
                .put().as(UserResponseDto.class);
        logger.info("User after update: {}", updateResponse);
        Assert.assertEquals(newEmail, updateResponse.email());
    }

    @Test
    public void shouldDeleteUserFromSystem() {
        var user = createUser(getUserDataWithRandomEmail());
        RequestSpecification req = given()
                .basePath(ENDPOINT_WITH_ID)
                .baseUri(HOST)
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + TOKEN)
                .pathParam("userId", user.id());
        req.delete().then().assertThat().statusCode(204);
        req.get().then().assertThat().statusCode(404);
    }

    @Test
    public void validateUserDataAgainstSchema() {
        
    }

    private static UserResponseDto createUser(UserRequestDto userData){
        RequestSpecification req = given()
                .basePath(ENDPOINT)
                .baseUri(HOST)
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + TOKEN)
                .body(userData);
        Response response = req.post().andReturn();
        logger.info("Created user: {}", response.prettyPrint());
        return response.then().extract().as(UserResponseDto.class);
    }

    private static UserRequestDto getUserDataWithRandomEmail() {
        String randomEmailSuffix = RandomStringUtils.randomAlphanumeric(5);
        UserRequestDto payload = new UserRequestDto(
                "Ivan Paulouski",
                "ivan.paulouski%s@gmail.com".formatted(randomEmailSuffix),
                "male",
                "active");
        return payload;
    }

}
