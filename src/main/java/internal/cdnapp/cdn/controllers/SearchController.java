package internal.cdnapp.cdn.controllers;


import internal.cdnapp.cdn.components.RedisService;
import internal.cdnapp.cdn.entity.CdnUrl;
import internal.cdnapp.cdn.repository.CdnUrlRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
public class SearchController {

    private final CdnUrlRepository cdnUrlRepository;
    private final RedisService redisService;

    public SearchController(CdnUrlRepository cdnUrlRepository, RedisService redisService) {
        this.cdnUrlRepository = cdnUrlRepository;
        this.redisService = redisService;

    }

    @PostMapping("/photo/url")
    public ResponseEntity<Object> getPhotoUrl(@RequestParam String name) {
        LocalDateTime expireDate;
        UUID uuid;

        if(redisService.get(name) != null) {
            return ResponseEntity.status(HttpStatus.OK).body(redisService.get(name));
        }
        Optional<CdnUrl> cdnUrl = cdnUrlRepository.findByName(name);

        if (cdnUrl.isPresent()) {
            expireDate = cdnUrl.get().getExpireDateTime();
            uuid = cdnUrl.get().getUuid();

            if (expireDate != null && expireDate.isBefore(LocalDateTime.now())) {
                redisService.del(name);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("That content expired");
            }

            redisService.set(uuid.toString(), cdnUrl.get().getUrl());
            redisService.set(name, cdnUrl.get().getUrl());
            return ResponseEntity.ok(cdnUrl.get().getUrl());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Failed to find that content");
        }
    }

    @GetMapping("/info/{uuid}")
    public ResponseEntity<Object> getInfo(@PathVariable UUID uuid) {
        Map<String, String> objectInfo = new HashMap<>();
        Optional<CdnUrl> cdnUrl = cdnUrlRepository.findById(uuid);

        if (!cdnUrl.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Failed to find that content");
        }

        objectInfo.put("uuid", uuid.toString());
        objectInfo.put("url", cdnUrl.get().getUrl());

        String fullName = cdnUrl.get().getName();
        String[] parts = fullName.split("\\.");
        String fileType = parts.length > 1 ? parts[1] : "";

        objectInfo.put("fileType", fileType.toUpperCase());
        objectInfo.put("fileName", parts[0]);

        return ResponseEntity.ok(objectInfo);
    }

}
