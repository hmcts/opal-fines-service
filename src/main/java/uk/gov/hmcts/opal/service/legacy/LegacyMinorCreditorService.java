package uk.gov.hmcts.opal.service.legacy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.CreditorAccountDto;
import uk.gov.hmcts.opal.dto.DefendantDto;
import uk.gov.hmcts.opal.dto.GetMinorCreditorAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.dto.MinorCreditorSearch;
import uk.gov.hmcts.opal.dto.PostMinorCreditorAccountsSearchResponse;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetMinorCreditorAccountAtAGlanceRequest;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetMinorCreditorAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.dto.legacy.search.LegacyMinorCreditorSearchResultsRequest;
import uk.gov.hmcts.opal.dto.legacy.search.LegacyMinorCreditorSearchResultsResponse;
import uk.gov.hmcts.opal.service.iface.MinorCreditorServiceInterface;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "opal.LegacyMinorCreditorService")
public class LegacyMinorCreditorService implements MinorCreditorServiceInterface {

    private final GatewayService gatewayService;

    private static final String SEARCH_MINOR_CREDITORS = "LIBRA.search_minor_creditors";

    private static final String GET_MINOR_CREDITORS_ACCOUNT_AT_A_GLANCE =
        "LIBRA.get_minor_creditors_account_at_a_glance";

    @Override
    public PostMinorCreditorAccountsSearchResponse searchMinorCreditors(MinorCreditorSearch minorCreditorEntity) {

        GatewayService.Response<LegacyMinorCreditorSearchResultsResponse> response =
            gatewayService.postToGateway(SEARCH_MINOR_CREDITORS,
                                         LegacyMinorCreditorSearchResultsResponse.class,
                                         createRequest(minorCreditorEntity), null
            );

        if (response.isError()) {
            log.error(":searchMinorCreditor: Legacy Gateway response: HTTP Response Code: {}", response.code);
            if (response.isException()) {
                log.error(":searchMinorCreditor:", response.exception);
            } else if (response.isLegacyFailure()) {
                log.error(":searchMinorCreditor: Legacy Gateway: body: \n{}", response.body);
                LegacyMinorCreditorSearchResultsResponse responseEntity = response.responseEntity;
                log.error(":searchMinorCreditor: Legacy Gateway: entity: \n{}", responseEntity.toXml());
            }
        } else if (response.isSuccessful()) {
            log.info(":searchMinorCreditor: Legacy Gateway response: Success.");
        }
        return toMinorSearchDto(response.responseEntity);
    }

    @Override
    public GetMinorCreditorAccountAtAGlanceResponse getMinorCreditorAtAGlance(String minorCreditorId) {

        GatewayService.Response<LegacyGetMinorCreditorAccountAtAGlanceResponse> response =
            gatewayService.postToGateway(GET_MINOR_CREDITORS_ACCOUNT_AT_A_GLANCE,
                                         LegacyGetMinorCreditorAccountAtAGlanceResponse.class,
                                         LegacyGetMinorCreditorAccountAtAGlanceRequest.createRequest(minorCreditorId),
                null
            );

        if (response.isError()) {
            log.error(":getMinorCreditorAtAGlance: Legacy Gateway response: HTTP Response Code: {}", response.code);
            if (response.isException()) {
                log.error(":getMinorCreditorAtAGlance:", response.exception);
            } else if (response.isLegacyFailure()) {
                log.error(":getMinorCreditorAtAGlance: Legacy Gateway: body: \n{}", response.body);
                LegacyGetMinorCreditorAccountAtAGlanceResponse responseEntity = response.responseEntity;
                log.error(":getMinorCreditorAtAGlance: Legacy Gateway: entity: \n{}", responseEntity.toXml());
            }
        } else if (response.isSuccessful()) {
            log.info(":getMinorCreditorAtAGlance: Legacy Gateway response: Success.");
        }
        return response.responseEntity == null
            ?
            GetMinorCreditorAccountAtAGlanceResponse.builder()
                .party(null)
                .address(null)
                .creditorAccountId(null)
                .defendant(null)
                .payment(null)
                .build()
            : response.responseEntity.toOpalResponse();
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


    private LegacyMinorCreditorSearchResultsRequest createRequest(MinorCreditorSearch request) {
        return LegacyMinorCreditorSearchResultsRequest.builder()
            .businessUnitIds(request.getBusinessUnitIds())
            .creditor(request.getCreditor())
            .accountNumber(request.getAccountNumber()).activeAccountsOnly(request.getActiveAccountsOnly()).build();
    }
}
