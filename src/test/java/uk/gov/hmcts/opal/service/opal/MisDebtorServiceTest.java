package uk.gov.hmcts.opal.service.opal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import uk.gov.hmcts.opal.dto.search.MisDebtorSearchDto;
import uk.gov.hmcts.opal.entity.MisDebtorEntity;
import uk.gov.hmcts.opal.repository.MisDebtorRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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

    @Test
    void testSearchMisDebtors() {
        // Arrange

        MisDebtorEntity misDebtorEntity = MisDebtorEntity.builder().build();
        Page<MisDebtorEntity> mockPage = new PageImpl<>(List.of(misDebtorEntity), Pageable.unpaged(), 999L);
        // when(misDebtorRepository.findBy(any(Specification.class), any())).thenReturn(mockPage);

        // Act
        List<MisDebtorEntity> result = misDebtorService.searchMisDebtors(MisDebtorSearchDto.builder().build());

        // Assert
        assertNull(result);

    }


}
