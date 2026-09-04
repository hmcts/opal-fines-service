package uk.gov.hmcts.opal.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.opal.util.FeatureFlags.RELEASE_1B;
import static uk.gov.hmcts.opal.util.FeatureFlags.RELEASE_1B_ENABLED_PROPERTY;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.common.launchdarkly.FeatureToggle;
import uk.gov.hmcts.opal.controllers.print.PrintRequestController;

class Release1bFeatureToggleAnnotationTest {

    @Test
    void shouldApplyRelease1bFeatureToggleToOnlyTheRequestedEndpoints() {
        List<Class<?>> controllerClasses = List.of(
            AmendmentController.class,
            BusinessUnitController.class,
            CourtController.class,
            CreateFineAccountsController.class,
            DebtorProfileSearchController.class,
            DefendantAccountApiController.class,
            DefendantAccountController.class,
            DraftAccountController.class,
            EnforcerController.class,
            LocalJusticeAreaController.class,
            MajorCreditorApiController.class,
            MajorCreditorController.class,
            MinorCreditorApiController.class,
            MinorCreditorController.class,
            NotesController.class,
            OffenceController.class,
            PrintRequestController.class,
            ProscutorController.class,
            ResultsApiController.class,
            RootController.class,
            TestingSupportController.class
        );

        Set<String> actualAnnotatedMethods = controllerClasses.stream()
            .flatMap(controllerClass -> Arrays.stream(controllerClass.getDeclaredMethods()))
            .filter(this::isRelease1bFeatureToggle)
            .map(method -> method.getDeclaringClass().getSimpleName() + "#" + method.getName())
            .collect(Collectors.toSet());

        Set<String> expectedAnnotatedMethods = Set.of(
            "DefendantAccountController#addPaymentTerms",
            "MajorCreditorApiController#getMajorCreditorHistory",
            "DefendantAccountApiController#getDefendantAccountHistory",
            "DefendantAccountController#replaceDefendantAccountParty",
            "DefendantAccountApiController#addEnforcement",
            "DefendantAccountApiController#addPaymentCardRequest",
            "DefendantAccountApiController#updateDefendantAccount",
            "DefendantAccountApiController#getConsolidatedAccounts",
            "MinorCreditorController#getMinorCreditorAccountHeaderSummary",
            "DefendantAccountApiController#postDefendantAccountSearch",
            "DefendantAccountApiController#removeDefendantAccountParty",
            "MinorCreditorApiController#getMinorCreditorAccountAtAGlance",
            "MajorCreditorApiController#getCentralFundByBusinessUnit",
            "DefendantAccountApiController#getEnforcementStatus",
            "DefendantAccountApiController#getDefendantAccountAtAGlance",
            "DefendantAccountController#getDefendantAccountParty",
            "DefendantAccountController#addDefendantAccountParty",
            "DefendantAccountApiController#getDefendantAccountFixedPenalty",
            "DefendantAccountApiController#removeEnforcementHold",
            "ResultsApiController#getResultById",
            "NotesController#addNote",
            "MinorCreditorApiController#getMinorCreditorAccount",
            "MajorCreditorApiController#getMajorCreditorAccountHeaderSummary",
            "MinorCreditorApiController#getMinorCreditorHistory",
            "DefendantAccountApiController#getImpositions",
            "MinorCreditorApiController#patchMinorCreditorAccount",
            "DefendantAccountApiController#getDefendantAccountHeaderSummary",
            "MinorCreditorApiController#postMinorCreditorSearch",
            "MajorCreditorApiController#getMajorCreditorAccountAtAGlance",
            "DefendantAccountController#defendantAccountPaymentTerms"
        );

        assertEquals(expectedAnnotatedMethods, actualAnnotatedMethods);

        controllerClasses.stream()
            .flatMap(controllerClass -> Arrays.stream(controllerClass.getDeclaredMethods()))
            .filter(this::isRelease1bFeatureToggle)
            .forEach(this::assertRelease1bToggleConfiguration);
    }

    private boolean isRelease1bFeatureToggle(Method method) {
        return method.isAnnotationPresent(FeatureToggle.class)
            && RELEASE_1B.equals(method.getAnnotation(FeatureToggle.class).feature());
    }

    private void assertRelease1bToggleConfiguration(Method method) {
        FeatureToggle featureToggle = method.getAnnotation(FeatureToggle.class);
        assertEquals(RELEASE_1B, featureToggle.feature(), method.getName());
        assertEquals(RELEASE_1B_ENABLED_PROPERTY, featureToggle.defaultValueProperty(), method.getName());
    }
}
