package utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

public class JsonUtil {

    // make jsonData final to address IDE warning
    private static final JsonNode jsonData;

    static {
        try {
            ObjectMapper mapper = new ObjectMapper();
            jsonData = mapper.readTree(
                    new File("src/test/resources/testdata/userData.json")
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to load JSON test data", e);
        }
    }

    public static String get(String keyPath) {
        String val = getOrDefault(keyPath, null);
        if (val == null) {
            throw new RuntimeException("Missing JSON value for key: " + keyPath);
        }
        return val;
    }

    public static String getOrDefault(String keyPath, String defaultValue) {
        if (keyPath == null || keyPath.isEmpty()) return defaultValue;
        String[] keys = keyPath.split("\\.");
        JsonNode node = jsonData;
        for (String key : keys) {
            if (node == null) return defaultValue;
            node = node.get(key);
        }
        if (node == null || node.isNull()) return defaultValue;
        try {
            return node.asText();
        } catch (Exception e) {
            return defaultValue;
        }
    }
}
