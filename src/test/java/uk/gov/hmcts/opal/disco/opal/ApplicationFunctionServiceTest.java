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
import uk.gov.hmcts.opal.dto.search.ApplicationFunctionSearchDto;
import uk.gov.hmcts.opal.entity.ApplicationFunctionEntity;
import uk.gov.hmcts.opal.repository.ApplicationFunctionRepository;

import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationFunctionServiceTest {

    @Mock
    private ApplicationFunctionRepository applicationFunctionRepository;

    @InjectMocks
    private ApplicationFunctionService applicationFunctionService;

    @Test
    void testGetApplicationFunction() {
        // Arrange

        ApplicationFunctionEntity applicationFunctionEntity = ApplicationFunctionEntity.builder().build();
        when(applicationFunctionRepository.getReferenceById(any())).thenReturn(applicationFunctionEntity);

        // Act
        ApplicationFunctionEntity result = applicationFunctionService.getApplicationFunction(1);

        // Assert
        assertNotNull(result);

    }

    @SuppressWarnings("unchecked")
    @Test
    void testSearchApplicationFunctions() {
        // Arrange
        SpecificationFluentQuery sfq = Mockito.mock(SpecificationFluentQuery.class);

        ApplicationFunctionEntity applicationFunctionEntity = ApplicationFunctionEntity.builder().build();
        Page<ApplicationFunctionEntity> mockPage = new PageImpl<>(List.of(applicationFunctionEntity),
                                                                  Pageable.unpaged(), 999L);
        when(applicationFunctionRepository.findBy(any(Specification.class), any())).thenAnswer(iom -> {
            iom.getArgument(1, Function.class).apply(sfq);
            return mockPage;
        });

        // Act
        List<ApplicationFunctionEntity> result = applicationFunctionService
            .searchApplicationFunctions(ApplicationFunctionSearchDto.builder().build());

        // Assert
        assertEquals(List.of(applicationFunctionEntity), result);

    }


}
