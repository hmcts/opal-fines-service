package uk.gov.hmcts.opal.controllers.develop;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.dto.search.SuspenseItemSearchDto;
import uk.gov.hmcts.opal.entity.SuspenseItemEntity;
import uk.gov.hmcts.opal.service.opal.SuspenseItemService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SuspenseItemControllerTest {

    @Mock
    private SuspenseItemService suspenseItemService;

    @InjectMocks
    private SuspenseItemController suspenseItemController;

    @Test
    void testGetSuspenseItem_Success() {
        // Arrange
        SuspenseItemEntity entity = SuspenseItemEntity.builder().build();

        when(suspenseItemService.getSuspenseItem(any(Long.class))).thenReturn(entity);

        // Act
        ResponseEntity<SuspenseItemEntity> response = suspenseItemController.getSuspenseItemById(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(entity, response.getBody());
        verify(suspenseItemService, times(1)).getSuspenseItem(any(Long.class));
    }

    @Test
    void testSearchSuspenseItems_Success() {
        // Arrange
        SuspenseItemEntity entity = SuspenseItemEntity.builder().build();
        List<SuspenseItemEntity> suspenseItemList = List.of(entity);

        when(suspenseItemService.searchSuspenseItems(any())).thenReturn(suspenseItemList);

        // Act
        SuspenseItemSearchDto searchDto = SuspenseItemSearchDto.builder().build();
        ResponseEntity<List<SuspenseItemEntity>> response = suspenseItemController.postSuspenseItemsSearch(searchDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(suspenseItemList, response.getBody());
        verify(suspenseItemService, times(1)).searchSuspenseItems(any());
    }

}
