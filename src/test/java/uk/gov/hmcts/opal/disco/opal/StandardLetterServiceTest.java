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
import uk.gov.hmcts.opal.dto.search.StandardLetterSearchDto;
import uk.gov.hmcts.opal.entity.StandardLetterEntity;
import uk.gov.hmcts.opal.repository.StandardLetterRepository;

import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StandardLetterServiceTest {

    @Mock
    private StandardLetterRepository standardLetterRepository;

    @InjectMocks
    private StandardLetterService standardLetterService;

    @Test
    void testGetStandardLetter() {
        // Arrange

        StandardLetterEntity standardLetterEntity = StandardLetterEntity.builder().build();
        when(standardLetterRepository.getReferenceById(any())).thenReturn(standardLetterEntity);

        // Act
        StandardLetterEntity result = standardLetterService.getStandardLetter(1);

        // Assert
        assertNotNull(result);

    }

    @SuppressWarnings("unchecked")
    @Test
    void testSearchStandardLetters() {
        // Arrange
        SpecificationFluentQuery sfq = Mockito.mock(SpecificationFluentQuery.class);

        StandardLetterEntity standardLetterEntity = StandardLetterEntity.builder().build();
        Page<StandardLetterEntity> mockPage = new PageImpl<>(List.of(standardLetterEntity), Pageable.unpaged(), 999L);
        when(standardLetterRepository.findBy(any(Specification.class), any())).thenAnswer(iom -> {
            iom.getArgument(1, Function.class).apply(sfq);
            return mockPage;
        });

        // Act
        List<StandardLetterEntity> result = standardLetterService.searchStandardLetters(StandardLetterSearchDto
                                                                                            .builder().build());

        // Assert
        assertEquals(List.of(standardLetterEntity), result);

    }


}
