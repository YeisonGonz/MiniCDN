package internal.cdnapp.cdn.repository;

import internal.cdnapp.cdn.entity.CdnUrl;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CdnUrlRepository extends JpaRepository<CdnUrl, UUID> {
    Optional<CdnUrl> findByName(String name);
}