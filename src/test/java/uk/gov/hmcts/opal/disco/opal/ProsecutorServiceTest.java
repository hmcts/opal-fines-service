package uk.gov.hmcts.opal.disco.opal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor.SpecificationFluentQuery;
import uk.gov.hmcts.opal.dto.reference.ProsecutorReferenceData;
import uk.gov.hmcts.opal.entity.ProsecutorEntity;
import uk.gov.hmcts.opal.mapper.ProsecutorMapper;
import uk.gov.hmcts.opal.repository.ProsecutorRepository;
import uk.gov.hmcts.opal.service.opal.ProsecutorService;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProsecutorServiceTest {

    @Mock
    private ProsecutorRepository prosecutorRepository;

    @Spy
    private ProsecutorMapper prosecutorMapper = Mappers.getMapper(ProsecutorMapper.class);

    @InjectMocks
    private ProsecutorService prosecutorService;

    @Test
    void testGetProsecutor() {
        // Arrange

        ProsecutorEntity entity = ProsecutorEntity.builder().prosecutorCode("AAAX").build();
        when(prosecutorRepository.findById(any())).thenReturn(Optional.of(entity));

        // Act
        ProsecutorEntity result = prosecutorService.getProsecutorById(1);

        // Assert
        assertEquals(entity, result);

    }

    @SuppressWarnings("unchecked")
    @Test
    void testProsecutorsReferenceData() {
        // Arrange
        SpecificationFluentQuery sfq = Mockito.mock(SpecificationFluentQuery.class);
        when(sfq.sortBy(any())).thenReturn(sfq);

        ProsecutorEntity entity = ProsecutorEntity.builder().prosecutorCode("AAAZ").build();
        Page<ProsecutorEntity> mockPage = new PageImpl<>(List.of(entity), Pageable.unpaged(), 999L);

        when(prosecutorRepository.findBy(any(Specification.class), any())).thenAnswer(iom -> {
            iom.getArgument(1, Function.class).apply(sfq);
            return mockPage;
        });

        ProsecutorReferenceData mappedRefData = ProsecutorReferenceData.builder().prosecutorCode("AAAZ").build();

        // Act
        List<ProsecutorReferenceData> result = prosecutorService.getReferenceData(Optional.empty());

        // Assert
        assertEquals(List.of(mappedRefData), result);

    }
}
