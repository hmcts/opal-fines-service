package uk.gov.hmcts.opal.controllers.develop;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.opal.dto.search.ConfigurationItemSearchDto;
import uk.gov.hmcts.opal.entity.ConfigurationItemLite;
import uk.gov.hmcts.opal.service.opal.ConfigurationItemService;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@ContextConfiguration(classes = ConfigurationItemController.class)
@ActiveProfiles({"integration"})
class ConfigurationItemControllerIntegrationTest {

    private static final String URL_BASE = "/dev/configuration-items/";

    @Autowired
    MockMvc mockMvc;

    @MockBean
    @Qualifier("configurationItemServiceProxy")
    ConfigurationItemService configurationItemService;

    @Test
    void testGetConfigurationItemById() throws Exception {
        ConfigurationItemLite configurationItemEntity = createConfigurationItemEntity();

        when(configurationItemService.getConfigurationItem(1L)).thenReturn(configurationItemEntity);

        mockMvc.perform(get(URL_BASE + "1"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.configurationItemId").value(1))
            .andExpect(jsonPath("$.itemName").value("Config Name"))
            .andExpect(jsonPath("$.businessUnitId").value(3))
            .andExpect(jsonPath("$.itemValue").value("Config Value"));
    }


    @Test
    void testGetConfigurationItemById_WhenConfigurationItemDoesNotExist() throws Exception {
        when(configurationItemService.getConfigurationItem(2L)).thenReturn(null);

        mockMvc.perform(get(URL_BASE + "2"))
            .andExpect(status().isNotFound());
    }

    @Test
    void testPostConfigurationItemsSearch() throws Exception {
        ConfigurationItemLite configurationItemEntity = createConfigurationItemEntity();

        when(configurationItemService.searchConfigurationItems(any(ConfigurationItemSearchDto.class)))
            .thenReturn(singletonList(configurationItemEntity));

        mockMvc.perform(post(URL_BASE + "search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"criteria\":\"value\"}"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].configurationItemId").value(1))
            .andExpect(jsonPath("$[0].itemName").value("Config Name"))
            .andExpect(jsonPath("$[0].businessUnitId").value(3))
            .andExpect(jsonPath("$[0].itemValue").value("Config Value"));
    }

    @Test
    void testPostConfigurationItemsSearch_WhenConfigurationItemDoesNotExist() throws Exception {
        mockMvc.perform(post(URL_BASE + "search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"criteria\":\"2\"}"))
            .andExpect(status().isOk());
    }

    private ConfigurationItemLite createConfigurationItemEntity() {
        return ConfigurationItemLite.builder()
            .configurationItemId(1L)
            .itemName("Config Name")
            .businessUnitId((short)3)
            .itemValue("Config Value")
            .itemValues(List.of("Config V1", "Config V2"))
            .build();
    }
}
