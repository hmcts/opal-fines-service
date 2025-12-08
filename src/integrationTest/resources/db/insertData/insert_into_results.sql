/**
 * CGI OPAL Program
 *
 * MODULE      : insert_into_results.sql
 *
 * DESCRIPTION : Load the RESULTS table with reference data for the Integration Tests
 *
 * VERSION HISTORY:
 *
 * Date          Author      Version     Nature of Change
 * ----------    -------     --------    ------------------------------------------------------------------------------------------------
 * 21/05/2025    R DODD      1.0         Inserts rows of data into the RESULTS table for the integration tests
 * 01/12/2025    M Mollins   1.1         Adjusted boolean fields and added rows to exercise controller filters
 **/

INSERT INTO results
(
    result_id,
    result_title,
    result_title_cy,
    result_type,
    active,
    imposition,
    imposition_category,
    imposition_allocation_priority,
    imposition_accruing,
    imposition_creditor,
    enforcement,
    enforcement_override,
    further_enforcement_warn,
    further_enforcement_disallow,
    enforcement_hold,
    requires_enforcer,       -- used for manual_enforcement_only filtering
    generates_hearing,      -- used for generates_hearing filtering
    generates_warrant,
    collection_order,
    extend_ttp_disallow,
    extend_ttp_preserve_last_enf,
    prevent_payment_card,
    lists_monies,
    result_parameters,
    manual_enforcement
)
VALUES
    (
        'AAAAAA',
        'First Ever Result Entry for Testing',
        'Cais am dynnu arian o fudd-daliadau',
        'Action',
        TRUE,    -- active
        FALSE,
        NULL,
        NULL,
        FALSE,
        NULL,
        TRUE,    -- enforcement = true
        TRUE,
        TRUE,
        FALSE,
        FALSE,
        TRUE,    -- requires_enforcer => manual_enforcement_only = true
        TRUE,    -- generates_hearing = true
        FALSE,
        FALSE,
        FALSE,
        FALSE,
        FALSE,
        FALSE,
        NULL,
        TRUE     -- manual_enforcement = true
    ),
    (
        'BBBBBB',
        'Complete Result Entry for Testing',
        'Cais Prawf Cyflawn',
        'Action',
        TRUE,    -- active
        TRUE,
        'Compensation',
        5,
        TRUE,
        'CF',
        TRUE,    -- enforcement = true
        TRUE,
        TRUE,
        TRUE,
        TRUE,
        TRUE,   -- requires_enforcer => manual_enforcement_only = false
        TRUE,   -- generates_hearing = false
        TRUE,
        TRUE,
        TRUE,
        TRUE,
        TRUE,
        TRUE,
        '{"param1":"value1","param2":"value2"}',
        FALSE
    ),
    (
        'DDDDDD',
        'Bail Warrant - dated',
        'Gorchymyn Gollwng - dyddiedig',
        'Action',
        TRUE,    -- active
        FALSE,
        NULL,
        NULL,
        FALSE,
        NULL,
        FALSE,   -- enforcement = false
        FALSE,
        FALSE,
        FALSE,
        FALSE,
        FALSE,   -- requires_enforcer = false
        FALSE,   -- generates_hearing = false
        TRUE,    -- generates_warrant = true
        TRUE,
        FALSE,
        FALSE,
        FALSE,
        FALSE,
        '{"warrantDate":"2023-08-15","issuedBy":"Court A"}',
        TRUE
    ),
    (
        'CC0000',
        'Inactive Result Entry',
        'Canlyniad Anweithredol',
        'Action',
        FALSE,   -- active = false (used to assert active filter excludes this)
        FALSE,
        NULL,
        NULL,
        FALSE,
        NULL,
        FALSE,
        FALSE,
        FALSE,
        FALSE,
        FALSE,
        FALSE,
        FALSE,
        FALSE,
        FALSE,
        FALSE,
        FALSE,
        FALSE,
        FALSE,
        NULL,
        FALSE
    );
