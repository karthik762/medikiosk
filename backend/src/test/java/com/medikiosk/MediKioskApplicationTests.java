package com.medikiosk;

import com.medikiosk.repository.SystemMetadataRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class MediKioskApplicationTests {

    @Autowired
    private SystemMetadataRepository systemMetadataRepository;

    @Test
    void contextLoads() {
        // Verify Spring context successfully initializes and Flyway migration executed
        assertThat(systemMetadataRepository).isNotNull();
        assertThat(systemMetadataRepository.count()).isGreaterThanOrEqualTo(1);
        assertThat(systemMetadataRepository.findById("schema_version")).isPresent();
    }
}
