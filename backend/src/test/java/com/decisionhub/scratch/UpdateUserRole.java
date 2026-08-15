package com.decisionhub.scratch;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class UpdateUserRole {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/decision_hub";
        String user = "postgres";
        String[] passwords = {"postgres", "password", "root", "", "admin"};
        
        for (String pwd : passwords) {
            try {
                Class.forName("org.postgresql.Driver");
                Connection conn = DriverManager.getConnection(url, user, pwd);
                
                // Update kavya@gmail.com to MODERATOR role
                String sql = "UPDATE users SET role = 'MODERATOR' WHERE email = 'kavya@gmail.com'";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                int rows = pstmt.executeUpdate();
                
                System.out.println("\n=== DATABASE UPDATE SUCCESSFUL (Password: '" + pwd + "') ===");
                System.out.println("Updated " + rows + " user(s) to MODERATOR role.");
                conn.close();
                break;
            } catch (Exception e) {
                // Try next password
            }
        }
    }
}
