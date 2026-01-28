package uk.gov.hmcts.opal.service.opal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import uk.gov.hmcts.opal.dto.response.SearchDataResponse;
import uk.gov.hmcts.opal.dto.search.AmendmentSearchDto;
import uk.gov.hmcts.opal.entity.amendment.AmendmentEntity;
import uk.gov.hmcts.opal.entity.amendment.AmendmentEntity_;
import uk.gov.hmcts.opal.entity.amendment.RecordType;
import uk.gov.hmcts.opal.repository.AmendmentRepository;
import uk.gov.hmcts.opal.service.persistence.AmendmentRepositoryService;

@ExtendWith(MockitoExtension.class)
class AmendmentServiceTest {

    @Mock
    private AmendmentRepositoryService amendmentRepositoryService;

    @Mock
    private AmendmentRepository amendmentRepository;

    @InjectMocks
    private AmendmentService service;

    @Test
    void testGetAmendment() {
        // Arrange

        AmendmentEntity amendmentEntity = AmendmentEntity.builder().build();
        when(amendmentRepositoryService.findById(anyLong())).thenReturn(amendmentEntity);

        // Act
        AmendmentEntity result = service.getAmendmentById(1L);

        // Assert
        assertNotNull(result);

    }

    @SuppressWarnings("unchecked")
    @Test
    void testSearchAmendments() {
        // Arrange
        AmendmentEntity amendment = AmendmentEntity.builder().build();
        Page<AmendmentEntity> mockPage =
            new PageImpl<>(List.of(amendment), Pageable.unpaged(), 999L);

        when(amendmentRepositoryService.getAmendmentsByCriteriaAsPage(
            any(AmendmentSearchDto.class), any(Sort.class)))
            .thenReturn(mockPage);

        ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);

        // Act
        SearchDataResponse<AmendmentEntity> result =
            service.searchAmendments(AmendmentSearchDto.builder().build());

        // Assert: response content
        assertEquals(
            SearchDataResponse.<AmendmentEntity>builder()
                .searchData(List.of(amendment))
                .build(),
            result
        );

        // Assert: repository was called with the expected sort (AMENDED_DATE desc)
        verify(amendmentRepositoryService, times(1))
            .getAmendmentsByCriteriaAsPage(any(AmendmentSearchDto.class), sortCaptor.capture());

        Sort expectedSort = Sort.by(Sort.Direction.DESC, AmendmentEntity_.AMENDED_DATE);
        assertEquals(expectedSort, sortCaptor.getValue());
    }

    @Test
    void testCallStoredProcs() {
        // Act
        service.auditInitialiseStoredProc(1L, RecordType.DEFENDANT_ACCOUNTS);
        service.auditFinaliseStoredProc(1L, RecordType.DEFENDANT_ACCOUNTS, (short)77,
                                        "USER_ME", "CaseRef001", "funcCodeA");
    }
}
