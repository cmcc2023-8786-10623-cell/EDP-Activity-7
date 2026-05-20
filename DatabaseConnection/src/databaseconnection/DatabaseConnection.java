package databaseconnection; // This must match the folder name in NetBeans

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/dentalsync_db";
    private static final String USER = "root";
    private static final String PASS = ""; 

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASS);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver not found: " + e.getMessage());
        }
    }

    // This main method allows you to "Run" the file to test it
    public static void main(String[] args) {
        try {
            Connection conn = getConnection();
            if (conn != null) {
                System.out.println("--- Connection Successful! ---");
                System.out.println("Connected to: dentalsync_db");
            }
        } catch (SQLException e) {
            System.err.println("Connection Failed!");
            e.printStackTrace();
        }
    }
}