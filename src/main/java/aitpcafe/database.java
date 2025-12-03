package aitpcafe;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class database {
    
    // غيري البيانات دي حسب إعدادات MySQL عندك
    private static final String DB_URL = "jdbc:mysql://localhost:3306/cafe";
    private static final String DB_USER = "root";  // غيري لو Username مختلف
    private static final String DB_PASSWORD = "";  // حطي الـ Password بتاعك
    
    public static Connection connectDB() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("Database connected successfully!");
            return connection;
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found!");
            e.printStackTrace();
            return null;
        } catch (SQLException e) {
            System.err.println("Connection failed: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}