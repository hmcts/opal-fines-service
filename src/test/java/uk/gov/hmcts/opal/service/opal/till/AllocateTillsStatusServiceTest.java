package uk.gov.hmcts.opal.service.opal.till;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.entity.TillEntity;
import uk.gov.hmcts.opal.entity.TillStatusEnum;
import uk.gov.hmcts.opal.repository.TillRepository;

@ExtendWith(MockitoExtension.class)
class AllocateTillsStatusServiceTest {

    private static final Long TILL_ID = 123L;

    @Mock
    private TillRepository tillRepository;

    @InjectMocks
    private AllocateTillsStatusService statusService;

    @Test
    void isProcessing_returnsTrueForProcessingTill() {
        when(tillRepository.findById(TILL_ID)).thenReturn(Optional.of(TillEntity.builder()
            .status(TillStatusEnum.Processing)
            .build()));

        assertThat(statusService.isProcessing(TILL_ID)).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = TillStatusEnum.class, names = "Processing", mode = EnumSource.Mode.EXCLUDE)
    void isProcessing_returnsFalseForOtherStatuses(TillStatusEnum status) {
        when(tillRepository.findById(TILL_ID)).thenReturn(Optional.of(TillEntity.builder()
            .status(status)
            .build()));

        assertThat(statusService.isProcessing(TILL_ID)).isFalse();
    }

    @Test
    void isProcessing_returnsFalseWhenTillDoesNotExist() {
        when(tillRepository.findById(TILL_ID)).thenReturn(Optional.empty());

        assertThat(statusService.isProcessing(TILL_ID)).isFalse();
    }
}
