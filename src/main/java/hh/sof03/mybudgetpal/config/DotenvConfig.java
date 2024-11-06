package hh.sof03.mybudgetpal.config;

import io.github.cdimascio.dotenv.Dotenv;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.beans.factory.annotation.Autowired;

@Configuration
public class DotenvConfig {

    @Autowired
    private Environment env;

    /**
     * Load dotenv based on active profile
     * 
     * @return Dotenv
     */
    @Bean
    public Dotenv dotenv() {
        String activeProfile = env.getActiveProfiles().length > 0 ? env.getActiveProfiles()[0] : "development";
        Dotenv dotenv;

        switch (activeProfile) {
            case "production":
                dotenv = Dotenv.configure()
                               .filename(".env.production")
                               .load();
                break;
            default:
                dotenv = Dotenv.configure()
                               .filename(".env.development")
                               .load();
                break;
        }

        return dotenv;
    }
}
