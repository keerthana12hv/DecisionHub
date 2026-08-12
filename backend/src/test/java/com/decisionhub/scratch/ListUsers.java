package com.decisionhub.scratch;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class ListUsers {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/decision_hub";
        String user = "postgres";
        String[] passwords = {"postgres", "password", "root", "", "admin"};
        
        for (String pwd : passwords) {
            try {
                Class.forName("org.postgresql.Driver");
                Connection conn = DriverManager.getConnection(url, user, pwd);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT username, email, role, status FROM users");
                
                System.out.println("\n=== USERS IN DATABASE (Password: '" + pwd + "') ===");
                while (rs.next()) {
                    System.out.printf("Username: %s | Email: %s | Role: %s | Status: %s%n",
                            rs.getString("username"),
                            rs.getString("email"),
                            rs.getString("role"),
                            rs.getString("status"));
                }
                conn.close();
                break;
            } catch (Exception e) {
                // Try next password
            }
        }
    }
}
