package uk.gov.hmcts.opal.disco.opal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityNotFoundException;
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
import uk.gov.hmcts.opal.entity.ProsecutorEntity;
import uk.gov.hmcts.opal.generated.model.ProsecutorsObject;
import uk.gov.hmcts.opal.mapper.ProsecutorMapper;
import uk.gov.hmcts.opal.repository.ProsecutorRepository;
import uk.gov.hmcts.opal.service.opal.OpalProsecutorService;

@ExtendWith(MockitoExtension.class)
class OpalProsecutorServiceTest {

    @Mock
    private ProsecutorRepository prosecutorRepository;

    @Mock
    private ProsecutorMapper prosecutorMapper;

    @InjectMocks
    private OpalProsecutorService prosecutorService;

    @Test
    void getProsecutorByKey_usesProsecutorCode() {
        ProsecutorEntity entity = ProsecutorEntity.builder().prosecutorCode((short) 10).build();
        ProsecutorsObject expected = ProsecutorsObject.builder().prosecutorCode((short) 10).name("Opal").build();
        when(prosecutorRepository.findById((short) 10)).thenReturn(Optional.of(entity));
        when(prosecutorMapper.toProsecutor(entity)).thenReturn(expected);

        assertEquals(expected, prosecutorService.getProsecutorByKey(10L));
        verify(prosecutorRepository).findById((short) 10);
    }

    @Test
    void getProsecutorByKey_rejectsKeyOutsideSmallintRange() {
        assertThrows(EntityNotFoundException.class, () -> prosecutorService.getProsecutorByKey(100_000L));
    }

    @SuppressWarnings("unchecked")
    @Test
    void getProsecutorsRefData_mapsGeneratedResponse() {
        SpecificationFluentQuery<ProsecutorEntity> query = mock(SpecificationFluentQuery.class);
        when(query.sortBy(any())).thenReturn(query);
        ProsecutorEntity entity = ProsecutorEntity.builder().prosecutorCode((short) 10).build();
        Page<ProsecutorEntity> page = new PageImpl<>(List.of(entity), Pageable.unpaged(), 1);
        when(prosecutorRepository.findBy(any(Specification.class), any())).thenAnswer(invocation -> {
            invocation.getArgument(1, Function.class).apply(query);
            return page;
        });
        ProsecutorsObject mapped = ProsecutorsObject.builder().prosecutorCode((short) 10).name("Opal").build();
        when(prosecutorMapper.toProsecutors(List.of(entity))).thenReturn(List.of(mapped));

        assertEquals(List.of(mapped), prosecutorService.getProsecutorsRefData(null).getRefData());
    }
}
