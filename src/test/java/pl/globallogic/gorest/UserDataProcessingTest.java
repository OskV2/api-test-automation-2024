package pl.globallogic.gorest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import pl.globallogic.gorest.dto.UserRequestDto;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class UserDataProcessingTest {

    List<UserRequestDto> users;
    ObjectMapper mapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(UserDataProcessingTest.class);

    @BeforeClass
    public void testSetUp() throws IOException {
        Path source = Path.of("src/test/resources/users.csv");
        users = loadUsersFromFile(source);
    }

    @Test
    public void shouldLoadUserObjectFromCSV() {
        String expectedName = "Ivan Paulouski";
        Assert.assertEquals(expectedName, users.get(0).name());
        //  Assert.assertEquals(expectedName, users.getFirst().name());  I DONT KNOW WHY THIS ISN'T WORKING BUT IT JUST ISN'T
    }

    @Test
    public void shouldSerializeUserObjectIntoJSON() throws JsonProcessingException {
        String firstUserAsString = mapper.writeValueAsString(users.get(0));
        logger.info("");
        Assert.assertTrue(firstUserAsString.contains("Ivan"));
    }

    @Test
    public void shouldDeserializeUserObjectIntoJSON() throws JsonProcessingException {
        var user = """
                {"name":"Ivan Paulouski", "email": "ivan.paulouski@gmail.com", "gender": "male", "status": "active"}
                """;
        UserRequestDto firstUser = mapper.readValue(user, UserRequestDto.class);
        logger.info("Deserialized user: {}", firstUser);
        Assert.assertTrue(firstUser.name().contains("Ivan"));
    }

    private List<UserRequestDto> loadUsersFromFile(Path source) throws IOException {
        return Files.readAllLines(source)
                .stream()
                .map(this::parseUser)
                .toList();
    }

    private UserRequestDto parseUser(String rawData) {
        String[] raw = rawData.split(",");
        UserRequestDto result = new UserRequestDto(raw[0], raw[1], raw[2], raw[3]);
        return result;
    }

    private String randomiseEmail() {
        String[] emailTokens = randomiseEmail().split("@");
        String bigLebowskiPersona = Faker.instance().lebowski().character();
        String randomPart = RandomStringUtils.randomAlphanumeric(4) + "@";
        return bigLebowskiPersona + randomPart + emailTokens[1];
    }
}
