package internal.cdnapp.cdn.repository;

import internal.cdnapp.cdn.entity.CdnUrl;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CdnUrlRepository extends JpaRepository<CdnUrl, String> {}