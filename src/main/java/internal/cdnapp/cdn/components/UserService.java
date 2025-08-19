package internal.cdnapp.cdn.components;

import internal.cdnapp.cdn.entity.User;
import internal.cdnapp.cdn.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository repo;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository repo, PasswordEncoder passwordEncoder) {
        this.repo = repo;
        this.passwordEncoder = passwordEncoder;
    }

    public User registrar(String nombre, String email, String password) {
        if (repo.findByEmail(email).isPresent()) {
            throw new RuntimeException("El email ya está registrado");
        }
        User user = new User(nombre, email, passwordEncoder.encode(password));
        return repo.save(user);
    }

    public User login(String email, String password) {
        User user = repo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Contraseña incorrecta");
        }
        return user;
    }
}
