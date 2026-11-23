INSERT INTO courts (
    court_id, business_unit_id, court_code, name
) VALUES (
    59000000000001, 78, 32001, 'OPAL_ONLY Court'
);

INSERT INTO enforcers (
    enforcer_id, business_unit_id, enforcer_code, name
) VALUES (
    59000000000002, 78, 32002, 'OPAL_ONLY Enforcer'
);

INSERT INTO local_justice_areas (
    local_justice_area_id, name, address_line_1, lja_code, lja_type
) VALUES (
    32761, 'OPAL_ONLY Local Justice Area', 'OPAL_ONLY Address', 'OPAL', 'LJA'
);

INSERT INTO major_creditors (
    major_creditor_id, business_unit_id, major_creditor_code, name
) VALUES (
    59000000000003, 78, 'OPAL', 'OPAL_ONLY Major Creditor'
);

INSERT INTO offences (
    offence_id, cjs_code, offence_title
) VALUES (
    59000000000004, 'OPALONLY', 'OPAL_ONLY Offence'
);

INSERT INTO prisons (
    prison_id, business_unit_id, prison_code, name, address_line_1
) VALUES (
    59000000000005, 78, 'OPAL', 'OPAL_ONLY Prison', 'OPAL_ONLY Address'
);

INSERT INTO prosecutors (
    prosecutor_id, prosecutor_code, name
) VALUES (
    32760, 'OPAL', 'OPAL_ONLY Prosecutor'
);

INSERT INTO results (
    result_id,
    result_title,
    result_title_cy,
    result_type,
    active,
    imposition,
    imposition_accruing,
    enforcement,
    enforcement_override,
    further_enforcement_warn,
    further_enforcement_disallow,
    enforcement_hold,
    requires_enforcer,
    generates_hearing,
    generates_warrant,
    collection_order,
    extend_ttp_disallow,
    extend_ttp_preserve_last_enf,
    prevent_payment_card,
    lists_monies,
    manual_enforcement
) VALUES (
    'OPONLY',
    'OPAL_ONLY Result',
    'OPAL_ONLY Result Welsh',
    'Result',
    TRUE,
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
    FALSE,
    FALSE,
    FALSE
);
