package uk.gov.hmcts.opal.service.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.jms.JMSException;
import jakarta.jms.TextMessage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.data.domain.Example;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.entity.draft.DraftAccountEntity;
import uk.gov.hmcts.opal.entity.draft.DraftAccountSource;
import uk.gov.hmcts.opal.entity.draft.DraftAccountStatus;
import uk.gov.hmcts.opal.entity.draft.DraftAccountType;
import uk.gov.hmcts.opal.exception.JsonSchemaValidationException;
import uk.gov.hmcts.opal.repository.DraftAccountRepository;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@Transactional
@DisplayName("VPFPO draft account consumer integration tests")
@Sql(scripts = {
    "classpath:db/insertData/insert_draft_account_mapping.sql",
    "classpath:db/insertData/insert_into_draft_account_reference_validation_data.sql"
})
class VpfpoDraftAccountConsumerIntegrationTest extends AbstractIntegrationTest {

    private static final short BUSINESS_UNIT_ID = 31001;
    private static final long ENFORCING_COURT_ID = 900000000001L;
    private static final long SOURCE_REFERENCE = 10322001L;

    @Autowired
    private VpfpoDraftAccountConsumerService consumer;

    @Autowired
    private DraftAccountRepository draftAccountRepository;

    private VpfpoDraftAccountQueueListener listener;

    @BeforeEach
    void setUp() {
        listener = new VpfpoDraftAccountQueueListener(consumer);
        userStateStub.addPermissions(BUSINESS_UNIT_ID, FinesPermission.CREATE_MANAGE_DRAFT_ACCOUNTS);
        SecurityContextHolder.setContext(SecurityContextHolder.createEmptyContext());
        SecurityContextHolder.getContext().setAuthentication(userStateStub.getOpalJwtAuthenticationToken());
    }

    @AfterEach
    void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @JiraStory("PO-10322")
    @JiraEpic("PO-7459")
    void createsMappedDraftAccount() throws IOException, JMSException {
        ObjectNode payload = payload();

        listener.onMessage(textMessage(payload.toString()));
        draftAccountRepository.flush();

        List<DraftAccountEntity> saved = draftAccounts();
        assertThat(saved).hasSize(1);
        DraftAccountEntity draft = saved.getFirst();
        assertThat(draft.getDraftAccountId()).isNotNull();
        assertThat(draft.getBusinessUnit().getBusinessUnitId()).isEqualTo(BUSINESS_UNIT_ID);
        assertThat(draft.getAccountType()).isEqualTo(DraftAccountType.FIXED_PENALTY);
        assertThat(draft.getAccountStatus()).isEqualTo(DraftAccountStatus.SUBMITTED);
        assertThat(draft.getSource()).isEqualTo(DraftAccountSource.VPFPO);
        assertThat(draft.getSourceReference()).isEqualTo(SOURCE_REFERENCE);
        assertThat(draft.getSubmittedBy()).isEqualTo("L031001JG");

        ObjectNode expectedAccount = ((ObjectNode) payload.get("account")).deepCopy();
        expectedAccount.put("enforcement_court_id", ENFORCING_COURT_ID);
        assertThat(objectMapper.readTree(draft.getAccount())).isEqualTo(expectedAccount);

        JsonNode snapshot = objectMapper.readTree(draft.getAccountSnapshot());
        assertThat(snapshot.get("defendant_name").asString()).isEqualTo("Example, Alex");
        assertThat(snapshot.get("business_unit_name").asString()).isEqualTo("VPFPO mapping test");
    }

    @ParameterizedTest
    @ValueSource(shorts = {30002, 30004})
    @JiraStory("PO-10322")
    @JiraEpic("PO-7459")
    void discardsMissingMapping(short ljaCode) throws IOException, JMSException {
        ObjectNode payload = payload();
        payload.put("business_unit_id", ljaCode);

        listener.onMessage(textMessage(payload.toString()));

        assertThat(draftAccounts()).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"defendant", "originator_name", "payment_terms"})
    @JiraStory("PO-10322")
    @JiraEpic("PO-7459")
    void rejectsInvalidAccountWithoutSaving(String requiredField) throws IOException {
        ObjectNode payload = payload();
        ((ObjectNode) payload.get("account")).remove(requiredField);

        assertThatThrownBy(() -> listener.onMessage(textMessage(payload.toString())))
            .isInstanceOf(JsonSchemaValidationException.class);

        assertThat(draftAccounts()).isEmpty();
    }

    @Test
    @JiraStory("PO-10322")
    @JiraEpic("PO-7459")
    void rejectsMalformedJsonWithoutSaving() {
        assertThatThrownBy(() -> listener.onMessage(textMessage("{invalid")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Unable to parse");

        assertThat(draftAccounts()).isEmpty();
    }

    @Test
    @JiraStory("PO-10322")
    @JiraEpic("PO-7459")
    void rejectsAmbiguousMappingWithoutSaving() throws IOException {
        ObjectNode payload = payload();
        payload.put("business_unit_id", 30003);

        assertThatThrownBy(() -> listener.onMessage(textMessage(payload.toString())))
            .isInstanceOf(IncorrectResultSizeDataAccessException.class);

        assertThat(draftAccounts()).isEmpty();
    }

    private ObjectNode payload() throws IOException {
        String json = new ClassPathResource("messages/vpfpo-draft-account.json")
            .getContentAsString(StandardCharsets.UTF_8);
        return (ObjectNode) objectMapper.readTree(json);
    }

    private List<DraftAccountEntity> draftAccounts() {
        return draftAccountRepository.findAll(Example.of(DraftAccountEntity.builder()
            .source(DraftAccountSource.VPFPO)
            .sourceReference(SOURCE_REFERENCE)
            .build()));
    }

    private static TextMessage textMessage(String payload) throws JMSException {
        TextMessage message = mock(TextMessage.class);
        when(message.getText()).thenReturn(payload);
        return message;
    }
}
