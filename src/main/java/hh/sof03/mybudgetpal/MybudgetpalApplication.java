package hh.sof03.mybudgetpal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;


@SpringBootApplication
public class MybudgetpalApplication {

  private static final Logger log = LoggerFactory.getLogger(MybudgetpalApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(MybudgetpalApplication.class, args);
	}

  @Bean
  public CommandLineRunner demo() {

      return (args) -> {
      };

  }

}
