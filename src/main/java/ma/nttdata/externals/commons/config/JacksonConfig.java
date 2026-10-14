package ma.nttdata.externals.commons.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.time.LocalDate;

/**
 * Global Jackson configuration.
 * Registers {@link NullSafeLocalDateDeserializer} to handle the literal
 * string {@code "null"} that some frontends send for empty date fields.
 *
 * <p>We expose a {@link Primary} {@link ObjectMapper} bean that is built
 * from Spring Boot's fully auto-configured {@link Jackson2ObjectMapperBuilder}
 * (which already includes JavaTimeModule, feature flags, etc.) and then
 * registers our custom module <em>after</em> the builder's modules.
 * In Jackson, the last module to register a deserializer for a given type
 * wins, so this guarantees our {@link NullSafeLocalDateDeserializer}
 * takes precedence over {@code JavaTimeModule}'s {@code LocalDateDeserializer}.</p>
 */
@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper mapper = builder.build();
        SimpleModule module = new SimpleModule("NullSafeLocalDateModule");
        module.addDeserializer(LocalDate.class, new NullSafeLocalDateDeserializer());
        mapper.registerModule(module);
        return mapper;
    }
}
