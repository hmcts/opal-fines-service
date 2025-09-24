package uk.gov.hmcts.opal.service.opal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import uk.gov.hmcts.opal.dto.response.SearchDataResponse;
import uk.gov.hmcts.opal.dto.search.AmendmentSearchDto;
import uk.gov.hmcts.opal.entity.amendment.AmendmentEntity;
import uk.gov.hmcts.opal.entity.amendment.RecordType;
import uk.gov.hmcts.opal.repository.AmendmentRepository;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

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
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

@ExtendWith(MockitoExtension.class)
class AmendmentServiceTest {

    @Mock
    private AmendmentRepository amendmentRepository;

    @InjectMocks
    private AmendmentService service;

    @Test
    void testGetAmendment() {
        // Arrange

        AmendmentEntity amendmentEntity = AmendmentEntity.builder().build();
        when(amendmentRepository.findById(any())).thenReturn(Optional.of(amendmentEntity));

        // Act
        AmendmentEntity result = service.getAmendmentById(1);

        // Assert
        assertNotNull(result);

    }

    @SuppressWarnings("unchecked")
    @Test
    void testSearchAmendments() {
        // Arrange
        JpaSpecificationExecutor.SpecificationFluentQuery sfq =
            Mockito.mock(JpaSpecificationExecutor.SpecificationFluentQuery.class);
        when(sfq.sortBy(any())).thenReturn(sfq);

        AmendmentEntity amendment = AmendmentEntity.builder().build();
        Page<AmendmentEntity> mockPage =
            new PageImpl<>(List.of(amendment), Pageable.unpaged(), 999L);
        when(amendmentRepository.findBy(any(Specification.class), any())).thenAnswer(iom -> {
            iom.getArgument(1, Function.class).apply(sfq);
            return mockPage;
        });

        // Act
        SearchDataResponse<AmendmentEntity> result = service.searchAmendments(AmendmentSearchDto.builder().build());

        // Assert
        assertEquals(SearchDataResponse.builder().searchData(List.of(amendment)).build(), result);

    }

    @Test
    void testCallStoredProcs() {
        // Act
        service.auditInitialiseStoredProc(1L, RecordType.DEFENDANT_ACCOUNTS);
        service.auditFinaliseStoredProc(1L, RecordType.DEFENDANT_ACCOUNTS, (short)77,
                                        "USER_ME", "CaseRef001", "funcCodeA");
    }
}
