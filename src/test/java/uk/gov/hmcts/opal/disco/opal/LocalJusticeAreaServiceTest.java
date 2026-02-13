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
import uk.gov.hmcts.opal.dto.search.LocalJusticeAreaSearchDto;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaEntity;
import uk.gov.hmcts.opal.dto.reference.LjaReferenceData;
import uk.gov.hmcts.opal.repository.LocalJusticeAreaRepository;
import uk.gov.hmcts.opal.service.opal.LocalJusticeAreaService;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocalJusticeAreaServiceTest {

    @Mock
    private LocalJusticeAreaRepository localJusticeAreaRepository;

    @InjectMocks
    private LocalJusticeAreaService localJusticeAreaService;

    @Test
    void testGetLocalJusticeArea() {
        // Arrange

        LocalJusticeAreaEntity localJusticeAreaEntity = LocalJusticeAreaEntity.builder().build();
        when(localJusticeAreaRepository.findById(any())).thenReturn(Optional.of(localJusticeAreaEntity));

        // Act
        LocalJusticeAreaEntity result = localJusticeAreaService.getLocalJusticeAreaById((short)1);

        // Assert
        assertNotNull(result);

    }

    @SuppressWarnings("unchecked")
    @Test
    void testSearchLocalJusticeAreas() {
        // Arrange
        SpecificationFluentQuery sfq = Mockito.mock(SpecificationFluentQuery.class);
        when(sfq.sortBy(any())).thenReturn(sfq);

        LocalJusticeAreaEntity localJusticeAreaEntity = LocalJusticeAreaEntity.builder().build();
        Page<LocalJusticeAreaEntity> mockPage = new PageImpl<>(List.of(localJusticeAreaEntity),
                                                               Pageable.unpaged(), 999L);
        when(localJusticeAreaRepository.findBy(any(Specification.class), any())).thenAnswer(iom -> {
            iom.getArgument(1, Function.class).apply(sfq);
            return mockPage;
        });

        // Act
        List<LocalJusticeAreaEntity> result = localJusticeAreaService
            .searchLocalJusticeAreas(LocalJusticeAreaSearchDto.builder().build());

        // Assert
        assertEquals(List.of(localJusticeAreaEntity), result);

    }

    @SuppressWarnings("unchecked")
    @Test
    void testLocalJusticeAreasReferenceData() {
        // Arrange
        SpecificationFluentQuery sfq = Mockito.mock(SpecificationFluentQuery.class);
        when(sfq.as(any())).thenReturn(sfq);
        when(sfq.sortBy(any())).thenReturn(sfq);

        LocalJusticeAreaEntity localJAEntity = LocalJusticeAreaEntity.builder().build();
        Page<LocalJusticeAreaEntity> mockPage = new PageImpl<>(List.of(localJAEntity), Pageable.unpaged(), 999L);
        when(localJusticeAreaRepository.findBy(any(Specification.class), any())).thenAnswer(iom -> {
            iom.getArgument(1, Function.class).apply(sfq);
            return mockPage;
        });

        // Act
        List<LjaReferenceData> result = localJusticeAreaService.getReferenceData(
            Optional.empty(), Optional.empty());

        // Assert
        assertEquals(List.of(localJAEntity), result);

    }

}
