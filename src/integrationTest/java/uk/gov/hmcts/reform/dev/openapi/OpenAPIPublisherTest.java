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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This test will be run by ${workspace}\.github\workflows\publish-openapi.yaml
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, classes = Application.class)
class OpenAPIPublisherTest {

    private final String testUrl;

    private final String outputFilePath;

    private final ResourceLoader resourceLoader;

    private final ObjectMapper objectMapper;

    @Autowired
    public OpenAPIPublisherTest(
        @Value("${TEST_URL:http://localhost:4000}") String testUrl,
        @Value("${API_DOC_PATH:build/openapi/openapi-spec.json}") String outputFilePath,
        ResourceLoader resourceLoader,
        ObjectMapper objectMapper
    ) {
        this.testUrl = testUrl;
        this.outputFilePath = outputFilePath;
        this.resourceLoader = resourceLoader;
        this.objectMapper = objectMapper;
    }

    @Test
    void shouldGenerateValidOpenApiSpecFollowedByUpload() throws Exception {
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

        // Print to console (still useful locally)
        System.out.println(pretty);

        // Write to a file for GitHub Actions
        Path output = Paths.get(outputFilePath);
        Files.createDirectories(output.getParent());
        Files.writeString(output, pretty);
        System.out.println("Uploaded as " + output);
    }
}
