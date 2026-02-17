package uk.gov.hmcts.reform.dev.openapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import uk.co.frankz.hmcts.dts.spring.Application;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, classes = Application.class)
class OpenAPIPublisherTest {

    @Value("${TEST_URL:http://localhost:4000}")
    private String testUrl;

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldGenerateValidOpenApiSpec() throws Exception {
        String openApiUrl = testUrl + "/v3/api-docs";

        Resource resource = resourceLoader.getResource(openApiUrl);

        assertThat(resource.exists()).isTrue();

        JsonNode json = objectMapper.readTree(resource.getInputStream());

        // Basic sanity checks
        assertThat(json.get("openapi").asText()).startsWith("3.");
        assertThat(json.get("paths").size()).isGreaterThan(0);
        assertThat(json.get("info").get("title").asText()).isNotBlank();

        // See it again: gradle integration --rerun --tests uk.gov.hmcts.reform.dev.openapi.OpenAPIPublisherTest --info
        String pretty = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
        System.out.println(pretty);
    }
}
