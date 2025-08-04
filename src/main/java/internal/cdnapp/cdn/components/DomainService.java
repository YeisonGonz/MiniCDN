package internal.cdnapp.cdn.components;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DomainService {
    private final String domain;

    public DomainService(@Value("${cdnapp.domain}") String domain) {
        this.domain = domain;
    }

    public String getDomain() {
        return domain;
    }
}
