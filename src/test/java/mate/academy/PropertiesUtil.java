package mate.academy;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

public class PropertiesUtil {
    public static Properties loadProperties(String fileName) {
        return loadProperties(new File(fileName));
    }

    public static Properties loadProperties(File file) {
        Properties properties = new Properties();
        try (FileInputStream inputStream = new FileInputStream(file)) {
            properties.load(inputStream);
        } catch (Exception e) {
            throw new RuntimeException("Error while reading properties from " + file.getName());
        }
        return properties;
    }
}
