package uk.gov.hmcts.opal.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.hmcts.opal.dto.NoteDto;

@Service
@RequiredArgsConstructor
//@ConditionalOnProperty(name = "app-mode", havingValue = "legacy", matchIfMissing = false)
@Slf4j
public class LegacyNoteService implements NoteServiceInterface {

    @Value("${legacy-gateway-url}")
    String gatewayUrl;

    private final RestTemplate restTemplate;

    @Override
    public NoteDto saveNote(NoteDto noteDto) {
        log.info("Sending note to {}", gatewayUrl);

        // Create a UriComponentsBuilder and add parameters
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString("")
            .queryParam("actionType", "postAccountNotes");

        ResponseEntity<String> responseEntity = restTemplate.postForEntity(
            builder.toUriString(), noteDto, String.class);

        if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getBody() != null) {
            String rawJson = responseEntity.getBody();
            log.info("Raw JSON response: {}", rawJson);

            try {
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.registerModule(new JavaTimeModule());
                JsonNode root = objectMapper.readTree(rawJson);

                return objectMapper.treeToValue(root, NoteDto.class);

            } catch (Exception e) {
                log.error("Error deserializing response: {}", e.getMessage(), e);
            }
        } else {
            log.warn("Received non-2xx response: {}", responseEntity.getStatusCode());
        }
        return null;
    }
}
