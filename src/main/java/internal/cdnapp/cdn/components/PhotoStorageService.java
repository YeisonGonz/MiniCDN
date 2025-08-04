package internal.cdnapp.cdn.components;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class PhotoStorageService {

    private final Path rootLocation;

    public PhotoStorageService(@Value("${cdnapp.storage.path}") String storagePath) {
        this.rootLocation = Paths.get(storagePath);
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo crear el directorio de carga", e);
        }
    }

    public Path getRootLocation() {
        return rootLocation;
    }
}
