package uk.gov.hmcts.opal.service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.user.authorisation.exception.PermissionNotAllowedException;
import uk.gov.hmcts.opal.common.user.authorisation.model.BusinessUnitUserV2;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserStateV2;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.AddDefendantAccountEnforcementRequest;
import uk.gov.hmcts.opal.dto.AddEnforcementResponse;
import uk.gov.hmcts.opal.dto.DefendantAccountHeaderSummary;
import uk.gov.hmcts.opal.dto.EnforcementStatus;
import uk.gov.hmcts.opal.dto.GetDefendantAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.dto.GetDefendantAccountConsolidatedAccountsResult;
import uk.gov.hmcts.opal.dto.GetDefendantAccountFixedPenaltyResponse;
import uk.gov.hmcts.opal.dto.UpdateDefendantAccountRequest;
import uk.gov.hmcts.opal.dto.UpdateDefendantAccountResponse;
import uk.gov.hmcts.opal.dto.history.DefendantAccountHistoryFilter;
import uk.gov.hmcts.opal.dto.history.DefendantAccountHistoryResponse;
import uk.gov.hmcts.opal.dto.history.HistoryItemType;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;
import uk.gov.hmcts.opal.dto.search.DefendantAccountSearchResultsDto;
import uk.gov.hmcts.opal.exception.RequiredPermissionException;
import uk.gov.hmcts.opal.exception.ResourceConflictException;
import uk.gov.hmcts.opal.generated.model.PostDefendantAccountSearchRequestDefendantAccount;
import uk.gov.hmcts.opal.generated.model.PostDefendantAccountSearchResponseDefendantAccount;
import uk.gov.hmcts.opal.generated.model.UpdateDefendantAccountRequestPayload;
import uk.gov.hmcts.opal.mapper.request.DefendantAccountSearchRequestMapper;
import uk.gov.hmcts.opal.mapper.response.DefendantAccountSearchResponseMapper;
import uk.gov.hmcts.opal.service.proxy.DefendantAccountServiceProxy;
import uk.gov.hmcts.opal.util.VersionUtils;

@Service
@Slf4j(topic = "opal.DefendantAccountService")
@RequiredArgsConstructor
public class DefendantAccountService {

    private final DefendantAccountServiceProxy defendantAccountServiceProxy;

    private final UserStateService userStateService;

    private final DefendantAccountSearchRequestMapper defendantAccountSearchRequestMapper;

    private final DefendantAccountSearchResponseMapper defendantAccountSearchResponseMapper;

    private final DefendantAccountSearchRequestValidator defendantAccountSearchRequestValidator;

    public DefendantAccountHeaderSummary getHeaderSummary(Long defendantAccountId) {
        log.debug(":getHeaderSummary:");

        UserStateV2 userState = userStateService.getUserStateFromSecurityContext();

        if (!userState.anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS)) {
            throw new PermissionNotAllowedException(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);
        }

        return defendantAccountServiceProxy.getHeaderSummary(defendantAccountId);
    }

    public GetDefendantAccountConsolidatedAccountsResult getConsolidatedAccounts(Long defendantAccountId) {
        log.debug(":getConsolidatedAccounts:");

        UserState userState = userStateService.getUserStateV1FromSecurityContext();

        if (!userState.anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS)) {
            throw new RequiredPermissionException(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);
        }

        return defendantAccountServiceProxy.getConsolidatedAccounts(defendantAccountId);
    }

    public DefendantAccountHistoryResponse getHistory(Long defendantAccountId,
        LocalDate dateFrom,
        LocalDate dateTo,
        List<String> itemTypes) {
        log.debug(":getHistory:");

        UserStateV2 userState = userStateService.getUserStateFromSecurityContext();

        if (!userState.anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS)) {
            throw new PermissionNotAllowedException(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);
        }

        DefendantAccountHistoryFilter filter = DefendantAccountHistoryFilter.builder()
            .dateFrom(dateFrom)
            .dateTo(dateTo)
            .itemTypes(toHistoryItemTypes(itemTypes))
            .build();

        return defendantAccountServiceProxy.getHistory(defendantAccountId, filter);
    }

    public PostDefendantAccountSearchResponseDefendantAccount searchDefendantAccounts(
        PostDefendantAccountSearchRequestDefendantAccount request
    ) {
        defendantAccountSearchRequestValidator.validateAndCheckFeature(request);

        AccountSearchDto accountSearchDto = defendantAccountSearchRequestMapper.toAccountSearchDto(request);
        DefendantAccountSearchResultsDto results = searchDefendantAccounts(accountSearchDto);

        return defendantAccountSearchResponseMapper.toResponse(results);
    }

    public DefendantAccountSearchResultsDto searchDefendantAccounts(AccountSearchDto accountSearchDto) {
        log.debug(":searchDefendantAccounts:");

        UserStateV2 userState = userStateService.getUserStateFromSecurityContext();

        if (userState.anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS)) {

            return defendantAccountServiceProxy.searchDefendantAccounts(accountSearchDto);
        } else {
            throw new PermissionNotAllowedException(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);
        }
    }

    private List<HistoryItemType> toHistoryItemTypes(List<String> itemTypes) {
        return itemTypes == null ? List.of() : itemTypes.stream()
            .flatMap(itemType -> Arrays.stream(itemType.split(",")))
            .map(String::trim)
            .filter(itemType -> !itemType.isEmpty())
            .map(HistoryItemType::fromValue)
            .toList();
    }

    public GetDefendantAccountPartyResponse getDefendantAccountParty(
        Long defendantAccountId,
        Long defendantAccountPartyId) {

        log.debug(":getDefendantAccountParty:");

        UserStateV2 userState = userStateService.getUserStateFromSecurityContext();

        if (userState.anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS)) {

            return defendantAccountServiceProxy.getDefendantAccountParty(defendantAccountId, defendantAccountPartyId);
        } else {
            throw new PermissionNotAllowedException(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);
        }
    }

    public GetDefendantAccountPaymentTermsResponse getPaymentTerms(Long defendantAccountId) {

        log.debug(":getPaymentTerms:");

        UserStateV2 userState = userStateService.getUserStateFromSecurityContext();

        if (userState.anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS)) {
            return defendantAccountServiceProxy.getPaymentTerms(defendantAccountId);
        } else {
            throw new PermissionNotAllowedException(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);
        }
    }

    public DefendantAccountAtAGlanceResponse getAtAGlance(Long defendantAccountId) {
        log.debug(":getAtAGlance");

        UserStateV2 userState = userStateService.getUserStateFromSecurityContext();

        if (userState.anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS)) {
            return defendantAccountServiceProxy.getAtAGlance(defendantAccountId);
        } else {
            throw new PermissionNotAllowedException(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);
        }
    }

    public GetDefendantAccountFixedPenaltyResponse getDefendantAccountFixedPenalty(
        Long defendantAccountId) {

        UserStateV2 userState = userStateService.getUserStateFromSecurityContext();

        if (!userState.anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS)) {
            throw new PermissionNotAllowedException(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);
        }

        return defendantAccountServiceProxy.getDefendantAccountFixedPenalty(defendantAccountId);
    }

    public UpdateDefendantAccountResponse updateDefendantAccount(Long defendantAccountId,
        String businessUnitId,
        UpdateDefendantAccountRequestPayload request,
        String ifMatch) {
        log.debug(":updateDefendantAccount:");

        if (ifMatch == null) {
            throw new ResourceConflictException(
                "Defendant Account", defendantAccountId, "If-Match header is required", null);
        }

        UserStateV2 userState = userStateService.getUserStateFromSecurityContext();

        if (userState.anyBusinessUnitUserHasPermission(FinesPermission.ACCOUNT_MAINTENANCE)) {

            //TODO Must be a way to enforce this through the OpenAPI code gen
            int nonNullCount = 0;
            if (request.getCommentAndNotes() != null) {
                nonNullCount++;
            }
            if (request.getEnforcementCourt() != null) {
                nonNullCount++;
            }
            if (request.getCollectionOrder() != null) {
                nonNullCount++;
            }
            if (request.getEnforcementOverride() != null) {
                nonNullCount++;
            }
            if (nonNullCount != 1) {
                throw new IllegalArgumentException("Exactly one update group must be provided");
            }

            String postedBy = userState.getBusinessUnitUserForBusinessUnit(Short.parseShort(businessUnitId))
                .map(uk.gov.hmcts.opal.common.user.authorisation.model.BusinessUnitUserV2::getBusinessUnitUserId)
                .filter(id -> !id.isBlank())
                .orElse(userState.getUsername());

            //Create internal DTO
            UpdateDefendantAccountRequest updateRequest = UpdateDefendantAccountRequest.builder()
                .defendantAccountId(defendantAccountId)
                .businessUnitId(businessUnitId)
                .businessUnitUserId(postedBy)
                .payload(request)
                .version(VersionUtils.extractBigInteger(ifMatch))
                .build();

            return defendantAccountServiceProxy.updateDefendantAccount(
                defendantAccountId, businessUnitId, updateRequest, postedBy, userState.getUsername()
            );
        } else {
            throw new PermissionNotAllowedException(FinesPermission.ACCOUNT_MAINTENANCE);
        }
    }

    public EnforcementStatus getEnforcementStatus(Long defendantAccountId) {

        log.debug(":getEnforcementStatus:");

        UserStateV2 userState = userStateService.getUserStateFromSecurityContext();

        if (userState.anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS)) {
            return defendantAccountServiceProxy.getEnforcementStatus(defendantAccountId);
        } else {
            throw new PermissionNotAllowedException(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);
        }
    }

    public GetDefendantAccountPartyResponse replaceDefendantAccountParty(Long defendantAccountId,
        Long defendantAccountPartyId,
        String ifMatch, String businessUnitId, DefendantAccountParty request) {
        log.debug(":replaceDefendantAccountParty");

        UserStateV2 userState = userStateService.getUserStateFromSecurityContext();

        short buId = Short.parseShort(businessUnitId);

        String postedBy = userState.getBusinessUnitUserForBusinessUnit(buId)
            .map(uk.gov.hmcts.opal.common.user.authorisation.model.BusinessUnitUserV2::getBusinessUnitUserId)
            .filter(id -> !id.isBlank())
            .orElse(userState.getUsername());

        if (userState.hasBusinessUnitUserWithPermission(buId,
                FinesPermission.ACCOUNT_MAINTENANCE)) {
            return defendantAccountServiceProxy.replaceDefendantAccountParty(defendantAccountId,
                defendantAccountPartyId, request, ifMatch, businessUnitId, postedBy, userState.getUsername(),
                getBusinessUnitUserIdForBusinessUnit(userState, buId));
        } else {
            throw new PermissionNotAllowedException(buId, FinesPermission.ACCOUNT_MAINTENANCE);
        }
    }

    private String getBusinessUnitUserIdForBusinessUnit(UserStateV2 userState, short buId) {
        return userState.getBusinessUnitUserForBusinessUnit(buId)
            .map(BusinessUnitUserV2::getBusinessUnitUserId)
            .filter(id -> !id.isBlank())
            .orElse("");
    }


    public AddPaymentCardRequestResponse addPaymentCardRequest(
        Long defendantAccountId,
        String businessUnitId,
        String businessUnitUserId,
        String ifMatch
    ) {
        log.debug(":addPaymentCardRequest:");

        UserStateV2 userState = userStateService.getUserStateFromSecurityContext();

        if (userState.anyBusinessUnitUserHasPermission(FinesPermission.AMEND_PAYMENT_TERMS)) {
            return defendantAccountServiceProxy.addPaymentCardRequest(
                defendantAccountId,
                businessUnitId,
                businessUnitUserId,
                ifMatch
            );
        } else {
            throw new PermissionNotAllowedException(FinesPermission.AMEND_PAYMENT_TERMS);
        }
    }

    public AddEnforcementResponse addEnforcement(Long defendantAccountId,
        String businessUnitId,
        String ifMatch,
        AddDefendantAccountEnforcementRequest request) {

        log.debug(":addEnforcement:");

        UserStateV2 userState = userStateService.getUserStateFromSecurityContext();

        String businessUnitUserId = userState.getBusinessUnitUserForBusinessUnit(Short.parseShort(businessUnitId))
            .map(uk.gov.hmcts.opal.common.user.authorisation.model.BusinessUnitUserV2::getBusinessUnitUserId)
            .filter(id -> !id.isBlank())
            .orElse(null);

        if (userState.anyBusinessUnitUserHasPermission(FinesPermission.ENTER_ENFORCEMENT)) {
            return defendantAccountServiceProxy.addEnforcement(
                defendantAccountId, businessUnitId, businessUnitUserId, ifMatch, request
            );
        } else {
            throw new PermissionNotAllowedException(FinesPermission.ENTER_ENFORCEMENT);
        }
    }

    public GetDefendantAccountPaymentTermsResponse addPaymentTerms(Long defendantAccountId,
        String businessUnitId,
        String ifMatch,
        AddDefendantAccountPaymentTermsRequest addPaymentTermsRequest) {

        log.debug(":addPaymentTerms:");

        UserStateV2 userState = userStateService.getUserStateFromSecurityContext();

        short buId = Short.parseShort(businessUnitId);
        String businessUnitUserId = userState.getBusinessUnitUserForBusinessUnit(buId)
            .map(uk.gov.hmcts.opal.common.user.authorisation.model.BusinessUnitUserV2::getBusinessUnitUserId)
            .filter(id -> !id.isBlank())
            .orElse(userState.getUsername());

        if (addPaymentTermsRequest != null && addPaymentTermsRequest.getPaymentTerms() != null) {
            addPaymentTermsRequest.getPaymentTerms().setPostedDetails(PostedDetails.builder()
                .postedBy(businessUnitUserId)
                .postedByName(userState.getName())
                .build());
        }

        if (userState.hasBusinessUnitUserWithPermission(buId,
            FinesPermission.AMEND_PAYMENT_TERMS)) {
            return defendantAccountServiceProxy.addPaymentTerms(defendantAccountId,
                businessUnitId,
                businessUnitUserId,
                ifMatch,
                addPaymentTermsRequest);
        } else {
            throw new PermissionNotAllowedException(buId, FinesPermission.AMEND_PAYMENT_TERMS);
        }
    }
}
