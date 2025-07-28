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
import uk.gov.hmcts.opal.dto.reference.OffenceReferenceData;
import uk.gov.hmcts.opal.dto.reference.OffenceSearchData;
import uk.gov.hmcts.opal.dto.search.OffenceSearchDto;
import uk.gov.hmcts.opal.entity.offence.OffenceEntity;
import uk.gov.hmcts.opal.entity.offence.OffenceEntityFull;
import uk.gov.hmcts.opal.mapper.OffenceMapper;
import uk.gov.hmcts.opal.repository.OffenceRepository;
import uk.gov.hmcts.opal.repository.OffenceRepositoryFull;
import uk.gov.hmcts.opal.service.opal.OffenceService;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
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

    @Mock
    private OffenceRepositoryFull offenceRepositoryFull;

    @Mock
    private OffenceMapper offenceMapper;

    @InjectMocks
    private OffenceService offenceService;

    @Test
    void testGetOffence() {
        // Arrange
        OffenceEntityFull offenceEntity = OffenceEntityFull.builder().build();
        when(offenceRepositoryFull.findById(any())).thenReturn(Optional.of(offenceEntity));
        // Act
        OffenceEntityFull result = offenceService.getOffence((short) 1);
        // Assert
        assertNotNull(result);
    }

    @SuppressWarnings("unchecked")
    @Test
    void testSearchOffences() {
        // Arrange
        SpecificationFluentQuery sfq = Mockito.mock(SpecificationFluentQuery.class);
        when(sfq.sortBy(any())).thenReturn(sfq);
        when(sfq.limit(anyInt())).thenReturn(sfq);

        OffenceEntity offenceEntity = OffenceEntity.builder().build();
        Page<OffenceEntity> mockPage = new PageImpl<>(List.of(offenceEntity), Pageable.unpaged(), 999L);
        when(offenceRepository.findBy(any(Specification.class), any())).thenAnswer(iom -> {
            iom.getArgument(1, Function.class).apply(sfq);
            return mockPage;
        });

        OffenceSearchData mappedSearchData = new OffenceSearchData(null, null, null, null, null, null, null, null);
        when(offenceMapper.toSearchData(offenceEntity)).thenReturn(mappedSearchData);

        // Act
        List<OffenceSearchData> result = offenceService.searchOffences(OffenceSearchDto.builder().build());
        // Assert
        assertEquals(List.of(mappedSearchData), result);
    }

    @SuppressWarnings("unchecked")
    @Test
    void testOffencesReferenceData() {
        // Arrange
        SpecificationFluentQuery sfq = Mockito.mock(SpecificationFluentQuery.class);
        when(sfq.sortBy(any())).thenReturn(sfq);

        OffenceEntity offenceEntity = OffenceEntity.builder()
            .offenceId(1L)
            .cjsCode("NINE")
            .offenceTitle("Theft from a Palace")
            .dateUsedFrom(LocalDateTime.of(1909, 3, 3, 3, 30))
            .offenceOas("A")
            .offenceOasCy("B")
            .build();
        Page<OffenceEntity> mockPage = new PageImpl<>(List.of(offenceEntity), Pageable.unpaged(), 999L);
        when(offenceRepository.findBy(any(Specification.class), any())).thenAnswer(iom -> {
            iom.getArgument(1, Function.class).apply(sfq);
            return mockPage;
        });

        OffenceReferenceData mappedRefData
            = new OffenceReferenceData(1L,
                                       "NINE",
                                       null,
                                       "Theft from a Palace",
                                       null,
                                       LocalDateTime.of(1909, 3, 3, 3, 30).atOffset(ZoneOffset.UTC),
                                       null, "A", "B");
        when(offenceMapper.toRefData(offenceEntity)).thenReturn(mappedRefData);

        // Act
        List<OffenceReferenceData> result = offenceService.getReferenceData(Optional.empty(),
                                                                            Optional.empty(),
                                                                            Optional.empty());
        // Assert
        assertEquals(List.of(mappedRefData), result);
    }
}
