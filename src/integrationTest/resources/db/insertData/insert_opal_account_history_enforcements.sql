INSERT INTO results (
    result_id, result_title, result_title_cy, result_type, active, imposition, imposition_accruing,
    enforcement, enforcement_override, further_enforcement_warn, further_enforcement_disallow,
    enforcement_hold, requires_enforcer, generates_hearing, generates_warrant, collection_order,
    extend_ttp_disallow, extend_ttp_preserve_last_enf, prevent_payment_card, lists_monies,
    manual_enforcement, allow_payment_terms, requires_employment_data, allow_additional_action,
    requires_lja
) VALUES (
    'HST02', 'Second history enforcement', 'Second history enforcement', 'Result', TRUE, FALSE, FALSE,
    TRUE, FALSE, FALSE, FALSE, FALSE, FALSE, TRUE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE,
    FALSE, FALSE, FALSE, FALSE, FALSE
) ON CONFLICT (result_id) DO NOTHING;

INSERT INTO enforcements (
    enforcement_id, defendant_account_id, posted_date, posted_by, result_id, reason, jail_days,
    warrant_reference, case_reference, hearing_date, hearing_court_id, posted_by_name,
    enforcement_account_type
) VALUES
(
    26220008, 262200, TIMESTAMP '2026-01-06 09:00:00', 'hist-user-8', 'HST02',
    'Second enforcement reason', 21, 'WR262201', 'CASE-HIST-2',
    TIMESTAMP '2026-02-06 10:00:00', 262200, 'History User Eight', 'COLL'
),
(
    26220009, 262200, TIMESTAMP '2026-01-07 09:00:00', 'hist-user-9', 'HST01',
    'Repeated enforcement reason', 28, 'WR262202', 'CASE-HIST-3',
    TIMESTAMP '2026-02-07 10:00:00', 262200, 'History User Nine', 'COLL'
);
