/**
 * OPAL Program
 *
 * MODULE      : insert_into_court_fees_entity_graph.sql
 *
 * DESCRIPTION : Inserts business unit and court fee data for Court Fees integration tests.
 *
 **/

INSERT INTO business_units (
    business_unit_id, business_unit_name, business_unit_code, business_unit_type,
    account_number_prefix, parent_business_unit_id, opal_domain, welsh_language
)
VALUES (953, 'Court Fees Business Unit', 'CFBU', 'Area',
    'CF', NULL, 'Fines', false),
    (954, 'BU no court fees', 'NCF', 'Area',
    'NF', NULL, 'Fines', false);

INSERT INTO court_fees (
    court_fee_id, business_unit_id, court_fee_code, description, amount, stats_code
)
VALUES
    (953001, 953, 'CF001', 'Court fee one', 12.50, 'ST001'),
    (953002, 953, 'CF002', 'Court fee two', 25.00, 'ST002');
