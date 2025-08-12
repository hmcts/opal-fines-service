package uk.gov.hmcts.opal.service.legacy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.PostMinorCreditorAccountsSearchResponse;
import uk.gov.hmcts.opal.dto.legacy.CreditorAccount;
import uk.gov.hmcts.opal.dto.legacy.Defendant;
import uk.gov.hmcts.opal.dto.legacy.search.LegacyMinorCreditorSearchResultsRequest;
import uk.gov.hmcts.opal.dto.legacy.search.LegacyMinorCreditorSearchResultsResponse;
import uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorSearch;
import uk.gov.hmcts.opal.service.iface.MinorCreditorServiceInterface;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "opal.LegacyMinorCreditorService")
public class LegacyMinorCreditorService implements MinorCreditorServiceInterface {

    private final GatewayService gatewayService;

    private static final String SEARCH_MINOR_CREDITORS = "searchMinorCreditors";

    @Override
    public PostMinorCreditorAccountsSearchResponse searchMinorCreditors(MinorCreditorSearch minorCreditorEntity) {

        LegacyMinorCreditorSearchResultsRequest request = LegacyMinorCreditorSearchResultsRequest.builder().build();

        GatewayService.Response<LegacyMinorCreditorSearchResultsResponse> response =
            gatewayService.postToGateway(SEARCH_MINOR_CREDITORS, LegacyMinorCreditorSearchResultsResponse.class, request
        );

            if (response.isError()) {
                log.error(":getHeaderSummary: Legacy Gateway response: HTTP Response Code: {}", response.code);
                if (response.isException()) {
                    log.error(":getHeaderSummary:", response.exception);
                } else if (response.isLegacyFailure()) {
                    log.error(":getHeaderSummary: Legacy Gateway: body: \n{}", response.body);
                    LegacyMinorCreditorSearchResultsResponse responseEntity = response.responseEntity;
                    log.error(":getHeaderSummary: Legacy Gateway: entity: \n{}", responseEntity.toXml());
                }
            } else if (response.isSuccessful()) {
                log.info(":getHeaderSummary: Legacy Gateway response: Success.");
            }
        return toMinorSearchDto(response.responseEntity);
    }

    private PostMinorCreditorAccountsSearchResponse toMinorSearchDto(LegacyMinorCreditorSearchResultsResponse legacyResponse) {
        return PostMinorCreditorAccountsSearchResponse.builder()
            .count(legacyResponse != null ? legacyResponse.getCount() : 0)
            .creditorAccounts(
                legacyResponse != null && legacyResponse.getCreditorAccounts() != null
                    ? legacyResponse.getCreditorAccounts().stream()
                    .map(legacy -> CreditorAccount.builder()
                        .creditorAccountId(legacy.getCreditorAccountId())
                        .accountNumber(legacy.getAccountNumber())
                        .organisation(legacy.isOrganisation())
                        .addressLine1(legacy.getAddressLine1())
                        .postcode(legacy.getPostcode())
                        .businessUnitName(legacy.getBusinessUnitName())
                        .businessUnitId(legacy.getBusinessUnitId())
                        .accountBalance(legacy.getAccountBalance())
                        .organisationName(legacy.getOrganisationName())
                        .surname(legacy.getSurname())
                        .firstnames(legacy.getFirstnames())
                        .defendant(
                            legacy.getDefendant() != null
                                ? Defendant.builder()
                                .organisationName(legacy.getDefendant().getOrganisationName())
                                .defendantSurname(legacy.getDefendant().getDefendantSurname())
                                .defendantFirstnames(legacy.getDefendant().getDefendantFirstnames())
                                .build()
                                : null
                        )
                        .build()
                    )
                    .collect(Collectors.toList())
                    : null
            )
            .build();

    }}

