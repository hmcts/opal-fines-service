package uk.gov.hmcts.opal.service.legacy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.common.legacy.service.GatewayService;
import uk.gov.hmcts.opal.common.legacy.service.GatewayService.Response;
import uk.gov.hmcts.opal.dto.GetDefendantAccountImpositionsResponse;
import uk.gov.hmcts.opal.dto.legacy.GetDefendantAccountImpositionsLegacyResponse;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetImpositionsRequest;
import uk.gov.hmcts.opal.mapper.legacy.DefendantAccountImpositionsLegacyResponseMapper;
import uk.gov.hmcts.opal.service.iface.ImpositionServiceInterface;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "opal.LegacyImpositionService")
public class LegacyImpositionService implements ImpositionServiceInterface {

    public static final String GET_IMPOSITIONS = "getDefendantAccountImpositions";

    /* ---- Services ---- */
    private final GatewayService gatewayService;
    private final DefendantAccountImpositionsLegacyResponseMapper impositionsResponseMapper;

    @Override
    public GetDefendantAccountImpositionsResponse getImpositions(Long defendantAccountId) {
        log.debug(":getImpositions: id: {}", defendantAccountId);

        try {
            Response<GetDefendantAccountImpositionsLegacyResponse> response = gatewayService.postToGateway(
                GET_IMPOSITIONS, GetDefendantAccountImpositionsLegacyResponse.class,
                createGetDefendantAccountImpositionsRequest(defendantAccountId.toString()), null);

            checkResponseForError(response, "getImpositions");

            return impositionsResponseMapper.toOpal(response.responseEntity);
        } catch (RuntimeException e) {
            log.error(":getImpositions: problem with call to Legacy: {}", e.getClass().getName());
            log.error(":getImpositions:", e);
            throw e;
        }
    }

    /* This is probably common code that will be needed across multiple Legacy requests to get
    Defendant Account details. */
    private LegacyGetImpositionsRequest createGetDefendantAccountImpositionsRequest(String defendantAccountId) {
        return LegacyGetImpositionsRequest.builder()
            .defendantAccountId(defendantAccountId)
            .build();
    }

    /* This looks like a candidate for communalization for several legacy services...*/
    private static <T> void checkResponseForError(Response<T> response, String method) {
        if (response.isError()) {
            log.error(":{}: legacy error HTTP {}", method, response.code);
            if (response.isException()) {
                log.error(":{}: exception:", method, response.exception);
            } else if (response.isLegacyFailure()) {
                log.error(":{}: legacy failure body:\n{}", method, response.body);
            }
        } else if (response.isSuccessful()) {
            log.info(":{}: legacy success.", method);
        }
    }

}
