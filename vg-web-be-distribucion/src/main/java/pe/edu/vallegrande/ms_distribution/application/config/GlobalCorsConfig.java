package pe.edu.vallegrande.ms_distribution.application.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class GlobalCorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // Permitir solo los orígenes especificados
        config.setAllowedOrigins(List.of(
                "https://lab.vallegrande.edu.pe",
                "http://localhost:4200",
                "http://127.0.0.1:4200"
        ));

        // Permitir todos los métodos
        config.addAllowedMethod("*");

        // Permitir todos los headers
        config.addAllowedHeader("*");

        // Habilitar credenciales ya que los orígenes están definidos
        config.setAllowCredentials(true);

        config.setMaxAge(3600L); // Cache del preflight por 1h

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }
}
