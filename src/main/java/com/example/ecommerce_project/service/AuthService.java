package com.example.ecommerce_project.service;

import com.example.ecommerce_project.constants.Role;
import com.example.ecommerce_project.dto.AuthRequest;
import com.example.ecommerce_project.dto.AuthResponse;
import com.example.ecommerce_project.dto.ChangePasswordRequest;
import com.example.ecommerce_project.exception.UserAlreadyExistsException;
import com.example.ecommerce_project.model.User;
import com.example.ecommerce_project.repository.UserRepository;
import com.example.ecommerce_project.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final JavaMailSender mailSender;
    private final MfaService mfaService;

    public AuthResponse register(AuthRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("მომხმარებელი ამ ელ. ფოსტით უკვე არსებობს!");
        }

        var user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .mfaEnabled(false)
                .build();

        userRepository.save(user);
        var jwtToken = jwtService.generateToken(user);
        return new AuthResponse(jwtToken);
    }

    @Transactional
    public AuthResponse login(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("მომხმარებელი ვერ მოიძებნა!"));

        boolean isMfaActive = Boolean.TRUE.equals(user.getMfaEnabled());

        if (isMfaActive || user.getRole() == Role.ADMIN) {

            if (!isMfaActive) {
                String secret = user.getMfaSecret();
                if (secret == null || secret.isEmpty()) {
                    secret = mfaService.generateSecretKey();
                    user.setMfaSecret(secret);
                    userRepository.save(user);
                }

                String qrCodeUri = mfaService.generateQrCodeImageUri(secret, user.getEmail());

                return AuthResponse.builder()
                        .token("")
                        .mfaRequired(true)
                        .isSetup(true)
                        .qrCodeUri(qrCodeUri)
                        .secret(secret)
                        .message("დაასკანერეთ QR კოდი Authenticator-ით!")
                        .build();
            }

            return AuthResponse.builder()
                    .token("")
                    .mfaRequired(true)
                    .isSetup(false)
                    .message("შეიყვანეთ Authenticator-ის 6-ნიშნა კოდი!")
                    .build();
        }

        var jwtToken = jwtService.generateToken(user);
        return new AuthResponse(jwtToken, false, "ავტორიზაცია წარმატებულია!");
    }

    @Transactional
    public AuthResponse verify2fa(String email, String code) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("მომხმარებელი ვერ მოიძებნა!"));

        if (user.getMfaSecret() == null) {
            throw new IllegalArgumentException("MFA Secret ვერ მოიძებნა!");
        }

        boolean isValid = mfaService.isCodeValid(user.getMfaSecret(), code);

        if (!isValid) {
            throw new IllegalArgumentException("არასწორი ან ვადაგასული 2FA კოდი!");
        }

        if (!Boolean.TRUE.equals(user.getMfaEnabled())) {
            user.setMfaEnabled(true);
            userRepository.save(user);
        }

        var jwtToken = jwtService.generateToken(user);
        return new AuthResponse(jwtToken, false, "ავტორიზაცია წარმატებულია!");
    }

    @Transactional
    public Map<String, String> setup2fa(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("მომხმარებელი ვერ მოიძებნა!"));

        String secret = mfaService.generateSecretKey();
        user.setMfaSecret(secret);
        userRepository.save(user);

        String qrCodeUri = mfaService.generateQrCodeImageUri(secret, user.getEmail());
        return Map.of("qrCodeUri", qrCodeUri, "secret", secret);
    }

    @Transactional
    public void verifyAndEnable2fa(String email, String code) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("მომხმარებელი ვერ მოიძებნა!"));

        if (!mfaService.isCodeValid(user.getMfaSecret(), code)) {
            throw new IllegalArgumentException("არასწორი 2FA კოდი!");
        }

        user.setMfaEnabled(true);
        userRepository.save(user);
    }

    public void processForgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("მომხმარებელი ამ მეილით ვერ მოიძებნა: " + email));

        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);

        String resetLink = "http://localhost:4200/reset-password?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("პაროლის აღდგენა - E-Shop");
        message.setText("გამარჯობა, პაროლის აღდგენისთვის გადადით მოცემულ ლინკზე (ვადა 15 წუთი):\n" + resetLink);

        mailSender.send(message);
    }

    public void updatePassword(String token, String newPassword) {
        User user = userRepository.findByResetToken(token)
                .orElseThrow(() -> new IllegalArgumentException("არავალიდური ან ვადაგასული ტოკენი!"));

        if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("ტოკენს ვადა გაუვიდა!");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);
    }

    public void changePassword(String email, ChangePasswordRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("მომხმარებელი ვერ მოიძებნა!"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("მიმდინარე პაროლი არასწორია!");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}