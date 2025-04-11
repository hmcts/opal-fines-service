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
import uk.gov.hmcts.opal.dto.search.OffenceSearchDto;
import uk.gov.hmcts.opal.entity.offence.OffenceEntity;
import uk.gov.hmcts.opal.entity.projection.OffenceReferenceData;
import uk.gov.hmcts.opal.entity.projection.OffenceSearchData;
import uk.gov.hmcts.opal.repository.OffenceRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OffenceServiceTest {

    @Mock
    private OffenceRepository offenceRepository;

    @InjectMocks
    private OffenceService offenceService;

    @Test
    void testGetOffence() {
        // Arrange

        OffenceEntity.Lite offenceEntity = OffenceEntity.Lite.builder().build();
        when(offenceRepository.findById(any())).thenReturn(Optional.of(offenceEntity));

        // Act
        OffenceEntity result = offenceService.getOffenceById(1);

        // Assert
        assertNotNull(result);

    }

    @SuppressWarnings("unchecked")
    @Test
    void testSearchOffences() {
        // Arrange
        FluentQuery.FetchableFluentQuery ffq = Mockito.mock(FluentQuery.FetchableFluentQuery.class);
        when(ffq.sortBy(any())).thenReturn(ffq);
        when(ffq.limit(anyInt())).thenReturn(ffq);

        OffenceEntity offenceEntity = OffenceEntity.Lite.builder().build();
        Page<OffenceEntity> mockPage = new PageImpl<>(List.of(offenceEntity), Pageable.unpaged(), 999L);
        when(offenceRepository.findBy(any(Specification.class), any())).thenAnswer(iom -> {
            iom.getArgument(1, Function.class).apply(ffq);
            return mockPage;
        });

        // Act
        List<OffenceSearchData> result = offenceService.searchOffences(OffenceSearchDto.builder().build());
        OffenceSearchData expected = new OffenceSearchData(null, null, null, null, null,
                                                           null, null, null);
        // Assert
        assertEquals(List.of(expected), result);

    }

    @SuppressWarnings("unchecked")
    @Test
    void testOffencesReferenceData() {
        // Arrange
        FluentQuery.FetchableFluentQuery ffq = Mockito.mock(FluentQuery.FetchableFluentQuery.class);
        when(ffq.sortBy(any())).thenReturn(ffq);

        OffenceEntity offenceEntity = OffenceEntity.Lite.builder()
            .offenceId(1L)
            .cjsCode("NINE")
            .offenceTitle("Theft from a Palace")
            .dateUsedFrom(LocalDateTime.of(1909,3,3,3,30))
            .offenceOas("A")
            .offenceOasCy("B")
            .build();
        Page<OffenceEntity> mockPage = new PageImpl<>(List.of(offenceEntity), Pageable.unpaged(), 999L);
        when(offenceRepository.findBy(any(Specification.class), any())).thenAnswer(iom -> {
            iom.getArgument(1, Function.class).apply(ffq);
            return mockPage;
        });

        // Act
        List<OffenceReferenceData> result = offenceService.getReferenceData(Optional.empty(), Optional.empty());

        OffenceReferenceData refData =
            new OffenceReferenceData(1L, "NINE", null, "Theft from a Palace",
                                     null, LocalDateTime.of(1909, 3, 3, 3, 30),
                                     null, "A", "B"
            );
        // Assert
        assertEquals(List.of(refData), result);

    }




}
