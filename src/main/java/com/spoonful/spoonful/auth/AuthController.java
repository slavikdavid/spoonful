package com.spoonful.spoonful.auth;

import com.spoonful.spoonful.user.User;
import com.spoonful.spoonful.user.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final EmailVerificationService emailVerification;

    public record RegisterRequest(String email, String password, String displayName) {}
    public record LoginRequest(String email, String password) {}

    @PostMapping("/register")
    public Map<String,Object> register(@Valid @RequestBody RegisterRequest req){
        users.findByEmail(req.email()).ifPresent(u -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Email in use");
        });
        User u = new User();
        u.setEmail(req.email());
        u.setDisplayName(req.displayName());
        u.setPasswordHash(encoder.encode(req.password()));
        u.setEmailVerified(false);
        users.save(u);

        emailVerification.sendVerification(u);
        return Map.of("status", "ok", "message", "Check your email to confirm registration.");
    }

    @GetMapping("/verify")
    public Map<String,Object> verify(@RequestParam String token){
        try {
            emailVerification.verify(token);
            return Map.of("status","ok","message","Email verified. You can now log in.");
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.GONE, e.getMessage()); // already used/expired
        }
    }

    @PostMapping("/login")
    public Map<String,Object> login(@RequestBody LoginRequest req){
        User u = users.findByEmail(req.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Bad credentials"));
        if (!encoder.matches(req.password(), u.getPasswordHash()))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Bad credentials");
        if (!u.isEmailVerified())
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Please verify your email.");

        return Map.of("token", jwt.issueToken(u.getId(), u.getEmail()));
    }
}
