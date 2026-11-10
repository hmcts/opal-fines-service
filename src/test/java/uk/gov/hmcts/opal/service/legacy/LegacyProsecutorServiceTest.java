package uk.gov.hmcts.opal.service.legacy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor.SpecificationFluentQuery;
import uk.gov.hmcts.opal.entity.ProsecutorLegacyEntity;
import uk.gov.hmcts.opal.generated.model.ProsecutorsObject;
import uk.gov.hmcts.opal.mapper.ProsecutorLegacyMapper;
import uk.gov.hmcts.opal.repository.ProsecutorLegacyRepository;

@ExtendWith(MockitoExtension.class)
class LegacyProsecutorServiceTest {

    @Mock
    private ProsecutorLegacyRepository prosecutorLegacyRepository;

    @Mock
    private ProsecutorLegacyMapper prosecutorLegacyMapper;

    @InjectMocks
    private LegacyProsecutorService prosecutorService;

    @Test
    void getProsecutorByKey_usesLegacyId() {
        ProsecutorLegacyEntity entity = ProsecutorLegacyEntity.builder().prosecutorId(10L).build();
        ProsecutorsObject expected = ProsecutorsObject.builder().prosecutorId(10L).name("Legacy").build();
        when(prosecutorLegacyRepository.findById(10L)).thenReturn(Optional.of(entity));
        when(prosecutorLegacyMapper.toProsecutor(entity)).thenReturn(expected);

        assertEquals(expected, prosecutorService.getProsecutorByKey(10L));
        verify(prosecutorLegacyRepository).findById(10L);
    }

    @SuppressWarnings("unchecked")
    @Test
    void getProsecutorsRefData_mapsGeneratedResponse() {
        SpecificationFluentQuery<ProsecutorLegacyEntity> query = mock(SpecificationFluentQuery.class);
        when(query.sortBy(any())).thenReturn(query);
        ProsecutorLegacyEntity entity = ProsecutorLegacyEntity.builder().prosecutorId(10L).build();
        Page<ProsecutorLegacyEntity> page = new PageImpl<>(List.of(entity), Pageable.unpaged(), 1);
        when(prosecutorLegacyRepository.findBy(any(Specification.class), any())).thenAnswer(invocation -> {
            invocation.getArgument(1, Function.class).apply(query);
            return page;
        });
        ProsecutorsObject mapped = ProsecutorsObject.builder().prosecutorId(10L).name("Legacy").build();
        when(prosecutorLegacyMapper.toProsecutors(List.of(entity))).thenReturn(List.of(mapped));

        assertEquals(List.of(mapped), prosecutorService.getProsecutorsRefData(null).getRefData());
    }
}
