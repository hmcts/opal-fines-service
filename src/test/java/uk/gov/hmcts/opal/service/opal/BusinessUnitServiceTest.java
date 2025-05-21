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
import uk.gov.hmcts.opal.dto.search.BusinessUnitSearchDto;
import uk.gov.hmcts.opal.entity.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.ConfigurationItemEntity;
import uk.gov.hmcts.opal.entity.projection.BusinessUnitReferenceData;
import uk.gov.hmcts.opal.repository.BusinessUnitRepository;

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

    @InjectMocks
    private BusinessUnitService businessUnitService;

    @Test
    void testGetBusinessUnit() {
        // Arrange

        BusinessUnitEntity businessUnitEntity = BusinessUnitEntity.builder().build();
        when(businessUnitRepository.findById(any())).thenReturn(Optional.of(businessUnitEntity));

        // Act
        BusinessUnitEntity result = businessUnitService.getBusinessUnit((short)1);

        // Assert
        assertNotNull(result);

    }

    @SuppressWarnings("unchecked")
    @Test
    void testSearchBusinessUnits() {
        // Arrange
        FluentQuery.FetchableFluentQuery ffq = Mockito.mock(FluentQuery.FetchableFluentQuery.class);

        BusinessUnitEntity businessUnitEntity = BusinessUnitEntity.builder().build();
        Page<BusinessUnitEntity> mockPage = new PageImpl<>(List.of(businessUnitEntity), Pageable.unpaged(), 999L);
        when(businessUnitRepository.findBy(any(Specification.class), any())).thenAnswer(iom -> {
            iom.getArgument(1, Function.class).apply(ffq);
            return mockPage;
        });

        // Act
        List<BusinessUnitEntity> result = businessUnitService
            .searchBusinessUnits(BusinessUnitSearchDto.builder().build());

        // Assert
        assertEquals(List.of(businessUnitEntity), result);

    }

    @SuppressWarnings("unchecked")
    @Test
    void testBusinessUnitsReferenceData() {
        // Arrange
        FluentQuery.FetchableFluentQuery ffq = Mockito.mock(FluentQuery.FetchableFluentQuery.class);
        when(ffq.sortBy(any())).thenReturn(ffq);

        BusinessUnitEntity businessUnitEntity = BusinessUnitEntity.builder()
            .businessUnitId((short)3)
            .businessUnitName("Big Business Unit")
            .welshLanguage(true)
            .configurationItems(List.of(
                ConfigurationItemEntity.builder()
                    .itemName("A Config Item")
                    .itemValue("A value")
                    .itemValues(List.of("Item Values One", "Item Values Two"))
                    .build()))
            .build();
        Page<BusinessUnitEntity> mockPage = new PageImpl<>(List.of(businessUnitEntity), Pageable.unpaged(), 999L);
        when(businessUnitRepository.findBy(any(Specification.class), any())).thenAnswer(iom -> {
            iom.getArgument(1, Function.class).apply(ffq);
            return mockPage;
        });

        // Act
        List<BusinessUnitReferenceData> result = businessUnitService.getReferenceData(Optional.empty());

        // Assert
        assertEquals(List.of(new BusinessUnitReferenceData(
            (short)3, "Big Business Unit", null, null, null,
            null, Boolean.TRUE, List.of(new BusinessUnitReferenceData.ConfigItemRefData(
                "A Config Item", "A value", List.of("Item Values One", "Item Values Two"))))), result);

    }

}
