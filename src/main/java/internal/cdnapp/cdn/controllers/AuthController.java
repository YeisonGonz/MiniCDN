package internal.cdnapp.cdn.controllers;

import internal.cdnapp.cdn.components.UserService;
import internal.cdnapp.cdn.entity.User;
import internal.cdnapp.cdn.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UserService service;
    private final JwtUtil jwtUtil;

    public AuthController(UserService service, JwtUtil jwtUtil) {
        this.service = service;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User user) {
        User registrado = service.registrar(user.getName(), user.getEmail(), user.getPassword());
        return ResponseEntity.ok(registrado);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody User user) {
        User logueado = service.login(user.getEmail(), user.getPassword());
        String token = jwtUtil.generateToken(logueado.getEmail());

        return ResponseEntity.ok(Map.of("token", token));
    }
}
