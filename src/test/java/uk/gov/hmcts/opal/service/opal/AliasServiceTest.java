package uk.gov.hmcts.opal.service.opal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.query.FluentQuery;
import uk.gov.hmcts.opal.dto.search.AliasSearchDto;
import uk.gov.hmcts.opal.entity.AliasEntity;
import uk.gov.hmcts.opal.repository.AliasRepository;

import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AliasServiceTest {

    @Mock
    private AliasRepository aliasRepository;

    @InjectMocks
    private AliasService aliasService;

    @Test
    void testGetAlias() {
        // Arrange

        AliasEntity aliasEntity = AliasEntity.builder().build();
        when(aliasRepository.getReferenceById(any())).thenReturn(aliasEntity);

        // Act
        AliasEntity result = aliasService.getAlias(1);

        // Assert
        assertNotNull(result);

    }

    @SuppressWarnings("unchecked")
    @Test
    void testSearchAliass() {
        // Arrange
        FluentQuery.FetchableFluentQuery ffq = Mockito.mock(FluentQuery.FetchableFluentQuery.class);

        AliasEntity aliasEntity = AliasEntity.builder().build();
        Page<AliasEntity> mockPage = new PageImpl<>(List.of(aliasEntity), Pageable.unpaged(), 999L);
        when(aliasRepository.findBy(any(Specification.class), any())).thenAnswer(iom -> {
            iom.getArgument(1, Function.class).apply(ffq);
            return mockPage;
        });

        // Act
        List<AliasEntity> result = aliasService.searchAliass(AliasSearchDto.builder().build());

        // Assert
        assertEquals(List.of(aliasEntity), result);

    }


}
