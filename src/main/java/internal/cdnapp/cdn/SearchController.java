package internal.cdnapp.cdn;


import internal.cdnapp.cdn.components.RedisService;
import internal.cdnapp.cdn.entity.CdnUrl;
import internal.cdnapp.cdn.repository.CdnUrlRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
public class SearchController {

    private final CdnUrlRepository cdnUrlRepository;
    private final RedisService redisService;

    public SearchController(CdnUrlRepository cdnUrlRepository, RedisService redisService) {
        this.cdnUrlRepository = cdnUrlRepository;
        this.redisService = redisService;

    }

    @PostMapping("/photo/url")
    public ResponseEntity<String> getPhotoUrl(@RequestParam String name) {
        LocalDateTime expireDate;

        if(redisService.get(name) != null) {
            return ResponseEntity.status(HttpStatus.OK).body(redisService.get(name));
        }
        Optional<CdnUrl> cdnUrl = cdnUrlRepository.findByName(name);

        if (cdnUrl.isPresent()) {
            expireDate = cdnUrl.get().getExpireDateTime();

            if (expireDate != null && expireDate.isBefore(LocalDateTime.now())) {
                redisService.del(name);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("That content expired");
            }

            redisService.set(name, cdnUrl.get().getUrl());
            return ResponseEntity.ok(cdnUrl.get().getUrl());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Failed to find that content");
        }
    }
}
