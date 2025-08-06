package internal.cdnapp.cdn;

import internal.cdnapp.cdn.components.DomainService;
import internal.cdnapp.cdn.components.PhotoStorageService;
import internal.cdnapp.cdn.components.RedisService;
import internal.cdnapp.cdn.entity.CdnUrl;
import internal.cdnapp.cdn.repository.CdnUrlRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.UrlResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@RestController
public class PhotoController {

    private final PhotoStorageService photoStorageService;
    private final DomainService domainService;
    private final CdnUrlRepository cdnUrlRepository;
    private final RedisService redisService;

    public PhotoController(PhotoStorageService photoStorageService,
                           DomainService domainService,
                           CdnUrlRepository cdnUrlRepository,
                           RedisService redisService) {
        this.photoStorageService = photoStorageService;
        this.domainService = domainService;
        this.cdnUrlRepository = cdnUrlRepository;
        this.redisService = redisService;
    }

    // curl -F "file=@./meow.webp" http://localhost:8080/photo Comando para subir la foto para pruebas
    @PostMapping("/photo")
    public ResponseEntity<String> addPhoto(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "expireAt", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime expireAt
    ) throws IOException {
        try {
            UUID uuid = UUID.randomUUID();
            String fileName = file.getOriginalFilename();

            // Creo el directorio con el UUID
            Path uuidDirectory = photoStorageService.getRootLocation().resolve(uuid.toString());
            Files.createDirectories(uuidDirectory);

            // Guardo el archivo en el directorio
            Path destinationFile = uuidDirectory.resolve(fileName);
            Files.copy(file.getInputStream(),destinationFile, StandardCopyOption.REPLACE_EXISTING);

            String publicUrl = domainService.getDomain() + "/photo/" + uuid;

            CdnUrl cdnUrl = new CdnUrl();
            cdnUrl.setUuid(uuid);
            cdnUrl.setName(fileName);
            cdnUrl.setUrl(publicUrl);
            cdnUrl.setFilePath(uuid.toString()+"/"+fileName);
            cdnUrl.setUpDate(LocalDateTime.now());

            if (expireAt != null) {
                cdnUrl.setExpireDateTime(expireAt);
            }

            cdnUrlRepository.save(cdnUrl);

            return ResponseEntity.ok(publicUrl);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Upload image failed" + e.getMessage());
        }
    }

    @GetMapping("/photo/{uuid}")
    public ResponseEntity<Resource> serveFile(@PathVariable UUID uuid) {
        String fileName;
        LocalDateTime expireDateTime;
        Optional<CdnUrl> cdnUrl = cdnUrlRepository.findById(uuid);

        if (cdnUrl.isPresent()) {
            expireDateTime = cdnUrl.get().getExpireDateTime();
            fileName = cdnUrl.get().getFilePath();
        }else{
            return ResponseEntity.notFound().build();
        }

        if (expireDateTime != null && expireDateTime.isBefore(LocalDateTime.now())) {
            redisService.del(uuid.toString());
            return ResponseEntity.status(HttpStatus.LOCKED).body(null);
        }

      try{
          Path file =  this.photoStorageService.getRootLocation().resolve(fileName);
          Resource resource = new UrlResource(file.toUri());

          if (resource.exists()) {
              // Especificar como obtener el media type para decirle al navegador que es una foto
              MediaType mediaType = MediaTypeFactory.getMediaType(resource.getFilename()).orElse(MediaType.APPLICATION_OCTET_STREAM);
              return ResponseEntity.ok().contentType(mediaType).body(resource);
          }else {
              return ResponseEntity.notFound().build();
          }

      } catch (MalformedURLException e) {
          throw new RuntimeException(e);
      }
    }
}
