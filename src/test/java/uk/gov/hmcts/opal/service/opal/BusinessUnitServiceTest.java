package uk.gov.hmcts.opal.service.opal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import uk.gov.hmcts.opal.dto.search.BusinessUnitSearchDto;
import uk.gov.hmcts.opal.entity.BusinessUnitEntity;
import uk.gov.hmcts.opal.repository.BusinessUnitRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BusinessUnitServiceTest {

    @Mock
    private BusinessUnitRepository businessUnitRepository;

    @InjectMocks
    private BusinessUnitService businessUnitService;

    @Test
    void testGetBusinessUnit() {
        // Arrange

        BusinessUnitEntity businessUnitEntity = BusinessUnitEntity.builder().build();
        when(businessUnitRepository.getReferenceById(any())).thenReturn(businessUnitEntity);

        // Act
        BusinessUnitEntity result = businessUnitService.getBusinessUnit(1);

        // Assert
        assertNotNull(result);

    }

    @Test
    void testSearchBusinessUnits() {
        // Arrange

        BusinessUnitEntity businessUnitEntity = BusinessUnitEntity.builder().build();
        Page<BusinessUnitEntity> mockPage = new PageImpl<>(List.of(businessUnitEntity), Pageable.unpaged(), 999L);
        // when(businessUnitRepository.findBy(any(Specification.class), any())).thenReturn(mockPage);

        // Act
        List<BusinessUnitEntity> result = businessUnitService
            .searchBusinessUnits(BusinessUnitSearchDto.builder().build());

        // Assert
        assertNull(result);

    }


}
