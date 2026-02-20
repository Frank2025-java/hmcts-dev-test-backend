package uk.co.frankz.hmcts.dts.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import static java.util.Arrays.stream;

@Configuration
public class GlobalCorsConfig {

    private static final String[] ALLOWED_ORIGINS = {
        "http://localhost:3100",
        "https://localhost:3100"};

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.setAllowCredentials(true);

        stream(ALLOWED_ORIGINS).forEach(frontendOrigin -> config.addAllowedOrigin(frontendOrigin));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}
