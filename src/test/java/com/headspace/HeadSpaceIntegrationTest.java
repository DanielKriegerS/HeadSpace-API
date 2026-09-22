package com.headspace;

import com.headspace.identity.infrastructure.persistence.repository.RoleSpringDataRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@EnabledIfEnvironmentVariable(named = "DOCKER_HOST", matches = ".*")
@ContextConfiguration(classes = {HeadSpaceApplication.class, TestcontainersConfiguration.class})
class HeadSpaceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RoleSpringDataRepository roleSpringDataRepository;

    @Test
    void applicationStartsAndRunsFlywayWithIdentityRoles() {
        Long count = jdbcTemplate.queryForObject("SELECT count(*) FROM role", Long.class);
        assertThat(count).isEqualTo(3L);
        assertThat(roleSpringDataRepository.count()).isEqualTo(3L);
    }

    @Test
    void meEndpointRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void openApiDocsAreAccessibleInTestProfile() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());
    }
}
