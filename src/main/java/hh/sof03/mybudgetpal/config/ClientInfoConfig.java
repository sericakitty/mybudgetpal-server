package hh.sof03.mybudgetpal.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import io.github.cdimascio.dotenv.Dotenv;

@Component
public class ClientInfoConfig {

    @Autowired
    private Dotenv dotenv;

    /**
     * Determine base url based on client type
     * 
     * @param request
     * @return String
     */
    public String determineBaseUrl(HttpServletRequest request) {
        String originHeader = request.getHeader("Origin");
        String clientTypeHeader = request.getHeader("X-Client-Type");

        String baseUrl;
        if ("mobile".equals(clientTypeHeader)) {
            baseUrl = dotenv.get("MOBILE_CORS_ORIGIN");
        } else if (originHeader != null) {
            baseUrl = dotenv.get("WEB_CORS_ORIGIN");
        } else {
            baseUrl = dotenv.get("WEB_CORS_ORIGIN"); // Default to web origin
        }

        return baseUrl;
    }

    /**
     * Determine client type based on request header
     * 
     * @param request
     * @return String
     */
    public String determineClientType(HttpServletRequest request) {
        String clientTypeHeader = request.getHeader("X-Client-Type");
        if ("mobile".equals(clientTypeHeader)) {
            return "mobile";
        } else {
            return "web";
        }
    }
}
