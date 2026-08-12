package com.decisionhub.scratch;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import org.springframework.security.crypto.bcrypt.BCrypt;

public class UpdateUserPassword {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/decision_hub";
        String user = "postgres";
        String[] passwords = {"postgres", "password", "root", "", "admin"};
        
        // Hashing "password123" using BCrypt
        String newPasswordHash = BCrypt.hashpw("password123", BCrypt.gensalt());
        
        for (String pwd : passwords) {
            try {
                Class.forName("org.postgresql.Driver");
                Connection conn = DriverManager.getConnection(url, user, pwd);
                
                // Update password for kavya@gmail.com and make sure role is MODERATOR
                String sql = "UPDATE users SET password_hash = ?, role = 'MODERATOR' WHERE email = 'kavya@gmail.com'";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, newPasswordHash);
                int rows = pstmt.executeUpdate();
                
                System.out.println("\n=== PASSWORD RESET SUCCESSFUL (Password: '" + pwd + "') ===");
                System.out.println("Reset password for " + rows + " user(s) to 'password123'.");
                conn.close();
                break;
            } catch (Exception e) {
                // Try next password
            }
        }
    }
}
