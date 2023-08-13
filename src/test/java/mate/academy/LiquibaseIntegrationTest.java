package mate.academy;

import java.sql.DriverManager;
import java.util.Optional;
import java.util.Properties;
import liquibase.Liquibase;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import mate.academy.model.Role;
import mate.academy.model.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class LiquibaseIntegrationTest {
    private static SessionFactory sessionFactory;

    @BeforeAll
    public static void initTest() {
        initLiquibase();
        initHibernate();
    }

    @Test
    public void liquibaseIntegration_userWithUserRoleAdded_ok() {
        String email = "user@example.com";
        User actual = findUserByEmail(email).orElse(null);
        Assertions.assertNotNull(actual,
                "User with email = " + email + " not found.");
        Assertions.assertTrue(actual.getRoles()
                        .stream()
                        .anyMatch(role -> role.getRoleName() == Role.RoleName.USER),
                "User " + email + " has no " + Role.RoleName.USER + " roles assigned.");
    }

    @Test
    public void liquibaseIntegration_adminWithAdminRoleAdded_ok() {
        String email = "admin@example.com";
        User actual = findUserByEmail(email).orElse(null);
        Assertions.assertNotNull(actual,
                "User with email = " + email + " not found.");
        Assertions.assertTrue(actual.getRoles()
                        .stream()
                        .anyMatch(role -> role.getRoleName() == Role.RoleName.ADMIN),
                "User " + email + " has no " + Role.RoleName.ADMIN + " roles assigned.");
    }

    private static void initLiquibase() {
        Properties properties = PropertiesUtil.loadProperties(
                "src/main/resources/liquibase.properties");
        String changeLogFile = properties.getProperty("changeLogFile");
        String jdbcUrl = "jdbc:hsqldb:mem:test";
        String username = "sa";
        String password = "";
        try (Liquibase liquibase = new Liquibase(changeLogFile, new ClassLoaderResourceAccessor(),
                DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(
                        DriverManager.getConnection(jdbcUrl, username, password))))) {
            liquibase.update();
            System.out.println("Liquibase migrations completed successfully.");
        } catch (Exception e) {
            throw new RuntimeException("Error while executing liquibase.update().", e);
        }
    }

    private static void initHibernate() {
        try {
            sessionFactory = new Configuration().configure().buildSessionFactory();
        } catch (Exception e) {
            throw new RuntimeException("Error creating SessionFactory.", e);
        }
    }

    private Optional<User> findUserByEmail(String email) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("FROM User u "
                            + "LEFT JOIN FETCH u.roles "
                            + "WHERE u.email = :email", User.class)
                    .setParameter("email", email)
                    .uniqueResultOptional();
        } catch (Exception e) {
            throw new RuntimeException("Can't find the user by email: " + email, e);
        }
    }
}
