package uk.gov.hmcts.opal.service.messaging;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;
import uk.gov.hmcts.opal.common.spring.security.OpalJwtAuthenticationToken;
import uk.gov.hmcts.opal.common.user.authentication.service.SystemUserAuthenticationService;
import uk.gov.hmcts.opal.common.util.SecurityUtil;
import uk.gov.hmcts.opal.dto.DraftAccountResponseDto;
import uk.gov.hmcts.opal.dto.UpdateDraftAccountRequestDto;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.draft.DraftAccountEntity;
import uk.gov.hmcts.opal.entity.draft.DraftAccountStatus;
import uk.gov.hmcts.opal.repository.DraftAccountRepository;
import uk.gov.hmcts.opal.service.DraftAccountService;

@ExtendWith(MockitoExtension.class)
public class PublishDraftAccountConsumerTest {

    @Mock
    private DraftAccountRepository repository;

    @Mock
    private AcknowledgementsQueuePublisher publisher;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private DraftAccountService draftAccountService;

    @Mock
    private SystemUserAuthenticationService systemUserAuthenticationService;

    @InjectMocks
    private PublishDraftAccountConsumerService consumerService;

    private static final String VALID_PAYLOAD = """
                                            {"draft_account_id": 1}""";

    @Mock
    private PublishDraftAccountQueueMessage message;

    MockedStatic<SecurityUtil> mockedSecurityUtil;
    OpalJwtAuthenticationToken mockedToken = mock(OpalJwtAuthenticationToken.class);

    @BeforeEach
    void setUp() {
        mockedSecurityUtil = mockStatic(SecurityUtil.class);
    }

    @AfterEach
    void tearDown() {
        mockedSecurityUtil.close();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  ", "\t", "\n"})
    void shouldThrowExceptionWhenPayloadIsBlank(String payload) throws Exception {
        mockedSecurityUtil.when(SecurityUtil::getOpalJwtAuthenticationTokenForCurrentUser)
            .thenReturn(mockedToken);

        assertThatThrownBy(() -> consumerService.consume(payload))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Publish draft account message payload is blank");

        verifyNoInteractions(repository);
        verifyNoInteractions(publisher);
        verifyNoInteractions(draftAccountService);
    }


    @Test
    void successfullyPublishesDraftAccount() {
        mockedSecurityUtil.when(SecurityUtil::getOpalJwtAuthenticationTokenForCurrentUser)
            .thenReturn(mockedToken);
        when(objectMapper.readValue(VALID_PAYLOAD, PublishDraftAccountQueueMessage.class)).thenReturn(pdaMessage());
        when(repository.findById(1L)).thenReturn(Optional.of(draftEntity()));
        UpdateDraftAccountRequestDto expectedDTO = UpdateDraftAccountRequestDto.builder()
            .businessUnitId((short)77)
            .build();
        when(draftAccountService.updateDraftAccount(1L, expectedDTO, "1"))
            .thenReturn(draftAccountResponseDto(DraftAccountStatus.PUBLISHED));

        consumerService.consume(VALID_PAYLOAD);

        verify(repository, times(1)).findById(1L);
        verify(draftAccountService, times(1))
            .updateDraftAccount(1L, expectedDTO, "1");
        verify(publisher, times(1)).publish(808L, 1000L);
        verify(publisher, never()).publishError(anyLong());
    }

    @Test
    void successfullyPublishesErrorFromDraftAccount() {
        mockedSecurityUtil.when(SecurityUtil::getOpalJwtAuthenticationTokenForCurrentUser)
            .thenReturn(mockedToken);
        when(objectMapper.readValue(VALID_PAYLOAD, PublishDraftAccountQueueMessage.class)).thenReturn(pdaMessage());
        when(repository.findById(1L)).thenReturn(Optional.of(draftEntity()));
        UpdateDraftAccountRequestDto expectedDTO = UpdateDraftAccountRequestDto.builder()
            .businessUnitId((short)77)
            .build();
        when(draftAccountService.updateDraftAccount(1L, expectedDTO, "1"))
            .thenReturn(draftAccountResponseDto(DraftAccountStatus.PUBLISHING_FAILED));

        consumerService.consume(VALID_PAYLOAD);

        verify(repository, times(1)).findById(1L);
        verify(draftAccountService, times(1))
            .updateDraftAccount(1L, expectedDTO, "1");
        verify(publisher, never()).publish(anyLong(), anyLong());
        verify(publisher, times(1)).publishError(808L);
    }

    @Test
    void consume_hanlesEntityNotFoundException() {
        mockedSecurityUtil.when(SecurityUtil::getOpalJwtAuthenticationTokenForCurrentUser)
            .thenReturn(mockedToken);
        when(objectMapper.readValue(VALID_PAYLOAD, PublishDraftAccountQueueMessage.class)).thenReturn(pdaMessage());
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> consumerService.consume(VALID_PAYLOAD))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessage("could not find draft account with id 1");

        verify(repository, times(1)).findById(1L);
        verify(draftAccountService, never()).updateDraftAccount(any(), any(), any());
        verify(publisher, never()).publish(anyLong(), anyLong());
        verify(publisher, never()).publishError(anyLong());
    }

    PublishDraftAccountQueueMessage pdaMessage() {
        return new PublishDraftAccountQueueMessage(1L);
    }

    DraftAccountEntity draftEntity() {
        return DraftAccountEntity.builder()
            .draftAccountId(1L)
            .sourceReference(808L)
            .versionNumber(1L)
            .businessUnit(
                BusinessUnitEntity.builder()
                    .businessUnitId((short)77)
                    .build()
            )
            .build();
    }

    DraftAccountResponseDto draftAccountResponseDto(DraftAccountStatus status) {
        return DraftAccountResponseDto.builder()
            .draftAccountId(1L)
            .accountStatus(status)
            .accountId(1000L)
            .build();
    }

}
