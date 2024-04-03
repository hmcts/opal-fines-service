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
import uk.gov.hmcts.opal.dto.search.ConfigurationItemSearchDto;
import uk.gov.hmcts.opal.entity.ConfigurationItemEntity;
import uk.gov.hmcts.opal.repository.ConfigurationItemRepository;

import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfigurationItemServiceTest {

    @Mock
    private ConfigurationItemRepository configurationItemRepository;

    @InjectMocks
    private ConfigurationItemService configurationItemService;

    @Test
    void testGetConfigurationItem() {
        // Arrange

        ConfigurationItemEntity configurationItemEntity = ConfigurationItemEntity.builder().build();
        when(configurationItemRepository.getReferenceById(any())).thenReturn(configurationItemEntity);

        // Act
        ConfigurationItemEntity result = configurationItemService.getConfigurationItem(1);

        // Assert
        assertNotNull(result);

    }

    @SuppressWarnings("unchecked")
    @Test
    void testSearchConfigurationItems() {
        // Arrange
        FluentQuery.FetchableFluentQuery ffq = Mockito.mock(FluentQuery.FetchableFluentQuery.class);

        ConfigurationItemEntity configurationItemEntity = ConfigurationItemEntity.builder().build();
        Page<ConfigurationItemEntity> mockPage = new PageImpl<>(List.of(configurationItemEntity),
                                                                Pageable.unpaged(), 999L);
        when(configurationItemRepository.findBy(any(Specification.class), any())).thenAnswer(iom -> {
            iom.getArgument(1, Function.class).apply(ffq);
            return mockPage;
        });

        // Act
        List<ConfigurationItemEntity> result = configurationItemService.searchConfigurationItems(
            ConfigurationItemSearchDto.builder().build());

        // Assert
        assertEquals(List.of(configurationItemEntity), result);

    }


}
