package uk.gov.hmcts.reform.dev;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import software.xdev.spring.data.eclipse.store.repository.config.EnableEclipseStoreRepositories;

@SpringBootApplication
@SuppressWarnings("HideUtilityClassConstructor") // Spring needs a constructor, its not a utility class
@EnableEclipseStoreRepositories // Spring adaptor for the Eclipse Store implementation of TaskStore
public class Application {

    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
