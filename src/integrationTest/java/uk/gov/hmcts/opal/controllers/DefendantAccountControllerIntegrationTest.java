package uk.gov.hmcts.opal.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.dto.AccountEnquiryDto;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class DefendantAccountControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    DefendantAccountEntity requestEntity = new DefendantAccountEntity();

    @BeforeEach
    public void setUp() {

        requestEntity.setBusinessUnitId(Short.valueOf("1"));
        requestEntity.setAccountNumber("1212");
    }

    @Test
    public void testPutDefendantAccount_Success() throws Exception {

        // Act and Assert
        mockMvc.perform(MockMvcRequestBuilders
                            .put("/api/defendant-account")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestEntity)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(jsonPath("$.accountNumber", is("1212")));
    }

    @Test
    public void testGetDefendantAccount_Success() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders
                            .put("/api/defendant-account")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestEntity)));

        // Arrange
        AccountEnquiryDto request = AccountEnquiryDto.builder().businessUnitId(Short.valueOf("1"))
            .accountNumber("1212").build();

        // Act and Assert
        mockMvc.perform(MockMvcRequestBuilders
                            .get("/api/defendant-account")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(jsonPath("$.accountNumber", is("1212")));
    }

    @Test
    public void testGetDefendantAccount_NoContent() throws Exception {
        // Arrange
        AccountEnquiryDto request = AccountEnquiryDto.builder().build();

        // Act and Assert
        mockMvc.perform(MockMvcRequestBuilders
                            .get("/api/defendant-account")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
            .andExpect(MockMvcResultMatchers.status().isNoContent());
    }
}

