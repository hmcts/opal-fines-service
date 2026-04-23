/**
* OPAL Program
*
* MODULE      : insert_into_courts_entity_graph.sql
*
* DESCRIPTION : Inserts Courts, Business Units and Local Justice Areas for Court entity graph integration tests
*
* VERSION HISTORY:
*
* Date          Author       Version     Nature of Change
* ----------    --------     --------    ----------------------------------------------------------------
* 23/04/2026    S WILLIAMS   1.0         PO-2885: Insert rows for court entity graph integration tests
*
**/

INSERT INTO business_units (
    business_unit_id,business_unit_name,business_unit_code,business_unit_type,
    account_number_prefix,parent_business_unit_id,opal_domain,welsh_language
    )
VALUES (
        951,'Court Graph Business Unit','CGBU','Area',
        'CG',NULL,'Fines',false
       );

INSERT INTO local_justice_areas (
    local_justice_area_id, name, address_line_1, address_line_2, address_line_3,
    postcode, lja_code, lja_type, address_line_4, address_line_5, end_date
)
VALUES (951, 'Court Graph LJA', 'Court Graph Address 1', 'Court Graph Address 2', NULL,
 'CG1 1CG', '0951', 'LJA', NULL, NULL, NULL
       );

INSERT INTO courts (
    court_id, business_unit_id, court_code, parent_court_id, name, name_cy,
    address_line_1, address_line_2, address_line_3,
    address_line_1_cy, address_line_2_cy, address_line_3_cy, postcode,
    local_justice_area_id, national_court_code, gob_enforcing_court_code,
    lja, court_type, division, session,
    start_time, max_load, record_session_times, max_court_duration, group_code
    )
VALUES (
    951001, 951, 951, NULL, 'Court Graph Parent', NULL,
    'Parent Address 1', 'Parent Address 2', NULL,
    NULL, NULL, NULL, 'CG1 1AA',
    951, 11, 12,
    951, 'MC', '01', 'AM',
    '10:00', 2, 'Y', 180, 'PARENT'
    ),
    (
    951002, 951, 952, 951001, 'Court Graph Child', NULL,
    'Child Address 1', 'Child Address 2', NULL,
    NULL, NULL, NULL, 'CG1 1AB',
    951, 21, 22,
    951, 'MC', '02', 'PM',
    '14:00', 4, 'N', 240, 'CHILD'
    );
