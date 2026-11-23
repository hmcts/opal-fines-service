package uk.gov.hmcts.opal.service.messaging;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import uk.gov.hmcts.opal.common.user.authentication.service.SystemUserAuthenticationService;
import uk.gov.hmcts.opal.common.user.authentication.service.SystemUserEnum;
import uk.gov.hmcts.opal.common.util.SecurityUtil;
import uk.gov.hmcts.opal.dto.DraftAccountResponseDto;
import uk.gov.hmcts.opal.dto.UpdateDraftAccountRequestDto;
import uk.gov.hmcts.opal.entity.draft.DraftAccountEntity;
import uk.gov.hmcts.opal.entity.draft.DraftAccountStatus;
import uk.gov.hmcts.opal.repository.DraftAccountRepository;
import uk.gov.hmcts.opal.service.DraftAccountService;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@Slf4j(topic = "opal.PublishDraftAccountConsumerService")
public class PublishDraftAccountConsumerService implements QueueConsumerInterface {

    private final DraftAccountRepository draftAccountRepository;
    private final AcknowledgementsQueuePublisher acknowledgementsPublisher;
    private final ObjectMapper objectMapper;
    private final DraftAccountService draftAccountService;
    private final SystemUserAuthenticationService systemUserAuthenticationService;

    @Override
    public void consume(String messagePayload) {
        systemUserAuthenticationService.setupAsSystemUser(SystemUserEnum.OPAL_SYSTEM_USER);

        PublishDraftAccountQueueMessage message = parse(messagePayload);
        DraftAccountEntity draftAccount = draftAccountRepository.findById(message.draftAccountId())
            .orElseThrow(() -> new EntityNotFoundException(
                "could not find draft account with id %d".formatted(message.draftAccountId())));

        long vpfpoRegistrationId = draftAccount.getSourceReference();
        String ifMatch = draftAccount.getVersion().toString();
        DraftAccountResponseDto publishedDraftAccount = draftAccountService.updateDraftAccount(
            message.draftAccountId(), toDraftAccountDTO(draftAccount), ifMatch);

        if (publishedDraftAccount.getAccountStatus() == DraftAccountStatus.PUBLISHED) {
            long defendantId = publishedDraftAccount.getAccountId();

            acknowledgementsPublisher.publish(vpfpoRegistrationId, defendantId);
        } else {
            acknowledgementsPublisher.publishError(vpfpoRegistrationId);
        }

        SecurityUtil.clearSecurityContext();
        log.info("Publish draft account queue message received: {}", messagePayload);
    }

    private PublishDraftAccountQueueMessage parse(String messagePayload) {
        if (messagePayload == null || messagePayload.isBlank()) {
            throw new IllegalArgumentException("Publish draft account message payload is blank");
        }
        try {
            return objectMapper.readValue(messagePayload, PublishDraftAccountQueueMessage.class);
        } catch (JacksonException ex) {
            log.error("Publish draft account  queue message parse failed", ex);
            throw new IllegalArgumentException("Unable to parse Publish draft account message payload", ex);
        }
    }

    private UpdateDraftAccountRequestDto toDraftAccountDTO(DraftAccountEntity entity) {
        return UpdateDraftAccountRequestDto.builder()
            .businessUnitId(entity.getBusinessUnit().getBusinessUnitId())
            .accountStatus(entity.getAccountStatus())
            .build();
    }
}
