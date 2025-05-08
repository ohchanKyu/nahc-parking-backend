package kr.ac.dankook.parkingApplication.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {

    private static final String DEVELOP_FRONT_ADDRESS = "http://localhost:5173";
    private static final String PROD_FRONT_ADDRESS = "https://www.nahc-parking.o-r.kr";

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOriginPattern(DEVELOP_FRONT_ADDRESS);
        config.addAllowedOriginPattern(PROD_FRONT_ADDRESS);
        config.addAllowedHeader("*");
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE","PATCH","OPTIONS"));
        source.registerCorsConfiguration("/**",config);
        return source;
    }
}
