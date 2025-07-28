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
import uk.gov.hmcts.opal.dto.search.SuspenseItemSearchDto;
import uk.gov.hmcts.opal.entity.SuspenseItemEntity;
import uk.gov.hmcts.opal.repository.SuspenseItemRepository;

import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SuspenseItemServiceTest {

    @Mock
    private SuspenseItemRepository suspenseItemRepository;

    @InjectMocks
    private SuspenseItemService suspenseItemService;

    @Test
    void testGetSuspenseItem() {
        // Arrange

        SuspenseItemEntity suspenseItemEntity = SuspenseItemEntity.builder().build();
        when(suspenseItemRepository.getReferenceById(any())).thenReturn(suspenseItemEntity);

        // Act
        SuspenseItemEntity result = suspenseItemService.getSuspenseItem(1);

        // Assert
        assertNotNull(result);

    }

    @SuppressWarnings("unchecked")
    @Test
    void testSearchSuspenseItems() {
        // Arrange
        SpecificationFluentQuery sfq = Mockito.mock(SpecificationFluentQuery.class);

        SuspenseItemEntity suspenseItemEntity = SuspenseItemEntity.builder().build();
        Page<SuspenseItemEntity> mockPage = new PageImpl<>(List.of(suspenseItemEntity), Pageable.unpaged(), 999L);
        when(suspenseItemRepository.findBy(any(Specification.class), any())).thenAnswer(iom -> {
            iom.getArgument(1, Function.class).apply(sfq);
            return mockPage;
        });

        // Act
        List<SuspenseItemEntity> result = suspenseItemService.searchSuspenseItems(
            SuspenseItemSearchDto.builder().build());

        // Assert
        assertEquals(List.of(suspenseItemEntity), result);

    }


}
