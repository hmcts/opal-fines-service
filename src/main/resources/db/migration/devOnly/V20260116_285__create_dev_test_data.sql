/**
* CGI OPAL Program
*
* MODULE      : create_dev_test_data.sql
*
* DESCRIPTION : Create test data to be used by Frontend for development testing
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    ---------------------------------------------------------------------------------------------------------------------
* 03/10/2025    C Cho       1.0         PO-2222 Create test data to be used by Frontend for development testing
*
**/

DO $$
DECLARE
    base_id INT;
BEGIN

    -- 1) DEFENDANT_ACCOUNTS (10 + 3 companies = 13 total)
    INSERT INTO public.defendant_accounts
    (defendant_account_id, business_unit_id, account_number, amount_imposed, amount_paid, account_balance, account_status, account_type, imposed_hearing_date, last_changed_date, last_movement_date, imposing_court_id, enforcing_court_id, last_hearing_court_id, last_hearing_date, last_enforcement, originator_name, originator_type, allow_writeoffs, allow_cheques, cheque_clearance_period, credit_trans_clearance_period, enf_override_result_id, enf_override_enforcer_id, enf_override_tfo_lja_id, collection_order, collection_order_date, further_steps_notice_date, confiscation_order_date, fine_registration_date, suspended_committal_date, consolidated_account_type, payment_card_requested, payment_card_requested_date, payment_card_requested_by, prosecutor_case_reference, originator_id, account_comments, account_note_1, account_note_2, account_note_3, jail_days, version_number, payment_card_requested_by_name) VALUES
    (99000000000001, 77, '12345678', 120.00, 0.00, 120.00, 'L', 'Fine', CURRENT_TIMESTAMP - INTERVAL '15 days', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, (SELECT court_id FROM public.courts ORDER BY court_id LIMIT 1), (SELECT court_id FROM public.courts ORDER BY court_id LIMIT 1), (SELECT court_id FROM public.courts ORDER BY court_id LIMIT 1), CURRENT_TIMESTAMP - INTERVAL '18 days', 'WP', 'Westminster Magistrates Court', 'AUTO_CP', false, true, 5, 3, (SELECT result_id FROM public.results ORDER BY result_id LIMIT 1), (SELECT enforcer_id FROM public.enforcers ORDER BY enforcer_id LIMIT 1), (SELECT local_justice_area_id FROM public.local_justice_areas ORDER BY local_justice_area_id LIMIT 1), false, NULL, CURRENT_TIMESTAMP - INTERVAL '10 days', NULL, CURRENT_TIMESTAMP - INTERVAL '15 days', NULL, NULL, true, CURRENT_TIMESTAMP - INTERVAL '5 days', 'L080JG', 'PROS2024001235', 'WMC001235', 'Speeding offence - automated camera detection', 'Vehicle registered to defendant', 'Payment card requested for online payment', NULL, 14, 1, 'Test User 1'),
    (99000000000002, 77, '23456789A', 80.00, 40.00, 40.00, 'L', 'Fine', CURRENT_TIMESTAMP - INTERVAL '22 days', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, (SELECT court_id FROM public.courts ORDER BY court_id LIMIT 1), (SELECT court_id FROM public.courts ORDER BY court_id LIMIT 1), (SELECT court_id FROM public.courts ORDER BY court_id LIMIT 1), CURRENT_TIMESTAMP - INTERVAL '25 days', 'REM', 'Brighton Magistrates Court', 'AUTO_CP', true, true, 5, 3, (SELECT result_id FROM public.results ORDER BY result_id OFFSET 1 LIMIT 1), (SELECT enforcer_id FROM public.enforcers ORDER BY enforcer_id LIMIT 1), NULL, false, NULL, CURRENT_TIMESTAMP - INTERVAL '20 days', NULL, CURRENT_TIMESTAMP - INTERVAL '22 days', NULL, NULL, true, CURRENT_TIMESTAMP - INTERVAL '12 days', 'L080JG', 'PROS2024002847', 'BMC002847', 'Parking violation in restricted area', 'Partial payment received', 'Card payment facility available', 'Reminder sent to defendant', NULL, 1, 'Test User 2'),
    (99000000000003, 77, '34567890', 200.00, 200.00, 0.00, 'C', 'Fine', CURRENT_TIMESTAMP - INTERVAL '45 days', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, (SELECT court_id FROM public.courts ORDER BY court_id LIMIT 1), (SELECT court_id FROM public.courts ORDER BY court_id LIMIT 1), (SELECT court_id FROM public.courts ORDER BY court_id LIMIT 1), CURRENT_TIMESTAMP - INTERVAL '48 days', NULL, 'Leeds Magistrates Court', 'AUTO_CP', false, true, 5, 3, NULL, NULL, NULL, false, NULL, NULL, NULL, CURRENT_TIMESTAMP - INTERVAL '45 days', NULL, NULL, false, NULL, NULL, 'PROS2024003691', 'LMC003691', 'Traffic violation - account completed', 'Full payment received', 'Account closed successfully', 'Payment received in full', NULL, 1, NULL),
    (99000000000004, 77, '45678901B', 100.00, 25.00, 75.00, 'L', 'Fine', CURRENT_TIMESTAMP - INTERVAL '8 days', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, (SELECT court_id FROM public.courts ORDER BY court_id LIMIT 1), (SELECT court_id FROM public.courts ORDER BY court_id LIMIT 1), (SELECT court_id FROM public.courts ORDER BY court_id LIMIT 1), CURRENT_TIMESTAMP - INTERVAL '11 days', 'ENF1', 'City of London Magistrates', 'AUTO_CP', true, true, 7, 4, (SELECT result_id FROM public.results ORDER BY result_id LIMIT 1), (SELECT enforcer_id FROM public.enforcers ORDER BY enforcer_id LIMIT 1), (SELECT local_justice_area_id FROM public.local_justice_areas ORDER BY local_justice_area_id LIMIT 1), false, NULL, CURRENT_TIMESTAMP - INTERVAL '3 days', NULL, CURRENT_TIMESTAMP - INTERVAL '8 days', NULL, NULL, true, CURRENT_TIMESTAMP - INTERVAL '2 days', 'L047SA', 'PROS2024004582', 'CLM004582', 'Speeding in central London zone', 'First reminder sent', 'Payment plan available', 'Enforcement action initiated', 28, 1, 'Test User 4'),
    (99000000000005, 77, '56789012', 150.00, 0.00, 150.00, 'TS', 'Fine', CURRENT_TIMESTAMP - INTERVAL '30 days', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, (SELECT court_id FROM public.courts ORDER BY court_id LIMIT 1), (SELECT court_id FROM public.courts ORDER BY court_id LIMIT 1), (SELECT court_id FROM public.courts ORDER BY court_id LIMIT 1), CURRENT_TIMESTAMP - INTERVAL '33 days', 'WP', 'Cambridge Magistrates Court', 'AUTO_CP', false, true, 5, 3, (SELECT result_id FROM public.results ORDER BY result_id OFFSET 2 LIMIT 1), (SELECT enforcer_id FROM public.enforcers ORDER BY enforcer_id OFFSET 1 LIMIT 1), (SELECT local_justice_area_id FROM public.local_justice_areas ORDER BY local_justice_area_id OFFSET 1 LIMIT 1), true, CURRENT_TIMESTAMP - INTERVAL '20 days', CURRENT_TIMESTAMP - INTERVAL '15 days', CURRENT_TIMESTAMP - INTERVAL '25 days', CURRENT_TIMESTAMP - INTERVAL '30 days', NULL, NULL, true, CURRENT_TIMESTAMP - INTERVAL '10 days', 'L060FO', 'PROS2024005739', 'CMC005739', 'Moving traffic violation on A14', 'Collection order active', 'Further steps notice issued', 'Transfer to Scotland pending', 21, 1, 'Test User 5'),
    (99000000000006, 77, '67890123C', 600.00, 60.00, 540.00, 'TO', 'Fine', CURRENT_TIMESTAMP - INTERVAL '6 days', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, (SELECT court_id FROM public.courts ORDER BY court_id LIMIT 1), (SELECT court_id FROM public.courts ORDER BY court_id LIMIT 1), (SELECT court_id FROM public.courts ORDER BY court_id LIMIT 1), CURRENT_TIMESTAMP - INTERVAL '9 days', 'ENF2', 'Test Court 6', 'MANUAL', true, true, 7, 5, (SELECT result_id FROM public.results ORDER BY result_id OFFSET 1 LIMIT 1), (SELECT enforcer_id FROM public.enforcers ORDER BY enforcer_id OFFSET 1 LIMIT 1), NULL, false, NULL, CURRENT_TIMESTAMP - INTERVAL '5 days', NULL, CURRENT_TIMESTAMP - INTERVAL '6 days', NULL, NULL, true, CURRENT_TIMESTAMP - INTERVAL '1 days', 'L106CO', 'PROSTEST000006', 'TC6000006', 'Test case for development', 'Enforcement action pending', 'Test data - do not action', 'Used for frontend testing', 42, 1, 'Test User 6'),
    (99000000000007, 77, '78901234', 700.00, 70.00, 630.00, 'L', 'Fine', CURRENT_TIMESTAMP - INTERVAL '7 days', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, (SELECT court_id FROM public.courts ORDER BY court_id LIMIT 1), (SELECT court_id FROM public.courts ORDER BY court_id LIMIT 1), (SELECT court_id FROM public.courts ORDER BY court_id LIMIT 1), CURRENT_TIMESTAMP - INTERVAL '10 days', 'WP', 'Test Court 7', 'MANUAL', true, true, 7, 5, NULL, NULL, NULL, false, NULL, CURRENT_TIMESTAMP - INTERVAL '6 days', NULL, CURRENT_TIMESTAMP - INTERVAL '7 days', NULL, NULL, true, CURRENT_TIMESTAMP - INTERVAL '2 days', 'L089BO', 'PROSTEST000007', 'TC7000007', 'Development test account 7', 'Payment instalments set up', 'Regular monitoring required', 'Test environment only', 35, 1, 'Test User 7'),
    (99000000000008, 77, '89012345D', 60.00, 0.00, 60.00, 'L', 'Fixed Penalty', CURRENT_TIMESTAMP - INTERVAL '5 days', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, (SELECT court_id FROM public.courts ORDER BY court_id LIMIT 1), (SELECT court_id FROM public.courts ORDER BY court_id LIMIT 1), (SELECT court_id FROM public.courts ORDER BY court_id LIMIT 1), NULL, NULL, 'Fixed Penalty Office', 'AUTO_FP', false, true, 5, 3, NULL, NULL, NULL, false, NULL, NULL, NULL, CURRENT_TIMESTAMP - INTERVAL '5 days', NULL, NULL, true, CURRENT_TIMESTAMP - INTERVAL '1 days', 'L036DO', 'FP2024000008', 'FP8000008', 'Fixed penalty notice for parking', 'Fixed penalty offence', 'Payment due within 28 days', 'No court hearing required', NULL, 1, 'Test User 8'),
    (99000000000009, 77, '90123456', 120.00, 0.00, 120.00, 'L', 'Fine', CURRENT_TIMESTAMP - INTERVAL '9 days', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, (SELECT court_id FROM public.courts ORDER BY court_id LIMIT 1), (SELECT court_id FROM public.courts ORDER BY court_id LIMIT 1), (SELECT court_id FROM public.courts ORDER BY court_id LIMIT 1), CURRENT_TIMESTAMP - INTERVAL '12 days', 'REM', 'Test Court 9', 'MANUAL', false, true, 5, 3, (SELECT result_id FROM public.results ORDER BY result_id OFFSET 3 LIMIT 1), (SELECT enforcer_id FROM public.enforcers ORDER BY enforcer_id OFFSET 3 LIMIT 1), (SELECT local_justice_area_id FROM public.local_justice_areas ORDER BY local_justice_area_id OFFSET 1 LIMIT 1), false, NULL, NULL, NULL, CURRENT_TIMESTAMP - INTERVAL '9 days', NULL, NULL, false, NULL, NULL, 'PROSTEST000009', 'TC9000009', 'Parent liable for minor offence', 'Parent or Guardian to Pay', 'Minor under 16 years old', 'Guardian responsible for payment', 56, 1, NULL),
    (99000000000010, 77, '01234567E', 80.00, 0.00, 80.00, 'L', 'Fixed Penalty', CURRENT_TIMESTAMP - INTERVAL '3 days', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, (SELECT court_id FROM public.courts ORDER BY court_id LIMIT 1), (SELECT court_id FROM public.courts ORDER BY court_id LIMIT 1), (SELECT court_id FROM public.courts ORDER BY court_id LIMIT 1), NULL, NULL, 'Traffic Enforcement Centre', 'AUTO_FP', true, true, 7, 3, NULL, NULL, NULL, false, NULL, NULL, NULL, CURRENT_TIMESTAMP - INTERVAL '3 days', NULL, NULL, true, CURRENT_TIMESTAMP - INTERVAL '1 days', 'L065AO', 'FP2024000010', 'FP10000010', 'Guardian to pay for minor violation', 'Parent or Guardian to Pay', 'Traffic violation by minor', 'Guardian assumes responsibility', 70, 1, 'Test User 10'),
    -- Company accounts
    (99000000000013, 77, '11111111F', 350.00, 0.00, 350.00, 'L', 'Fine', CURRENT_TIMESTAMP - INTERVAL '12 days', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, (SELECT court_id FROM public.courts ORDER BY court_id LIMIT 1), (SELECT court_id FROM public.courts ORDER BY court_id LIMIT 1), (SELECT court_id FROM public.courts ORDER BY court_id LIMIT 1), CURRENT_TIMESTAMP - INTERVAL '15 days', 'WP', 'Commercial Court London', 'MANUAL', true, true, 7, 5, (SELECT result_id FROM public.results ORDER BY result_id LIMIT 1), (SELECT enforcer_id FROM public.enforcers ORDER BY enforcer_id LIMIT 1), NULL, false, NULL, CURRENT_TIMESTAMP - INTERVAL '8 days', NULL, CURRENT_TIMESTAMP - INTERVAL '12 days', NULL, NULL, true, CURRENT_TIMESTAMP - INTERVAL '3 days', 'L080JG', 'CORPTEST000013', 'COM13000013', 'Corporate environmental violation', 'Company liability case', 'Environmental protection fine', 'Corporate responsibility', NULL, 1, 'Test User 13'),
    (99000000000014, 77, '22222222', 850.00, 100.00, 750.00, 'L', 'Fine', CURRENT_TIMESTAMP - INTERVAL '18 days', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, (SELECT court_id FROM public.courts ORDER BY court_id LIMIT 1), (SELECT court_id FROM public.courts ORDER BY court_id LIMIT 1), (SELECT court_id FROM public.courts ORDER BY court_id LIMIT 1), CURRENT_TIMESTAMP - INTERVAL '21 days', 'ENF1', 'Business Magistrates Court', 'AUTO_CP', true, true, 5, 3, (SELECT result_id FROM public.results ORDER BY result_id OFFSET 1 LIMIT 1), (SELECT enforcer_id FROM public.enforcers ORDER BY enforcer_id OFFSET 1 LIMIT 1), (SELECT local_justice_area_id FROM public.local_justice_areas ORDER BY local_justice_area_id LIMIT 1), true, CURRENT_TIMESTAMP - INTERVAL '10 days', CURRENT_TIMESTAMP - INTERVAL '12 days', NULL, CURRENT_TIMESTAMP - INTERVAL '18 days', NULL, NULL, true, CURRENT_TIMESTAMP - INTERVAL '5 days', 'L047SA', 'CORPTEST000014', 'COM14000014', 'Health and safety violation', 'Partial payment received', 'Collection order issued', 'Enforcement proceedings active', NULL, 1, 'Test User 14'),
    (99000000000015, 77, '33333333G', 1250.00, 1250.00, 0.00, 'C', 'Fine', CURRENT_TIMESTAMP - INTERVAL '60 days', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, (SELECT court_id FROM public.courts ORDER BY court_id LIMIT 1), (SELECT court_id FROM public.courts ORDER BY court_id LIMIT 1), (SELECT court_id FROM public.courts ORDER BY court_id LIMIT 1), CURRENT_TIMESTAMP - INTERVAL '63 days', NULL, 'Corporate Enforcement Unit', 'MANUAL', false, true, 7, 5, NULL, NULL, NULL, false, NULL, NULL, NULL, CURRENT_TIMESTAMP - INTERVAL '60 days', NULL, NULL, false, NULL, NULL, 'CORPTEST000015', 'COM15000015', 'Trading standards violation - completed', 'Account settled in full', 'Corporate compliance achieved', 'Case closed successfully', NULL, 1, NULL),
    -- Additional company accounts with full aliases
    (99000000000016, 77, '44444444H', 450.00, 50.00, 400.00, 'L', 'Fine', CURRENT_TIMESTAMP - INTERVAL '25 days', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, (SELECT court_id FROM public.courts ORDER BY court_id LIMIT 1), (SELECT court_id FROM public.courts ORDER BY court_id LIMIT 1), (SELECT court_id FROM public.courts ORDER BY court_id LIMIT 1), CURRENT_TIMESTAMP - INTERVAL '28 days', 'REM', 'Corporate Magistrates Court', 'AUTO_CP', true, true, 5, 3, (SELECT result_id FROM public.results ORDER BY result_id OFFSET 2 LIMIT 1), (SELECT enforcer_id FROM public.enforcers ORDER BY enforcer_id OFFSET 2 LIMIT 1), NULL, false, NULL, CURRENT_TIMESTAMP - INTERVAL '20 days', NULL, CURRENT_TIMESTAMP - INTERVAL '25 days', NULL, NULL, true, CURRENT_TIMESTAMP - INTERVAL '8 days', 'L090XY', 'CORPTEST000016', 'COM16000016', 'Data protection violation', 'GDPR compliance breach', 'Privacy regulation fine', 'Data handling violation', NULL, 1, 'Test User 16'),
    (99000000000017, 77, '55555555', 720.00, 0.00, 720.00, 'L', 'Fine', CURRENT_TIMESTAMP - INTERVAL '14 days', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, (SELECT court_id FROM public.courts ORDER BY court_id LIMIT 1), (SELECT court_id FROM public.courts ORDER BY court_id LIMIT 1), (SELECT court_id FROM public.courts ORDER BY court_id LIMIT 1), CURRENT_TIMESTAMP - INTERVAL '17 days', 'WP', 'Industrial Court London', 'MANUAL', false, true, 7, 5, (SELECT result_id FROM public.results ORDER BY result_id OFFSET 3 LIMIT 1), (SELECT enforcer_id FROM public.enforcers ORDER BY enforcer_id OFFSET 3 LIMIT 1), (SELECT local_justice_area_id FROM public.local_justice_areas ORDER BY local_justice_area_id OFFSET 2 LIMIT 1), false, NULL, CURRENT_TIMESTAMP - INTERVAL '10 days', NULL, CURRENT_TIMESTAMP - INTERVAL '14 days', NULL, NULL, true, CURRENT_TIMESTAMP - INTERVAL '4 days', 'L110ZA', 'CORPTEST000017', 'COM17000017', 'Construction safety violation', 'Building regulation breach', 'Site safety compliance', 'Construction standards violation', 14, 1, 'Test User 17'),
    (99000000000018, 77, '66666666I', 980.00, 280.00, 700.00, 'L', 'Fine', CURRENT_TIMESTAMP - INTERVAL '35 days', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, (SELECT court_id FROM public.courts ORDER BY court_id LIMIT 1), (SELECT court_id FROM public.courts ORDER BY court_id LIMIT 1), (SELECT court_id FROM public.courts ORDER BY court_id LIMIT 1), CURRENT_TIMESTAMP - INTERVAL '38 days', 'ENF1', 'Financial Services Court', 'AUTO_CP', true, true, 5, 3, (SELECT result_id FROM public.results ORDER BY result_id OFFSET 4 LIMIT 1), (SELECT enforcer_id FROM public.enforcers ORDER BY enforcer_id OFFSET 4 LIMIT 1), (SELECT local_justice_area_id FROM public.local_justice_areas ORDER BY local_justice_area_id OFFSET 3 LIMIT 1), true, CURRENT_TIMESTAMP - INTERVAL '25 days', CURRENT_TIMESTAMP - INTERVAL '30 days', NULL, CURRENT_TIMESTAMP - INTERVAL '35 days', NULL, NULL, true, CURRENT_TIMESTAMP - INTERVAL '12 days', 'L120BC', 'CORPTEST000018', 'COM18000018', 'Financial services violation', 'FCA compliance breach', 'Financial regulation fine', 'Investment services violation', NULL, 1, 'Test User 18')
    ON CONFLICT DO NOTHING;

    -- 2) PARTIES (defendants)
    INSERT INTO public.parties
    (party_id, organisation, surname, forenames, title, address_line_1, address_line_2, postcode, birth_date) VALUES
    (99000000000001, false, 'Johnson', 'Michael James', 'Mr', '42 Oak Avenue', 'Hammersmith', 'W6 8RF', DATE '1985-03-15'),
    (99000000000002, false, 'Williams', 'Sarah Louise', 'Ms', '15 Victoria Street', 'Brighton', 'BN1 4ER', DATE '1992-11-08'),
    (99000000000003, false, 'Brown', 'David Alexander', 'Mr', 'Flat 7 Maple Court', 'Leeds', 'LS2 9JT', DATE '1978-07-22'),
    (99000000000004, false, 'Davis', 'Emma Charlotte', 'Mrs', '128 High Street', 'Oxford', 'OX1 4DH', DATE '1988-01-30'),
    (99000000000005, false, 'Wilson', 'James Robert', 'Mr', '9 Church Lane', 'Cambridge', 'CB2 1AG', DATE '1995-09-12'),
    (99000000000006, false, 'Thompson', 'Robert Michael', 'Mr', 'Flat 6 Test House', 'Test Road', 'ZZ1 06ZZ', DATE '1986-01-01'),
    (99000000000007, false, 'Anderson', 'Patricia Helen', 'Mrs', 'Flat 7 Test House', 'Test Road', 'ZZ1 07ZZ', DATE '1987-01-01'),
    (99000000000008, false, 'Martinez', 'Carlos Antonio', 'Mr', 'Flat 8 Test House', 'Test Road', 'ZZ1 08ZZ', DATE '1988-01-01'),
    (99000000000009, false, 'Taylor', 'Jennifer Marie', 'Mrs', 'Flat 9 Test House', 'Test Road', 'ZZ1 09ZZ', DATE '1989-01-01'),
    (99000000000010, false, 'Garcia', 'Maria Elena', 'Mrs', 'Flat 10 Test House', 'Test Road', 'ZZ1 10ZZ', DATE '1990-01-01')
    ON CONFLICT DO NOTHING;

    -- Company parties
    INSERT INTO public.parties
    (party_id, organisation, organisation_name, address_line_1, address_line_2, postcode) VALUES
    (99000000000013, true, 'TechGlobal Solutions Ltd', '1 Corporate Plaza', 'Business District', 'EC1A 1BB'),
    (99000000000014, true, 'Manufacturing Dynamics PLC', '45 Industrial Estate', 'Manufacturing Quarter', 'M1 5XY'),
    (99000000000015, true, 'Retail Excellence Group Ltd', '78 Commercial Street', 'City Centre', 'B2 4HJ'),
    -- Additional company parties with full aliases
    (99000000000016, true, 'DataSecure Technologies Ltd', '25 Innovation Drive', 'Tech Park', 'RG6 1PT'),
    (99000000000017, true, 'BuildSafe Construction PLC', '150 Industrial Way', 'Construction Quarter', 'M15 6AB'),
    (99000000000018, true, 'Prime Financial Services Ltd', '88 Canary Wharf', 'Financial District', 'E14 5AB')
    ON CONFLICT DO NOTHING;

    -- Add minor defendants for parent/guardian cases
    INSERT INTO public.parties
    (party_id, organisation, surname, forenames, title, address_line_1, address_line_2, postcode, birth_date) VALUES
    (99000000000011, false, 'Taylor', 'Joshua Michael', 'Mr', 'Flat 9 Test House', 'Test Road', 'ZZ1 09ZZ', DATE '2010-01-01'),
    (99000000000012, false, 'Garcia', 'Sofia Isabella', 'Ms', 'Flat 10 Test House', 'Test Road', 'ZZ1 10ZZ', DATE '2012-01-01')
    ON CONFLICT DO NOTHING;

    -- 3) ALIASES
    INSERT INTO public.aliases
    (alias_id, party_id, sequence_number, surname, forenames) VALUES
    (99000000001001, 99000000000001, 1, 'Johnstone', 'Mike'),
    (99000000001002, 99000000000001, 2, 'Johnson', 'Michael J'),
    (99000000001003, 99000000000001, 3, 'Jonson', 'Mick'),
    (99000000001004, 99000000000001, 4, 'Johnston', 'Mickey'),
    (99000000001005, 99000000000001, 5, 'Johnsen', 'M James'),
    (99000000001006, 99000000000002, 1, 'Williams-Smith', 'Sarah'),
    (99000000001007, 99000000000002, 2, 'Williams', 'Sarah L'),
    (99000000001008, 99000000000002, 3, 'Williamson', 'Sara'),
    (99000000001009, 99000000000002, 4, 'William', 'S Louise'),
    (99000000001010, 99000000000002, 5, 'Willis', 'Sarah Louise'),
    (99000000001011, 99000000000003, 1, 'Browne', 'Dave'),
    (99000000001012, 99000000000003, 2, 'Brown', 'David A'),
    (99000000001013, 99000000000003, 3, 'Browning', 'D Alexander'),
    (99000000001014, 99000000000003, 4, 'Bruno', 'David'),
    (99000000001015, 99000000000003, 5, 'Brownson', 'Alex'),
    (99000000001016, 99000000000004, 1, 'Davies', 'Emma'),
    (99000000001017, 99000000000004, 2, 'Davis', 'Emma C'),
    (99000000001018, 99000000000004, 3, 'Davidson', 'E Charlotte'),
    (99000000001019, 99000000000004, 4, 'Dave', 'Emma Charlotte'),
    (99000000001020, 99000000000004, 5, 'Davison', 'Emmy'),
    (99000000001021, 99000000000005, 1, 'Williamson', 'Jimmy'),
    (99000000001022, 99000000000005, 2, 'Wilson', 'James R'),
    (99000000001023, 99000000000005, 3, 'Willis', 'Jim'),
    (99000000001024, 99000000000005, 4, 'Wilkinson', 'J Robert'),
    (99000000001025, 99000000000005, 5, 'Williams', 'Jamie'),
    (99000000001026, 99000000000006, 1, 'Thomson', 'Robert'),
    (99000000001027, 99000000000006, 2, 'Thompson', 'Robert M'),
    (99000000001028, 99000000000006, 3, 'Thompsen', 'Rob'),
    (99000000001029, 99000000000006, 4, 'Thomas', 'Robert Michael'),
    (99000000001030, 99000000000006, 5, 'Thomason', 'Bobby'),
    (99000000001031, 99000000000007, 1, 'Andersen', 'Patricia'),
    (99000000001032, 99000000000007, 2, 'Anderson', 'Patricia H'),
    (99000000001033, 99000000000007, 3, 'Andrews', 'Pat'),
    (99000000001034, 99000000000007, 4, 'Andre', 'Patricia Helen'),
    (99000000001035, 99000000000007, 5, 'Anders', 'Patty'),
    (99000000001036, 99000000000008, 1, 'Martin', 'Carlos'),
    (99000000001037, 99000000000008, 2, 'Martinez', 'Carlos A'),
    (99000000001038, 99000000000008, 3, 'Martins', 'Charlie'),
    (99000000001039, 99000000000008, 4, 'Martinelli', 'C Antonio'),
    (99000000001040, 99000000000008, 5, 'Martino', 'Carl'),
    (99000000001041, 99000000000009, 1, 'Tyler', 'Jennifer'),
    (99000000001042, 99000000000009, 2, 'Taylor', 'Jennifer M'),
    (99000000001043, 99000000000009, 3, 'Tayler', 'Jen'),
    (99000000001044, 99000000000009, 4, 'Taylors', 'J Marie'),
    (99000000001045, 99000000000009, 5, 'Tailor', 'Jenny'),
    (99000000001046, 99000000000010, 1, 'Garcias', 'Maria'),
    (99000000001047, 99000000000010, 2, 'Garcia', 'Maria E'),
    (99000000001048, 99000000000010, 3, 'Garci', 'Marie'),
    (99000000001049, 99000000000010, 4, 'Gardner', 'M Elena'),
    (99000000001050, 99000000000010, 5, 'Garcez', 'Mary')
    ON CONFLICT DO NOTHING;

    -- Company aliases - all 5 for each organisation
    INSERT INTO public.aliases
    (alias_id, party_id, sequence_number, organisation_name) VALUES
    -- TechGlobal Solutions Ltd (99000000000013)
    (99000000001100, 99000000000013, 1, 'TechGlobal Solutions Limited'),
    (99000000001101, 99000000000013, 2, 'Tech Global Solutions Ltd'),
    (99000000001102, 99000000000013, 3, 'TechGlobal Ltd'),
    (99000000001103, 99000000000013, 4, 'Tech-Global Solutions'),
    (99000000001104, 99000000000013, 5, 'TG Solutions Ltd'),
    -- Manufacturing Dynamics PLC (99000000000014)
    (99000000001105, 99000000000014, 1, 'Manufacturing Dynamics Public Limited Company'),
    (99000000001106, 99000000000014, 2, 'Mfg Dynamics PLC'),
    (99000000001107, 99000000000014, 3, 'Manufacturing Dynamics Ltd'),
    (99000000001108, 99000000000014, 4, 'Man Dynamics PLC'),
    (99000000001109, 99000000000014, 5, 'MD Manufacturing PLC'),
    -- Retail Excellence Group Ltd (99000000000015)
    (99000000001110, 99000000000015, 1, 'Retail Excellence Group Limited'),
    (99000000001111, 99000000000015, 2, 'RE Group Ltd'),
    (99000000001112, 99000000000015, 3, 'Retail Excellence Ltd'),
    (99000000001113, 99000000000015, 4, 'Excellence Retail Group'),
    (99000000001114, 99000000000015, 5, 'REG Limited'),
    -- DataSecure Technologies Ltd (99000000000016)
    (99000000001115, 99000000000016, 1, 'DataSecure Technologies Limited'),
    (99000000001116, 99000000000016, 2, 'Data Secure Technologies Ltd'),
    (99000000001117, 99000000000016, 3, 'DataSecure Tech Ltd'),
    (99000000001118, 99000000000016, 4, 'DS Technologies Ltd'),
    (99000000001119, 99000000000016, 5, 'DataSec Ltd'),
    -- BuildSafe Construction PLC (99000000000017)
    (99000000001120, 99000000000017, 1, 'BuildSafe Construction Public Limited Company'),
    (99000000001121, 99000000000017, 2, 'Build Safe Construction PLC'),
    (99000000001122, 99000000000017, 3, 'BuildSafe Construction Ltd'),
    (99000000001123, 99000000000017, 4, 'BS Construction PLC'),
    (99000000001124, 99000000000017, 5, 'BuildSafe PLC'),
    -- Prime Financial Services Ltd (99000000000018)
    (99000000001125, 99000000000018, 1, 'Prime Financial Services Limited'),
    (99000000001126, 99000000000018, 2, 'Prime Financial Ltd'),
    (99000000001127, 99000000000018, 3, 'PFS Ltd'),
    (99000000001128, 99000000000018, 4, 'Prime Finance Services'),
    (99000000001129, 99000000000018, 5, 'Prime FS Ltd')
    ON CONFLICT DO NOTHING;

    -- 4) DEBTOR_DETAIL
    INSERT INTO public.debtor_detail
    (party_id, employer_name, employer_address_line_1, employer_address_line_2, employer_postcode, vehicle_make, vehicle_registration) VALUES
    (99000000000001, 'TechCorp Solutions Ltd', '1 Business Park', 'Canary Wharf', 'E14 5AB', 'Ford', 'BX67 KLM'),
    (99000000000002, 'Brighton Healthcare NHS', '45 Medical Centre', 'Brighton', 'BN2 5RT', 'Volkswagen', 'FG19 XYZ'),
    (99000000000003, 'Leeds Manufacturing Co', '12 Industrial Estate', 'Leeds', 'LS10 2PQ', 'BMW', 'HJ21 ABC'),
    (99000000000004, 'Oxford University Press', '23 Academic Way', 'Oxford', 'OX2 6DP', 'Audi', 'LK18 DEF'),
    (99000000000005, 'Cambridge Consulting', '8 Innovation Hub', 'Cambridge', 'CB3 0HE', 'Mercedes', 'NM20 GHI'),
    (99000000000006, 'Global Manufacturing Ltd', '1 Business Park', 'Unit 6', 'ZZ9 9ZZ', 'Toyota', 'REG0006'),
    (99000000000007, 'Healthcare Solutions Inc', '1 Business Park', 'Unit 7', 'ZZ9 9ZZ', 'Honda', 'REG0007'),
    (99000000000008, 'Financial Services Corp', '1 Business Park', 'Unit 8', 'ZZ9 9ZZ', 'Nissan', 'REG0008'),
    (99000000000009, 'Educational Trust Fund', '1 Business Park', 'Unit 9', 'ZZ9 9ZZ', 'Ford', 'REG0009'),
    (99000000000010, 'Community Services Ltd', '1 Business Park', 'Unit 10', 'ZZ9 9ZZ', 'Vauxhall', 'REG0010'),
    -- Company debtor details (no employer info for companies)
    (99000000000013, NULL, NULL, NULL, NULL, 'Mercedes', 'CORP0013'),
    (99000000000014, NULL, NULL, NULL, NULL, 'BMW', 'CORP0014'),
    (99000000000015, NULL, NULL, NULL, NULL, 'Audi', 'CORP0015'),
    -- Additional company debtor details
    (99000000000016, NULL, NULL, NULL, NULL, 'Tesla', 'CORP0016'),
    (99000000000017, NULL, NULL, NULL, NULL, 'Volvo', 'CORP0017'),
    (99000000000018, NULL, NULL, NULL, NULL, 'Jaguar', 'CORP0018')
    ON CONFLICT DO NOTHING;

    -- Add employee_reference for all accounts with employer_name
    UPDATE public.debtor_detail SET employee_reference = 'EMP001001' WHERE party_id = 99000000000001;
    UPDATE public.debtor_detail SET employee_reference = 'EMP002002' WHERE party_id = 99000000000002;
    UPDATE public.debtor_detail SET employee_reference = 'EMP003003' WHERE party_id = 99000000000003;
    UPDATE public.debtor_detail SET employee_reference = 'EMP004004' WHERE party_id = 99000000000004;
    UPDATE public.debtor_detail SET employee_reference = 'EMP005005' WHERE party_id = 99000000000005;
    UPDATE public.debtor_detail SET employee_reference = 'EMP006006' WHERE party_id = 99000000000006;
    UPDATE public.debtor_detail SET employee_reference = 'EMP007007' WHERE party_id = 99000000000007;
    UPDATE public.debtor_detail SET employee_reference = 'EMP008008' WHERE party_id = 99000000000008;
    UPDATE public.debtor_detail SET employee_reference = 'EMP009009' WHERE party_id = 99000000000009;
    UPDATE public.debtor_detail SET employee_reference = 'EMP010010' WHERE party_id = 99000000000010;

    -- 5) DEFENDANT_ACCOUNT_PARTIES
    INSERT INTO public.defendant_account_parties
    (defendant_account_party_id, defendant_account_id, party_id, association_type, debtor) VALUES
    (99000000002001, 99000000000001, 99000000000001, 'Defendant', true),
    (99000000002002, 99000000000002, 99000000000002, 'Defendant', true),
    (99000000002003, 99000000000003, 99000000000003, 'Defendant', true),
    (99000000002004, 99000000000004, 99000000000004, 'Defendant', true),
    (99000000002005, 99000000000005, 99000000000005, 'Defendant', true),
    (99000000002006, 99000000000006, 99000000000006, 'Defendant', true),
    (99000000002007, 99000000000007, 99000000000007, 'Defendant', true),
    (99000000002008, 99000000000008, 99000000000008, 'Defendant', true),
    (99000000002009, 99000000000009, 99000000000011, 'Defendant', false),
    (99000000002010, 99000000000010, 99000000000012, 'Defendant', false),
    (99000000002011, 99000000000009, 99000000000009, 'Parent/Guardian', true),
    (99000000002012, 99000000000010, 99000000000010, 'Parent/Guardian', true),
    -- Company associations
    (99000000002013, 99000000000013, 99000000000013, 'Defendant', true),
    (99000000002014, 99000000000014, 99000000000014, 'Defendant', true),
    (99000000002015, 99000000000015, 99000000000015, 'Defendant', true),
    -- Additional company associations
    (99000000002016, 99000000000016, 99000000000016, 'Defendant', true),
    (99000000002017, 99000000000017, 99000000000017, 'Defendant', true),
    (99000000002018, 99000000000018, 99000000000018, 'Defendant', true)
    ON CONFLICT DO NOTHING;

    -- 6) PAYMENT_CARD_REQUESTS
    INSERT INTO public.payment_card_requests (defendant_account_id) VALUES
    (99000000000001),
    (99000000000002),
    (99000000000003),
    (99000000000004),
    (99000000000005),
    (99000000000006),
    (99000000000007),
    (99000000000008),
    (99000000000009),
    (99000000000010),
    (99000000000013),
    (99000000000014),
    (99000000000015),
    (99000000000016),
    (99000000000017),
    (99000000000018)
    ON CONFLICT DO NOTHING;

    -- 7) Minor Creditor Party + Creditor Account (type 'MN')
    INSERT INTO public.parties (party_id, organisation, organisation_name, address_line_1, postcode, account_type) VALUES
    (99000000000900, true, 'Minor Creditor Test Ltd', '1 Test Street', 'ZZ1 1ZZ', 'Creditor')
    ON CONFLICT DO NOTHING;

    -- Additional minor creditor parties for each defendant account
    INSERT INTO public.parties (party_id, organisation, organisation_name, address_line_1, address_line_2, postcode, account_type) VALUES
    (99000000000901, true, 'Speed Camera Services Ltd', '10 Technology Way', 'Reading', 'RG6 1PT', 'Creditor'),
    (99000000000902, true, 'Parking Enforcement Corp', '25 Municipal Street', 'Brighton', 'BN1 3UH', 'Creditor'),
    (99000000000903, true, 'Traffic Violations Bureau', '88 Civic Centre', 'Leeds', 'LS1 3AD', 'Creditor'),
    (99000000000904, true, 'City Enforcement Agency', '45 Government House', 'Oxford', 'OX1 1PT', 'Creditor'),
    (99000000000905, true, 'Highway Patrol Services', '12 Law Enforcement Way', 'Cambridge', 'CB2 1TN', 'Creditor'),
    (99000000000906, true, 'Metropolitan Traffic Unit', '67 Police Station Road', 'London', 'EC1A 4HD', 'Creditor'),
    (99000000000907, true, 'Regional Safety Authority', '34 Safety House', 'Manchester', 'M1 1AA', 'Creditor'),
    (99000000000908, true, 'Fixed Penalty Office', '90 Administration Block', 'Birmingham', 'B1 1BB', 'Creditor'),
    (99000000000909, true, 'Youth Court Services', '15 Justice Building', 'Liverpool', 'L1 8JQ', 'Creditor'),
    (99000000000910, true, 'Guardian Liability Unit', '22 Family Court Centre', 'Cardiff', 'CF10 1AA', 'Creditor'),
    (99000000000913, true, 'Environmental Protection Agency', '5 Green Park', 'Bristol', 'BS1 6XN', 'Creditor'),
    (99000000000914, true, 'Health & Safety Executive', '78 Industrial Square', 'Sheffield', 'S1 2HE', 'Creditor'),
    (99000000000915, true, 'Trading Standards Office', '33 Consumer House', 'Nottingham', 'NG1 5DT', 'Creditor')
    ON CONFLICT DO NOTHING;

    INSERT INTO public.creditor_accounts
    (creditor_account_id, business_unit_id, account_number, creditor_account_type, prosecution_service, major_creditor_id, minor_creditor_party_id, from_suspense, hold_payout, pay_by_bacs, bank_sort_code, bank_account_number, bank_account_name, bank_account_reference) VALUES
    (99000000000800, 77, '87654321', 'MN', false, NULL, 99000000000900, false, false, true, '000000', '00000000', 'Minor Creditor', 'REF'),
    -- Individual defendant creditor accounts
    (99000000000801, 77, '87654322', 'MN', false, NULL, 99000000000901, false, false, true, '123456', '12345678', 'Speed Camera', 'REF001'),
    (99000000000802, 77, '87654323', 'MN', false, NULL, 99000000000902, false, false, true, '234567', '23456789', 'Parking Control', 'REF002'),
    (99000000000803, 77, '87654324', 'MN', false, NULL, 99000000000903, false, false, true, '345678', '34567890', 'Traffic Bureau', 'REF003'),
    (99000000000804, 77, '87654325', 'MN', false, NULL, 99000000000904, false, false, true, '456789', '45678901', 'City Enforcement', 'REF004'),
    (99000000000805, 77, '87654326', 'MN', false, NULL, 99000000000905, false, false, true, '567890', '56789012', 'Highway Patrol', 'REF005'),
    (99000000000806, 77, '87654327', 'MN', false, NULL, 99000000000906, false, false, true, '678901', '67890123', 'Metro Traffic', 'REF006'),
    (99000000000807, 77, '87654328', 'MN', false, NULL, 99000000000907, false, false, true, '789012', '78901234', 'Regional Safety', 'REF007'),
    (99000000000808, 77, '87654329', 'MN', false, NULL, 99000000000908, false, false, true, '890123', '89012345', 'Penalty Office', 'REF008'),
    (99000000000809, 77, '87654330', 'MN', false, NULL, 99000000000909, false, false, true, '901234', '90123456', 'Youth Court', 'REF009'),
    (99000000000810, 77, '87654331', 'MN', false, NULL, 99000000000910, false, false, true, '012345', '01234567', 'Guardian Unit', 'REF010'),
    -- Company defendant creditor accounts
    (99000000000813, 77, '87654334', 'MN', false, NULL, 99000000000913, false, false, true, '135792', '13579246', 'Environment Agency', 'REF013'),
    (99000000000814, 77, '87654335', 'MN', false, NULL, 99000000000914, false, false, true, '246813', '24681357', 'Safety Executive', 'REF014'),
    (99000000000815, 77, '87654336', 'MN', false, NULL, 99000000000915, false, false, true, '357924', '35792468', 'Trading Standards', 'REF015')
    ON CONFLICT DO NOTHING;

    -- 8) IMPOSITIONS (one per account) – using specific minor creditors per defendant
    INSERT INTO public.impositions
    (imposition_id, defendant_account_id, posted_date, posted_by, posted_by_name, result_id, imposed_amount, paid_amount, creditor_account_id, offence_title, offence_code) VALUES
    (99000000003001, 99000000000001, CURRENT_TIMESTAMP - INTERVAL '1 days', 'L080JG', 'opal-test', (SELECT result_id FROM public.results ORDER BY result_id LIMIT 1), 100.00, 10.00, 99000000000801, 'Obstruct person executing search warrant for TV receiver', 'CA03013'),
    (99000000003002, 99000000000002, CURRENT_TIMESTAMP - INTERVAL '2 days', 'L080JG', 'opal-test-2', (SELECT result_id FROM public.results ORDER BY result_id LIMIT 1), 200.00, 20.00, 99000000000802, 'Obstruct person executing search warrant for TV receiver', 'CA03013'),
    (99000000003003, 99000000000003, CURRENT_TIMESTAMP - INTERVAL '3 days', 'L026SH', 'opal-test-3', (SELECT result_id FROM public.results ORDER BY result_id LIMIT 1), 300.00, 30.00, 99000000000803, 'Obstruct person executing search warrant for TV receiver', 'CA03013'),
    (99000000003004, 99000000000004, CURRENT_TIMESTAMP - INTERVAL '4 days', 'L047SA', 'opal-test-4', (SELECT result_id FROM public.results ORDER BY result_id LIMIT 1), 400.00, 40.00, 99000000000804, 'Obstruct person executing search warrant for TV receiver', 'CA03013'),
    (99000000003005, 99000000000005, CURRENT_TIMESTAMP - INTERVAL '5 days', 'L060FO', 'opal-test-5', (SELECT result_id FROM public.results ORDER BY result_id LIMIT 1), 500.00, 50.00, 99000000000805, 'Obstruct person executing search warrant for TV receiver', 'CA03013'),
    (99000000003006, 99000000000006, CURRENT_TIMESTAMP - INTERVAL '6 days', 'L106CO', 'opal-test-6', (SELECT result_id FROM public.results ORDER BY result_id LIMIT 1), 600.00, 60.00, 99000000000806, 'Test Offence 6', 'OFF0006'),
    (99000000003007, 99000000000007, CURRENT_TIMESTAMP - INTERVAL '7 days', 'L089BO', 'opal-test-7', (SELECT result_id FROM public.results ORDER BY result_id LIMIT 1), 700.00, 70.00, 99000000000807, 'Test Offence 7', 'OFF0007'),
    (99000000003008, 99000000000008, CURRENT_TIMESTAMP - INTERVAL '8 days', 'L036DO', 'opal-test-8', (SELECT result_id FROM public.results ORDER BY result_id LIMIT 1), 800.00, 80.00, 99000000000808, 'Test Offence 8', 'OFF0008'),
    (99000000003009, 99000000000009, CURRENT_TIMESTAMP - INTERVAL '9 days', 'L045EO', 'opal-test-9', (SELECT result_id FROM public.results ORDER BY result_id LIMIT 1), 900.00, 90.00, 99000000000809, 'Test Offence 9', 'OFF0009'),
    (99000000003010, 99000000000010, CURRENT_TIMESTAMP - INTERVAL '10 days', 'L065AO', 'opal-test-10', (SELECT result_id FROM public.results ORDER BY result_id LIMIT 1), 1000.00, 100.00, 99000000000810, 'Test Offence 10', 'OFF0010'),
    -- Company impositions
    (99000000003013, 99000000000013, CURRENT_TIMESTAMP - INTERVAL '12 days', 'L080JG', 'opal-test-13', (SELECT result_id FROM public.results ORDER BY result_id LIMIT 1), 350.00, 0.00, 99000000000813, 'Environmental Protection Violation', 'ENV001'),
    (99000000003014, 99000000000014, CURRENT_TIMESTAMP - INTERVAL '18 days', 'L047SA', 'opal-test-14', (SELECT result_id FROM public.results ORDER BY result_id OFFSET 1 LIMIT 1), 850.00, 100.00, 99000000000814, 'Health and Safety at Work Violation', 'HSW002'),
    (99000000003015, 99000000000015, CURRENT_TIMESTAMP - INTERVAL '60 days', 'L026SH', 'opal-test-15', (SELECT result_id FROM public.results ORDER BY result_id OFFSET 2 LIMIT 1), 1250.00, 1250.00, 99000000000815, 'Trading Standards Violation', 'TRD003')
    ON CONFLICT DO NOTHING;

    -- 9) DEFENDANT_TRANSACTIONS
    INSERT INTO public.defendant_transactions
    (defendant_transaction_id, defendant_account_id, posted_date, posted_by, transaction_type, transaction_amount, payment_method, payment_reference, text, status, status_date, posted_by_name) VALUES
    (99000000004001, 99000000000001, CURRENT_TIMESTAMP - INTERVAL '1 days', 'L080JG', 'PAY', 10.00, 'CC', 'PMT000001', 'Card payment', 'C', CURRENT_TIMESTAMP - INTERVAL '1 days', 'opal-test'),
    (99000000004002, 99000000000002, CURRENT_TIMESTAMP - INTERVAL '2 days', 'L080JG', 'PAY', 20.00, 'CC', 'PMT000002', 'Card payment', 'C', CURRENT_TIMESTAMP - INTERVAL '2 days', 'opal-test-2'),
    (99000000004003, 99000000000003, CURRENT_TIMESTAMP - INTERVAL '3 days', 'L026SH', 'PAY', 30.00, 'CC', 'PMT000003', 'Card payment', 'C', CURRENT_TIMESTAMP - INTERVAL '3 days', 'opal-test-3'),
    (99000000004004, 99000000000004, CURRENT_TIMESTAMP - INTERVAL '4 days', 'L047SA', 'PAY', 40.00, 'CC', 'PMT000004', 'Card payment', 'C', CURRENT_TIMESTAMP - INTERVAL '4 days', 'opal-test-4'),
    (99000000004005, 99000000000005, CURRENT_TIMESTAMP - INTERVAL '5 days', 'L060FO', 'PAY', 50.00, 'CC', 'PMT000005', 'Card payment', 'C', CURRENT_TIMESTAMP - INTERVAL '5 days', 'opal-test-5'),
    (99000000004006, 99000000000006, CURRENT_TIMESTAMP - INTERVAL '6 days', 'L106CO', 'PAY', 60.00, 'CC', 'PMT000006', 'Card payment', 'C', CURRENT_TIMESTAMP - INTERVAL '6 days', 'opal-test-6'),
    (99000000004007, 99000000000007, CURRENT_TIMESTAMP - INTERVAL '7 days', 'L089BO', 'PAY', 70.00, 'CC', 'PMT000007', 'Card payment', 'C', CURRENT_TIMESTAMP - INTERVAL '7 days', 'opal-test-7'),
    (99000000004008, 99000000000008, CURRENT_TIMESTAMP - INTERVAL '8 days', 'L036DO', 'PAY', 80.00, 'CC', 'PMT000008', 'Card payment', 'C', CURRENT_TIMESTAMP - INTERVAL '8 days', 'opal-test-8'),
    (99000000004009, 99000000000009, CURRENT_TIMESTAMP - INTERVAL '9 days', 'L045EO', 'PAY', 90.00, 'CC', 'PMT000009', 'Card payment', 'C', CURRENT_TIMESTAMP - INTERVAL '9 days', 'opal-test-9'),
    (99000000004010, 99000000000010, CURRENT_TIMESTAMP - INTERVAL '10 days', 'L065AO', 'PAY', 100.00, 'CC', 'PMT000010', 'Card payment', 'C', CURRENT_TIMESTAMP - INTERVAL '10 days', 'opal-test-10'),
    -- Company transactions
    (99000000004014, 99000000000014, CURRENT_TIMESTAMP - INTERVAL '15 days', 'L047SA', 'PAY', 100.00, 'CC', 'PMT000014', 'Bank transfer payment', 'C', CURRENT_TIMESTAMP - INTERVAL '15 days', 'opal-test-14'),
    (99000000004015, 99000000000015, CURRENT_TIMESTAMP - INTERVAL '55 days', 'L026SH', 'PAY', 1250.00, 'CC', 'PMT000015', 'Full settlement payment', 'C', CURRENT_TIMESTAMP - INTERVAL '55 days', 'opal-test-15')
    ON CONFLICT DO NOTHING;

    -- 10) ALLOCATIONS (link transactions to impositions)
    INSERT INTO public.allocations
    (allocation_id, imposition_id, allocated_date, allocated_amount, transaction_type, allocation_function, defendant_transaction_id) VALUES
    (99000000005001, 99000000003001, CURRENT_TIMESTAMP - INTERVAL '1 days', 10.00, 'PAYMENT', 'C104A', 99000000004001),
    (99000000005002, 99000000003002, CURRENT_TIMESTAMP - INTERVAL '2 days', 20.00, 'PAYMENT', 'C104A', 99000000004002),
    (99000000005003, 99000000003003, CURRENT_TIMESTAMP - INTERVAL '3 days', 30.00, 'PAYMENT', 'C104A', 99000000004003),
    (99000000005004, 99000000003004, CURRENT_TIMESTAMP - INTERVAL '4 days', 40.00, 'PAYMENT', 'C104A', 99000000004004),
    (99000000005005, 99000000003005, CURRENT_TIMESTAMP - INTERVAL '5 days', 50.00, 'PAYMENT', 'C104A', 99000000004005),
    (99000000005006, 99000000003006, CURRENT_TIMESTAMP - INTERVAL '6 days', 60.00, 'PAYMENT', 'AutoTest', 99000000004006),
    (99000000005007, 99000000003007, CURRENT_TIMESTAMP - INTERVAL '7 days', 70.00, 'PAYMENT', 'AutoTest', 99000000004007),
    (99000000005008, 99000000003008, CURRENT_TIMESTAMP - INTERVAL '8 days', 80.00, 'PAYMENT', 'AutoTest', 99000000004008),
    (99000000005009, 99000000003009, CURRENT_TIMESTAMP - INTERVAL '9 days', 90.00, 'PAYMENT', 'AutoTest', 99000000004009),
    (99000000005010, 99000000003010, CURRENT_TIMESTAMP - INTERVAL '10 days', 100.00, 'PAYMENT', 'AutoTest', 99000000004010),
    -- Company allocations
    (99000000005014, 99000000003014, CURRENT_TIMESTAMP - INTERVAL '15 days', 100.00, 'PAYMENT', 'CorpTest', 99000000004014),
    (99000000005015, 99000000003015, CURRENT_TIMESTAMP - INTERVAL '55 days', 1250.00, 'PAYMENT', 'CorpTest', 99000000004015)
    ON CONFLICT DO NOTHING;

    -- 11) CREDITOR_TRANSACTIONS
    INSERT INTO public.creditor_transactions
    (creditor_transaction_id, creditor_account_id, posted_date, posted_by, posted_by_name, transaction_type, transaction_amount, imposition_result_id, payment_processed, payment_reference, status, status_date) VALUES
    (99000000006001, 99000000000801, CURRENT_TIMESTAMP - INTERVAL '1 days', 'L080JG', 'opal-test', 'PAYMNT', 10.00, (SELECT result_id FROM public.results ORDER BY result_id LIMIT 1), true, 'PMT000001', 'C', CURRENT_TIMESTAMP - INTERVAL '1 days'),
    (99000000006002, 99000000000802, CURRENT_TIMESTAMP - INTERVAL '2 days', 'L080JG', 'opal-test-2', 'PAYMNT', 20.00, (SELECT result_id FROM public.results ORDER BY result_id LIMIT 1), true, 'PMT000002', 'C', CURRENT_TIMESTAMP - INTERVAL '2 days'),
    (99000000006003, 99000000000803, CURRENT_TIMESTAMP - INTERVAL '3 days', 'L026SH', 'opal-test-3', 'PAYMNT', 30.00, (SELECT result_id FROM public.results ORDER BY result_id LIMIT 1), true, 'PMT000003', 'C', CURRENT_TIMESTAMP - INTERVAL '3 days'),
    (99000000006004, 99000000000804, CURRENT_TIMESTAMP - INTERVAL '4 days', 'L047SA', 'opal-test-4', 'PAYMNT', 40.00, (SELECT result_id FROM public.results ORDER BY result_id LIMIT 1), true, 'PMT000004', 'C', CURRENT_TIMESTAMP - INTERVAL '4 days'),
    (99000000006005, 99000000000805, CURRENT_TIMESTAMP - INTERVAL '5 days', 'L060FO', 'opal-test-5', 'PAYMNT', 50.00, (SELECT result_id FROM public.results ORDER BY result_id LIMIT 1), true, 'PMT000005', 'C', CURRENT_TIMESTAMP - INTERVAL '5 days'),
    (99000000006006, 99000000000806, CURRENT_TIMESTAMP - INTERVAL '6 days', 'L106CO', 'opal-test-6', 'PAYMNT', 60.00, (SELECT result_id FROM public.results ORDER BY result_id LIMIT 1), true, 'PMT000006', 'C', CURRENT_TIMESTAMP - INTERVAL '6 days'),
    (99000000006007, 99000000000807, CURRENT_TIMESTAMP - INTERVAL '7 days', 'L089BO', 'opal-test-7', 'PAYMNT', 70.00, (SELECT result_id FROM public.results ORDER BY result_id LIMIT 1), true, 'PMT000007', 'C', CURRENT_TIMESTAMP - INTERVAL '7 days'),
    (99000000006008, 99000000000808, CURRENT_TIMESTAMP - INTERVAL '8 days', 'L036DO', 'opal-test-8', 'PAYMNT', 80.00, (SELECT result_id FROM public.results ORDER BY result_id LIMIT 1), true, 'PMT000008', 'C', CURRENT_TIMESTAMP - INTERVAL '8 days'),
    (99000000006009, 99000000000809, CURRENT_TIMESTAMP - INTERVAL '9 days', 'L045EO', 'opal-test-9', 'PAYMNT', 90.00, (SELECT result_id FROM public.results ORDER BY result_id LIMIT 1), true, 'PMT000009', 'C', CURRENT_TIMESTAMP - INTERVAL '9 days'),
    (99000000006010, 99000000000810, CURRENT_TIMESTAMP - INTERVAL '10 days', 'L065AO','opal-test-10','PAYMNT', 100.00, (SELECT result_id FROM public.results ORDER BY result_id LIMIT 1), true, 'PMT000010', 'C', CURRENT_TIMESTAMP - INTERVAL '10 days'),
    -- Company creditor transactions
    (99000000006014, 99000000000814, CURRENT_TIMESTAMP - INTERVAL '15 days', 'L047SA', 'opal-test-14', 'PAYMNT', 100.00, (SELECT result_id FROM public.results ORDER BY result_id OFFSET 1 LIMIT 1), true, 'PMT000014', 'C', CURRENT_TIMESTAMP - INTERVAL '15 days'),
    (99000000006015, 99000000000815, CURRENT_TIMESTAMP - INTERVAL '55 days', 'L026SH', 'opal-test-15', 'PAYMNT', 1250.00, (SELECT result_id FROM public.results ORDER BY result_id OFFSET 2 LIMIT 1), true, 'PMT000015', 'C', CURRENT_TIMESTAMP - INTERVAL '55 days')
    ON CONFLICT DO NOTHING;

    -- 12) FIXED_PENALTY_OFFENCES
    INSERT INTO public.fixed_penalty_offences
    (defendant_account_id, ticket_number, vehicle_registration, offence_location, issued_date, offence_date, offence_time) VALUES
    (99000000000001, 'SP240001235', 'BX67KLM', 'High Street, Central London', CURRENT_DATE - INTERVAL '15 days', CURRENT_DATE - INTERVAL '15 days', '14:30'),
    (99000000000002, 'PK240002847', 'FG19XYZ', 'Seafront Car Park, Brighton', CURRENT_DATE - INTERVAL '22 days', CURRENT_DATE - INTERVAL '22 days', '09:15'),
    (99000000000003, 'TR240003691', 'HJ21ABC', 'M1 Motorway Junction 45', CURRENT_DATE - INTERVAL '45 days', CURRENT_DATE - INTERVAL '45 days', '16:45'),
    (99000000000004, 'SP240004582', 'LK18DEF', 'Oxford Street, London', CURRENT_DATE - INTERVAL '8 days', CURRENT_DATE - INTERVAL '8 days', '11:20'),
    (99000000000005, 'MV240005739', 'NM20GHI', 'A14 Cambridge Bypass', CURRENT_DATE - INTERVAL '30 days', CURRENT_DATE - INTERVAL '30 days', '08:45'),
    (99000000000006, 'TICKET000006', 'REG0006', 'Testville Main Street', CURRENT_DATE - INTERVAL '6 days', CURRENT_DATE - INTERVAL '6 days', '10:00'),
    (99000000000007, 'TICKET000007', 'REG0007', 'Testville High Street', CURRENT_DATE - INTERVAL '7 days', CURRENT_DATE - INTERVAL '7 days', '10:00'),
    (99000000000008, 'FP24000008', 'REG0008', 'City Centre Car Park', CURRENT_DATE - INTERVAL '5 days', CURRENT_DATE - INTERVAL '5 days', '14:15'),
    (99000000000009, 'TICKET000009', 'REG0009', 'School Zone Violation', CURRENT_DATE - INTERVAL '9 days', CURRENT_DATE - INTERVAL '9 days', '08:30'),
    (99000000000010, 'FP24000010', 'REG0010', 'Residential Area Speeding', CURRENT_DATE - INTERVAL '3 days', CURRENT_DATE - INTERVAL '3 days', '16:45'),
    -- Company fixed penalty offences
    (99000000000013, 'CORP000013', 'CORP0013', 'Corporate Vehicle Violation', CURRENT_DATE - INTERVAL '12 days', CURRENT_DATE - INTERVAL '12 days', '11:30'),
    (99000000000014, 'CORP000014', 'CORP0014', 'Commercial Vehicle Offence', CURRENT_DATE - INTERVAL '18 days', CURRENT_DATE - INTERVAL '18 days', '09:45'),
    (99000000000015, 'CORP000015', 'CORP0015', 'Fleet Vehicle Violation', CURRENT_DATE - INTERVAL '60 days', CURRENT_DATE - INTERVAL '60 days', '14:20')
    ON CONFLICT DO NOTHING;

    -- 13) ENFORCEMENTS
    INSERT INTO public.enforcements
    (enforcement_id, defendant_account_id, posted_date, posted_by, reason, result_responses, warrant_reference, posted_by_name) VALUES
    (99000000007001, 99000000000001, CURRENT_TIMESTAMP - INTERVAL '1 days', 'L080JG', 'Test enforcement', '{"k":"v"}'::json, '001/25/00001', 'opal-test'),
    (99000000007002, 99000000000002, CURRENT_TIMESTAMP - INTERVAL '2 days', 'L080JG', 'Test enforcement', '{"k":"v"}'::json, '001/25/00002', 'opal-test-2'),
    (99000000007003, 99000000000003, CURRENT_TIMESTAMP - INTERVAL '3 days', 'L026SH', 'Test enforcement', '{"k":"v"}'::json, '002/25/00003', 'opal-test-3'),
    (99000000007004, 99000000000004, CURRENT_TIMESTAMP - INTERVAL '4 days', 'L047SA', 'Test enforcement', '{"k":"v"}'::json, '002/25/00004', 'opal-test-4'),
    (99000000007005, 99000000000005, CURRENT_TIMESTAMP - INTERVAL '5 days', 'L060FO', 'Test enforcement', '{"k":"v"}'::json, '003/25/00005', 'opal-test-5'),
    (99000000007006, 99000000000006, CURRENT_TIMESTAMP - INTERVAL '6 days', 'L106CO', 'Test enforcement', '{"k":"v"}'::json, '003/25/00006', 'opal-test-6'),
    (99000000007007, 99000000000007, CURRENT_TIMESTAMP - INTERVAL '7 days', 'L089BO', 'Test enforcement', '{"k":"v"}'::json, '004/25/00007', 'opal-test-7'),
    (99000000007008, 99000000000008, CURRENT_TIMESTAMP - INTERVAL '8 days', 'L036DO', 'Test enforcement', '{"k":"v"}'::json, '004/25/00008', 'opal-test-8'),
    (99000000007009, 99000000000009, CURRENT_TIMESTAMP - INTERVAL '9 days', 'L045EO', 'Test enforcement', '{"k":"v"}'::json, '005/25/00009', 'opal-test-9'),
    (99000000007010, 99000000000010, CURRENT_TIMESTAMP - INTERVAL '10 days', 'L065AO', 'Test enforcement', '{"k":"v"}'::json, '005/25/00010', 'opal-test-10'),
    -- Company enforcements
    (99000000007013, 99000000000013, CURRENT_TIMESTAMP - INTERVAL '8 days', 'L080JG', 'Corporate enforcement action', '{"type":"corporate"}'::json, '006/25/00013', 'opal-test-13'),
    (99000000007014, 99000000000014, CURRENT_TIMESTAMP - INTERVAL '12 days', 'L047SA', 'Business compliance enforcement', '{"type":"business"}'::json, '006/25/00014', 'opal-test-14')
    ON CONFLICT DO NOTHING;

    -- 14) REPORT_INSTANCES (1)
    INSERT INTO public.report_instances
    (report_instance_id, report_id, business_unit_id, audit_sequence, created_timestamp, requested_by, requested_at, generation_status, requested_by_name, report_parameters, location) VALUES
    (99000000008000, (SELECT report_id FROM public.reports ORDER BY report_id LIMIT 1), ARRAY[77]::smallint[], 1, CURRENT_TIMESTAMP, 12345678, CURRENT_TIMESTAMP, 'READY', 'opal-test', '{"from":"auto"}'::json, 'test-location')
    ON CONFLICT DO NOTHING;

    -- 15) REPORT_ENTRIES (10 + 3 companies = 13 total)
    INSERT INTO public.report_entries
    (report_entry_id, business_unit_id, report_id, entry_timestamp, reported_timestamp, associated_record_type, associated_record_id, report_instance_id) VALUES
    (99000000009001, 77, (SELECT report_id FROM public.reports ORDER BY report_id LIMIT 1), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'defendant_accounts', '99000000000001', 99000000008000),
    (99000000009002, 77, (SELECT report_id FROM public.reports ORDER BY report_id LIMIT 1), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'defendant_accounts', '99000000000002', 99000000008000),
    (99000000009003, 77, (SELECT report_id FROM public.reports ORDER BY report_id LIMIT 1), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'defendant_accounts', '99000000000003', 99000000008000),
    (99000000009004, 77, (SELECT report_id FROM public.reports ORDER BY report_id LIMIT 1), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'defendant_accounts', '99000000000004', 99000000008000),
    (99000000009005, 77, (SELECT report_id FROM public.reports ORDER BY report_id LIMIT 1), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'defendant_accounts', '99000000000005', 99000000008000),
    (99000000009006, 77, (SELECT report_id FROM public.reports ORDER BY report_id LIMIT 1), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'defendant_accounts', '99000000000006', 99000000008000),
    (99000000009007, 77, (SELECT report_id FROM public.reports ORDER BY report_id LIMIT 1), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'defendant_accounts', '99000000000007', 99000000008000),
    (99000000009008, 77, (SELECT report_id FROM public.reports ORDER BY report_id LIMIT 1), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'defendant_accounts', '99000000000008', 99000000008000),
    (99000000009009, 77, (SELECT report_id FROM public.reports ORDER BY report_id LIMIT 1), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'defendant_accounts', '99000000000009', 99000000008000),
    (99000000009010, 77, (SELECT report_id FROM public.reports ORDER BY report_id LIMIT 1), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'defendant_accounts', '99000000000010', 99000000008000),
    -- Company report entries
    (99000000009013, 77, (SELECT report_id FROM public.reports ORDER BY report_id LIMIT 1), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'defendant_accounts', '99000000000013', 99000000008000),
    (99000000009014, 77, (SELECT report_id FROM public.reports ORDER BY report_id LIMIT 1), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'defendant_accounts', '99000000000014', 99000000008000),
    (99000000009015, 77, (SELECT report_id FROM public.reports ORDER BY report_id LIMIT 1), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'defendant_accounts', '99000000000015', 99000000008000)
    ON CONFLICT DO NOTHING;

    -- 16) CONTROL_TOTALS (3)
    INSERT INTO public.control_totals
    (control_total_id, business_unit_id, item_number, amount, associated_record_type, associated_record_id, ct_report_instance_id) VALUES
    (99000000009501, 77, 1, 100.00, 'REPORT_INSTANCE', '99000000008000', 99000000008000),
    (99000000009502, 77, 2, 200.00, 'REPORT_INSTANCE', '99000000008000', 99000000008000),
    (99000000009503, 77, 3, 300.00, 'REPORT_INSTANCE', '99000000008000', 99000000008000)
    ON CONFLICT DO NOTHING;

    -- 17) TILLS (1)
    INSERT INTO public.tills
    (till_id, business_unit_id, till_number, owned_by) VALUES
    (99000000010000, 77, 9001, 'L080JG')
    ON CONFLICT DO NOTHING;

    -- 18) NOTES (AA only)
    INSERT INTO public.notes
    (note_id, note_type, associated_record_type, associated_record_id, note_text, posted_date, posted_by, posted_by_name) VALUES
    (99000000011001, 'AA', 'defendant_accounts', '99000000000001', 'Auto test note for account 99000000000001', CURRENT_TIMESTAMP, 'L080JG', 'opal-test'),
    (99000000011002, 'AA', 'defendant_accounts', '99000000000002', 'Auto test note for account 99000000000002', CURRENT_TIMESTAMP, 'L080JG', 'opal-test-2'),
    (99000000011003, 'AA', 'defendant_accounts', '99000000000003', 'Auto test note for account 99000000000003', CURRENT_TIMESTAMP, 'L026SH', 'opal-test-3'),
    (99000000011004, 'AA', 'defendant_accounts', '99000000000004', 'Auto test note for account 99000000000004', CURRENT_TIMESTAMP, 'L047SA', 'opal-test-4'),
    (99000000011005, 'AA', 'defendant_accounts', '99000000000005', 'Auto test note for account 99000000000005', CURRENT_TIMESTAMP, 'L060FO', 'opal-test-5'),
    (99000000011006, 'AA', 'defendant_accounts', '99000000000006', 'Auto test note for account 99000000000006', CURRENT_TIMESTAMP, 'L106CO', 'opal-test-6'),
    (99000000011007, 'AA', 'defendant_accounts', '99000000000007', 'Auto test note for account 99000000000007', CURRENT_TIMESTAMP, 'L089BO', 'opal-test-7'),
    (99000000011008, 'AA', 'defendant_accounts', '99000000000008', 'Auto test note for account 99000000000008', CURRENT_TIMESTAMP, 'L036DO', 'opal-test-8'),
    (99000000011009, 'AA', 'defendant_accounts', '99000000000009', 'Auto test note for account 99000000000009', CURRENT_TIMESTAMP, 'L045EO', 'opal-test-9'),
    (99000000011010, 'AA', 'defendant_accounts', '99000000000010', 'Auto test note for account 99000000000010', CURRENT_TIMESTAMP, 'L065AO', 'opal-test-10'),
    -- Company notes
    (99000000011013, 'AA', 'defendant_accounts', '99000000000013', 'Corporate account - environmental compliance case', CURRENT_TIMESTAMP, 'L080JG', 'opal-test-13'),
    (99000000011014, 'AA', 'defendant_accounts', '99000000000014', 'Business entity - health and safety violation', CURRENT_TIMESTAMP, 'L047SA', 'opal-test-14'),
    (99000000011015, 'AA', 'defendant_accounts', '99000000000015', 'Company account - trading standards case completed', CURRENT_TIMESTAMP, 'L026SH', 'opal-test-15')
    ON CONFLICT DO NOTHING;

    -- 19) DOCUMENT_INSTANCES (10 + 3 companies = 13 total)
    INSERT INTO public.document_instances
    (document_instance_id, document_id, business_unit_id, generated_date, generated_by, associated_record_type, associated_record_id, status, document_content) VALUES
    (99000000012001, (SELECT document_id FROM public.documents ORDER BY document_id LIMIT 1), 77, CURRENT_TIMESTAMP, 'L080JG', 'defendant_accounts', '99000000000001', 'Generated', XMLPARSE(DOCUMENT '<doc><account>99000000000001</account></doc>')),
    (99000000012002, (SELECT document_id FROM public.documents ORDER BY document_id LIMIT 1), 77, CURRENT_TIMESTAMP, 'L080JG', 'defendant_accounts', '99000000000002', 'Generated', XMLPARSE(DOCUMENT '<doc><account>99000000000002</account></doc>')),
    (99000000012003, (SELECT document_id FROM public.documents ORDER BY document_id LIMIT 1), 77, CURRENT_TIMESTAMP, 'L026SH', 'defendant_accounts', '99000000000003', 'Generated', XMLPARSE(DOCUMENT '<doc><account>99000000000003</account></doc>')),
    (99000000012004, (SELECT document_id FROM public.documents ORDER BY document_id LIMIT 1), 77, CURRENT_TIMESTAMP, 'L047SA', 'defendant_accounts', '99000000000004', 'Generated', XMLPARSE(DOCUMENT '<doc><account>99000000000004</account></doc>')),
    (99000000012005, (SELECT document_id FROM public.documents ORDER BY document_id LIMIT 1), 77, CURRENT_TIMESTAMP, 'L060FO', 'defendant_accounts', '99000000000005', 'Generated', XMLPARSE(DOCUMENT '<doc><account>99000000000005</account></doc>')),
    (99000000012006, (SELECT document_id FROM public.documents ORDER BY document_id LIMIT 1), 77, CURRENT_TIMESTAMP, 'L106CO', 'defendant_accounts', '99000000000006', 'Generated', XMLPARSE(DOCUMENT '<doc><account>99000000000006</account></doc>')),
    (99000000012007, (SELECT document_id FROM public.documents ORDER BY document_id LIMIT 1), 77, CURRENT_TIMESTAMP, 'L089BO', 'defendant_accounts', '99000000000007', 'Generated', XMLPARSE(DOCUMENT '<doc><account>99000000000007</account></doc>')),
    (99000000012008, (SELECT document_id FROM public.documents ORDER BY document_id LIMIT 1), 77, CURRENT_TIMESTAMP, 'L036DO', 'defendant_accounts', '99000000000008', 'Generated', XMLPARSE(DOCUMENT '<doc><account>99000000000008</account></doc>')),
    (99000000012009, (SELECT document_id FROM public.documents ORDER BY document_id LIMIT 1), 77, CURRENT_TIMESTAMP, 'L045EO', 'defendant_accounts', '99000000000009', 'Generated', XMLPARSE(DOCUMENT '<doc><account>99000000000009</account></doc>')),
    (99000000012010, (SELECT document_id FROM public.documents ORDER BY document_id LIMIT 1), 77, CURRENT_TIMESTAMP, 'L065AO', 'defendant_accounts', '99000000000010', 'Generated', XMLPARSE(DOCUMENT '<doc><account>99000000000010</account></doc>')),
    -- Company document instances
    (99000000012013, (SELECT document_id FROM public.documents ORDER BY document_id LIMIT 1), 77, CURRENT_TIMESTAMP, 'L080JG', 'defendant_accounts', '99000000000013', 'Generated', XMLPARSE(DOCUMENT '<doc><account>99000000000013</account><type>corporate</type></doc>')),
    (99000000012014, (SELECT document_id FROM public.documents ORDER BY document_id LIMIT 1), 77, CURRENT_TIMESTAMP, 'L047SA', 'defendant_accounts', '99000000000014', 'Generated', XMLPARSE(DOCUMENT '<doc><account>99000000000014</account><type>business</type></doc>')),
    (99000000012015, (SELECT document_id FROM public.documents ORDER BY document_id LIMIT 1), 77, CURRENT_TIMESTAMP, 'L026SH', 'defendant_accounts', '99000000000015', 'Generated', XMLPARSE(DOCUMENT '<doc><account>99000000000015</account><type>company</type></doc>'))
    ON CONFLICT DO NOTHING;

    -- 20) PAYMENT_TERMS (10 + 3 companies = 13 total)
    INSERT INTO public.payment_terms
    (payment_terms_id, defendant_account_id, posted_date, posted_by, terms_type_code, effective_date, instalment_period, instalment_amount, instalment_lump_sum, jail_days, extension, account_balance, posted_by_name, active) VALUES
    (99000000013001, 99000000000001, CURRENT_TIMESTAMP - INTERVAL '1 days', 'L080JG', 'I', CURRENT_DATE, 'M', 10.00, 0.00, NULL, false, 90.00, 'opal-test', true),
    (99000000013002, 99000000000002, CURRENT_TIMESTAMP - INTERVAL '2 days', 'L080JG', 'I', CURRENT_DATE, 'M', 20.00, 0.00, NULL, false, 180.00, 'opal-test-2', true),
    (99000000013003, 99000000000003, CURRENT_TIMESTAMP - INTERVAL '3 days', 'L026SH', 'I', CURRENT_DATE, 'M', 30.00, 0.00, NULL, false, 270.00, 'opal-test-3', true),
    (99000000013004, 99000000000004, CURRENT_TIMESTAMP - INTERVAL '4 days', 'L047SA', 'I', CURRENT_DATE, 'M', 40.00, 0.00, NULL, false, 360.00, 'opal-test-4', true),
    (99000000013005, 99000000000005, CURRENT_TIMESTAMP - INTERVAL '5 days', 'L060FO', 'I', CURRENT_DATE, 'M', 50.00, 0.00, NULL, false, 450.00, 'opal-test-5', true),
    (99000000013006, 99000000000006, CURRENT_TIMESTAMP - INTERVAL '6 days', 'L106CO', 'I', CURRENT_DATE, 'M', 60.00, 0.00, NULL, false, 540.00, 'opal-test-6', true),
    (99000000013007, 99000000000007, CURRENT_TIMESTAMP - INTERVAL '7 days', 'L089BO', 'I', CURRENT_DATE, 'M', 70.00, 0.00, NULL, false, 630.00, 'opal-test-7', true),
    (99000000013008, 99000000000008, CURRENT_TIMESTAMP - INTERVAL '8 days', 'L036DO', 'I', CURRENT_DATE, 'M', 80.00, 0.00, NULL, false, 720.00, 'opal-test-8', true),
    (99000000013009, 99000000000009, CURRENT_TIMESTAMP - INTERVAL '9 days', 'L045EO', 'I', CURRENT_DATE, 'M', 90.00, 0.00, NULL, false, 810.00, 'opal-test-9', true),
    (99000000013010, 99000000000010, CURRENT_TIMESTAMP - INTERVAL '10 days', 'L065AO', 'I', CURRENT_DATE, 'M', 100.00, 0.00, NULL, false, 900.00, 'opal-test-10', true),
    -- Company payment terms
    (99000000013013, 99000000000013, CURRENT_TIMESTAMP - INTERVAL '10 days', 'L080JG', 'I', CURRENT_DATE, 'M', 175.00, 0.00, NULL, false, 350.00, 'opal-test-13', true),
    (99000000013014, 99000000000014, CURRENT_TIMESTAMP - INTERVAL '15 days', 'L047SA', 'I', CURRENT_DATE, 'Q', 250.00, 0.00, NULL, false, 750.00, 'opal-test-14', true)
    ON CONFLICT DO NOTHING;

    SELECT COALESCE(MAX(draft_account_id), 0) + 100000 INTO base_id FROM draft_accounts;

    -- Insert 20 draft_accounts test entries with real JSONs and all statuses
    INSERT INTO draft_accounts (
        draft_account_id,
        business_unit_id,
        created_date,
        submitted_by,
        account,
        account_type,
        account_snapshot,
        account_status,
        timeline_data,
        submitted_by_name,
        account_status_date,
        version_number
    ) VALUES
        -- 1 SUBMITTED
        (
            base_id + 0,
            77,
            CURRENT_DATE - INTERVAL '0 days',
            'L077JG',
            '{"offences": [{"offence_id": 33369, "impositions": [ {"result_id": "FVS", "amount_paid": 50, "amount_imposed": 125, "minor_creditor": null, "major_creditor_id": null}, {"result_id": "FCPC", "amount_paid": 0, "amount_imposed": 80, "minor_creditor": null, "major_creditor_id": null}, {"result_id": "FO", "amount_paid": 0, "amount_imposed": 45, "minor_creditor": null, "major_creditor_id": null} ], "date_of_sentence": "15/02/2023", "imposing_court_id": 770000000021 }], "defendant": { "dob": "2008-09-14", "title": "Mr", "gender": null, "pnc_id": null, "surname": "MORGAN", "forenames": "Liam James", "post_code": "NW1 6XE", "cro_number": null, "occupation": null, "company_flag": false, "company_name": null, "debtor_detail": { "aliases": [{"alias_surname": "SMITH", "alias_forenames": "Jay Thomas", "alias_company_name": null}], "vehicle_make": "Peugeot 208", "vehicle_registration_mark": "YX08 WDE", "hearing_language": null, "document_language": null, "employee_reference": "QQ654321D", "employer_post_code": "AL1 5XZ", "employer_company_name": "Greenfield Garden Services", "employer_email_address": "admin@greenfieldgardens.co.uk", "employer_address_line_1": "Unit 12", "employer_address_line_2": "Park Farm Estate", "employer_address_line_3": "Lemsford Lane", "employer_address_line_4": "Hatfield", "employer_address_line_5": "Herts", "employer_telephone_number": "01727889900" }, "nationality_1": null, "nationality_2": null, "prison_number": null, "address_line_1": "Flat 5B", "address_line_2": "221B Baker Street", "address_line_3": "Marylebone", "address_line_4": null, "address_line_5": null, "custody_status": null, "email_address_1": "liam.morgan@example.com", "email_address_2": "liam.j.morgan@gmail.com", "parent_guardian": null, "interpreter_lang": null, "ethnicity_observed": null, "telephone_number_home": "020 7123 9876", "driving_licence_number": null, "ethnicity_self_defined": null, "telephone_number_mobile": "07700 900456", "national_insurance_number": "QQ654321D", "telephone_number_business": "0161 496 2001" }, "account_type": "fine", "account_notes": [ { "note_type": "AC", "account_note_text": "Under 18 – flag for youth handling", "account_note_serial": 3 }, { "note_type": "AA", "account_note_text": "Youth defendant. Still in part-time education. Collection order issued 22/02/2023. Parent/guardian details pending. Confirm contact via email before enforcement.", "account_note_serial": 2 }, { "note_type": "AA", "account_note_text": "A collection order was previously made on 22/02/2023 prior to this account creation", "account_note_serial": 1 } ], "originator_id": 101, "payment_terms": { "enforcements": null, "effective_date": "2023-03-22", "lump_sum_amount": 50, "instalment_amount": 25, "instalment_period": "W", "default_days_in_jail": null, "payment_terms_type_code": "I" }, "defendant_type": "adultOrYouthOnly", "originator_name": "Aberdare County Court", "fp_ticket_detail": null, "enforcement_court_id": 770000000021, "payment_card_request": null, "account_sentence_date": "2023-02-15", "collection_order_date": "2023-02-22", "collection_order_made": true, "suspended_committal_date": null, "prosecutor_case_reference": "21MET409283", "collection_order_made_today": null }',
            'fine',
            '{"account_type": "fine", "created_date": "2025-06-03T11:15:00.000000Z", "submitted_by": "L077JG", "date_of_birth": "2008-09-14", "defendant_name": "MORGAN, Liam James", "submitted_by_name": "opal-test", "business_unit_name": "Camberwell Green"}',
            'SUBMITTED',
            '[{"status": "Submitted", "username": "opal-test", "reason_text": null, "status_date": "2025-06-03"}]',
            'opal-test',
            CURRENT_DATE - INTERVAL '0 days',
            0
        ),
        -- 2 SUBMITTED
        (
            base_id + 1,
            77,
            CURRENT_DATE - INTERVAL '0 days',
            'L077JG',
            '{"offences": [{"offence_id": 33369, "impositions": [ {"result_id": "FVS", "amount_paid": 50, "amount_imposed": 125, "minor_creditor": null, "major_creditor_id": null}, {"result_id": "FCPC", "amount_paid": 0, "amount_imposed": 80, "minor_creditor": null, "major_creditor_id": null}, {"result_id": "FO", "amount_paid": 0, "amount_imposed": 45, "minor_creditor": null, "major_creditor_id": null} ], "date_of_sentence": "15/02/2023", "imposing_court_id": 770000000021 }], "defendant": { "dob": "2010-05-18", "title": "Miss", "gender": null, "pnc_id": null, "surname": "BENNETT", "forenames": "Ava Grace", "post_code": "BS1 4ST", "cro_number": null, "occupation": null, "company_flag": false, "company_name": null, "debtor_detail": null, "nationality_1": null, "nationality_2": null, "prison_number": null, "address_line_1": "Flat 2A", "address_line_2": "12 Kings Square", "address_line_3": "Stokes Croft", "address_line_4": null, "address_line_5": null, "custody_status": null, "email_address_1": "ava.bennett@studentmail.com", "email_address_2": "ava.g.bennett@gmail.com", "parent_guardian": { "company_flag": false, "company_name": null, "surname": "BENNETT", "forenames": "Sophia", "dob": "1982-03-09", "national_insurance_number": "QQ112233C", "address_line_1": "24 St Marks Road", "address_line_2": "Easton", "address_line_3": "Bristol", "address_line_4": "Avon", "address_line_5": null, "post_code": "BS5 6JD", "telephone_number_home": "0117 654 3210", "telephone_number_business": "0117 998 2301", "telephone_number_mobile": "07700 934567", "email_address_1": "sophia.bennett@greenmail.com", "email_address_2": "s.bennett78@gmail.com", "debtor_detail": { "vehicle_make": "Nissan Micra", "vehicle_registration_mark": "BX12 HKA", "document_language": null, "hearing_language": null, "employee_reference": "QQ112233C", "employer_company_name": "South West Care Co.", "employer_address_line_1": "The Orchard Building", "employer_address_line_2": "25 Elder Street", "employer_address_line_3": "Stokes Croft", "employer_address_line_4": "Bristol", "employer_address_line_5": null, "employer_post_code": "BS1 3TP", "employer_telephone_number": "0117 456 9900", "employer_email_address": "hr@southwestcare.org", "aliases": [{"alias_surname": "WRIGHT", "alias_forenames": "Sophie Marie", "alias_company_name": null}] },"title":null }, "interpreter_lang": null, "ethnicity_observed": null, "telephone_number_home": "0117 123 4567", "driving_licence_number": null, "ethnicity_self_defined": null, "telephone_number_mobile": "07700 912345", "national_insurance_number": null, "telephone_number_business": null }, "account_type": "fine", "account_notes": [ { "note_type": "AC", "account_note_text": "Parent or guardian responsible for payment", "account_note_serial": 3 }, { "note_type": "AA", "account_note_text": "Child defendant. Debtor is parent Sophia Bennett. Contact all communications through guardian. Collection order created 22/02/2023.", "account_note_serial": 2 }, { "note_type": "AA", "account_note_text": "A collection order was previously made on 22/02/2023 prior to this account creation", "account_note_serial": 1 } ], "originator_id": 101, "payment_terms": { "enforcements": null, "effective_date": "2023-03-22", "lump_sum_amount": 50, "instalment_amount": 25, "instalment_period": "W", "default_days_in_jail": null, "payment_terms_type_code": "I" }, "defendant_type": "parentOrGuardianToPay", "originator_name": "Aberdare County Court", "fp_ticket_detail": null, "enforcement_court_id": 770000000021, "payment_card_request": null, "account_sentence_date": "2023-02-15", "collection_order_date": "2023-02-22", "collection_order_made": true, "suspended_committal_date": null, "prosecutor_case_reference": "CRN2023456789", "collection_order_made_today": null }',
            'fine',
            '{"account_type": "fine", "created_date": "2025-06-03T11:45:00.000000Z", "submitted_by": "L077JG", "date_of_birth": "2010-05-18", "defendant_name": "BENNETT, Ava Grace", "submitted_by_name": "opal-test", "business_unit_name": "Camberwell Green"}',
            'SUBMITTED',
            '[{"status": "Submitted", "username": "opal-test", "reason_text": null, "status_date": "2025-06-03"}]',
            'opal-test',
            CURRENT_DATE - INTERVAL '0 days',
            0
        ),
        -- 3 SUBMITTED
        (
            base_id + 2,
            77,
            CURRENT_DATE - INTERVAL '0 days',
            'L077JG',
            '{"offences": [{"offence_id": 33369, "impositions": [ {"result_id": "FVS", "amount_paid": 50, "amount_imposed": 125, "minor_creditor": null, "major_creditor_id": null}, {"result_id": "FCPC", "amount_paid": 0, "amount_imposed": 80, "minor_creditor": null, "major_creditor_id": null}, {"result_id": "FO", "amount_paid": 0, "amount_imposed": 45, "minor_creditor": null, "major_creditor_id": null} ], "date_of_sentence": "15/02/2023", "imposing_court_id": 770000000021 }], "defendant": { "company_flag": true, "company_name": "Brightline Engineering Ltd", "dob": null, "title": null, "gender": null, "forenames": null, "surname": null, "pnc_id": null, "cro_number": null, "national_insurance_number": null, "occupation": null, "post_code": "EC1A 4HD", "address_line_1": "1 Techway", "address_line_2": "Farringdon", "address_line_3": "London", "address_line_4": null, "address_line_5": null, "custody_status": null, "email_address_1": "admin@brightline-eng.co.uk", "email_address_2": "contact@brightline-eng.co.uk", "telephone_number_home": null, "telephone_number_mobile": null, "telephone_number_business": "020 7946 7766", "parent_guardian": null, "interpreter_lang": null, "ethnicity_observed": null, "ethnicity_self_defined": null, "driving_licence_number": null, "debtor_detail": { "document_language": null, "hearing_language": null, "aliases": [{"alias_company_name": "Brightline Eng UK Ltd"}] } }, "account_type": "fine", "account_notes": [ { "note_type": "AC", "account_note_text": "Company defendant – no collection order", "account_note_serial": 1 } ], "originator_id": 101, "payment_terms": { "enforcements": null, "effective_date": "2023-03-22", "lump_sum_amount": 50, "instalment_amount": 25, "instalment_period": "W", "default_days_in_jail": null, "payment_terms_type_code": "I" }, "defendant_type": "company", "originator_name": "Aberdare County Court", "fp_ticket_detail": null, "enforcement_court_id": 770000000021, "payment_card_request": null, "account_sentence_date": null, "collection_order_date": null, "collection_order_made": false, "suspended_committal_date": null, "prosecutor_case_reference": "CRN2023/001245", "collection_order_made_today": null }',
            'fine',
            '{"account_type": "fine", "created_date": "2025-06-03T12:00:00.000000Z", "submitted_by": "L077JG", "date_of_birth": null, "defendant_name": "Brightline Engineering Ltd", "submitted_by_name": "opal-test", "business_unit_name": "Camberwell Green"}',
            'SUBMITTED',
            '[{"status": "Submitted", "username": "opal-test", "reason_text": null, "status_date": "2025-06-03"}]',
            'opal-test',
            CURRENT_DATE - INTERVAL '0 days',
            0
        ),
        -- 4 SUBMITTED
        (
            base_id + 3,
            77,
            CURRENT_DATE - INTERVAL '0 days',
            'L077JG',
            '{"offences": [{"offence_id": 33369, "impositions": [ {"result_id": "FVS", "amount_paid": 50, "amount_imposed": 125, "minor_creditor": null, "major_creditor_id": null}, {"result_id": "FCPC", "amount_paid": 0, "amount_imposed": 80, "minor_creditor": null, "major_creditor_id": null}, {"result_id": "FO", "amount_paid": 0, "amount_imposed": 45, "minor_creditor": null, "major_creditor_id": null} ], "date_of_sentence": "15/02/2023", "imposing_court_id": 770000000021 }], "defendant": { "dob": "1988-11-02", "title": "Mr", "forenames": "Daniel Lee", "surname": "Hughes", "post_code": "LE1 6JG", "national_insurance_number": "QQ876543C", "company_flag": false, "company_name": null, "address_line_1": "12 Queens Road", "address_line_2": "Clarendon Park", "address_line_3": "Leicester", "address_line_4": null, "address_line_5": null, "debtor_detail": { "vehicle_make": "Vauxhall Corsa", "vehicle_registration_mark": "LV10 NHT", "document_language": null, "hearing_language": null, "employee_reference": "QQ876543C", "employer_company_name": "Midlands Warehouse Ltd", "employer_address_line_1": "Unit 4", "employer_address_line_2": "Narborough Industrial Estate", "employer_address_line_3": "Leicester", "employer_address_line_4": "Leicestershire", "employer_address_line_5": null, "employer_post_code": "LE19 4XU", "employer_telephone_number": "0116 234 5678", "employer_email_address": "hr@midlandswarehouse.co.uk", "aliases": [] }, "email_address_1": "daniel.hughes@example.com", "email_address_2": "d.hughes1988@gmail.com", "telephone_number_home": "0116 778 2345", "telephone_number_business": null, "telephone_number_mobile": "07700 987654", "parent_guardian": null, "interpreter_lang": null, "ethnicity_observed": null, "ethnicity_self_defined": null, "driving_licence_number": null }, "account_type": "fine", "account_notes": [ { "note_type": "AA", "account_note_text": "Account rejected because the defendant''s post code was invalid. Submission failed validation against business unit address requirements.", "account_note_serial": 2 }, { "note_type": "AC", "account_note_text": "Invalid post code", "account_note_serial": 3 } ], "originator_id": 101, "payment_terms": { "enforcements": null, "effective_date": "2023-05-01", "lump_sum_amount": 100, "instalment_amount": 50, "instalment_period": "M", "default_days_in_jail": null, "payment_terms_type_code": "I" }, "defendant_type": "adultOrYouthOnly", "originator_name": "Leicester Magistrates Court", "prosecutor_case_reference": "PCR2024778899", "collection_order_made": false, "collection_order_date": null, "collection_order_made_today": null, "account_sentence_date": null, "enforcement_court_id": 770000000021, "fp_ticket_detail": null, "payment_card_request": null }',
            'fine',
            '{"account_type": "fine", "created_date": "2025-06-03T12:30:00.000000Z", "submitted_by": "L077JG", "date_of_birth": "1988-11-02", "defendant_name": "HUGHES, Daniel Lee", "submitted_by_name": "opal-test", "business_unit_name": "Camberwell Green"}',
            'SUBMITTED',
            '[{"status": "Submitted", "username": "opal-test", "reason_text": null, "status_date": "2025-06-03"}]',
            'opal-test',
            CURRENT_DATE - INTERVAL '0 days',
            0
        ),
        -- 5 REJECTED
        (
            base_id + 4,
            77,
            CURRENT_DATE - INTERVAL '2 days',
            'L077JG',
            '{"offences": [{"offence_id": 33369, "impositions": [ {"result_id": "FVS", "amount_paid": 50, "amount_imposed": 125, "minor_creditor": null, "major_creditor_id": null} ], "date_of_sentence": "10/05/2023", "imposing_court_id": 770000000021 }], "defendant": { "dob": "2003-05-10", "title": "Mr", "gender": null, "pnc_id": null, "surname": "SMITH", "forenames": "John", "post_code": "W1A 1AA", "cro_number": null, "occupation": null, "company_flag": false, "company_name": null, "debtor_detail": { "employee_reference": "EMPL123456", "employer_company_name": "Example Employer Ltd", "employer_address_line_1": "1 Employer Street", "employer_address_line_2": "Suite 100", "employer_address_line_3": "Business Park", "employer_address_line_4": "City", "employer_address_line_5": "Region", "employer_post_code": "EX1 2MP", "employer_telephone_number": "01234 567890", "employer_email_address": "hr@exampleemployer.com" }, "nationality_1": null, "nationality_2": null, "prison_number": null, "address_line_1": "12 Main Street", "address_line_2": null, "address_line_3": null, "address_line_4": null, "address_line_5": null, "custody_status": null, "email_address_1": "john.smith@example.com", "email_address_2": null, "parent_guardian": null, "interpreter_lang": null, "ethnicity_observed": null, "telephone_number_home": "020 7000 1111", "driving_licence_number": null, "ethnicity_self_defined": null, "telephone_number_mobile": "07700 111222", "national_insurance_number": "QQ123456A", "telephone_number_business": null }, "account_type": "fine", "account_notes": [ { "note_type": "AA", "account_note_text": "Account rejected due to missing post code.", "account_note_serial": 1 } ], "originator_id": 101, "payment_terms": { "enforcements": null, "effective_date": "2023-03-22", "lump_sum_amount": 50, "instalment_amount": 25, "instalment_period": "W", "default_days_in_jail": null, "payment_terms_type_code": "I" }, "defendant_type": "adultOrYouthOnly", "originator_name": "Aberdare County Court", "fp_ticket_detail": null, "enforcement_court_id": 770000000021, "payment_card_request": null, "account_sentence_date": "2023-05-10", "collection_order_date": "2023-05-15", "collection_order_made": false, "suspended_committal_date": null, "prosecutor_case_reference": "CRN2023123456", "collection_order_made_today": null }',
            'fine',
            '{"account_type": "fine", "created_date": "2025-06-01T09:00:00.000000Z", "submitted_by": "L077JG", "date_of_birth": "2003-05-10", "defendant_name": "SMITH, John", "submitted_by_name": "opal-test", "business_unit_name": "Camberwell Green"}',
            'REJECTED',
            '[{"status": "Submitted", "username": "opal-test", "reason_text": null, "status_date": "2025-06-01"},{"status": "Rejected", "username": "opal-test", "reason_text": "Missing post code.", "status_date": "2025-06-01"}]',
            'opal-test',
            CURRENT_DATE - INTERVAL '2 days',
            0
        ),
        -- 6 REJECTED (already resubmitted once)
        (
            base_id + 5,
            77,
            CURRENT_DATE - INTERVAL '5 days',
            'L077JG',
            '{"offences": [{"offence_id": 33369, "impositions": [ {"result_id": "FVS", "amount_paid": 50, "amount_imposed": 125, "minor_creditor": null, "major_creditor_id": null} ], "date_of_sentence": "30/12/1975", "imposing_court_id": 770000000021 }], "defendant": { "dob": "1975-12-30", "title": "Ms", "gender": "F", "pnc_id": null, "surname": "TAYLOR", "forenames": "Emily", "post_code": "M1 1AE", "cro_number": null, "occupation": null, "company_flag": false, "company_name": null, "debtor_detail": { "employee_reference": "EMPL123456", "employer_company_name": "Example Employer Ltd", "employer_address_line_1": "1 Employer Street", "employer_address_line_2": "Suite 100", "employer_address_line_3": "Business Park", "employer_address_line_4": "City", "employer_address_line_5": "Region", "employer_post_code": "EX1 2MP", "employer_telephone_number": "01234 567890", "employer_email_address": "hr@exampleemployer.com" }, "nationality_1": null, "nationality_2": null, "prison_number": null, "address_line_1": "45 Market Street", "address_line_2": null, "address_line_3": null, "address_line_4": null, "address_line_5": null, "custody_status": null, "email_address_1": "emily.taylor@example.com", "email_address_2": null, "parent_guardian": null, "interpreter_lang": null, "ethnicity_observed": null, "telephone_number_home": "0161 123 4567", "driving_licence_number": null, "ethnicity_self_defined": null, "telephone_number_mobile": "07700 333444", "national_insurance_number": "QQ223344B", "telephone_number_business": null }, "account_type": "fine", "account_notes": [ { "note_type": "AA", "account_note_text": "Account rejected due to invalid NINO.", "account_note_serial": 1 }, { "note_type": "AA", "account_note_text": "Account notes missing.", "account_note_serial": 2 } ], "originator_id": 101, "payment_terms": { "enforcements": null, "effective_date": "2023-03-22", "lump_sum_amount": 50, "instalment_amount": 25, "instalment_period": "W", "default_days_in_jail": null, "payment_terms_type_code": "I" }, "defendant_type": "adultOrYouthOnly", "originator_name": "Aberdare County Court", "fp_ticket_detail": null, "enforcement_court_id": 770000000021, "payment_card_request": null, "account_sentence_date": "1975-12-30", "collection_order_date": "1976-01-10", "collection_order_made": false, "suspended_committal_date": null, "prosecutor_case_reference": "CRN2023987654", "collection_order_made_today": null }',
            'fine',
            '{"account_type": "fine", "created_date": "2025-05-29T10:00:00.000000Z", "submitted_by": "L077JG", "date_of_birth": "1975-12-30", "defendant_name": "TAYLOR, Emily", "submitted_by_name": "opal-test", "business_unit_name": "Camberwell Green"}',
            'REJECTED',
            '[{"status": "Submitted", "username": "opal-test", "reason_text": null, "status_date": "2025-05-29"},{"status": "Rejected", "username": "opal-test", "reason_text": "Invalid NINO.", "status_date": "2025-05-29"},{"status": "Resubmitted", "username": "opal-test", "reason_text": null, "status_date": "2025-05-30"},{"status": "Rejected", "username": "opal-test", "reason_text": "Account notes missing.", "status_date": "2025-05-31"}]',
            'opal-test',
            CURRENT_DATE - INTERVAL '5 days',
            0
        ),
        -- 7 REJECTED (company)
        (
            base_id + 6,
            77,
            CURRENT_DATE - INTERVAL '6 days',
            'L077JG',
            '{"offences": [{"offence_id": 33369, "impositions": [ {"result_id": "FVS", "amount_paid": 100, "amount_imposed": 200, "minor_creditor": null, "major_creditor_id": null} ], "date_of_sentence": null, "imposing_court_id": 770000000021 }], "defendant": { "company_flag": true, "company_name": "Zenith Tech Ltd", "dob": null, "title": null, "gender": null, "forenames": null, "surname": null, "pnc_id": null, "cro_number": null, "national_insurance_number": null, "occupation": null, "post_code": "N1 9GU", "address_line_1": "8 Science Park", "address_line_2": null, "address_line_3": null, "address_line_4": null, "address_line_5": null, "custody_status": null, "email_address_1": "info@zenithtech.co.uk", "email_address_2": null, "telephone_number_home": null, "telephone_number_mobile": null, "telephone_number_business": "020 7123 4567", "parent_guardian": null, "interpreter_lang": null, "ethnicity_observed": null, "ethnicity_self_defined": null, "driving_licence_number": null, "debtor_detail": { "document_language": null, "hearing_language": null, "aliases": [{"alias_company_name": "Zenith Technologies Ltd"}] } }, "account_type": "fine", "account_notes": [ { "note_type": "AA", "account_note_text": "Company registration missing.", "account_note_serial": 1 } ], "originator_id": 101, "payment_terms": { "enforcements": null, "effective_date": "2023-03-22", "lump_sum_amount": 50, "instalment_amount": 25, "instalment_period": "W", "default_days_in_jail": null, "payment_terms_type_code": "I" }, "defendant_type": "company", "originator_name": "Aberdare County Court", "fp_ticket_detail": null, "enforcement_court_id": 770000000021, "payment_card_request": null, "account_sentence_date": null, "collection_order_date": null, "collection_order_made": false, "suspended_committal_date": null, "prosecutor_case_reference": "CRN2023123987", "collection_order_made_today": null }',
            'fine',
            '{"account_type": "fine", "created_date": "2025-05-28T11:00:00.000000Z", "submitted_by": "L077JG", "date_of_birth": null, "defendant_name": "Zenith Tech Ltd", "submitted_by_name": "opal-test", "business_unit_name": "Camberwell Green"}',
            'REJECTED',
            '[{"status": "Submitted", "username": "opal-test", "reason_text": null, "status_date": "2025-05-28"},{"status": "Rejected", "username": "opal-test", "reason_text": "Company registration missing.", "status_date": "2025-05-29"}]',
            'opal-test',
            CURRENT_DATE - INTERVAL '6 days',
            0
        ),
        -- 8 REJECTED (parent/guardian to pay)
        (
            base_id + 7,
            77,
            CURRENT_DATE - INTERVAL '7 days',
            'L077JG',
            '{"offences": [{"offence_id": 33369, "impositions": [ {"result_id": "FVS", "amount_paid": 30, "amount_imposed": 90, "minor_creditor": null, "major_creditor_id": null} ], "date_of_sentence": "02/03/2012", "imposing_court_id": 770000000021 }], "defendant": { "dob": "2012-03-02", "title": "Miss", "gender": "F", "pnc_id": null, "surname": "WHITE", "forenames": "Ella", "post_code": "LS1 3AD", "cro_number": null, "occupation": null, "company_flag": false, "company_name": null, "debtor_detail": null, "nationality_1": null, "nationality_2": null, "prison_number": null, "address_line_1": "15 Park Avenue", "address_line_2": null, "address_line_3": null, "address_line_4": null, "address_line_5": null, "custody_status": null, "email_address_1": "ella.white@example.com", "email_address_2": null, "parent_guardian": { "company_flag": false, "company_name": null, "surname": "WHITE", "forenames": "Helen", "dob": "1980-05-05", "national_insurance_number": "QQ445566C", "address_line_1": "15 Park Avenue", "address_line_2": null, "address_line_3": null, "address_line_4": null, "address_line_5": null, "post_code": "LS1 3AD", "telephone_number_home": "0113 123 4567", "telephone_number_business": null, "telephone_number_mobile": "07700 555666", "email_address_1": "helen.white@example.com", "email_address_2": null, "debtor_detail": { "employee_reference": "EMPL123456", "employer_company_name": "Example Employer Ltd", "employer_address_line_1": "1 Employer Street", "employer_address_line_2": "Suite 100", "employer_address_line_3": "Business Park", "employer_address_line_4": "City", "employer_address_line_5": "Region", "employer_post_code": "EX1 2MP", "employer_telephone_number": "01234 567890", "employer_email_address": "hr@exampleemployer.com" },"title":null }, "interpreter_lang": null, "ethnicity_observed": null, "telephone_number_home": "0113 987 6543", "driving_licence_number": null, "ethnicity_self_defined": null, "telephone_number_mobile": "07700 888999", "national_insurance_number": null, "telephone_number_business": null }, "account_type": "fine", "account_notes": [ { "note_type": "AA", "account_note_text": "Parent/guardian contact missing.", "account_note_serial": 1 } ], "originator_id": 101, "payment_terms": { "enforcements": null, "effective_date": "2023-03-22", "lump_sum_amount": 50, "instalment_amount": 25, "instalment_period": "W", "default_days_in_jail": null, "payment_terms_type_code": "I" }, "defendant_type": "parentOrGuardianToPay", "originator_name": "Aberdare County Court", "fp_ticket_detail": null, "enforcement_court_id": 770000000021, "payment_card_request": null, "account_sentence_date": "2012-03-02", "collection_order_date": "2012-03-10", "collection_order_made": true, "suspended_committal_date": null, "prosecutor_case_reference": "CRN2023123988", "collection_order_made_today": null }',
            'fine',
            '{"account_type": "fine", "created_date": "2025-05-27T12:00:00.000000Z", "submitted_by": "L077JG", "date_of_birth": "2012-03-02", "defendant_name": "WHITE, Ella", "submitted_by_name": "opal-test", "business_unit_name": "Camberwell Green"}',
            'REJECTED',
            '[{"status": "Submitted", "username": "opal-test", "reason_text": null, "status_date": "2025-05-27"},{"status": "Rejected", "username": "opal-test", "reason_text": "Parent/guardian contact missing.", "status_date": "2025-05-28"}]',
            'opal-test',
            CURRENT_DATE - INTERVAL '7 days',
            0
        ),
        -- 9 DELETED (normal)
        (
            base_id + 8,
            77,
            CURRENT_DATE - INTERVAL '6 days',
            'L077JG',
            '{"offences": [{"offence_id": 33369, "impositions": [ {"result_id": "FVS", "amount_paid": 60, "amount_imposed": 130, "minor_creditor": null, "major_creditor_id": null} ], "date_of_sentence": "21/08/1992", "imposing_court_id": 770000000021 }], "defendant": { "dob": "1992-08-21", "title": "Mr", "gender": "M", "pnc_id": null, "surname": "JONES", "forenames": "Michael", "post_code": "B2 4QA", "cro_number": null, "occupation": null, "company_flag": false, "company_name": null, "debtor_detail": null, "nationality_1": null, "nationality_2": null, "prison_number": null, "address_line_1": "22 Station Road", "address_line_2": null, "address_line_3": null, "address_line_4": null, "address_line_5": null, "custody_status": null, "email_address_1": "michael.jones@example.com", "email_address_2": null, "parent_guardian": null, "interpreter_lang": null, "ethnicity_observed": null, "telephone_number_home": "0121 111 2222", "driving_licence_number": null, "ethnicity_self_defined": null, "telephone_number_mobile": "07700 222333", "national_insurance_number": "QQ334455D", "telephone_number_business": null }, "account_type": "fine", "account_notes": [ { "note_type": "AA", "account_note_text": "Court code invalid.", "account_note_serial": 1 } ], "originator_id": 101, "payment_terms": { "enforcements": null, "effective_date": "2023-03-22", "lump_sum_amount": 50, "instalment_amount": 25, "instalment_period": "W", "default_days_in_jail": null, "payment_terms_type_code": "I" }, "defendant_type": "adultOrYouthOnly", "originator_name": "Aberdare County Court", "fp_ticket_detail": null, "enforcement_court_id": 770000000021, "payment_card_request": null, "account_sentence_date": "1992-08-21", "collection_order_date": "1992-09-01", "collection_order_made": false, "suspended_committal_date": null, "prosecutor_case_reference": "CRN2023987655", "collection_order_made_today": null }',
            'fine',
            '{"account_type": "fine", "created_date": "2025-05-26T13:00:00.000000Z", "submitted_by": "L077JG", "date_of_birth": "1992-08-21", "defendant_name": "JONES, Michael", "submitted_by_name": "opal-test", "business_unit_name": "Camberwell Green"}',
            'DELETED',
            '[{"status": "Submitted", "username": "opal-test", "reason_text": null, "status_date": "2025-05-26"},{"status": "Rejected", "username": "opal-test", "reason_text": "Court code invalid.", "status_date": "2025-05-27"},{"status": "Resubmitted", "username": "opal-test", "reason_text": null, "status_date": "2025-05-28"},{"status": "Deleted", "username": "opal-test", "reason_text": "User deleted draft.", "status_date": "2025-05-29"}]',
            'opal-test',
            CURRENT_DATE - INTERVAL '6 days',
            0
        ),
        -- 10 DELETED (company)
        (
            base_id + 9,
            77,
            CURRENT_DATE - INTERVAL '3 days',
            'L077JG',
            '{"offences": [{"offence_id": 33369, "impositions": [ {"result_id": "FVS", "amount_paid": 90, "amount_imposed": 190, "minor_creditor": null, "major_creditor_id": null} ], "date_of_sentence": null, "imposing_court_id": 770000000021 }], "defendant": { "company_flag": true, "company_name": "Acme Holdings Ltd", "dob": null, "title": null, "gender": null, "forenames": null, "surname": null, "pnc_id": null, "cro_number": null, "national_insurance_number": null, "occupation": null, "post_code": "OX1 1AA", "address_line_1": "1 High Street", "address_line_2": null, "address_line_3": null, "address_line_4": null, "address_line_5": null, "custody_status": null, "email_address_1": "contact@acmeholdings.co.uk", "email_address_2": null, "telephone_number_home": null, "telephone_number_mobile": null, "telephone_number_business": "01865 123456", "parent_guardian": null, "interpreter_lang": null, "ethnicity_observed": null, "ethnicity_self_defined": null, "driving_licence_number": null, "debtor_detail": { "document_language": null, "hearing_language": null, "aliases": [{"alias_company_name": "Acme Ltd"}], "employee_reference": "EMPL123456", "employer_company_name": "Example Employer Ltd", "employer_address_line_1": "1 Employer Street", "employer_address_line_2": "Suite 100", "employer_address_line_3": "Business Park", "employer_address_line_4": "City", "employer_address_line_5": "Region", "employer_post_code": "EX1 2MP", "employer_telephone_number": "01234 567890", "employer_email_address": "hr@exampleemployer.com" } }, "account_type": "fine", "account_notes": [ { "note_type": "AA", "account_note_text": "User deleted draft.", "account_note_serial": 1 } ], "originator_id": 101, "payment_terms": { "enforcements": null, "effective_date": "2023-03-22", "lump_sum_amount": 50, "instalment_amount": 25, "instalment_period": "W", "default_days_in_jail": null, "payment_terms_type_code": "I" }, "defendant_type": "company", "originator_name": "Aberdare County Court", "fp_ticket_detail": null, "enforcement_court_id": 770000000021, "payment_card_request": null, "account_sentence_date": null, "collection_order_date": null, "collection_order_made": false, "suspended_committal_date": null, "prosecutor_case_reference": "CRN2023123989", "collection_order_made_today": null }',
            'fine',
            '{"account_type": "fine", "created_date": "2025-05-25T14:00:00.000000Z", "submitted_by": "L077JG", "date_of_birth": null, "defendant_name": "Acme Holdings Ltd", "submitted_by_name": "opal-test", "business_unit_name": "Camberwell Green"}',
            'DELETED',
            '[{"status": "Submitted", "username": "opal-test", "reason_text": null, "status_date": "2025-05-25"},{"status": "Deleted", "username": "opal-test", "reason_text": "User deleted draft.", "status_date": "2025-05-26"}]',
            'opal-test',
            CURRENT_DATE - INTERVAL '3 days',
            0
        ),
        -- 11 DELETED (parent/guardian)
        (
            base_id + 10,
            77,
            CURRENT_DATE - INTERVAL '2 days',
            'L077JG',
            '{"offences": [{"offence_id": 33369, "impositions": [ {"result_id": "FVS", "amount_paid": 20, "amount_imposed": 50, "minor_creditor": null, "major_creditor_id": null} ], "date_of_sentence": "11/06/2013", "imposing_court_id": 770000000021 }], "defendant": { "dob": "2013-06-11", "title": "Mr", "gender": "M", "pnc_id": null, "surname": "LEE", "forenames": "Oscar", "post_code": "CF10 1AA", "cro_number": null, "occupation": null, "company_flag": false, "company_name": null, "debtor_detail": null, "nationality_1": null, "nationality_2": null, "prison_number": null, "address_line_1": "3 Castle Street", "address_line_2": null, "address_line_3": null, "address_line_4": null, "address_line_5": null, "custody_status": null, "email_address_1": "oscar.lee@example.com", "email_address_2": null, "parent_guardian": { "company_flag": false, "company_name": null, "surname": "LEE", "forenames": "Susan", "dob": "1985-02-02", "national_insurance_number": "QQ556677D", "address_line_1": "3 Castle Street", "address_line_2": null, "address_line_3": null, "address_line_4": null, "address_line_5": null, "post_code": "CF10 1AA", "telephone_number_home": "029 1234 5678", "telephone_number_business": null, "telephone_number_mobile": "07700 999888", "email_address_1": "susan.lee@example.com", "email_address_2": null, "debtor_detail": { "employee_reference": "EMPL123456", "employer_company_name": "Example Employer Ltd", "employer_address_line_1": "1 Employer Street", "employer_address_line_2": "Suite 100", "employer_address_line_3": "Business Park", "employer_address_line_4": "City", "employer_address_line_5": "Region", "employer_post_code": "EX1 2MP", "employer_telephone_number": "01234 567890", "employer_email_address": "hr@exampleemployer.com" },"title":null }, "interpreter_lang": null, "ethnicity_observed": null, "telephone_number_home": "029 8765 4321", "driving_licence_number": null, "ethnicity_self_defined": null, "telephone_number_mobile": "07700 777666", "national_insurance_number": null, "telephone_number_business": null }, "account_type": "fine", "account_notes": [ { "note_type": "AA", "account_note_text": "Missing employer details.", "account_note_serial": 1 } ], "originator_id": 101, "payment_terms": { "enforcements": null, "effective_date": "2023-03-22", "lump_sum_amount": 50, "instalment_amount": 25, "instalment_period": "W", "default_days_in_jail": null, "payment_terms_type_code": "I" }, "defendant_type": "parentOrGuardianToPay", "originator_name": "Aberdare County Court", "fp_ticket_detail": null, "enforcement_court_id": 770000000021, "payment_card_request": null, "account_sentence_date": "2013-06-11", "collection_order_date": "2013-06-20", "collection_order_made": true, "suspended_committal_date": null, "prosecutor_case_reference": "CRN2023123990", "collection_order_made_today": null }',
            'fine',
            '{"account_type": "fine", "created_date": "2025-05-24T15:00:00.000000Z", "submitted_by": "L077JG", "date_of_birth": "2013-06-11", "defendant_name": "LEE, Oscar", "submitted_by_name": "opal-test", "business_unit_name": "Camberwell Green"}',
            'DELETED',
            '[{"status": "Submitted", "username": "opal-test", "reason_text": null, "status_date": "2025-05-24"},{"status": "Rejected", "username": "opal-test", "reason_text": "Missing employer details.", "status_date": "2025-05-25"},{"status": "Resubmitted", "username": "opal-test", "reason_text": null, "status_date": "2025-05-26"},{"status": "Deleted", "username": "opal-test", "reason_text": "User deleted draft.", "status_date": "2025-05-27"}]',
            'opal-test',
            CURRENT_DATE - INTERVAL '2 days',
            0
        ),
        -- 12 DELETED (no resubmission)
        (
            base_id + 11,
            77,
            CURRENT_DATE - INTERVAL '4 days',
            'L077JG',
            '{"offences": [{"offence_id": 33369, "impositions": [ {"result_id": "FVS", "amount_paid": 40, "amount_imposed": 80, "minor_creditor": null, "major_creditor_id": null} ], "date_of_sentence": "01/01/1980", "imposing_court_id": 770000000021 }], "defendant": { "dob": "1980-01-01", "title": "Mrs", "gender": "F", "pnc_id": null, "surname": "WALKER", "forenames": "Anna", "post_code": "G1 2FF", "cro_number": null, "occupation": null, "company_flag": false, "company_name": null, "debtor_detail": null, "nationality_1": null, "nationality_2": null, "prison_number": null, "address_line_1": "7 Riverbank Road", "address_line_2": null, "address_line_3": null, "address_line_4": null, "address_line_5": null, "custody_status": null, "email_address_1": "anna.walker@example.com", "email_address_2": null, "parent_guardian": null, "interpreter_lang": null, "ethnicity_observed": null, "telephone_number_home": "0141 123 4567", "driving_licence_number": null, "ethnicity_self_defined": null, "telephone_number_mobile": "07700 444555", "national_insurance_number": "QQ667788E", "telephone_number_business": null }, "account_type": "fine", "account_notes": [ { "note_type": "AA", "account_note_text": "User deleted draft.", "account_note_serial": 1 } ], "originator_id": 101, "payment_terms": { "enforcements": null, "effective_date": "2023-03-22", "lump_sum_amount": 50, "instalment_amount": 25, "instalment_period": "W", "default_days_in_jail": null, "payment_terms_type_code": "I" }, "defendant_type": "adultOrYouthOnly", "originator_name": "Aberdare County Court", "fp_ticket_detail": null, "enforcement_court_id": 770000000021, "payment_card_request": null, "account_sentence_date": "1980-01-01", "collection_order_date": "1980-01-10", "collection_order_made": false, "suspended_committal_date": null, "prosecutor_case_reference": "CRN2023123991", "collection_order_made_today": null }',
            'fine',
            '{"account_type": "fine", "created_date": "2025-05-23T16:00:00.000000Z", "submitted_by": "L077JG", "date_of_birth": "1980-01-01", "defendant_name": "WALKER, Anna", "submitted_by_name": "opal-test", "business_unit_name": "Camberwell Green"}',
            'DELETED',
            '[{"status": "Submitted", "username": "opal-test", "reason_text": null, "status_date": "2025-05-23"},{"status": "Deleted", "username": "opal-test", "reason_text": "User deleted draft.", "status_date": "2025-05-24"}]',
            'opal-test',
            CURRENT_DATE - INTERVAL '4 days',
            0
        ),
        -- 13 PUBLISHING_PENDING
        (
            base_id + 12,
            77,
            CURRENT_DATE - INTERVAL '1 days',
            'L077JG',
            '{"offences": [{"offence_id": 33369, "impositions": [ {"result_id": "FVS", "amount_paid": 70, "amount_imposed": 120, "minor_creditor": null, "major_creditor_id": null} ], "date_of_sentence": "10/10/2000", "imposing_court_id": 770000000021 }], "defendant": { "dob": "2000-10-10", "title": "Mr", "gender": "M", "pnc_id": null, "surname": "CLARK", "forenames": "Sam", "post_code": "E1 7AA", "cro_number": null, "occupation": null, "company_flag": false, "company_name": null, "debtor_detail": null, "nationality_1": null, "nationality_2": null, "prison_number": null, "address_line_1": "4 Tower Road", "address_line_2": null, "address_line_3": null, "address_line_4": null, "address_line_5": null, "custody_status": null, "email_address_1": "sam.clark@example.com", "email_address_2": null, "parent_guardian": null, "interpreter_lang": null, "ethnicity_observed": null, "telephone_number_home": "020 8000 4444", "driving_licence_number": null, "ethnicity_self_defined": null, "telephone_number_mobile": "07700 555666", "national_insurance_number": "QQ778899F", "telephone_number_business": null }, "account_type": "fine", "account_notes": [ { "note_type": "AA", "account_note_text": "Publishing pending for account.", "account_note_serial": 1 } ], "originator_id": 101, "payment_terms": { "enforcements": null, "effective_date": "2023-03-22", "lump_sum_amount": 50, "instalment_amount": 25, "instalment_period": "W", "default_days_in_jail": null, "payment_terms_type_code": "I" }, "defendant_type": "adultOrYouthOnly", "originator_name": "Aberdare County Court", "fp_ticket_detail": null, "enforcement_court_id": 770000000021, "payment_card_request": null, "account_sentence_date": "2000-10-10", "collection_order_date": "2000-10-20", "collection_order_made": false, "suspended_committal_date": null, "prosecutor_case_reference": "CRN2023123992", "collection_order_made_today": null }',
            'fine',
            '{"account_type": "fine", "created_date": "2025-06-02T09:00:00.000000Z", "submitted_by": "L077JG", "date_of_birth": "2000-10-10", "defendant_name": "CLARK, Sam", "submitted_by_name": "opal-test", "business_unit_name": "Camberwell Green"}',
            'PUBLISHING_PENDING',
            '[{"status": "Submitted", "username": "opal-test", "reason_text": null, "status_date": "2025-06-02"},{"status": "Approved", "username": "opal-test", "reason_text": null, "status_date": "2025-06-02"}]',
            'opal-test',
            CURRENT_DATE - INTERVAL '1 days',
            0
        ),
        -- 14 PUBLISHING_PENDING (company)
        (
            base_id + 13,
            77,
            CURRENT_DATE - INTERVAL '3 days',
            'L077JG',
            '{"offences": [{"offence_id": 33369, "impositions": [ {"result_id": "FVS", "amount_paid": 110, "amount_imposed": 210, "minor_creditor": null, "major_creditor_id": null} ], "date_of_sentence": null, "imposing_court_id": 770000000021 }], "defendant": { "company_flag": true, "company_name": "Blue Sky Enterprises", "dob": null, "title": null, "gender": null, "forenames": null, "surname": null, "pnc_id": null, "cro_number": null, "national_insurance_number": null, "occupation": null, "post_code": "BR1 1AA", "address_line_1": "100 Blue Lane", "address_line_2": null, "address_line_3": null, "address_line_4": null, "address_line_5": null, "custody_status": null, "email_address_1": "info@blueskyent.co.uk", "email_address_2": null, "telephone_number_home": null, "telephone_number_mobile": null, "telephone_number_business": "020 8000 7777", "parent_guardian": null, "interpreter_lang": null, "ethnicity_observed": null, "ethnicity_self_defined": null, "driving_licence_number": null, "debtor_detail": { "document_language": null, "hearing_language": null, "aliases": [{"alias_company_name": "Blue Sky Ltd"}], "employee_reference": "EMPL123456", "employer_company_name": "Example Employer Ltd", "employer_address_line_1": "1 Employer Street", "employer_address_line_2": "Suite 100", "employer_address_line_3": "Business Park", "employer_address_line_4": "City", "employer_address_line_5": "Region", "employer_post_code": "EX1 2MP", "employer_telephone_number": "01234 567890", "employer_email_address": "hr@exampleemployer.com" } }, "account_type": "fine", "account_notes": [ { "note_type": "AA", "account_note_text": "Publishing pending for company account.", "account_note_serial": 1 } ], "originator_id": 101, "payment_terms": { "enforcements": null, "effective_date": "2023-03-22", "lump_sum_amount": 50, "instalment_amount": 25, "instalment_period": "W", "default_days_in_jail": null, "payment_terms_type_code": "I" }, "defendant_type": "company", "originator_name": "Aberdare County Court", "fp_ticket_detail": null, "enforcement_court_id": 770000000021, "payment_card_request": null, "account_sentence_date": null, "collection_order_date": null, "collection_order_made": false, "suspended_committal_date": null, "prosecutor_case_reference": "CRN2023123993", "collection_order_made_today": null }',
            'fine',
            '{"account_type": "fine", "created_date": "2025-05-31T10:00:00.000000Z", "submitted_by": "L077JG", "date_of_birth": null, "defendant_name": "Blue Sky Enterprises", "submitted_by_name": "opal-test", "business_unit_name": "Camberwell Green"}',
            'PUBLISHING_PENDING',
            '[{"status": "Submitted", "username": "opal-test", "reason_text": null, "status_date": "2025-05-31"},{"status": "Approved", "username": "opal-test", "reason_text": null, "status_date": "2025-05-31"}]',
            'opal-test',
            CURRENT_DATE - INTERVAL '3 days',
            0
        ),
        -- 15 PUBLISHING_PENDING (parent/guardian)
        (
            base_id + 14,
            77,
            CURRENT_DATE - INTERVAL '4 days',
            'L077JG',
            '{"offences": [{"offence_id": 33369, "impositions": [ {"result_id": "FVS", "amount_paid": 55, "amount_imposed": 115, "minor_creditor": null, "major_creditor_id": null} ], "date_of_sentence": "17/07/2011", "imposing_court_id": 770000000021 }], "defendant": { "dob": "2011-07-17", "title": "Miss", "gender": "F", "pnc_id": null, "surname": "DAVIES", "forenames": "Ruby", "post_code": "L1 8JQ", "cro_number": null, "occupation": null, "company_flag": false, "company_name": null, "debtor_detail": null, "nationality_1": null, "nationality_2": null, "prison_number": null, "address_line_1": "22 Albert Road", "address_line_2": null, "address_line_3": null, "address_line_4": null, "address_line_5": null, "custody_status": null, "email_address_1": "ruby.davies@example.com", "email_address_2": null, "parent_guardian": { "company_flag": false, "company_name": null, "surname": "DAVIES", "forenames": "James", "dob": "1983-03-03", "national_insurance_number": "QQ889900G", "address_line_1": "22 Albert Road", "address_line_2": null, "address_line_3": null, "address_line_4": null, "address_line_5": null, "post_code": "L1 8JQ", "telephone_number_home": "0151 123 4567", "telephone_number_business": null, "telephone_number_mobile": "07700 333222", "email_address_1": "james.davies@example.com", "email_address_2": null, "debtor_detail": null,"title":null }, "interpreter_lang": null, "ethnicity_observed": null, "telephone_number_home": "0151 987 6543", "driving_licence_number": null, "ethnicity_self_defined": null, "telephone_number_mobile": "07700 111999", "national_insurance_number": null, "telephone_number_business": null }, "account_type": "fine", "account_notes": [ { "note_type": "AA", "account_note_text": "Publishing pending for parent/guardian account.", "account_note_serial": 1 } ], "originator_id": 101, "payment_terms": { "enforcements": null, "effective_date": "2023-03-22", "lump_sum_amount": 50, "instalment_amount": 25, "instalment_period": "W", "default_days_in_jail": null, "payment_terms_type_code": "I" }, "defendant_type": "parentOrGuardianToPay", "originator_name": "Aberdare County Court", "fp_ticket_detail": null, "enforcement_court_id": 770000000021, "payment_card_request": null, "account_sentence_date": "2011-07-17", "collection_order_date": "2011-07-25", "collection_order_made": true, "suspended_committal_date": null, "prosecutor_case_reference": "CRN2023123994", "collection_order_made_today": null }',
            'fine',
            '{"account_type": "fine", "created_date": "2025-05-30T11:00:00.000000Z", "submitted_by": "L077JG", "date_of_birth": "2011-07-17", "defendant_name": "DAVIES, Ruby", "submitted_by_name": "opal-test", "business_unit_name": "Camberwell Green"}',
            'PUBLISHING_PENDING',
            '[{"status": "Submitted", "username": "opal-test", "reason_text": null, "status_date": "2025-05-30"},{"status": "Approved", "username": "opal-test", "reason_text": null, "status_date": "2025-05-30"}]',
            'opal-test',
            CURRENT_DATE - INTERVAL '4 days',
            0
        ),
        -- 16 PUBLISHING_FAILED
        (
            base_id + 15,
            77,
            CURRENT_DATE - INTERVAL '2 days',
            'L077JG',
            '{"offences": [{"offence_id": 33369, "impositions": [ {"result_id": "FVS", "amount_paid": 80, "amount_imposed": 150, "minor_creditor": null, "major_creditor_id": null} ], "date_of_sentence": "09/09/1999", "imposing_court_id": 770000000021 }], "defendant": { "dob": "1999-09-09", "title": "Mr", "gender": "M", "pnc_id": null, "surname": "GREEN", "forenames": "Oliver", "post_code": "NG1 6AA", "cro_number": null, "occupation": null, "company_flag": false, "company_name": null, "debtor_detail": null, "nationality_1": null, "nationality_2": null, "prison_number": null, "address_line_1": "9 Forest Road", "address_line_2": null, "address_line_3": null, "address_line_4": null, "address_line_5": null, "custody_status": null, "email_address_1": "oliver.green@example.com", "email_address_2": null, "parent_guardian": null, "interpreter_lang": null, "ethnicity_observed": null, "telephone_number_home": "0115 123 9876", "driving_licence_number": null, "ethnicity_self_defined": null, "telephone_number_mobile": "07700 666555", "national_insurance_number": "QQ990011H", "telephone_number_business": null }, "account_type": "fine", "account_notes": [ { "note_type": "AA", "account_note_text": "Publishing failed: Integration error.", "account_note_serial": 1 } ], "originator_id": 101, "payment_terms": { "enforcements": null, "effective_date": "2023-03-22", "lump_sum_amount": 50, "instalment_amount": 25, "instalment_period": "W", "default_days_in_jail": null, "payment_terms_type_code": "I" }, "defendant_type": "adultOrYouthOnly", "originator_name": "Aberdare County Court", "fp_ticket_detail": null, "enforcement_court_id": 770000000021, "payment_card_request": null, "account_sentence_date": "1999-09-09", "collection_order_date": "1999-09-20", "collection_order_made": false, "suspended_committal_date": null, "prosecutor_case_reference": "CRN2023123995", "collection_order_made_today": null }',
            'fine',
            '{"account_type": "fine", "created_date": "2025-06-01T09:00:00.000000Z", "submitted_by": "L077JG", "date_of_birth": "1999-09-09", "defendant_name": "GREEN, Oliver", "submitted_by_name": "opal-test", "business_unit_name": "Camberwell Green"}',
            'PUBLISHING_FAILED',
            '[{"status": "Submitted", "username": "opal-test", "reason_text": null, "status_date": "2025-06-01"},{"status": "Approved", "username": "opal-test", "reason_text": null, "status_date": "2025-06-01"}]',
            'opal-test',
            CURRENT_DATE - INTERVAL '2 days',
            0
        ),
        -- 17 PUBLISHING_FAILED (company)
        (
            base_id + 16,
            77,
            CURRENT_DATE - INTERVAL '6 days',
            'L077JG',
            '{"offences": [{"offence_id": 33369, "impositions": [ {"result_id": "FVS", "amount_paid": 75, "amount_imposed": 175, "minor_creditor": null, "major_creditor_id": null} ], "date_of_sentence": null, "imposing_court_id": 770000000021 }], "defendant": { "company_flag": true, "company_name": "Sunrise Logistics", "dob": null, "title": null, "gender": null, "forenames": null, "surname": null, "pnc_id": null, "cro_number": null, "national_insurance_number": null, "occupation": null, "post_code": "BA1 2AA", "address_line_1": "2 Transport Way", "address_line_2": null, "address_line_3": null, "address_line_4": null, "address_line_5": null, "custody_status": null, "email_address_1": "admin@sunriselogistics.co.uk", "email_address_2": null, "telephone_number_home": null, "telephone_number_mobile": null, "telephone_number_business": "01225 123456", "parent_guardian": null, "interpreter_lang": null, "ethnicity_observed": null, "ethnicity_self_defined": null, "driving_licence_number": null, "debtor_detail": { "document_language": null, "hearing_language": null, "aliases": [{"alias_company_name": "Sunrise Logistics UK"}], "employee_reference": "EMPL123456", "employer_company_name": "Example Employer Ltd", "employer_address_line_1": "1 Employer Street", "employer_address_line_2": "Suite 100", "employer_address_line_3": "Business Park", "employer_address_line_4": "City", "employer_address_line_5": "Region", "employer_post_code": "EX1 2MP", "employer_telephone_number": "01234 567890", "employer_email_address": "hr@exampleemployer.com" } }, "account_type": "fine", "account_notes": [ { "note_type": "AA", "account_note_text": "Publishing failed: Timeout.", "account_note_serial": 1 } ], "originator_id": 101, "payment_terms": { "enforcements": null, "effective_date": "2023-03-22", "lump_sum_amount": 50, "instalment_amount": 25, "instalment_period": "W", "default_days_in_jail": null, "payment_terms_type_code": "I" }, "defendant_type": "company", "originator_name": "Aberdare County Court", "fp_ticket_detail": null, "enforcement_court_id": 770000000021, "payment_card_request": null, "account_sentence_date": null, "collection_order_date": null, "collection_order_made": false, "suspended_committal_date": null, "prosecutor_case_reference": "CRN2023123996", "collection_order_made_today": null }',
            'fine',
            '{"account_type": "fine", "created_date": "2025-05-28T10:00:00.000000Z", "submitted_by": "L077JG", "date_of_birth": null, "defendant_name": "Sunrise Logistics", "submitted_by_name": "opal-test", "business_unit_name": "Camberwell Green"}',
            'PUBLISHING_FAILED',
            '[{"status": "Submitted", "username": "opal-test", "reason_text": null, "status_date": "2025-05-28"},{"status": "Approved", "username": "opal-test", "reason_text": null, "status_date": "2025-05-28"}]',
            'opal-test',
            CURRENT_DATE - INTERVAL '6 days',
            0
        ),
        -- 18 PUBLISHING_FAILED (parent/guardian)
        (
            base_id + 17,
            77,
            CURRENT_DATE - INTERVAL '7 days',
            'L077JG',
            '{"offences": [{"offence_id": 33369, "impositions": [ {"result_id": "FVS", "amount_paid": 65, "amount_imposed": 165, "minor_creditor": null, "major_creditor_id": null} ], "date_of_sentence": "12/12/2009", "imposing_court_id": 770000000021 }], "defendant": { "dob": "2009-12-12", "title": "Mr", "gender": "M", "pnc_id": null, "surname": "KING", "forenames": "Jacob", "post_code": "S1 2AA", "cro_number": null, "occupation": null, "company_flag": false, "company_name": null, "debtor_detail": null, "nationality_1": null, "nationality_2": null, "prison_number": null, "address_line_1": "66 Queen Street", "address_line_2": null, "address_line_3": null, "address_line_4": null, "address_line_5": null, "custody_status": null, "email_address_1": "jacob.king@example.com", "email_address_2": null, "parent_guardian": { "company_flag": false, "company_name": null, "surname": "KING", "forenames": "Martin", "dob": "1981-08-08", "national_insurance_number": "QQ112244A", "address_line_1": "66 Queen Street", "address_line_2": null, "address_line_3": null, "address_line_4": null, "address_line_5": null, "post_code": "S1 2AA", "telephone_number_home": "0114 234 5678", "telephone_number_business": null, "telephone_number_mobile": "07700 222333", "email_address_1": "martin.king@example.com", "email_address_2": null, "debtor_detail": null }, "interpreter_lang": null, "ethnicity_observed": null, "telephone_number_home": "0114 876 5432", "driving_licence_number": null, "ethnicity_self_defined": null, "telephone_number_mobile": "07700 333444", "national_insurance_number": null, "telephone_number_business": null }, "account_type": "fine", "account_notes": [ { "note_type": "AA", "account_note_text": "Publishing failed: Court not found.", "account_note_serial": 1 } ], "originator_id": 101, "payment_terms": { "enforcements": null, "effective_date": "2023-03-22", "lump_sum_amount": 50, "instalment_amount": 25, "instalment_period": "W", "default_days_in_jail": null, "payment_terms_type_code": "I" }, "defendant_type": "parentOrGuardianToPay", "originator_name": "Aberdare County Court", "fp_ticket_detail": null, "enforcement_court_id": 770000000021, "payment_card_request": null, "account_sentence_date": "2009-12-12", "collection_order_date": "2009-12-22", "collection_order_made": true, "suspended_committal_date": null, "prosecutor_case_reference": "CRN2023123997", "collection_order_made_today": null }',
            'fine',
            '{"account_type": "fine", "created_date": "2025-05-27T11:00:00.000000Z", "submitted_by": "L077JG", "date_of_birth": "2009-12-12", "defendant_name": "KING, Jacob", "submitted_by_name": "opal-test", "business_unit_name": "Camberwell Green"}',
            'PUBLISHING_FAILED',
            '[{"status": "Submitted", "username": "opal-test", "reason_text": null, "status_date": "2025-05-27"},{"status": "Approved", "username": "opal-test", "reason_text": null, "status_date": "2025-05-27"}]',
            'opal-test',
            CURRENT_DATE - INTERVAL '7 days',
            0
        ),
        -- 19 PUBLISHING_PENDING (different user)
        (
            base_id + 18,
            77,
            CURRENT_DATE - INTERVAL '3 days',
            'L077JG',
            '{"offences": [{"offence_id": 33369, "impositions": [ {"result_id": "FVS", "amount_paid": 55, "amount_imposed": 95, "minor_creditor": null, "major_creditor_id": null} ], "date_of_sentence": "04/04/1986", "imposing_court_id": 770000000021 }], "defendant": { "dob": "1986-04-04", "title": "Mr", "gender": "M", "pnc_id": null, "surname": "COOPER", "forenames": "Ben", "post_code": "NE1 1AA", "cro_number": null, "occupation": null, "company_flag": false, "company_name": null, "debtor_detail": null, "nationality_1": null, "nationality_2": null, "prison_number": null, "address_line_1": "5 North Road", "address_line_2": null, "address_line_3": null, "address_line_4": null, "address_line_5": null, "custody_status": null, "email_address_1": "ben.cooper@example.com", "email_address_2": null, "parent_guardian": null, "interpreter_lang": null, "ethnicity_observed": null, "telephone_number_home": "0191 123 4567", "driving_licence_number": null, "ethnicity_self_defined": null, "telephone_number_mobile": "07700 999888", "national_insurance_number": "QQ223355C", "telephone_number_business": null }, "account_type": "fine", "account_notes": [ { "note_type": "AA", "account_note_text": "Publishing pending for account.", "account_note_serial": 1 } ], "originator_id": 101, "payment_terms": { "enforcements": null, "effective_date": "2023-03-22", "lump_sum_amount": 50, "instalment_amount": 25, "instalment_period": "W", "default_days_in_jail": null, "payment_terms_type_code": "I" }, "defendant_type": "adultOrYouthOnly", "originator_name": "Aberdare County Court", "fp_ticket_detail": null, "enforcement_court_id": 770000000021, "payment_card_request": null, "account_sentence_date": "1986-04-04", "collection_order_date": "1986-04-10", "collection_order_made": false, "suspended_committal_date": null, "prosecutor_case_reference": "CRN2023123998", "collection_order_made_today": null }',
            'fine',
            '{"account_type": "fine", "created_date": "2025-05-31T14:00:00.000000Z", "submitted_by": "L077JG", "date_of_birth": "1986-04-04", "defendant_name": "COOPER, Ben", "submitted_by_name": "opal-test", "business_unit_name": "Camberwell Green"}',
            'PUBLISHING_PENDING',
            '[{"status": "Submitted", "username": "opal-test", "reason_text": null, "status_date": "2025-05-31"},{"status": "Approved", "username": "opal-test", "reason_text": null, "status_date": "2025-05-31"}]',
            'opal-test',
            CURRENT_DATE - INTERVAL '3 days',
            0
        ),
        -- 20 DELETED (with rejected and resubmitted)
        (
            base_id + 19,
            77,
            CURRENT_DATE - INTERVAL '1 days',
            'L077JG',
            '{"offences": [{"offence_id": 33369, "impositions": [ {"result_id": "FVS", "amount_paid": 45, "amount_imposed": 100, "minor_creditor": null, "major_creditor_id": null} ], "date_of_sentence": "03/03/1997", "imposing_court_id": 770000000021 }], "defendant": { "dob": "1997-03-03", "title": "Ms", "gender": "F", "pnc_id": null, "surname": "EVANS", "forenames": "Lucy", "post_code": "PL1 1AA", "cro_number": null, "occupation": null, "company_flag": false, "company_name": null, "debtor_detail": { "employee_reference": "EMPL123456", "employer_company_name": "Example Employer Ltd", "employer_address_line_1": "1 Employer Street", "employer_address_line_2": "Suite 100", "employer_address_line_3": "Business Park", "employer_address_line_4": "City", "employer_address_line_5": "Region", "employer_post_code": "EX1 2MP", "employer_telephone_number": "01234 567890", "employer_email_address": "hr@exampleemployer.com" }, "nationality_1": null, "nationality_2": null, "prison_number": null, "address_line_1": "101 Harbour View", "address_line_2": null, "address_line_3": null, "address_line_4": null, "address_line_5": null, "custody_status": null, "email_address_1": "lucy.evans@example.com", "email_address_2": null, "parent_guardian": null, "interpreter_lang": null, "ethnicity_observed": null, "telephone_number_home": "01752 123456", "driving_licence_number": null, "ethnicity_self_defined": null, "telephone_number_mobile": "07700 555444", "national_insurance_number": "QQ445577A", "telephone_number_business": null }, "account_type": "fine", "account_notes": [ { "note_type": "AA", "account_note_text": "Missing DOB.", "account_note_serial": 1 }, { "note_type": "AA", "account_note_text": "User deleted draft.", "account_note_serial": 2 } ], "originator_id": 101, "payment_terms": { "enforcements": null, "effective_date": "2023-03-22", "lump_sum_amount": 50, "instalment_amount": 25, "instalment_period": "W", "default_days_in_jail": null, "payment_terms_type_code": "I" }, "defendant_type": "adultOrYouthOnly", "originator_name": "Aberdare County Court", "fp_ticket_detail": null, "enforcement_court_id": 770000000021, "payment_card_request": null, "account_sentence_date": "1997-03-03", "collection_order_date": "1997-03-10", "collection_order_made": false, "suspended_committal_date": null, "prosecutor_case_reference": "CRN2023123999", "collection_order_made_today": null }',
            'fine',
            '{"account_type": "fine", "created_date": "2025-05-22T12:00:00.000000Z", "submitted_by": "L077JG", "date_of_birth": "1997-03-03", "defendant_name": "EVANS, Lucy", "submitted_by_name": "opal-test", "business_unit_name": "Camberwell Green"}',
            'DELETED',
            '[{"status": "Submitted", "username": "opal-test", "reason_text": null, "status_date": "2025-05-22"},{"status": "Rejected", "username": "opal-test", "reason_text": "Missing DOB.", "status_date": "2025-05-23"},{"status": "Resubmitted", "username": "opal-test", "reason_text": null, "status_date": "2025-05-24"},{"status": "Deleted", "username": "opal-test", "reason_text": "User deleted draft.", "status_date": "2025-05-25"}]',
            'opal-test',
            CURRENT_DATE - INTERVAL '1 days',
            0
        )
    ;

END $$;
