package com.decisionhub.service.impl.authentication;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

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


@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CustomUserDetailsService customUserDetailsService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;


    public AuthServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            CustomUserDetailsService customUserDetailsService,
            PasswordResetTokenRepository passwordResetTokenRepository
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.customUserDetailsService = customUserDetailsService;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
    }


    @Override
    public RegisterResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }


        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }


        User user = new User();

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(
                passwordEncoder.encode(request.getPassword())
        );
        user.setRole(PlatformRole.USER);
        user.setStatus(UserStatus.ACTIVE);
        user.setCreatedAt(LocalDateTime.now());

        userRepository.save(user);


        return new RegisterResponse(
                "User registered successfully"
        );
    }


    @Override
    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new RuntimeException("Invalid email or password"));


        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPasswordHash())) {

            throw new RuntimeException("Invalid email or password");
        }


        UserDetails userDetails =
                customUserDetailsService
                        .loadUserByUsername(
                                request.getEmail());


        String jwtToken =
                jwtService.generateToken(userDetails);


        return new LoginResponse(jwtToken);
    }



    @Override
    public ProfileResponse getProfile() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();


        String email = authentication.getName();


        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));


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


        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new RuntimeException("User not found"));



        passwordResetTokenRepository.deleteByUser(user);



        String token = UUID.randomUUID().toString();



        PasswordResetToken passwordResetToken =
                new PasswordResetToken();


        passwordResetToken.setTokenHash(token);
        passwordResetToken.setUser(user);
        passwordResetToken.setExpiresAt(
                LocalDateTime.now().plus(30, ChronoUnit.MINUTES)
        );



        passwordResetTokenRepository.save(passwordResetToken);

    }



    @Override
    @Transactional
    public ResetPasswordResponse resetPassword(
            ResetPasswordRequest request
    ) {


        Optional<PasswordResetToken> tokenOptional =
                passwordResetTokenRepository
                        .findByTokenHash(request.getToken());



        PasswordResetToken passwordResetToken =
                tokenOptional.orElseThrow(
                        () -> new RuntimeException("Invalid reset token")
                );



        if (passwordResetToken.getExpiresAt()
                .isBefore(LocalDateTime.now())) {


            throw new RuntimeException(
                    "Reset token has expired"
            );
        }



        User user = passwordResetToken.getUser();



        user.setPasswordHash(
                passwordEncoder.encode(
                        request.getNewPassword()
                )
        );



        userRepository.save(user);



        passwordResetTokenRepository.delete(
                passwordResetToken
        );



        return new ResetPasswordResponse(
                "Password reset successfully"
        );
    }
}