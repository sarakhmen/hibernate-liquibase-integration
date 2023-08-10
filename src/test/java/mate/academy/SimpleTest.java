package mate.academy;


import liquibase.Liquibase;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import mate.academy.model.Role;
import mate.academy.model.User;
import mate.academy.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.DriverManager;
import java.util.Properties;
import java.util.Set;

public class SimpleTest {
    @Test
    public void liquibaseIntegration_properlyConfigured_ok(){
        User user = new User();
        user.setEmail("test@gmail.com");
        user.setPassword("qwerty");
        user.setFirstName("test1");
        user.setLastName("test2");
        Role role = new Role();
        role.setRoleName(Role.RoleName.ADMIN);

        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            user.setRoles(Set.of(role));
            session.persist(role);
            session.persist(user);
            transaction.commit();
            user = session.get(User.class, 1);
            System.out.println(user);
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error while executing persistent operation.", e);
        }
    }

    @BeforeAll
    public static void initTest() {
        Properties properties = loadProperties("liquibase.properties");
        String jdbcUrl = properties.getProperty("url");
        String username = properties.getProperty("username");
        String password = properties.getProperty("password");
        String changeLogFile = properties.getProperty("changeLogFile");
        try (Liquibase liquibase = new Liquibase(changeLogFile, new ClassLoaderResourceAccessor(),
                DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(
                        DriverManager.getConnection(jdbcUrl, username, password))))) {
            liquibase.update("");
            System.out.println("Liquibase migrations completed successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Properties loadProperties(String fileName) {
        Properties properties = new Properties();
        try {
            properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName));
        } catch (Exception e) {
            throw new RuntimeException("Error while reading properties from " + fileName);
        }
        return properties;
    }
}
