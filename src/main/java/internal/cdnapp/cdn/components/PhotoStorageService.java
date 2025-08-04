package internal.cdnapp.cdn.components;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class PhotoStorageService {

    private final Path rootLocation;

    public PhotoStorageService(@Value("${cdnapp.storage.path}") String storagePath) {
        this.rootLocation = Paths.get(storagePath);
    }

    public Path getRootLocation() {
        return rootLocation;
    }
}
