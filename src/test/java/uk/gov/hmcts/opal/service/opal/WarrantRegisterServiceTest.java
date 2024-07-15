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
import uk.gov.hmcts.opal.dto.search.WarrantRegisterSearchDto;
import uk.gov.hmcts.opal.entity.WarrantRegisterEntity;
import uk.gov.hmcts.opal.repository.WarrantRegisterRepository;

import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WarrantRegisterServiceTest {

    @Mock
    private WarrantRegisterRepository warrantRegisterRepository;

    @InjectMocks
    private WarrantRegisterService warrantRegisterService;

    @Test
    void testGetWarrantRegister() {
        // Arrange

        WarrantRegisterEntity warrantRegisterEntity = WarrantRegisterEntity.builder().build();
        when(warrantRegisterRepository.getReferenceById(any())).thenReturn(warrantRegisterEntity);

        // Act
        WarrantRegisterEntity result = warrantRegisterService.getWarrantRegister(1);

        // Assert
        assertNotNull(result);

    }

    @SuppressWarnings("unchecked")
    @Test
    void testSearchWarrantRegisters() {
        // Arrange
        FluentQuery.FetchableFluentQuery ffq = Mockito.mock(FluentQuery.FetchableFluentQuery.class);

        WarrantRegisterEntity warrantRegisterEntity = WarrantRegisterEntity.builder().build();
        Page<WarrantRegisterEntity> mockPage = new PageImpl<>(List.of(warrantRegisterEntity), Pageable.unpaged(), 999L);
        when(warrantRegisterRepository.findBy(any(Specification.class), any())).thenAnswer(iom -> {
            iom.getArgument(1, Function.class).apply(ffq);
            return mockPage;
        });

        // Act
        List<WarrantRegisterEntity> result = warrantRegisterService.searchWarrantRegisters(WarrantRegisterSearchDto
                                                                                               .builder().build());

        // Assert
        assertEquals(List.of(warrantRegisterEntity), result);

    }


}
