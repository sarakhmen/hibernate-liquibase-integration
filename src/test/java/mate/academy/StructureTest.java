package mate.academy;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.yaml.snakeyaml.Yaml;

public class StructureTest {
    public static final String RESOURCES_FOLDER = "src/main/resources";
    private static Map<String, String> hibernateAttributes;
    private static Map<String, String> liquibaseAttributes;

    @BeforeAll
    public static void initTest() {
        hibernateAttributes = parseHibernateConfig(getFileByPath(RESOURCES_FOLDER
                + "/hibernate.cfg.xml"));
        liquibaseAttributes = parseLiquibaseProperties(getFileByPath(RESOURCES_FOLDER
                + "/liquibase.properties"));
    }

    @Test
    public void structure_liquibasePropertiesCorrespondsHibernateConfig_ok() {
        Assertions.assertEquals(hibernateAttributes.get("connection.url"),
                liquibaseAttributes.get("url"),
                "Your hibernate and liquibase connection url differs.");
        Assertions.assertEquals(hibernateAttributes.get("connection.username"),
                liquibaseAttributes.get("username"),
                "Your hibernate and liquibase username url differs.");
        Assertions.assertEquals(hibernateAttributes.get("connection.password"),
                liquibaseAttributes.get("password"),
                "Your hibernate and liquibase password url differs.");
    }

    @Test
    public void structure_userAndRoleModelMappingsExist_ok() {
        Assertions.assertTrue(hibernateAttributes.containsKey("mate.academy.model.User"),
                "Your \"hibernate.cfg.xml\" file doesn't contain mappings for "
                        + "\"mate.academy.model.User\" model.");
        Assertions.assertTrue(hibernateAttributes.containsKey("mate.academy.model.Role"),
                "Your \"hibernate.cfg.xml\" file doesn't contain mappings for "
                        + "\"mate.academy.model.Role\" model.");
    }

    @Test
    public void structure_hibernateValidateOptionIsUsed_ok() {
        Assertions.assertEquals("validate", hibernateAttributes.get("hbm2ddl.auto"),
                "You should specify \"validate\" value for \"hbm2ddl.auto\" property.");
    }

    @Test
    public void structure_changelogMasterUsesYamlChangesets_ok() {
        String changeLogFileAttribute = liquibaseAttributes.get("changeLogFile");
        if (StringUtils.isEmpty(changeLogFileAttribute)) {
            Assertions.fail("You should specify \"changeLogFile\" property within your "
                    + "\"liquibase.properties\" file");
        }
        validateFileFormatIsYaml(changeLogFileAttribute);
        File changeLogFile = getFileByPath(changeLogFileAttribute.startsWith(RESOURCES_FOLDER)
                ? changeLogFileAttribute : RESOURCES_FOLDER + changeLogFileAttribute);
        findIncludedChangelogFiles(changeLogFile)
                .forEach(this::validateFileFormatIsYaml);
    }

    private static File getFileByPath(String filePath) {
        File file = new File(filePath);
        if (!file.isFile()) {
            Assertions.fail("You should create a following file: " + filePath);
        }
        return file;
    }

    private static Map<String, String> parseHibernateConfig(File hibernateConfig) {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        HashMap<String, String> configAttributes = new HashMap<>();
        try {
            DocumentBuilder documentBuilder = dbFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(hibernateConfig);
            Node sessionFactory = document.getDocumentElement()
                    .getElementsByTagName("session-factory").item(0);
            NodeList attributeNodes = sessionFactory.getChildNodes();
            for (int i = 0; i < attributeNodes.getLength(); i++) {
                Node attributeNode = attributeNodes.item(i);
                if (attributeNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element attributeElement = (Element) attributeNode;
                    String attributeName = null;
                    String attributeValue = null;
                    if (attributeElement.getNodeName().equals("property")) {
                        attributeName = attributeElement.getAttribute("name");
                        attributeValue = attributeElement.getTextContent();
                    } else if (attributeElement.getNodeName().equals("mapping")) {
                        attributeName = attributeElement.getAttribute("class");
                    }
                    if (StringUtils.isNotEmpty(attributeName)) {
                        configAttributes.put(attributeName, attributeValue);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return configAttributes;
    }

    private static Map<String, String> parseLiquibaseProperties(File liquibasePropertiesFile) {
        Properties properties = PropertiesUtil.loadProperties(liquibasePropertiesFile);
        return (Map<String, String>) (Map) properties;
    }

    private void validateFileFormatIsYaml(String filePath) {
        if (!StringUtils.endsWithAny(filePath, ".yaml", ".yml")) {
            Assertions.fail("You should specify your " + filePath
                    + " using database-agnostic file format (.yaml/.yml)");
        }
    }

    private List<String> findIncludedChangelogFiles(File changelogFile) {
        return filterIncludedFiles(readYamlChangelog(changelogFile));
    }

    private Map<String, Object> readYamlChangelog(File changelogFile) {
        Yaml yaml = new Yaml();
        try (InputStream inputStream = new FileInputStream(changelogFile)) {
            return yaml.load(inputStream);
        } catch (Exception e) {
            throw new RuntimeException("Error while parsing changelog file: "
                    + changelogFile.getName(), e);
        }
    }

    private List<String> filterIncludedFiles(Map<String, Object> yamlChangelog) {
        return Stream.ofNullable(yamlChangelog.get("databaseChangeLog"))
                .filter(List.class::isInstance)
                .flatMap(attr -> ((List) attr).stream())
                .filter(Map.class::isInstance)
                .map(attr -> ((Map) attr).get("include"))
                .filter(Map.class::isInstance)
                .map(attributesMap -> ((Map<String, Object>) attributesMap).get("file"))
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .toList();
    }
}
