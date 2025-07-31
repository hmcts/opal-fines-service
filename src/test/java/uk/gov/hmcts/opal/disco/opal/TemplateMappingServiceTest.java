package uk.gov.hmcts.opal.disco.opal;

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
import org.springframework.data.jpa.repository.JpaSpecificationExecutor.SpecificationFluentQuery;
import uk.gov.hmcts.opal.dto.search.TemplateMappingSearchDto;
import uk.gov.hmcts.opal.entity.TemplateMappingEntity;
import uk.gov.hmcts.opal.repository.TemplateMappingRepository;

import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TemplateMappingServiceTest {

    @Mock
    private TemplateMappingRepository templateMappingRepository;

    @InjectMocks
    private TemplateMappingService templateMappingService;

    @Test
    void testGetTemplateMapping() {
        // Arrange

        TemplateMappingEntity templateMappingEntity = TemplateMappingEntity.builder().build();
        when(templateMappingRepository
                 .findDistinctByTemplate_TemplateIdAndApplicationFunction_ApplicationFunctionId(anyLong(), anyLong()))
            .thenReturn(templateMappingEntity);

        // Act
        TemplateMappingEntity result = templateMappingService.getTemplateMapping(1L, 1L);

        // Assert
        assertNotNull(result);

    }

    @SuppressWarnings("unchecked")
    @Test
    void testSearchTemplateMappings() {
        // Arrange
        SpecificationFluentQuery sfq = Mockito.mock(SpecificationFluentQuery.class);

        TemplateMappingEntity templateMappingEntity = TemplateMappingEntity.builder().build();
        Page<TemplateMappingEntity> mockPage = new PageImpl<>(List.of(templateMappingEntity),
                                                              Pageable.unpaged(), 999L);
        when(templateMappingRepository.findBy(any(Specification.class), any())).thenAnswer(iom -> {
            iom.getArgument(1, Function.class).apply(sfq);
            return mockPage;
        });

        // Act
        List<TemplateMappingEntity> result = templateMappingService
            .searchTemplateMappings(TemplateMappingSearchDto.builder().build());

        // Assert
        assertEquals(List.of(templateMappingEntity), result);

    }


}
