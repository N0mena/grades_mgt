package hei.school.minou.service.url;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class UrlService {

    private final String baseUrl;

    public UrlService(@Value("${app.base-url}") String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String buildRedirectUrl(String path) {
        return "redirect:" + baseUrl + path;
    }
}