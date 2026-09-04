package uk.gov.hmcts.opal.service.legacy;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.opal.common.legacy.service.GatewayService;
import uk.gov.hmcts.opal.common.legacy.service.GatewayService.Response;
import uk.gov.hmcts.opal.dto.GetMinorCreditorAccountHeaderSummaryResponse;
import uk.gov.hmcts.opal.dto.MinorCreditorAccountResponse;
import uk.gov.hmcts.opal.dto.legacy.GetMinorCreditorAccountHeaderSummaryLegacyRequest;
import uk.gov.hmcts.opal.dto.legacy.GetMinorCreditorAccountHeaderSummaryLegacyResponse;
import uk.gov.hmcts.opal.dto.legacy.GetMinorCreditorAccountHeaderSummaryLegacyResponse.CreditorHeaderLegacy;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetMinorCreditorAccountAtAGlanceRequest;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetMinorCreditorAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetMinorCreditorAccountRequest;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetMinorCreditorAccountResponse;
import uk.gov.hmcts.opal.dto.legacy.LegacyUpdateMinorCreditorAccountRequest;
import uk.gov.hmcts.opal.dto.legacy.LegacyUpdateMinorCreditorAccountResponse;
import uk.gov.hmcts.opal.dto.legacy.search.LegacyMinorCreditorSearchResultsRequest;
import uk.gov.hmcts.opal.dto.legacy.search.LegacyMinorCreditorSearchResultsResponse;
import uk.gov.hmcts.opal.dto.response.GetMinorCreditorHistoryResponse;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountEntity;
import uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorHistoryFilters;
import uk.gov.hmcts.opal.generated.model.MinorCreditorAccountSearchDefendant;
import uk.gov.hmcts.opal.generated.model.MinorCreditorAccountSearchResultMinorCreditor;
import uk.gov.hmcts.opal.generated.model.MinorCreditorAccountsSearchResponse;
import uk.gov.hmcts.opal.generated.model.MinorCreditorSearchRequest;
import uk.gov.hmcts.opal.generated.model.PatchMinorCreditorAccountRequest;
import uk.gov.hmcts.opal.generated.model.MinorCreditorAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.mapper.MinorCreditorMapper;
import uk.gov.hmcts.opal.mapper.legacy.GetMinorCreditorAccountHeaderSummaryResponseLegacyMapper;
import uk.gov.hmcts.opal.mapper.legacy.LegacyMinorCreditorAccountResponseMapper;
import uk.gov.hmcts.opal.mapper.legacy.LegacyUpdateMinorCreditorAccountResponseMapper;
import uk.gov.hmcts.opal.mapper.request.UpdateMinorCreditorAccountRequestMapper;
import uk.gov.hmcts.opal.mapper.response.MinorCreditorAccountAtAGlanceResponseMapper;
import uk.gov.hmcts.opal.repository.CreditorAccountRepository;
import uk.gov.hmcts.opal.service.iface.MinorCreditorServiceInterface;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "opal.LegacyMinorCreditorService")
public class LegacyMinorCreditorService implements MinorCreditorServiceInterface {

    private static final String SEARCH_MINOR_CREDITORS = "getMinorCreditorAccount";
    private static final String GET_MINOR_CREDITOR_ACCOUNT_PARTY = "GET_MINOR_CREDITOR_ACCOUNT_PARTY";
    private static final String GET_MINOR_CREDITORS_ACCOUNT_AT_A_GLANCE =
        "LIBRA.get_minor_creditors_account_at_a_glance";
    private static final String GET_MINOR_CREDITORS_ACCOUNT_HEADER_SUMMARY =
        "LIBRA.get_minor_creditors_account_header_summary";
    private static final String UPDATE_MINOR_CREDITOR_ACCOUNT = "updateMinorCreditorAccount";

    private final GatewayService gatewayService;
    private final MinorCreditorAccountAtAGlanceResponseMapper atAGlanceResponseMapper;
    private final LegacyMinorCreditorAccountResponseMapper minorCreditorAccountResponseMapper;
    private final CreditorAccountRepository creditorAccountRepository;
    private final GetMinorCreditorAccountHeaderSummaryResponseLegacyMapper headerSummaryResponseMapper;
    private final UpdateMinorCreditorAccountRequestMapper updateMinorCreditorAccountRequestMapper;
    private final LegacyUpdateMinorCreditorAccountResponseMapper updateMinorCreditorAccountResponseMapper;
    private final LegacyBusinessUnitCodeResolver legacyBusinessUnitCodeResolver;
    private final MinorCreditorMapper minorCreditorMapper;

    @Override
    public MinorCreditorAccountsSearchResponse searchMinorCreditors(MinorCreditorSearchRequest minorCreditorEntity) {

        Response<LegacyMinorCreditorSearchResultsResponse> response =
            gatewayService.postToGateway(SEARCH_MINOR_CREDITORS,
                LegacyMinorCreditorSearchResultsResponse.class,
                createRequest(minorCreditorEntity),
                null
            );

        checkResponseForError(response, "searchMinorCreditors");

        return toMinorSearchDto(response.responseEntity);
    }

    @Override
    public MinorCreditorAccountAtAGlanceResponse getMinorCreditorAtAGlance(Long minorCreditorId) {

        Response<LegacyGetMinorCreditorAccountAtAGlanceResponse> response =
            gatewayService.postToGateway(GET_MINOR_CREDITORS_ACCOUNT_AT_A_GLANCE,
                LegacyGetMinorCreditorAccountAtAGlanceResponse.class,
                LegacyGetMinorCreditorAccountAtAGlanceRequest.builder()
                    .creditorAccountId(String.valueOf(minorCreditorId))
                    .build(),
                null
            );

        checkResponseForError(response, "getMinorCreditorAtAGlance");

        return atAGlanceResponseMapper.toDto(response.responseEntity);
    }

    @Override
    public GetMinorCreditorAccountHeaderSummaryResponse getHeaderSummary(Long minorCreditorAccountId) {

        Response<GetMinorCreditorAccountHeaderSummaryLegacyResponse> response =
            gatewayService.postToGateway(GET_MINOR_CREDITORS_ACCOUNT_HEADER_SUMMARY,
                GetMinorCreditorAccountHeaderSummaryLegacyResponse.class,
                GetMinorCreditorAccountHeaderSummaryLegacyRequest.builder()
                    .creditorAccountId(String.valueOf(minorCreditorAccountId))
                    .build(),
                null
            );

        checkResponseForError(response, "getHeaderSummary");

        GetMinorCreditorAccountHeaderSummaryResponse mapped = headerSummaryResponseMapper
            .toOpal(response.responseEntity);

        Optional<CreditorAccountEntity> creditorAccount = creditorAccountRepository
            .findById(minorCreditorAccountId);
        mapped.setRepayment(creditorAccount.map(CreditorAccountEntity::isRepayment)
            .orElse(false));

        CreditorHeaderLegacy creditor = response.responseEntity.getCreditor();
        mapped.setVersion(creditor.getAccountVersion());
        applyResolvedBusinessUnitCode(mapped, response.responseEntity.getBusinessUnit());

        return mapped;
    }

    private void applyResolvedBusinessUnitCode(
        GetMinorCreditorAccountHeaderSummaryResponse mapped,
        uk.gov.hmcts.opal.dto.legacy.common.BusinessUnitSummary legacyBusinessUnit
    ) {
        if (mapped.getBusinessUnit() == null || legacyBusinessUnit == null) {
            return;
        }

        mapped.getBusinessUnit().setBusinessUnitCode(
            legacyBusinessUnitCodeResolver.resolve(
                legacyBusinessUnit.getBusinessUnitId(),
                legacyBusinessUnit.getBusinessUnitCode()
            )
        );
    }

    @Override
    public MinorCreditorAccountResponse getMinorCreditorAccount(Long minorCreditorAccountId) {
        Response<LegacyGetMinorCreditorAccountResponse> response =
            gatewayService.postToGateway(
                GET_MINOR_CREDITOR_ACCOUNT_PARTY,
                LegacyGetMinorCreditorAccountResponse.class,
                LegacyGetMinorCreditorAccountRequest.builder()
                    .accountId(String.valueOf(minorCreditorAccountId))
                    .build(),
                null
            );

        checkResponseForError(response, "getMinorCreditorAccount");

        MinorCreditorAccountResponse mappedResponse =
            minorCreditorAccountResponseMapper.toMinorCreditorAccountResponse(response.responseEntity);

        if (mappedResponse != null) {
            Optional<CreditorAccountEntity> creditorAccount =
                creditorAccountRepository.findById(minorCreditorAccountId);
            mappedResponse.setBusinessUnitId(
                creditorAccount.map(CreditorAccountEntity::getBusinessUnitId).orElse(null)
            );
            mappedResponse.setRepayment(
                creditorAccount.map(CreditorAccountEntity::isRepayment).orElse(false)
            );
        }

        return mappedResponse;
    }

    @Override
    public GetMinorCreditorHistoryResponse getMinorCreditorHistory(
        Long minorCreditorAccountId,
        MinorCreditorHistoryFilters filters) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Not yet implemented");
    }

    @Override
    public MinorCreditorAccountResponse updateMinorCreditorAccount(
        Long minorCreditorAccountId,
        PatchMinorCreditorAccountRequest request,
        BigInteger etag,
        String postedBy,
        String postedByName,
        Short businessUnitId
    ) {
        log.info("Legacy :updateMinorCreditorAccount: id={}", minorCreditorAccountId);

        LegacyUpdateMinorCreditorAccountRequest legacyRequest =
            updateMinorCreditorAccountRequestMapper.toLegacyUpdateMinorCreditorAccountRequest(
                minorCreditorAccountId,
                businessUnitId,
                postedBy,
                etag,
                request
            );

        Response<LegacyUpdateMinorCreditorAccountResponse> response =
            gatewayService.postToGateway(
                UPDATE_MINOR_CREDITOR_ACCOUNT,
                LegacyUpdateMinorCreditorAccountResponse.class,
                legacyRequest,
                null
            );

        checkResponseForError(response, "updateMinorCreditorAccount");

        return updateMinorCreditorAccountResponseMapper.toMinorCreditorAccountResponse(response.responseEntity);
    }

    private LegacyMinorCreditorSearchResultsRequest createRequest(MinorCreditorSearchRequest request) {
        return LegacyMinorCreditorSearchResultsRequest.builder()
            .businessUnitIds(request.getBusinessUnitIds())
            .creditor(minorCreditorMapper.toCreditor(request.getCreditor()))
            .accountNumber(request.getAccountNumber())
            .activeAccountsOnly(Boolean.TRUE.equals(request.getActiveAccountsOnly()))
            .build();
    }

    private MinorCreditorAccountsSearchResponse toMinorSearchDto(
        LegacyMinorCreditorSearchResultsResponse legacyResponse) {

        if (legacyResponse == null) {
            return MinorCreditorAccountsSearchResponse.builder()
                .count(0)
                .creditorAccounts(List.of())
                .build();
        }

        List<MinorCreditorAccountSearchResultMinorCreditor> mappedAccounts = Optional
            .ofNullable(legacyResponse.getCreditorAccounts())
            .orElse(List.of())
            .stream()
            .map(legacy -> MinorCreditorAccountSearchResultMinorCreditor.builder()
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
                        MinorCreditorAccountSearchDefendant.builder()
                            .defendantAccountId(legacy.getDefendant().getDefendantAccountId())
                            .organisation(legacy.getDefendant().isOrganisation())
                            .organisationName(legacy.getDefendant().getOrganisationName())
                            .firstnames(legacy.getDefendant().getFirstnames())
                            .surname(legacy.getDefendant().getSurname())
                            .build()
                )
                .build())
            .toList();

        return MinorCreditorAccountsSearchResponse.builder()
            .count(legacyResponse.getCount())
            .creditorAccounts(mappedAccounts)
            .build();
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
