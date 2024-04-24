package uk.gov.hmcts.opal.controllers.develop;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.dto.search.AliasSearchDto;
import uk.gov.hmcts.opal.entity.AliasEntity;
import uk.gov.hmcts.opal.service.opal.AliasService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AliasControllerTest {

    @Mock
    private AliasService aliasService;

    @InjectMocks
    private AliasController aliasController;

    @Test
    void testGetAlias_Success() {
        // Arrange
        AliasEntity entity = AliasEntity.builder().build();

        when(aliasService.getAlias(any(Long.class))).thenReturn(entity);

        // Act
        ResponseEntity<AliasEntity> response = aliasController.getAliasById(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(entity, response.getBody());
        verify(aliasService, times(1)).getAlias(any(Long.class));
    }

    @Test
    void testSearchAliass_Success() {
        // Arrange
        AliasEntity entity = AliasEntity.builder().build();
        List<AliasEntity> aliasList = List.of(entity);

        when(aliasService.searchAliass(any())).thenReturn(aliasList);

        // Act
        AliasSearchDto searchDto = AliasSearchDto.builder().build();
        ResponseEntity<List<AliasEntity>> response = aliasController.postAliasesSearch(searchDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(aliasList, response.getBody());
        verify(aliasService, times(1)).searchAliass(any());
    }

}
