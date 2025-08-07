package internal.cdnapp.cdn;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DebugController {

    @Value("${security.api-key}")
    private String apiKey;

    @GetMapping("/debug/apikey")
    public String getApiKey() {
        return "API Key cargada: " + apiKey;
    }
}
