package com.decisionhub.service.impl.authentication;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.decisionhub.config.JwtService;
import com.decisionhub.dto.request.authentication.ForgotPasswordRequest;
import com.decisionhub.dto.request.authentication.LoginRequest;
import com.decisionhub.dto.request.authentication.RegisterRequest;
import com.decisionhub.dto.request.authentication.ResetPasswordRequest;
import com.decisionhub.dto.response.authentication.LoginResponse;
import com.decisionhub.dto.response.authentication.ProfileResponse;
import com.decisionhub.dto.response.authentication.RegisterResponse;
import com.decisionhub.dto.response.authentication.ResetPasswordResponse;
import com.decisionhub.entity.authentication.PasswordResetToken;
import com.decisionhub.entity.authentication.User;
import com.decisionhub.enums.authentication.PlatformRole;
import com.decisionhub.enums.authentication.UserStatus;
import com.decisionhub.repository.authentication.PasswordResetTokenRepository;
import com.decisionhub.repository.authentication.UserRepository;
import com.decisionhub.service.interfaces.authentication.AuthService;

import com.decisionhub.exception.ResourceAlreadyExistsException; 
import com.decisionhub.exception.ResourceNotFoundException;
import com.decisionhub.exception.BadRequestException;
import com.decisionhub.exception.UnauthorizedActionException;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CustomUserDetailsService customUserDetailsService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final AuthenticationManager authenticationManager;

    public AuthServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            CustomUserDetailsService customUserDetailsService,
            PasswordResetTokenRepository passwordResetTokenRepository,
            AuthenticationManager authenticationManager
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.customUserDetailsService = customUserDetailsService;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.authenticationManager = authenticationManager;
    }

    // SHA-256 Hashing method for reset tokens
    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            // Fixed: Throwing IllegalStateException instead of a generic RuntimeException
            throw new IllegalStateException("Failed to hash reset token", e);
        }
    }

    @Override
    public RegisterResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResourceAlreadyExistsException("Email already exists");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ResourceAlreadyExistsException("Username already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(PlatformRole.USER);
        user.setStatus(UserStatus.ACTIVE);

        userRepository.save(user);

        return new RegisterResponse("User registered successfully");
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        
        // 1. Authenticate user (this internally hits loadUserByUsername)
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // 2. Fetch our custom User entity strictly to get the ID for our JWT extra claims
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // 3. Fixed: Extract UserDetails directly from the successful Authentication object
        // This avoids calling customUserDetailsService.loadUserByUsername() a second time.
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        
        // Pass ID and Role into the JWT Claims
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("id", user.getId());
        extraClaims.put("role", user.getRole().name());

        String jwtToken = jwtService.generateToken(extraClaims, userDetails);

        return new LoginResponse(jwtToken);
    }

    @Override
    public ProfileResponse getProfile() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedActionException("User is not authenticated");
        }

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return new ProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.getStatus()
        );
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {

        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            passwordResetTokenRepository.deleteByUser(user);

            String token = UUID.randomUUID().toString();
            PasswordResetToken passwordResetToken = new PasswordResetToken();
            
            // Hash the token before saving
            passwordResetToken.setTokenHash(hashToken(token)); 
            passwordResetToken.setUser(user);
            passwordResetToken.setExpiresAt(LocalDateTime.now().plus(30, ChronoUnit.MINUTES));

            passwordResetTokenRepository.save(passwordResetToken);
            
            //TODO: Replace this with actual Email Service in production.
            // MOCKING EMAIL DELIVERY FOR DEMO PURPOSES:
            System.out.println("\n=================================================");
            System.out.println("RAW RESET TOKEN FOR POSTMAN: " + token);
            System.out.println("=================================================\n");
            
            // TODO: Integrate Email Service here to send the raw UUID string `token` to the user
        }
    }

    @Override
    @Transactional
    public ResetPasswordResponse resetPassword(ResetPasswordRequest request) {

        // Hash the incoming raw token to lookup the stored hash in the database
        PasswordResetToken passwordResetToken = passwordResetTokenRepository
                .findByTokenHash(hashToken(request.getToken()))
                .orElseThrow(() -> new ResourceNotFoundException("Invalid reset token"));

        if (passwordResetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Reset token has expired");
        }

        User user = passwordResetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        
        userRepository.save(user);
        passwordResetTokenRepository.delete(passwordResetToken);

        return new ResetPasswordResponse("Password reset successfully");
    }
}