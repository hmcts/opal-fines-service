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
import uk.gov.hmcts.opal.dto.search.MisDebtorSearchDto;
import uk.gov.hmcts.opal.entity.MisDebtorEntity;
import uk.gov.hmcts.opal.repository.MisDebtorRepository;

import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MisDebtorServiceTest {

    @Mock
    private MisDebtorRepository misDebtorRepository;

    @InjectMocks
    private MisDebtorService misDebtorService;

    @Test
    void testGetMisDebtor() {
        // Arrange

        MisDebtorEntity misDebtorEntity = MisDebtorEntity.builder().build();
        when(misDebtorRepository.getReferenceById(any())).thenReturn(misDebtorEntity);

        // Act
        MisDebtorEntity result = misDebtorService.getMisDebtor(1);

        // Assert
        assertNotNull(result);

    }

    @SuppressWarnings("unchecked")
    @Test
    void testSearchMisDebtors() {
        // Arrange
        FluentQuery.FetchableFluentQuery ffq = Mockito.mock(FluentQuery.FetchableFluentQuery.class);

        MisDebtorEntity misDebtorEntity = MisDebtorEntity.builder().build();
        Page<MisDebtorEntity> mockPage = new PageImpl<>(List.of(misDebtorEntity), Pageable.unpaged(), 999L);
        when(misDebtorRepository.findBy(any(Specification.class), any())).thenAnswer(iom -> {
            iom.getArgument(1, Function.class).apply(ffq);
            return mockPage;
        });

        // Act
        List<MisDebtorEntity> result = misDebtorService.searchMisDebtors(MisDebtorSearchDto.builder().build());

        // Assert
        assertEquals(List.of(misDebtorEntity), result);

    }


}
