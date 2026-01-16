package utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;

public class JsonUtil {

    private static JsonNode jsonData;

    static {
        try {
            ObjectMapper mapper = new ObjectMapper();
            jsonData = mapper.readTree(
                    new File("src/test/resources/testdata/userData.json")
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getValue(String section, String userType, String key) {
        return jsonData
                .get(section)
                .get(userType)
                .get(key)
                .asText();
    }
}
