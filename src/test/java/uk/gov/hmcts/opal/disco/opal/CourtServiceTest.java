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
import uk.gov.hmcts.opal.dto.search.CourtSearchDto;
import uk.gov.hmcts.opal.entity.court.CourtEntity;
import uk.gov.hmcts.opal.dto.reference.CourtReferenceData;
import uk.gov.hmcts.opal.mapper.CourtMapper;
import uk.gov.hmcts.opal.repository.CourtRepository;
import uk.gov.hmcts.opal.service.opal.CourtService;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourtServiceTest {

    @Mock
    private CourtRepository courtRepository;

    @Mock
    CourtMapper courtMapper;

    @InjectMocks
    private CourtService courtService;

    @Test
    void testGetCourt() {
        // Arrange
        CourtEntity courtEntity = CourtEntity.builder().build();
        when(courtRepository.findById(any())).thenReturn(Optional.of(courtEntity));

        // Act
        CourtEntity result = courtService.getCourtById(1);

        // Assert
        assertNotNull(result);
    }

    @SuppressWarnings("unchecked")
    @Test
    void testSearchCourts() {
        // Arrange
        SpecificationFluentQuery sfq = Mockito.mock(SpecificationFluentQuery.class);
        when(sfq.sortBy(any())).thenReturn(sfq);

        CourtEntity courtEntity = CourtEntity.builder().build();
        Page<CourtEntity> mockPage = new PageImpl<>(List.of(courtEntity), Pageable.unpaged(), 999L);
        when(courtRepository.findBy(any(Specification.class), any())).thenAnswer(iom -> {
            iom.getArgument(1, Function.class).apply(sfq);
            return mockPage;
        });

        // Act
        List<CourtEntity> result = courtService.searchCourts(CourtSearchDto.builder().build());

        // Assert
        assertEquals(List.of(courtEntity), result);
    }

    @SuppressWarnings("unchecked")
    @Test
    void testCourtsReferenceData() {
        // Arrange
        SpecificationFluentQuery sfq = Mockito.mock(SpecificationFluentQuery.class);
        when(sfq.sortBy(any())).thenReturn(sfq);

        // Create court entity with direct field values (no relationships)
        CourtEntity courtEntity = CourtEntity.builder()
            .courtId(1L)
            .businessUnitId((short) 73)
            .courtCode((short) 101)
            .name("Test Court")
            .nameCy("Test Court Cy")
            .localJusticeAreaId((short) 2577)
            .courtType("MC")
            .division("01")
            .build();

        Page<CourtEntity> mockPage = new PageImpl<>(List.of(courtEntity), Pageable.unpaged(), 1L);

        // Create expected reference data
        CourtReferenceData refData = new CourtReferenceData(
            1L,                    // courtId
            (short) 73,            // businessUnitId
            (short) 101,           // courtCode
            "Test Court",          // name
            "MC",                  // courtType
            (short) 2577,          // localJusticeAreaId (lja)
            "01"                   // division
        );

        // Set up mocks
        when(courtRepository.findBy(any(Specification.class), any())).thenAnswer(iom -> {
            Function<SpecificationFluentQuery, Page<CourtEntity>> function = iom.getArgument(1);
            return function.apply(sfq);
        }).thenReturn(mockPage);

        when(sfq.page(any())).thenReturn(mockPage);
        when(courtMapper.toRefData(courtEntity)).thenReturn(refData);

        // Act
        List<CourtReferenceData> result = courtService.getReferenceData(Optional.empty(), Optional.empty());

        // Assert
        assertEquals(List.of(refData), result);
    }
}
