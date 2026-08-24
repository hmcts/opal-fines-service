package uk.gov.hmcts.opal.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.opal.util.FeatureFlags.RELEASE_1B;
import static uk.gov.hmcts.opal.util.FeatureFlags.RELEASE_1B_ENABLED_PROPERTY;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
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
            // Defendant Account API controller
            "DefendantAccountApiController#addDefendantAccountParty",
            "DefendantAccountApiController#addEnforcement",
            "DefendantAccountApiController#addPaymentCardRequest",
            "DefendantAccountApiController#getConsolidatedAccounts",
            "DefendantAccountApiController#getDefendantAccountAtAGlance",
            "DefendantAccountApiController#getDefendantAccountFixedPenalty",
            "DefendantAccountApiController#getDefendantAccountHeaderSummary",
            "DefendantAccountApiController#getDefendantAccountHistory",
            "DefendantAccountApiController#getDefendantAccountParty",
            "DefendantAccountApiController#getEnforcementStatus",
            "DefendantAccountApiController#getImpositions",
            "DefendantAccountApiController#postDefendantAccountSearch",
            "DefendantAccountApiController#removeEnforcementHold",
            "DefendantAccountApiController#replaceDefendantAccountParty",
            "DefendantAccountApiController#updateDefendantAccount",

            // Defendant Account controller
            "DefendantAccountController#addPaymentTerms",
            "DefendantAccountController#defendantAccountPaymentTerms",
            "DefendantAccountController#removeDefendantAccountParty",

            // Major Creditor API controller
            "MajorCreditorApiController#getCentralFundByBusinessUnit",
            "MajorCreditorApiController#getMajorCreditorAccountAtAGlance",
            "MajorCreditorApiController#getMajorCreditorAccountHeaderSummary",
            "MajorCreditorApiController#getMajorCreditorHistory",

            // Minor Creditor API controller
            "MinorCreditorApiController#getMinorCreditorAccount",
            "MinorCreditorApiController#getMinorCreditorAccountAtAGlance",
            "MinorCreditorApiController#getMinorCreditorHistory",
            "MinorCreditorApiController#patchMinorCreditorAccount",
            "MinorCreditorApiController#postMinorCreditorSearch",

            // Minor Creditor controller
            "MinorCreditorController#getMinorCreditorAccountHeaderSummary",

            // Notes controller
            "NotesController#addNote",

            // Results API controller
            "ResultsApiController#getResultById"
        );

        assertEquals(expectedAnnotatedMethods, actualAnnotatedMethods,
            buildFeatureToggleMismatchMessage(expectedAnnotatedMethods, actualAnnotatedMethods));

        controllerClasses.stream()
            .flatMap(controllerClass -> Arrays.stream(controllerClass.getDeclaredMethods()))
            .filter(this::isRelease1bFeatureToggle)
            .forEach(this::assertRelease1bToggleConfiguration);
    }

    private boolean isRelease1bFeatureToggle(Method method) {
        return method.isAnnotationPresent(FeatureToggle.class)
            && RELEASE_1B.equals(method.getAnnotation(FeatureToggle.class).feature());
    }

    private String buildFeatureToggleMismatchMessage(Set<String> expectedAnnotatedMethods,
                                                     Set<String> actualAnnotatedMethods) {
        Set<String> missingFeatureToggles = new HashSet<>(expectedAnnotatedMethods);
        missingFeatureToggles.removeAll(actualAnnotatedMethods);

        Set<String> unexpectedFeatureToggles = new HashSet<>(actualAnnotatedMethods);
        unexpectedFeatureToggles.removeAll(expectedAnnotatedMethods);

        return "Missing Release 1b feature toggles: %s%nUnexpected Release 1b feature toggles: %s"
            .formatted(missingFeatureToggles, unexpectedFeatureToggles);
    }

    private void assertRelease1bToggleConfiguration(Method method) {
        FeatureToggle featureToggle = method.getAnnotation(FeatureToggle.class);
        assertEquals(RELEASE_1B, featureToggle.feature(), method.getName());
        assertEquals(RELEASE_1B_ENABLED_PROPERTY, featureToggle.defaultValueProperty(), method.getName());
    }
}
