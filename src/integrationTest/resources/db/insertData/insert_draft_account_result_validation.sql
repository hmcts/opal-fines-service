-- Dedicated result rows for draft account validation in both modes.
INSERT INTO results (result_id, result_title, result_type, active, imposition, imposition_creditor, enforcement, imposition_accruing, enforcement_override, further_enforcement_warn, further_enforcement_disallow, enforcement_hold, requires_enforcer, generates_hearing, generates_warrant, collection_order, extend_ttp_disallow, extend_ttp_preserve_last_enf, prevent_payment_card, lists_monies, manual_enforcement) VALUES
    ('DVOPEN', 'Opal enforcement', 'Result', TRUE, FALSE, NULL, TRUE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE),
    ('DVLGEN', 'Legacy enforcement', 'Result', TRUE, FALSE, NULL, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE),
    ('DVOPIM', 'Opal imposition', 'Result', TRUE, TRUE, NULL, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE),
    ('DVLGIM', 'Legacy imposition', 'Result', TRUE, FALSE, NULL, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE);

INSERT INTO results_legacy (result_id, result_title, result_type, active, imposition, imposition_creditor, enforcement, imposition_accruing, enforcement_override, further_enforcement_warn, further_enforcement_disallow, enforcement_hold, requires_enforcer, generates_hearing, generates_warrant, collection_order, extend_ttp_disallow, extend_ttp_preserve_last_enf, prevent_payment_card, lists_monies, manual_enforcement) VALUES
    ('DVOPEN', 'Opal enforcement', 'Result', FALSE, FALSE, NULL, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE),
    ('DVLGEN', 'Legacy enforcement', 'Result', TRUE, FALSE, NULL, TRUE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE),
    ('DVOPIM', 'Opal imposition', 'Result', TRUE, FALSE, NULL, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE),
    ('DVLGIM', 'Legacy imposition', 'Result', TRUE, TRUE, NULL, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE);
