package uk.gov.hmcts.opal.service.messaging;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;
import uk.gov.hmcts.opal.dto.AddDraftAccountRequestDto;
import uk.gov.hmcts.opal.repository.LocalJusticeAreaRepository;
import uk.gov.hmcts.opal.service.DraftAccountService;

@Component
@Slf4j(topic = "opal.VpfpoDraftAccountConsumer")
@RequiredArgsConstructor
public class VpfpoDraftAccountConsumerService implements QueueConsumerInterface {

    private final ObjectMapper objectMapper;
    private final DraftAccountService draftAccountService;
    private final LocalJusticeAreaRepository localJusticeAreaRepository;

    @Override
    public void consume(String messagePayload) {
        VpfpoDraftAccountMessage request = parse(messagePayload);

        Optional<AddDraftAccountRequestDto> converted = convert(request);
        if (converted.isEmpty()) {
            log.info("This message was not processed: no draft account mapping for LJA {}",
                request.ljaCode());
            return;
        }

        draftAccountService.submitDraftAccount(converted.get());

        log.info("Draft account submitted draftAccountId={}", request.draftAccountId());
    }

    public VpfpoDraftAccountMessage parse(String payload) {
        if (payload == null || payload.isBlank()) {
            throw new IllegalArgumentException("Message payload is null or blank");
        }

        try {
            return objectMapper.readValue(payload, VpfpoDraftAccountMessage.class);
        } catch (JacksonException exception) {
            throw new IllegalArgumentException("Unable to parse ", exception);
        }
    }

    private Optional<AddDraftAccountRequestDto> convert(VpfpoDraftAccountMessage request) {
        return localJusticeAreaRepository.findDraftAccountMappingByLjaCode(request.ljaCode())
            .map(mapping -> {
                String snapshot = request.accountSnapshot() == null
                    ? null : objectMapper.writeValueAsString(request.accountSnapshot());

                ObjectNode account = request.account().deepCopy();
                account.put("enforcement_court_id", mapping.enforcingCourtId());

                return AddDraftAccountRequestDto.builder()
                    .draftAccountId(request.draftAccountId())
                    .createdDate(request.createdDate())
                    .validatedDate(request.validatedDate())
                    .businessUnitId(mapping.businessUnitId())
                    .account(account.toString())
                    .accountSnapshot(snapshot)
                    .accountType(request.accountType())
                    .accountStatus(request.accountStatus())
                    .statusMessage(request.statusMessage())
                    .timelineData(request.timelineData())
                    .source(request.source())
                    .sourceReference(request.sourceReference())
                    .build();
            });
    }
}
