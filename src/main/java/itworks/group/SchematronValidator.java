package itworks.group;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
public class SchematronValidator {

    static final Logger logger = LoggerFactory.getLogger(SpringApplication.class);
    public static void main(String[] args) {
        SpringApplication.run(SchematronValidator.class, args);
        logger.debug("Starting application in debug with next args: {}", String.join(",", args));
        logger.info("Starting application with {} args", args.length);
    }
}

