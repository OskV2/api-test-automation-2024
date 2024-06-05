package pl.globallogic.gorest;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pl.globallogic.gorest.api.UserApi;
import pl.globallogic.gorest.dto.UserRequestDto;
import pl.globallogic.gorest.dto.UserResponseDto;

import java.util.List;

public class UserManagementSmokeTest {

    private static final String TOKEN = System.getProperty("token");
    private UserApi userApi;
    private static Logger logger = LoggerFactory.getLogger(UserManagementSmokeTest.class);

    @BeforeMethod
    public void testSetUp() {
        userApi = new UserApi(TOKEN);
    }

    @AfterMethod
    public void testCleanUp () {
        logger.info("Put your test clean up");
    }

    @Test
    public void shouldListAllUsersInformationWithPaginationOptions() {
        int user_per_page = 10;
        int target_page = 3;
        List<UserResponseDto> users = userApi.getUsers(target_page, user_per_page);
        Assert.assertNotNull(users.get(0).id());
        //  Assert.assertNotNull(users.getFirst().id());  I DONT KNOW WHY THIS ISN'T WORKING BUT IT JUST ISN'T
    }

    @Test
    public void shouldCreateUserAndReturnId() {
        UserRequestDto payload = getUserDataWithRandomEmail();
        UserResponseDto newUser = userApi.createUser(payload);
        logger.info("ID for new user is: {}", newUser.id());
        Assert.assertNotNull(newUser.id());
    }

    @Test
    public void shouldGetUserInformationById() {
        var user = userApi.createUser(getUserDataWithRandomEmail());
        UserResponseDto userFromGet = userApi.getUser(user.id());
        Assert.assertEquals(user.name(), userFromGet.name());
    }

    @Test
    public void shouldUpdateUserWithNewEmail() {
        var user = userApi.createUser(getUserDataWithRandomEmail());
        UserRequestDto updateUserPayload = getUserDataWithRandomEmail();
        UserResponseDto updateResponse = userApi.updateUser(user.id(), updateUserPayload);
        Assert.assertEquals(updateUserPayload.email(), updateResponse.email());
    }

    @Test
    public void shouldDeleteUserFromSystem() {
        var user = userApi.createUser(getUserDataWithRandomEmail());
        userApi.deleteUser(user.id());
        userApi.getResponse().then().assertThat().statusCode(204);
    }

    @Test
    public void validateUserDataAgainstSchema() {
        var user = userApi.createUser(getUserDataWithRandomEmail());
        userApi.validateResponseAgainstSchema(user.id(), "user_schema.json");
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