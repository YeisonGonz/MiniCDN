package internal.cdnapp.cdn;


import internal.cdnapp.cdn.entity.CdnUrl;
import internal.cdnapp.cdn.repository.CdnUrlRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public class SearchController {

    private final CdnUrlRepository cdnUrlRepository;

    public SearchController(CdnUrlRepository cdnUrlRepository) {
        this.cdnUrlRepository = cdnUrlRepository;
    }

    @GetMapping("/photo/url")
    public ResponseEntity<String> getPhotoUrl(@RequestParam String name) {
        Optional<CdnUrl> cdnUrl = cdnUrlRepository.findByName(name);

        if (cdnUrl.isPresent()) {
            return ResponseEntity.ok(cdnUrl.get().getUrl());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No se encontró una URL para el nombre: " + name);
        }
    }
}
