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
import org.springframework.data.jpa.repository.JpaSpecificationExecutor.SpecificationFluentQuery;
import uk.gov.hmcts.opal.dto.search.AmendmentSearchDto;
import uk.gov.hmcts.opal.entity.AmendmentEntity;
import uk.gov.hmcts.opal.repository.AmendmentRepository;

import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AmendmentServiceTest {

    @Mock
    private AmendmentRepository amendmentRepository;

    @InjectMocks
    private AmendmentService amendmentService;

    @Test
    void testGetAmendment() {
        // Arrange

        AmendmentEntity amendmentEntity = AmendmentEntity.builder().build();
        when(amendmentRepository.getReferenceById(any())).thenReturn(amendmentEntity);

        // Act
        AmendmentEntity result = amendmentService.getAmendment(1);

        // Assert
        assertNotNull(result);

    }

    @SuppressWarnings("unchecked")
    @Test
    void testSearchAmendments() {
        // Arrange
        SpecificationFluentQuery sfq = Mockito.mock(SpecificationFluentQuery.class);

        AmendmentEntity amendmentEntity = AmendmentEntity.builder().build();
        Page<AmendmentEntity> mockPage = new PageImpl<>(List.of(amendmentEntity), Pageable.unpaged(), 999L);
        when(amendmentRepository.findBy(any(Specification.class), any())).thenAnswer(iom -> {
            iom.getArgument(1, Function.class).apply(sfq);
            return mockPage;
        });

        // Act
        List<AmendmentEntity> result = amendmentService.searchAmendments(AmendmentSearchDto.builder().build());

        // Assert
        assertEquals(List.of(amendmentEntity), result);

    }


}
