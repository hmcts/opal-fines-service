package uk.gov.hmcts.opal.service.legacy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.common.legacy.service.GatewayService;
import uk.gov.hmcts.opal.common.legacy.service.GatewayService.Response;
import uk.gov.hmcts.opal.dto.CreditorAccountDto;
import uk.gov.hmcts.opal.dto.DefendantDto;
import uk.gov.hmcts.opal.dto.GetMinorCreditorAccountHeaderSummaryResponse;
import uk.gov.hmcts.opal.dto.GetMinorCreditorAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.dto.MinorCreditorAccountResponse;
import uk.gov.hmcts.opal.dto.MinorCreditorSearch;
import uk.gov.hmcts.opal.dto.PostMinorCreditorAccountsSearchResponse;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetMinorCreditorAccountAtAGlanceRequest;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetMinorCreditorAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetMinorCreditorAccountHeaderSummaryRequest;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetMinorCreditorAccountHeaderSummaryResponse;
import uk.gov.hmcts.opal.dto.legacy.search.LegacyMinorCreditorSearchResultsRequest;
import uk.gov.hmcts.opal.dto.legacy.search.LegacyMinorCreditorSearchResultsResponse;
import uk.gov.hmcts.opal.mapper.response.GetMinorCreditorAccountAtAGlanceResponseMapper;
import uk.gov.hmcts.opal.generated.model.PatchMinorCreditorAccountRequest;
import uk.gov.hmcts.opal.service.iface.MinorCreditorServiceInterface;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "opal.LegacyMinorCreditorService")
public class LegacyMinorCreditorService implements MinorCreditorServiceInterface {

    private final GatewayService gatewayService;

    private final GetMinorCreditorAccountAtAGlanceResponseMapper atAGlanceResponseMapper;

    private static final String SEARCH_MINOR_CREDITORS = "LIBRA.search_minor_creditors";

    private static final String GET_MINOR_CREDITORS_ACCOUNT_AT_A_GLANCE =
        "LIBRA.get_minor_creditors_account_at_a_glance";

    private static final String GET_MINOR_CREDITORS_ACCOUNT_HEADER_SUMMARY =
        "LIBRA.get_minor_creditors_account_header_summary";

    @Override
    public PostMinorCreditorAccountsSearchResponse searchMinorCreditors(MinorCreditorSearch minorCreditorEntity) {

        Response<LegacyMinorCreditorSearchResultsResponse> response =
            gatewayService.postToGateway(SEARCH_MINOR_CREDITORS,
                                         LegacyMinorCreditorSearchResultsResponse.class,
                                         createRequest(minorCreditorEntity), null
            );

        checkResponseForError(response, "searchMinorCreditors");

        return toMinorSearchDto(response.responseEntity);
    }

    private PostMinorCreditorAccountsSearchResponse toMinorSearchDto(
        LegacyMinorCreditorSearchResultsResponse legacyResponse) {

        if (legacyResponse == null) {
            return PostMinorCreditorAccountsSearchResponse.builder()
                .count(0)
                .creditorAccounts(List.of())
                .build();
        }

        List<CreditorAccountDto> mappedAccounts = Optional.ofNullable(legacyResponse.getCreditorAccounts())
            .orElse(List.of())
            .stream()
            .map(legacy -> CreditorAccountDto.builder()
                .creditorAccountId(legacy.getCreditorAccountId())
                .accountNumber(legacy.getAccountNumber())
                .organisation(legacy.isOrganisation())
                .organisationName(legacy.getOrganisationName())
                .firstnames(legacy.getFirstnames())
                .surname(legacy.getSurname())
                .addressLine1(legacy.getAddressLine1())
                .postcode(legacy.getPostcode())
                .businessUnitName(legacy.getBusinessUnitName())
                .businessUnitId(legacy.getBusinessUnitId())
                .accountBalance(BigDecimal.valueOf(legacy.getAccountBalance()))
                .defendant(
                    legacy.getDefendant() == null ? null :
                        DefendantDto.builder()
                            .defendantAccountId(legacy.getDefendant().getDefendantAccountId())
                            .organisation(legacy.getDefendant().isOrganisation())
                            .organisationName(legacy.getDefendant().getOrganisationName())
                            .firstnames(legacy.getDefendant().getFirstnames())
                            .surname(legacy.getDefendant().getSurname())
                            .build()
                )
                .build())
            .toList();

        return PostMinorCreditorAccountsSearchResponse.builder()
            .count(legacyResponse.getCount())
            .creditorAccounts(mappedAccounts)
            .build();
    }

    @Override
    public GetMinorCreditorAccountAtAGlanceResponse getMinorCreditorAtAGlance(Long minorCreditorId) {

        Response<LegacyGetMinorCreditorAccountAtAGlanceResponse> response =
            gatewayService.postToGateway(GET_MINOR_CREDITORS_ACCOUNT_AT_A_GLANCE,
                LegacyGetMinorCreditorAccountAtAGlanceResponse.class,
                LegacyGetMinorCreditorAccountAtAGlanceRequest.builder().creditorAccountId(
                    String.valueOf(minorCreditorId)).build(),
                null
            );

        checkResponseForError(response, "getMinorCreditorAtAGlance");

        GetMinorCreditorAccountAtAGlanceResponse mapped = atAGlanceResponseMapper.toDto(response.responseEntity);
        mapped.setVersion(response.responseEntity.getCreditorAccountVersion());
        return mapped;
    }

    private LegacyMinorCreditorSearchResultsRequest createRequest(MinorCreditorSearch request) {
        return LegacyMinorCreditorSearchResultsRequest.builder()
            .businessUnitIds(request.getBusinessUnitIds())
            .creditor(request.getCreditor())
            .accountNumber(request.getAccountNumber()).activeAccountsOnly(request.getActiveAccountsOnly()).build();
    }

    @Override
    public GetMinorCreditorAccountHeaderSummaryResponse getHeaderSummary(Long minorCreditorAccountId) {

        Response<LegacyGetMinorCreditorAccountHeaderSummaryResponse> response =
            gatewayService.postToGateway(GET_MINOR_CREDITORS_ACCOUNT_HEADER_SUMMARY,
                LegacyGetMinorCreditorAccountHeaderSummaryResponse.class,
                LegacyGetMinorCreditorAccountHeaderSummaryRequest.builder()
                    .creditorAccountId(String.valueOf(minorCreditorAccountId)).build(),
                null
            );

        checkResponseForError(response, "getHeaderSummary");


        throw new UnsupportedOperationException(
            "Legacy mode not implemented for GET /minor-creditor-accounts/{id}/header-summary");
    }

    @Override
    public MinorCreditorAccountResponse updateMinorCreditorAccount(
        Long minorCreditorAccountId,
        PatchMinorCreditorAccountRequest request,
        BigInteger etag,
        String postedBy,
        Short businessUnitId
    ) {
        log.debug(":updateMinorCreditorAccount: Legacy mode not implemented. minorCreditorAccountId={}",
            minorCreditorAccountId);

        throw new UnsupportedOperationException(
            "Legacy mode not implemented for PATCH /minor-creditor-accounts/{id}");
    }

    private static <T> void checkResponseForError(Response<T> response, String method) {
        if (response.isError()) {
            log.error(":{}: Legacy Gateway response: HTTP Response Code {}", method, response.code);
            if (response.isException()) {
                log.error(":{}: Exception Message:", method, response.exception);
            } else if (response.isLegacyFailure()) {
                log.error(":{}: Legacy Failure: Body:\n{}", method, response.body);
            }
        } else if (response.isSuccessful()) {
            log.info(":{}: Legacy Gateway response: Success.", method);
        }
    }
}
