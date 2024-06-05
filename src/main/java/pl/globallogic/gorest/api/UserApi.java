package pl.globallogic.gorest.api;

import io.restassured.http.ContentType;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.globallogic.gorest.dto.UserRequestDto;
import pl.globallogic.gorest.dto.UserResponseDto;

import java.util.List;


import static io.restassured.RestAssured.given;

public class UserApi {

    private static final Logger logger = LoggerFactory.getLogger(UserApi.class);

    private static final String HOST = System.getProperty("host") + "public/v2";
    private static final String ENDPOINT = "/users";
    private static final String ENDPOINT_WITH_ID = "/users/{userId}";

    private String token;
    private RequestSpecification request;
    private Response response;

    public UserApi(String token) {
        this.token = token;
    }

    public Response getResponse() {
        return response;
    }

    public List<UserResponseDto> getUsers() {
        int defaultPage = 1;
        int defaultPerPage = 10;
        return getUsers(defaultPage, defaultPerPage);
    }

    public List<UserResponseDto> getUsers(int page, int perPage) {
        logger.info("Fetching users from the page '{}' with '{}' of users per page", page, perPage);
        setUpRequest();
        response = request.queryParam("page", page).queryParam("per_page", perPage)
            .when()
            .get();
        List<UserResponseDto> users = response.then().extract().jsonPath().getList("", UserResponseDto.class);
        logger.info("Fetched users: {}", users);
        return users;
    }

    public void setUpRequest() {
        logger.info("Setting up request for host '{}'", HOST);
        request = given()
                .basePath(ENDPOINT)
                .baseUri(HOST)
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token);
    }

    public UserResponseDto createUser(UserRequestDto payload) {
        setUpRequest();
        logger.info("Creating user with following data: {}", payload);
        Response response = request.body(payload).post().andReturn();
        logger.info("Created user: {}", response.prettyPrint());
        return response.then().extract().as(UserResponseDto.class);
    }

    public UserResponseDto getUser(String userId) {
        //  set up request if necessary
        setUpRequest();
        request.basePath(ENDPOINT_WITH_ID).pathParam("userId", userId);

        //  store the response for further analysis
        response = request.get().andReturn();

        //  extract the data and return
        UserResponseDto user = response.then().extract().as(UserResponseDto.class);
        logger.info("User with id '{}': {} ", userId, user);
        return user;
    }

    public UserResponseDto updateUser(String userId, UserRequestDto userData) {
        setUpRequest();
        logger.info("Updating user id='{}' with new data: {}", userId, userData);
        request.basePath(ENDPOINT_WITH_ID).pathParam("userId", userId).body(userData);
        response = request.put().andReturn();
        var user = response.as(UserResponseDto.class);
        logger.info("Updated user: {}", user);
        return user;
    }

    public void deleteUser(String userId) {
        setUpRequest();
        logger.info("Deleting user with id {}", userId);
        response = request.basePath(ENDPOINT_WITH_ID).pathParam("userId", userId).delete();
    }

    public void validateResponseAgainstSchema(String userId, String schemaPath) {
        setUpRequest();
        response = request.basePath(ENDPOINT_WITH_ID).pathParam("userId", userId).get().andReturn();
        logger.info("Validating response body against schema in '{}'", schemaPath);
        response.then().body(JsonSchemaValidator.matchesJsonSchemaInClasspath(schemaPath));
    }
}
