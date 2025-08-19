package internal.cdnapp.cdn.controllers;

import internal.cdnapp.cdn.components.PhotoStorageService;
import internal.cdnapp.cdn.entity.CdnUrl;
import internal.cdnapp.cdn.repository.CdnUrlRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/prune")
public class PruneController {

    private final CdnUrlRepository cdnUrlRepository;
    private final PhotoStorageService photoStorageService;

    @Autowired
    public PruneController(final CdnUrlRepository cdnUrlRepository, final PhotoStorageService photoStorageService) {
        this.cdnUrlRepository = cdnUrlRepository;
        this.photoStorageService = photoStorageService;
    }

    public boolean deletePhoto( UUID uuid){
        Optional<CdnUrl> cdnUrlOptional = cdnUrlRepository.findById(uuid);

        if (cdnUrlOptional.isEmpty()) {
            return false;
        }

        CdnUrl cdnUrl = cdnUrlOptional.get();
        Path photoFile = photoStorageService.getRootLocation().resolve(cdnUrl.getFilePath());

        try {
            Files.deleteIfExists(photoFile);

            Path dir = photoFile.getParent();
            if (Files.isDirectory(dir) && Files.list(dir).findAny().isEmpty()) {
                Files.delete(dir);
            }
            cdnUrlRepository.deleteById(uuid);
            return true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<String> delete(@PathVariable("uuid") UUID uuid) {
        boolean deleted = deletePhoto(uuid);
        if (deleted) {
            return ResponseEntity.ok("photo deleted successfully");
        } else {
            return ResponseEntity.ok("failed to delete photo");
        }
    }
}
