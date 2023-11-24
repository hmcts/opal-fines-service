package uk.gov.hmcts.opal.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.opal.dto.NoteDto;

@Service
@RequiredArgsConstructor
//@ConditionalOnProperty(name = "app-mode", havingValue = "legacy", matchIfMissing = false)
@Slf4j
public class LegacyNoteService implements NoteServiceInterface {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public NoteDto saveNote(NoteDto noteDto) {
        String gatewayUrl = "https://postman-echo.com/post";
        log.info("Sending note to {}", gatewayUrl);

        ResponseEntity<String> responseEntity = restTemplate.postForEntity(gatewayUrl, noteDto, String.class);

        if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getBody() != null) {
            String rawJson = responseEntity.getBody();
            log.info("Raw JSON response: {}", rawJson);

            try {
                JsonNode root = objectMapper.readTree(rawJson);
                JsonNode jsonNode = root.path("json");
                if (!jsonNode.isMissingNode()) {
                    return objectMapper.treeToValue(jsonNode, NoteDto.class);
                }
            } catch (Exception e) {
                log.error("Error deserializing response: {}", e.getMessage(), e);
            }
        } else {
            log.warn("Received non-2xx response: {}", responseEntity.getStatusCode());
        }
        return null;
    }
}
