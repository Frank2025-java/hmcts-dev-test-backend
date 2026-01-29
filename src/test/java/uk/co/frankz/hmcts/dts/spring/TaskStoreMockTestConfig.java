package uk.co.frankz.hmcts.dts.spring;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TaskStoreMockTestConfig {
    @Bean
    public TaskStore keyValueRepository() {
        return  Mockito.mock(TaskStore.class);
    }

    @Bean
    public Mapper mapper() {
        return  new Mapper();
    }


}
