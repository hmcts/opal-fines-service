# Legacy Action Type Alignment Design

## Objective

Align the eight legacy gateway action types identified in the action-to-API audit with the supplied canonical names. The change must affect only the action type query value sent to the legacy gateway.

## Scope

Update the action types for these APIs:

| API operation | Canonical action type |
|---|---|
| Search minor creditor accounts | `searchMinorCreditorAccounts` |
| Get a minor creditor account | `getMinorCreditorAccount` |
| Get minor creditor account header summary | `getMinorCreditorAccountHeaderSummary` |
| Get minor creditor account at a glance | `getMinorCreditorAccountAtAGlance` |
| Get minor creditor account history | `getMinorCreditorAccountHistory` |
| Get major creditor account header summary | `getMajorCreditorAccountHeaderSummary` |
| Get major creditor account at a glance | `getMajorCreditorAccountAtAGlance` |
| Get major creditor account history | `getMajorCreditorAccountHistory` |

The existing `LIBRA.get_consolidated_accounts` action type is explicitly out of scope.

## Implementation

Change only the action constants in `LegacyMinorCreditorService` and `LegacyMajorCreditorAccountService`. Controllers, proxies, gateway interfaces, request and response DTOs, OpenAPI definitions, feature toggles, and error handling remain unchanged.

The request flow remains:

1. The public controller receives the API request.
2. The mode-aware proxy selects the legacy service.
3. The legacy service builds the existing request payload.
4. `GatewayService` receives the canonical action type with the unchanged payload.

No compatibility aliasing or fallback to the old action names will be introduced because the supplied list is the canonical legacy contract.

## Testing

Tests will assert the expected action type as an independent string literal rather than importing the production constant. This ensures that an incorrect production constant causes a test failure.

The test-first sequence is:

1. Update or add unit assertions for all eight canonical action types.
2. Run the targeted tests and confirm they fail because the production constants still contain the old values.
3. Change the production constants.
4. Re-run the targeted tests and confirm they pass.
5. Run `./gradlew checkstyleMain` and `./gradlew build`.

Existing integration tests that depend on the production constants will be reviewed and changed to independent literals where needed to retain contract-level coverage.

## Risks and Constraints

- The legacy gateway must recognise the supplied canonical names. This design treats the supplied list as authoritative.
- Only the action type changes; request schemas and response mappings are unaffected.
- No unrelated legacy action constants or dead code will be refactored.
