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
import uk.gov.hmcts.opal.entity.OpalProsecutorEntity;
import uk.gov.hmcts.opal.generated.model.ProsecutorsObject;
import uk.gov.hmcts.opal.mapper.OpalProsecutorMapper;
import uk.gov.hmcts.opal.repository.OpalProsecutorRepository;
import uk.gov.hmcts.opal.service.opal.OpalProsecutorService;

@ExtendWith(MockitoExtension.class)
class OpalProsecutorServiceTest {

    @Mock
    private OpalProsecutorRepository opalProsecutorRepository;

    @Mock
    private OpalProsecutorMapper opalProsecutorMapper;

    @InjectMocks
    private OpalProsecutorService prosecutorService;

    @Test
    void getProsecutorByKey_usesProsecutorCode() {
        OpalProsecutorEntity entity = OpalProsecutorEntity.builder().prosecutorCode("001X").build();
        ProsecutorsObject expected = ProsecutorsObject.builder().prosecutorCode("001X").name("Opal").build();
        when(opalProsecutorRepository.findById("001X")).thenReturn(Optional.of(entity));
        when(opalProsecutorMapper.toProsecutor(entity)).thenReturn(expected);

        assertEquals(expected, prosecutorService.getProsecutorByKey("001X"));
        verify(opalProsecutorRepository).findById("001X");
    }

    @Test
    void getProsecutorByKey_rejectsUnknownCode() {
        assertThrows(EntityNotFoundException.class, () -> prosecutorService.getProsecutorByKey("NONE"));
    }

    @SuppressWarnings("unchecked")
    @Test
    void getProsecutorsRefData_mapsGeneratedResponse() {
        SpecificationFluentQuery<OpalProsecutorEntity> query = mock(SpecificationFluentQuery.class);
        when(query.sortBy(any())).thenReturn(query);
        OpalProsecutorEntity entity = OpalProsecutorEntity.builder().prosecutorCode("001X").build();
        Page<OpalProsecutorEntity> page = new PageImpl<>(List.of(entity), Pageable.unpaged(), 1);
        when(opalProsecutorRepository.findBy(any(Specification.class), any())).thenAnswer(invocation -> {
            invocation.getArgument(1, Function.class).apply(query);
            return page;
        });
        ProsecutorsObject mapped = ProsecutorsObject.builder().prosecutorCode("001X").name("Opal").build();
        when(opalProsecutorMapper.toProsecutors(List.of(entity))).thenReturn(List.of(mapped));

        assertEquals(List.of(mapped), prosecutorService.getProsecutorsRefData(null).getRefData());
    }
}
