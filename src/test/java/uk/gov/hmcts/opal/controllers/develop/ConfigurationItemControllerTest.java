package uk.gov.hmcts.opal.controllers.develop;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.dto.search.ConfigurationItemSearchDto;
import uk.gov.hmcts.opal.entity.ConfigurationItemEntity;
import uk.gov.hmcts.opal.service.opal.ConfigurationItemService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfigurationItemControllerTest {

    @Mock
    private ConfigurationItemService configurationItemService;

    @InjectMocks
    private ConfigurationItemController configurationItemController;

    @Test
    void testGetConfigurationItem_Success() {
        // Arrange
        ConfigurationItemEntity entity = ConfigurationItemEntity.builder().build();

        when(configurationItemService.getConfigurationItem(any(Long.class))).thenReturn(entity);

        // Act
        ResponseEntity<ConfigurationItemEntity> response = configurationItemController.getConfigurationItemById(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(entity, response.getBody());
        verify(configurationItemService, times(1)).getConfigurationItem(any(Long.class));
    }

    @Test
    void testSearchConfigurationItems_Success() {
        // Arrange
        ConfigurationItemEntity entity = ConfigurationItemEntity.builder().build();
        List<ConfigurationItemEntity> configurationItemList = List.of(entity);

        when(configurationItemService.searchConfigurationItems(any())).thenReturn(configurationItemList);

        // Act
        ConfigurationItemSearchDto searchDto = ConfigurationItemSearchDto.builder().build();
        ResponseEntity<List<ConfigurationItemEntity>> response = configurationItemController
            .postConfigurationItemsSearch(searchDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(configurationItemList, response.getBody());
        verify(configurationItemService, times(1)).searchConfigurationItems(any());
    }

}
