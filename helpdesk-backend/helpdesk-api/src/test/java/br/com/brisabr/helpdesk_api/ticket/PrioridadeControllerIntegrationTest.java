package br.com.brisabr.helpdesk_api.ticket;

import br.com.brisabr.helpdesk_api.dto.PrioridadeCreateDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de integração para PrioridadeController.
 *
 * Testa os endpoints REST com Spring Security habilitado.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
class PrioridadeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "USER")
    void shouldListAllPrioridades() throws Exception {
        mockMvc.perform(get("/api/prioridades"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldCreatePrioridadeAsAdmin() throws Exception {
        PrioridadeCreateDTO dto = new PrioridadeCreateDTO("Urgente");

        mockMvc.perform(post("/api/prioridades")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.nome").value("Urgente"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldForbidCreatePrioridadeAsUser() throws Exception {
        PrioridadeCreateDTO dto = new PrioridadeCreateDTO("Baixa");

        mockMvc.perform(post("/api/prioridades")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldRejectInvalidPrioridade() throws Exception {
        PrioridadeCreateDTO dto = new PrioridadeCreateDTO(""); // Nome vazio

        mockMvc.perform(post("/api/prioridades")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldRejectPrioridadeNomeTooLong() throws Exception {
        // Nome com mais de 50 caracteres
        String nomeLongo = "A".repeat(51);
        PrioridadeCreateDTO dto = new PrioridadeCreateDTO(nomeLongo);

        mockMvc.perform(post("/api/prioridades")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }
}
