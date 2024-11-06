package hh.sof03.mybudgetpal.config;

import io.github.cdimascio.dotenv.Dotenv;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {

    /**
     * Configure JavaMailSender
     * 
     * @param dotenv
     * @return JavaMailSender
     */
    @Bean
    public JavaMailSender getJavaMailSender(Dotenv dotenv) {

        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(dotenv.get("SPIRNG_MAIL_HOST"));
        mailSender.setPort(Integer.parseInt(dotenv.get("SPIRNG_MAIL_PORT")));

        mailSender.setUsername(dotenv.get("SPRING_MAIL_EMAIL"));
        mailSender.setPassword(dotenv.get("SPRING_MAIL_APP_PASSWORD"));

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.smtp.auth", dotenv.get("SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH"));
        props.put("mail.smtp.starttls.enable", dotenv.get("SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE"));
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.debug", "true");
        props.put("mail.default-encoding", "UTF-8");

        return mailSender;
    }
}
