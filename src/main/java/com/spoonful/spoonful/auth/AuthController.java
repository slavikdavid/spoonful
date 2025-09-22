package com.spoonful.spoonful.auth;

import com.spoonful.spoonful.user.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public record RegisterRequest(String email, String password, String displayName) {}
    public record LoginRequest(String email, String password) {}

    @PostMapping("/register")
    public Map<String,Object> register(@Valid @RequestBody RegisterRequest req){
        users.findByEmail(req.email()).ifPresent(u -> { throw new ResponseStatusException(HttpStatus.CONFLICT,"Email in use"); });
        User u = new User();
        u.setEmail(req.email());
        u.setDisplayName(req.displayName());
        u.setPasswordHash(encoder.encode(req.password()));
        users.save(u);
        return Map.of("token", jwt.issueToken(u.getId(), u.getEmail()));
    }

    @PostMapping("/login")
    public Map<String,Object> login(@RequestBody LoginRequest req){
        User u = users.findByEmail(req.email()).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Bad creds"));
        if (!encoder.matches(req.password(), u.getPasswordHash())) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Bad creds");
        return Map.of("token", jwt.issueToken(u.getId(), u.getEmail()));
    }
}