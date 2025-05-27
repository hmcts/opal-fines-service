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
import uk.gov.hmcts.opal.dto.search.CourtSearchDto;
import uk.gov.hmcts.opal.entity.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.court.CourtEntity;
import uk.gov.hmcts.opal.dto.reference.CourtReferenceData;
import uk.gov.hmcts.opal.mapper.CourtMapper;
import uk.gov.hmcts.opal.repository.CourtRepository;

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
        FluentQuery.FetchableFluentQuery ffq = Mockito.mock(FluentQuery.FetchableFluentQuery.class);
        when(ffq.sortBy(any())).thenReturn(ffq);

        CourtEntity courtEntity = CourtEntity.builder().build();
        Page<CourtEntity> mockPage = new PageImpl<>(List.of(courtEntity), Pageable.unpaged(), 999L);
        when(courtRepository.findBy(any(Specification.class), any())).thenAnswer(iom -> {
            iom.getArgument(1, Function.class).apply(ffq);
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
        FluentQuery.FetchableFluentQuery ffq = Mockito.mock(FluentQuery.FetchableFluentQuery.class);
        when(ffq.sortBy(any())).thenReturn(ffq);

        CourtEntity courtEntity = CourtEntity.builder()
            .businessUnit(BusinessUnitEntity.builder().businessUnitId((short)007).build()).build();
        Page<CourtEntity> mockPage = new PageImpl<>(List.of(courtEntity), Pageable.unpaged(), 999L);
        when(courtRepository.findBy(any(Specification.class), any())).thenAnswer(iom -> {
            iom.getArgument(1, Function.class).apply(ffq);
            return mockPage;
        });

        // Act
        List<CourtReferenceData> result = courtService.getReferenceData(Optional.empty(), Optional.empty());

        CourtReferenceData refData =  new CourtReferenceData(
            courtEntity.getCourtId(),
            courtEntity.getBusinessUnit().getBusinessUnitId(),
            courtEntity.getCourtCode(),
            courtEntity.getName(),
            courtEntity.getNameCy(),
            courtEntity.getNationalCourtCode()
        );


        when(courtMapper.toRefData(courtEntity)).thenReturn(refData);

        List<CourtReferenceData> res = courtService.getReferenceData(Optional.empty(), Optional.empty());


        // Assert
        assertEquals(List.of(refData), res);

    }
}
