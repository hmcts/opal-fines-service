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
import uk.gov.hmcts.opal.dto.reference.BusinessUnitReferenceData;
import uk.gov.hmcts.opal.dto.search.BusinessUnitSearchDto;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitFullEntity;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitLiteEntity;
import uk.gov.hmcts.opal.entity.configurationitem.ConfigurationItemEntity;
import uk.gov.hmcts.opal.repository.BusinessUnitLiteRepository;
import uk.gov.hmcts.opal.repository.BusinessUnitRepository;
import uk.gov.hmcts.opal.service.opal.BusinessUnitService;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BusinessUnitServiceTest {

    @Mock
    private BusinessUnitRepository businessUnitRepository;

    @Mock
    private BusinessUnitLiteRepository businessUnitLiteRepository;

    @InjectMocks
    private BusinessUnitService businessUnitService;

    @Test
    void testGetBusinessUnit() {
        // Arrange
        BusinessUnitFullEntity businessUnitEntity = BusinessUnitFullEntity.builder().build();
        when(businessUnitRepository.findById(any())).thenReturn(Optional.of(businessUnitEntity));

        // Act
        BusinessUnitFullEntity result = businessUnitService.getBusinessUnit((short)1);

        // Assert
        assertNotNull(result);
    }

    @SuppressWarnings("unchecked")
    @Test
    void testSearchBusinessUnits() {
        // Arrange
        SpecificationFluentQuery sfq = Mockito.mock(SpecificationFluentQuery.class);

        BusinessUnitFullEntity businessUnitEntity = BusinessUnitFullEntity.builder().build();
        Page<BusinessUnitFullEntity> mockPage = new PageImpl<>(List.of(businessUnitEntity), Pageable.unpaged(), 999L);
        when(businessUnitRepository.findBy(any(Specification.class), any())).thenAnswer(iom -> {
            iom.getArgument(1, Function.class).apply(sfq);
            return mockPage;
        });

        // Act
        List<BusinessUnitFullEntity> result = businessUnitService
            .searchBusinessUnits(BusinessUnitSearchDto.builder().build());

        // Assert
        assertEquals(List.of(businessUnitEntity), result);
    }

    @SuppressWarnings("unchecked")
    @Test
    void testBusinessUnitsReferenceData() {
        // Arrange
        SpecificationFluentQuery sfq = Mockito.mock(SpecificationFluentQuery.class);
        when(sfq.sortBy(any())).thenReturn(sfq);

        // Use BusinessUnitLiteEntity instead of BusinessUnitFullEntity
        BusinessUnitLiteEntity businessUnitEntityLite = BusinessUnitLiteEntity.builder()
            .businessUnitId((short)3)
            .businessUnitName("Big Business Unit")
            .welshLanguage(true)
            .configurationItems(List.of(
                ConfigurationItemEntity.Lite.builder()
                    .itemName("A Config Item")
                    .itemValue("A value")
                    .itemValues(List.of("Item Values One", "Item Values Two"))
                    .build()))
            .build();

        Page<BusinessUnitLiteEntity> mockPage = new PageImpl<>(List.of(businessUnitEntityLite),
                                                               Pageable.unpaged(), 999L);

        // Mock the lite repository instead of the full repository
        when(businessUnitLiteRepository.findBy(any(Specification.class), any())).thenAnswer(iom -> {
            iom.getArgument(1, Function.class).apply(sfq);
            return mockPage;
        });

        // Act
        List<BusinessUnitReferenceData> result = businessUnitService.getReferenceData(Optional.empty());

        // Assert
        assertEquals(List.of(new BusinessUnitReferenceData(
            (short)3, "Big Business Unit", null,
            null, null,
            null, Boolean.TRUE, List.of(new BusinessUnitReferenceData.ConfigItemRefData(
            "A Config Item", "A value", List.of("Item Values One", "Item Values Two"))))), result);
    }
}
