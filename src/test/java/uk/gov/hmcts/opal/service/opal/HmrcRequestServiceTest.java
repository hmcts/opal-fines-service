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
import uk.gov.hmcts.opal.dto.search.HmrcRequestSearchDto;
import uk.gov.hmcts.opal.entity.HmrcRequestEntity;
import uk.gov.hmcts.opal.repository.HmrcRequestRepository;

import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HmrcRequestServiceTest {

    @Mock
    private HmrcRequestRepository hmrcRequestRepository;

    @InjectMocks
    private HmrcRequestService hmrcRequestService;

    @Test
    void testGetHmrcRequest() {
        // Arrange

        HmrcRequestEntity hmrcRequestEntity = HmrcRequestEntity.builder().build();
        when(hmrcRequestRepository.getReferenceById(any())).thenReturn(hmrcRequestEntity);

        // Act
        HmrcRequestEntity result = hmrcRequestService.getHmrcRequest(1);

        // Assert
        assertNotNull(result);

    }

    @SuppressWarnings("unchecked")
    @Test
    void testSearchHmrcRequests() {
        // Arrange
        SpecificationFluentQuery sfq = Mockito.mock(SpecificationFluentQuery.class);

        HmrcRequestEntity hmrcRequestEntity = HmrcRequestEntity.builder().build();
        Page<HmrcRequestEntity> mockPage = new PageImpl<>(List.of(hmrcRequestEntity), Pageable.unpaged(), 999L);
        when(hmrcRequestRepository.findBy(any(Specification.class), any())).thenAnswer(iom -> {
            iom.getArgument(1, Function.class).apply(sfq);
            return mockPage;
        });

        // Act
        List<HmrcRequestEntity> result = hmrcRequestService.searchHmrcRequests(HmrcRequestSearchDto.builder().build());

        // Assert
        assertEquals(List.of(hmrcRequestEntity), result);

    }


}
