
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class JdbcConnectionTest {

    public static void main(String[] args) throws Exception {
        String url = System.getenv("DB_URL");
        String username = System.getenv("DB_USERNAME");
        String password = System.getenv("DB_PASSWORD");

        if (url == null || username == null || password == null) {
            throw new IllegalStateException(
                    "Missing DB_URL, DB_USERNAME or DB_PASSWORD"
            );
        }

        System.out.println("Java version: " +
                System.getProperty("java.version"));

        System.out.println("JDBC driver: " +
                DriverManager.getDriver(url).getClass().getName());

        System.out.println("Connecting to Supabase...");

        try (Connection connection =
                     DriverManager.getConnection(url, username, password)) {

            System.out.println("Connection successful!");

            try (Statement statement = connection.createStatement();
                 ResultSet rs = statement.executeQuery(
                         "SELECT version(), current_database(), current_user")) {

                if (rs.next()) {
                    System.out.println("PostgreSQL: " + rs.getString(1));
                    System.out.println("Database: " + rs.getString(2));
                    System.out.println("User: " + rs.getString(3));
                }
            }
        }
    }
}
