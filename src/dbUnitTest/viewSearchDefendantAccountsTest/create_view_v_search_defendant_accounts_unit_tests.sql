/**
* CGI OPAL Program
*
* MODULE      : v_search_defendant_accounts_unit_tests.sql
*
* DESCRIPTION : Unit tests for v_search_defendant_accounts view
*               Tests verify that the view correctly retrieves search defendant account
*               information.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    ------------------------------------------------------------------------------------
* 23/09/2025    P Brumby    1.0         PO-2236 Unit tests for v_search_defendant_accounts view
**/

-- Start timing measurements for test execution
\timing

DO LANGUAGE 'plpgsql' $$
BEGIN
    RAISE NOTICE '=== Cleanup data before tests ===';
    
    -- Clear down data
    DELETE FROM impositions WHERE imposition_id BETWEEN 1 AND 20;
    DELETE FROM results WHERE result_id IN ('Res-1', 'Res-2', 'Res-3', 'Res-4', 'Res-5', 'Res-6', 'Res-7', 'Res-8', 'Res-9', 'Res-10');
    DELETE FROM creditor_transactions WHERE creditor_transaction_id BETWEEN 1 AND 30;
    DELETE FROM creditor_accounts WHERE creditor_account_id BETWEEN 1001 AND 1010;
    DELETE FROM aliases WHERE alias_id BETWEEN 1 AND 13;
    DELETE FROM defendant_account_parties WHERE defendant_account_party_id BETWEEN 1 AND 15;
    DELETE FROM defendant_accounts WHERE defendant_account_id BETWEEN 1 AND 10;
    DELETE FROM parties WHERE party_id BETWEEN 1 AND 30;

    RAISE NOTICE '=== Data cleanup before tests completed ===';

END $$;

-- Test setup: Create test data
DO $$
DECLARE
    -- Account 1: With aliases, payment terms, and complete profile
    v_business_unit_id        smallint := 9999;
    v_business_unit_name      varchar(200) := 'Test Business Unit';
    v_business_unit_type      varchar(20) := 'Accounting Division';
    v_local_justice_area_id   smallint := 999;
    v_lja_name                varchar(100) := 'Test Local Justice Area';
    v_lja_code                varchar(3) := 'TLA';
    v_defendant_account_id    bigint := 999901;
    v_party_id_defendant      bigint := 999901;
    v_court_id                bigint := 999901;
    v_court_name              varchar(60) := 'Test Court';
    v_court_code              varchar(10) := 'TESTCRT';
    v_result_id_enforcement   varchar(6) := 'DW';
    v_result_id_override      varchar(6) := 'SE';
    v_payment_terms_id        bigint := 999901;
    v_alias_id                bigint := 999901;
    v_document_id             varchar(12) := '999901';
    v_enforcer_id             bigint := 999901;
    v_enforcement_id          bigint := 999901;
BEGIN
    RAISE NOTICE '=== Setting up test data for v_search_defendant_accounts tests ===';

   -- Note - Business units are created from build Flyway

    -- Create test defendant accounts
    INSERT INTO defendant_accounts 
              (
                defendant_account_id,
                business_unit_id,
                account_number,
                amount_imposed,
                amount_paid, 
                account_balance,
                account_status,
                completed_date, 
                account_type,
                prosecutor_case_reference,
                last_enforcement
              ) VALUES 
                (1, 5, '25000001E', 150.00, 100.00, 50.00, 'L', NULL, 'Fine', 'PCR-1', 'DW'),
                (2, 5, '25000002A', 250.00, 250.00, 0.00, 'C', '2025-07-01', 'Fixed Penalty', 'PCR-2', NULL),
                (3, 8, '25000003T', 300.00, 150.00, 150.00, 'L', NULL, 'Conditional Caution', 'PCR-3', NULL),
                (4, 8, '25000004P', 450.00, 400.00, 50.00, 'L', NULL, 'Confiscation', 'PCR-4', NULL),
                (5, 9, '25000005L', 100.00, 50.00, 50.00, 'L', NULL, 'Fine', 'PCR-5', NULL),
                (6, 9, '25000006H', 500.00, 500.00, 0.00, 'C', '2025-07-02', 'Fixed Penalty', 'PCR-6', 'DW'),     --No alias
                (7, 10, '25000007D', 350.00, 150.00, 200.00, 'L', NULL, 'Confiscation', 'PCR-7', NULL),           --No alias
                (8, 10, '25000008W', 120.00, 60.00, 60.00, 'L', NULL, 'Fine', 'PCR-8', NULL),
                (9, 11, '25000009S', 600.00, 300.00, 300.00, 'L', NULL, 'Conditional Caution', 'PCR-9', NULL),    --No alias
                (10, 11, '25000010H', 250.00, 250.00, 0.00, 'C', '2025-07-03', 'Fixed Penalty', 'PCR-10', NULL);  --No alias

    -- Create test parties 
    -- column initials removed
    INSERT INTO parties 
              (
                party_id, 
                organisation, 
                organisation_name, 
                surname, 
                forenames, 
                title, 
                address_line_1, 
                postcode, 
                birth_date, 
                age, 
                telephone_mobile, 
                email_1, 
                national_insurance_number
              ) VALUES 
                --Defendant related parties
                (1, false, NULL, 'Surname1-DA1-Def', 'Fn1', 'Mr', 'address 1 DA1 Def', 'AB1 2CC', '1985-02-14', 39, '07123456789', 'fn1.surname1@example.com', 'NI010101A'),
                (2, false, NULL, 'Surname2-DA1-PG' , 'Fn2', 'Ms', 'address 2 DA1 PG', 'AB2 2CC', '1990-07-09', 35, '07234567890', 'fn2.surname2@example.com', 'NI020202A'),
                (3, false, NULL, 'Surname3-DA2-Def', 'Fn3', 'Mr', 'address 3 DA2 Def', 'AB3 2CC', '1978-11-23', 46, '07345678901', 'fn3.surname3@example.com', 'NI030303A'),
                (4, false, NULL, 'Surname4-DA2-PG' , 'Fn4', 'Mrs', 'address 4 DA2 PG', 'AB4 2CC', '1988-05-01', 36, '07456789012', 'fn4.surname4@example.com', 'NI040404A'),
                (5, false, NULL, 'Surname5-DA3-Def', 'Fn5', 'Mr', 'address 5 DA3 Def', 'AB5 2CC', '1995-12-30', 29, '07567890123', 'fn5.surname5@example.com', 'NI050505A'),
                (6, false, NULL, 'Surname6-DA3-PG' , 'Fn6', 'Ms', 'address 6 DA3 PG', 'AB6 2CC', '1982-08-19', 42, '07678901234', 'fn6.surname6@example.com', 'NI060606A'),
                (7, false, NULL, 'Surname7-DA4-Def', 'Fn7', 'Mr', 'address 7 DA4 Def', 'AB7 2CC', '1995-12-30', 29, '07567890123', 'fn7.surname5@example.com', 'NI070707A'),
                (8, false, NULL, 'Surname8-DA4-PG' , 'Fn8', 'Ms', 'address 8 DA4 PG', 'AB8 2CC', '1982-08-19', 42, '07678901234', 'fn8.surname6@example.com', 'NI080808A'),
                (9, false, NULL, 'Surname9-DA5-Def', 'Fn9', 'Mr', 'address 9 DA5 Def', 'AB9 2CC', '1995-12-30', 29, '07567890123', 'fn9.surname5@example.com', 'NI090909A'),
                (10, false, NULL, 'Surname10-DA5-PG', 'Fn10', 'Ms', 'address 10 DA5 PG', 'AB10 2CC', '1982-08-19', 42, '07678901234', 'fn10.surname6@example.com', 'NI101010A'),
                --Following only have defendant_account_parties.association_type='Defendant'
                (11, false, NULL, 'Surname11-DA6-Def', 'Fn11', 'Mr', 'address 11 DA6 Def', 'AB11 2CC', '1992-03-17', 33, '07789012345', 'fn11.surname7@example.com', 'NI111111A'), --No alias
                (12, false, NULL, 'Surname12-DA7-Def', 'Fn12', 'Ms', 'address 12 DA7 Def', 'AB12 2CC', '1980-06-25', 44, '07890123456', 'fn12.surname8@example.com', 'NI121212A'), --No alias
                (13, true, 'OrganisationName13-DA8', 'OrgSurname13-DA8-Def', 'OrgFn13', 'Mr', 'address 13 DA8 Def', 'AB13 2CC', '1986-09-12', 38, '07901234567', 'fn13.surname9@example.com', 'NI131313A'),
                (14, false, NULL, 'Surname14-DA9-Def', 'Fn14', 'Miss', 'address 14 DA9 Def', 'AB14 2CC', '1993-01-03', 32, '07012345678', 'fn14.surname10@example.com', 'NI141414A'), --No alias
                (15, false, NULL, 'Surname15-DA10-Def', 'Fn15', 'Miss', 'address 15 DA10 Def', 'AB15 2CC', '1993-01-04', 32, '07012345678', 'fn15.surname10@example.com', 'NI151515A'), --No alias
                --Creditor related parties
                (16, false, NULL, 'Surname16-CA1001-Def1', 'Fn16', 'Miss', 'address 16 CA1001 Def', 'AB16 2CC', '1993-01-04', 32, '07012345678', 'fn16.surname10@example.com', 'NI161616A'),
                (17, false, NULL, 'Surname17-CA1002-Def2', 'Fn17', 'Miss', 'address 17 CA1002 Def', 'AB17 2CC', '1993-01-04', 32, '07012345678', 'fn17.surname10@example.com', 'NI171717A'),
                (18, false, NULL, 'Surname18-CA1003-Def3', 'Fn18', 'Miss', 'address 18 CA1003 Def', 'AB18 2CC', '1993-01-04', 32, '07012345678', 'fn18.surname10@example.com', 'NI181818A'),
                (19, false, NULL, 'Surname19-CA1004-Def4', 'Fn19', 'Miss', 'address 19 CA1004 Def', 'AB19 2CC', '1993-01-04', 32, '07012345678', 'fn19.surname10@example.com', 'NI191919A'),
                (20, false, NULL, 'Surname20-CA1005-Def5', 'Fn20', 'Miss', 'address 20 CA1005 Def', 'AB20 2CC', '1993-01-04', 32, '07012345678', 'fn20.surname10@example.com', 'NI202020A'),
                (21, false, NULL, 'Surname21-CA1006-Def6', 'Fn21', 'Miss', 'address 21 CA1006 Def', 'AB21 2CC', '1993-01-04', 32, '07012345678', 'fn21.surname10@example.com', 'NI212121A'),
                (22, false, NULL, 'Surname22-CA1007-Def7', 'Fn22', 'Miss', 'address 22 CA1007 Def', 'AB22 2CC', '1993-01-04', 32, '07012345678', 'fn22.surname10@example.com', 'NI222222A'),
                (23, false, NULL, 'Surname23-CA1008-Def8', 'Fn23', 'Miss', 'address 23 CA1008 Def', 'AB23 2CC', '1993-01-04', 32, '07012345678', 'fn23.surname10@example.com', 'NI232323A'),
                (24, true, 'OrganisationName24-CA1009', 'Surname24-CA1009-Def9', 'Fn24', 'Miss', 'address 24 CA1009 Def', 'AB24 2CC', '1993-01-04', 32, '07012345678', 'fn24.surname10@example.com', 'NI242424A'),
                (25, true, 'OrganisationName25-CA1010', 'Surname25-CA1010-Def10', 'Fn25', 'Miss', 'address 25 CA1010 Def', 'AB25 2CC', '1993-01-04', 32, '07012345678', 'fn25.surname10@example.com', 'NI252525A');

    -- Create test defendant account parties 
    INSERT INTO defendant_account_parties 
              (
                defendant_account_party_id, 
                defendant_account_id, 
                party_id, 
                association_type, 
                debtor
              ) VALUES 
                (1, 1, 1, 'Defendant', true),
                (2, 1, 2, 'Parent/Guardian', false),
                (3, 2, 3, 'Defendant', true),
                (4, 2, 4, 'Parent/Guardian', false),
                (5, 3, 5, 'Defendant', true),
                (6, 3, 6, 'Parent/Guardian', false),
                (7, 4, 7, 'Defendant', false),
                (8, 4, 8, 'Parent/Guardian', true),
                (9, 5, 9, 'Defendant', false),
                (10, 5, 10, 'Parent/Guardian', true),
                --'Defendant only'
                (11, 6, 11, 'Defendant', true),  --No alias
                (12, 7, 12, 'Defendant', true),  --No alias
                (13, 8, 13, 'Defendant', true),
                (14, 9, 14, 'Defendant', true),  --No alias
                (15, 10, 15, 'Defendant', true);  --No alias

    -- Create test defendant account parties 
    -- column initials removed
    INSERT INTO aliases 
              (
                alias_id, 
                party_id, 
                surname, 
                forenames, 
                sequence_number, 
                organisation_name
              ) VALUES 
                (1, 1, 'Surname1-DA1-Def-Alias', 'Fn1-Alias', 1, NULL),
                (2, 2, 'Surname2-DA1-PG-Alias', 'Fn2-Alias', 1, NULL),
                (3, 3, 'Surname3-DA2-Def-Alias', 'Fn3-Alias', 1, NULL),
                (4, 4, 'Surname4-DA2-PG-Alias', 'Fn4-Alias', 1, NULL),
                (5, 5, 'Surname5-DA3-Def-Alias', 'Fn5-Alias', 1, NULL),
                (6, 6, 'Surname6-DA3-PG-Alias', 'Fn6-Alias', 1, NULL),
                (7, 7, 'Surname7-DA4-Def-Alias', 'Fn7-Alias', 1, NULL),
                (8, 8, 'Surname8-DA4-PG-Alias', 'Fn8-Alias', 1, NULL),
                (9, 9, 'Surname9-DA5-Def-Alias-1', 'Fn9-Alias', 1, NULL),
                (10, 9, 'Surname9-DA5-Def-Alias-2', 'Fn9-Alias', 2, NULL),
                (11, 9, 'Surname9-DA5-Def-Alias-3', 'Fn9-Alias', 3, NULL),
                (12, 10, 'Surname10-DA5-PG-Alias1', 'Fn10-Alias', 1, NULL),
                (13, 13, 'OrgSurname13-DA8-Def-Alias', 'OrgFn13-Alias', 1, 'OrganisationName13-DA8-Alias');

    -- Create test creditor accounts 
    INSERT INTO creditor_accounts 
              (
                creditor_account_id, 
                business_unit_id, 
                account_number, 
                creditor_account_type, 
                prosecution_service,
                major_creditor_id, 
                minor_creditor_party_id, 
                from_suspense, 
                hold_payout, 
                pay_by_bacs
              ) VALUES 
                (1001, 5, '25000011D', 'MN', true, NULL, 16, false, true, true),
                (1002, 5, '25000012W', 'MN', false, NULL, 17, true, true, false),
                (1003, 8, '25000013S', 'MN', true, NULL, 18, false, true, true),
                (1004, 8, '25000014O', 'MN', false, NULL, 19, false, true, false),
                (1005, 9, '25000015K', 'MN', true, NULL, 20, true, true, true),
                (1006, 9, '25000016G', 'MN', false, NULL, 21, false, true, false),   --creditor_transactions both PAYMNT & XFER are TRUE 
                (1007, 10, '25000017C', 'MN', true, NULL, 22, true, true, true),
                (1008, 10, '25000018V', 'MN', false, NULL, 23, false, true, false),  --Multiple impositions
                --Have no creditor_transactions
                (1009, 11, '25000019R', 'MN', true, NULL, 24, false, true, true),
                (1010, 11, '25000020G', 'MN', false, NULL, 25, true, true, false);   --Also no impositions

    -- Create test creditor transactions 
    INSERT INTO creditor_transactions 
              (
                creditor_transaction_id, 
                creditor_account_id, 
                posted_date, 
                posted_by, 
                posted_by_name,
                transaction_type, 
                transaction_amount, 
                payment_processed, 
                payment_reference, 
                status, 
                status_date
              ) VALUES 
                -- Account 1  - creditor_account_balance = 100 (PAYMT & XFER = FALSE)
                (1, 1001, '2025-07-01 10:15:00', 'admin01', 'Admin One', 'PAYMNT', 80.00, false, 'REF6001', 'C', '2025-07-01'),
                (2, 1001, '2025-07-04 11:00:00', 'admin02', 'Admin Two', 'CREDT', 50.00, true, 'REF6002', 'C', '2025-07-04'),
                (3, 1001, '2025-07-09 12:30:00', 'admin01', 'Admin One', 'XFER', 20.00, false, 'REF6003', 'C', '2025-07-09'),
                -- Account 2  - creditor_account_balance = 0 (PAYMT & XFER = TRUE)
                (4, 1002, '2025-07-02 09:10:00', 'admin02', 'Admin Two', 'PAYMNT', 200.00, true, 'REF6004', 'C', '2025-07-02'),
                (5, 1002, '2025-07-05 13:20:00', 'admin03', 'Admin Three', 'CREDT', 75.00, false, 'REF6005', 'C', '2025-07-05'),
                (6, 1002, '2025-07-11 15:45:00', 'admin02', 'Admin Two', 'XFER', 50.00, true, 'REF6006', 'C', '2025-07-11'),
                -- Account 3  - creditor_account_balance = 150 (PAYMT & XFER = FALSE)
                (7, 1003, '2025-07-03 14:00:00', 'admin01', 'Admin One', 'PAYMNT', 100.00, false, 'REF6007', 'C', '2025-07-03'),
                (8, 1003, '2025-07-06 10:35:00', 'admin02', 'Admin Two', 'CREDT', 80.00, false, 'REF6008', 'C', '2025-07-06'),
                (9, 1003, '2025-07-10 17:25:00', 'admin03', 'Admin Three', 'XFER', 50.00, false, 'REF6009', 'C', '2025-07-10'),
                -- Account 4  - creditor_account_balance = 220 (PAYMT = TRUE, XFER = FALSE)
                (10, 1004, '2025-07-02 08:00:00', 'admin01', 'Admin One', 'PAYMNT', 180.00, true, 'REF6010', 'C', '2025-07-02'),
                (11, 1004, '2025-07-07 14:45:00', 'admin03', 'Admin Three', 'CREDT', 60.00, true, 'REF6011', 'C', '2025-07-07'),
                (12, 1004, '2025-07-12 09:50:00', 'admin02', 'Admin Two', 'XFER', 220.00, false, 'REF6012', 'C', '2025-07-12'),
                -- Account 5  - creditor_account_balance = 40 (PAYMT = FALSE, XFER = TRUE)
                (13, 1005, '2025-07-01 12:00:00', 'admin02', 'Admin Two', 'PAYMNT', 10.00, false, 'REF6013', 'C', '2025-07-01'),
                (14, 1005, '2025-07-05 16:30:00', 'admin01', 'Admin One', 'CREDT', 80.00, true, 'REF6014', 'C', '2025-07-05'),
                (15, 1005, '2025-07-10 11:15:00', 'admin03', 'Admin Three', 'XFER', 40.00, true, 'REF6015', 'C', '2025-07-10'),
                -- Account 6  - creditor_account_balance = 0 (PAYMT & XFER = TRUE)
                (16, 1006, '2025-07-03 13:20:00', 'admin03', 'Admin Three', 'PAYMNT', 220.00, true, 'REF6016', 'C', '2025-07-03'),
                (17, 1006, '2025-07-08 09:35:00', 'admin01', 'Admin One', 'CREDT', 90.00, false, 'REF6017', 'C', '2025-07-08'),
                (18, 1006, '2025-07-13 14:10:00', 'admin02', 'Admin Two', 'XFER', 280.00, true, 'REF6018', 'C', '2025-07-13'),
                -- Account 7  - creditor_account_balance = 150 (PAYMT & XFER = FALSE)
                (19, 1007, '2025-07-01 08:45:00', 'admin01', 'Admin One', 'PAYMNT', 125.00, false, 'REF6019', 'C', '2025-07-01'),
                (20, 1007, '2025-07-05 15:50:00', 'admin03', 'Admin Three', 'CREDT', 95.00, true, 'REF6020', 'C', '2025-07-05'),
                (21, 1007, '2025-07-09 10:05:00', 'admin02', 'Admin Two', 'XFER', 25.00, false, 'REF6021', 'C', '2025-07-09'),
                -- Account 8  - creditor_account_balance = 60 (PAYMT & XFER = FALSE)
                (22, 1008, '2025-07-04 09:25:00', 'admin01', 'Admin One', 'PAYMNT', 40.00, false, 'REF6022', 'C', '2025-07-04'),
                (23, 1008, '2025-07-08 14:40:00', 'admin02', 'Admin Two', 'CREDT', 65.00, false, 'REF6023', 'C', '2025-07-08'),
                (24, 1008, '2025-07-14 12:20:00', 'admin03', 'Admin Three', 'XFER', 20.00, false, 'REF6024', 'C', '2025-07-14');
                /*
                -- Account 9  - creditor_account_balance = 300 (PAYMT & XFER = FALSE)
                (25, 1009, '2025-07-03 11:05:00', 'admin03', 'Admin Three', 'PAYMNT', 190.00, false, 'REF6025', 'C', '2025-07-03'),
                (26, 1009, '2025-07-07 08:55:00', 'admin01', 'Admin One', 'CREDT', 85.00, true, 'REF6026', 'C', '2025-07-07'),
                (27, 1009, '2025-07-12 13:30:00', 'admin02', 'Admin Two', 'XFER', 110.00, false, 'REF6027', 'C', '2025-07-12'),
                -- Account 10  - creditor_account_balance = 0 (PAYMT & XFER = TRUE)
                (28, 1010, '2025-07-02 10:20:00', 'admin02', 'Admin Two', 'PAYMNT', 175.00, true, 'REF6028', 'C', '2025-07-02'),
                (29, 1010, '2025-07-06 11:50:00', 'admin03', 'Admin Three', 'CREDT', 70.00, true, 'REF6029', 'C', '2025-07-06'),
                (30, 1010, '2025-07-10 16:05:00', 'admin01', 'Admin One', 'XFER', 75.00, true, 'REF6030', 'C', '2025-07-10')
                */

    -- Create test enforcement results
    INSERT INTO results 
              (
                result_id,
                result_title,
                result_type, -- Must be 'Result' or 'Action' per check constraint
                active,
                imposition,
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
                imposition_accruing
              ) VALUES 
                ('Res-1', 'Imposition 1 Distress Warrant', 'Result', TRUE, FALSE, TRUE, FALSE, FALSE, FALSE, FALSE, TRUE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, TRUE, FALSE),
                ('Res-2', 'Imposition 2 Distress Warrant', 'Result', TRUE, FALSE, TRUE, FALSE, FALSE, FALSE, FALSE, TRUE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, TRUE, FALSE),
                ('Res-3', 'Imposition 3 Distress Warrant', 'Result', TRUE, FALSE, TRUE, FALSE, FALSE, FALSE, FALSE, TRUE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, TRUE, FALSE),
                ('Res-4', 'Imposition 4 Distress Warrant', 'Result', TRUE, FALSE, TRUE, FALSE, FALSE, FALSE, FALSE, TRUE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, TRUE, FALSE),
                ('Res-5', 'Imposition 5 Distress Warrant', 'Result', TRUE, FALSE, TRUE, FALSE, FALSE, FALSE, FALSE, TRUE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, TRUE, FALSE),
                ('Res-6', 'Imposition 6 Distress Warrant', 'Result', TRUE, FALSE, TRUE, FALSE, FALSE, FALSE, FALSE, TRUE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, TRUE, FALSE),
                ('Res-7', 'Imposition 7 Distress Warrant', 'Result', TRUE, FALSE, TRUE, FALSE, FALSE, FALSE, FALSE, TRUE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, TRUE, FALSE),
                ('Res-8', 'Imposition 8 Distress Warrant', 'Result', TRUE, FALSE, TRUE, FALSE, FALSE, FALSE, FALSE, TRUE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, TRUE, FALSE),
                ('Res-9', 'Imposition 9 Distress Warrant', 'Result', TRUE, FALSE, TRUE, FALSE, FALSE, FALSE, FALSE, TRUE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, TRUE, FALSE),
                ('Res-10', 'Imposition 10 Suspend Enforcement', 'Result', TRUE, FALSE, TRUE, TRUE, TRUE, TRUE, TRUE, FALSE, FALSE, FALSE, FALSE, FALSE, TRUE, FALSE, FALSE, FALSE);

    -- Create test impositions 
    INSERT INTO impositions
              (
                imposition_id, 
                defendant_account_id, 
                posted_date, 
                result_id, 
                imposed_amount, 
                paid_amount, 
                creditor_account_id, 
                completed
              ) VALUES 
                (1, 1, '2025-01-01', 'Res-1', 150, 100, 1001, false),
                (2, 2, '2025-01-02', 'Res-2', 250, 250, 1002, true),
                (3, 3, '2025-01-03', 'Res-3', 300, 150, 1003, false),
                (4, 4, '2025-01-04', 'Res-4', 450, 400, 1004, false),
                (5, 5, '2025-01-05', 'Res-5', 100, 50, 1005, false),
                (6, 6, '2025-01-06', 'Res-6', 500, 500, 1006, true),
                (7, 7, '2025-01-07', 'Res-7', 350, 150, 1007, false),
                (8, 8, '2025-01-08', 'Res-8', 80, 40, 1008, false),
                (9, 8, '2025-01-09', 'Res-9', 40, 20, 1008, false),
                (10, 9, '2025-01-10', 'Res-10', 600, 300, 1009, false);
                --(11, 10, '2025-01-11', 1, 250, 250, 1010, true)
    
    RAISE NOTICE 'Test data setup completed: defendant_account_ids are 1 to 10';
END $$;

-- Test 1: Comprehensive test for account with all details
DO $$
DECLARE
    -- Account information variables
    v_defendant_account_id      bigint;
    v_account_number            varchar(20);
    v_prosecutor_case_reference varchar(40);  -- new
    v_last_enforcement          varchar(6);
    v_account_status            varchar(2); -- new
    v_defendant_account_balance numeric(18,2); -- new
    v_completed_date            timestamp; -- new
    v_business_unit_id          integer; -- new
    v_business_unit_name        varchar(200); -- new
    
    -- Party and debtor type information
    v_party_id                  bigint;
    v_title                     varchar(20);
    v_forenames                 varchar(50);
    v_surname                   varchar(50);
    v_birth_date                timestamp;
    v_organisation              boolean;
    v_organisation_name         varchar(80);
    v_parent_guardian_surname   varchar(50); -- new
    v_parent_guardian_forenames varchar(50); -- new
    
    -- Address information
    v_address_line_1            varchar(35);
    v_postcode                  varchar(10);
    v_national_insurance_number varchar(10);
    
    -- Alias information
    v_alias1                    varchar(110); -- new
    v_alias2                    varchar(110);  -- new
    v_alias3                    varchar(110);  -- new
    v_alias4                    varchar(110);  -- new
    v_alias5                    varchar(110);  -- new
    
BEGIN
    -- Test scenarios: 
    -- Scenario 1: Verify live defendant account with parent/guardian, with 1 alias
    -- Scenario 2: Verify completed defendant account with parent/guardian, with 1 alias
    -- Scenario 3: Verify live defendant account with parent/guardian, with 1 alias
    -- Scenario 4: Verify live defendant account with parent/guardian, with 1 alias
    -- Scenario 5: Verify live defendant account with parent/guardian, with 3 aliases
    -- Scenario 6: Verify completed defendant account with no parent/guardian, without an alias
    -- Scenario 7: Verify live defendant account with no parent/guardian, without an alias
    -- Scenario 8: Verify live defendant account for an organisation with no parent/guardian, with 1 alias
    -- Scenario 9: Verify live defendant account with no parent/guardian, without an alias
    -- Scenario 10: Verify completed defendant account with no parent/guardian, without an alias

    FOR i IN 1..10 LOOP
        
        RAISE NOTICE '--- Running test Scenario % ---', i;

        -- Query the view for all information in one go
        SELECT defendant_account_id,
               account_number,
               prosecutor_case_reference,
               last_enforcement,
               account_status,
               defendant_account_balance,
               completed_date,
               business_unit_id,
               business_unit_name,
               party_id,
               organisation,
               organisation_name,
               address_line_1,
               postcode,
               title,
               forenames,
               surname,
               birth_date,
               national_insurance_number,
               parent_guardian_surname,
               parent_guardian_forenames,
               -- Aliases fields (up to 5)
               alias1,
               alias2,
               alias3,
               alias4,
               alias5
        INTO   v_defendant_account_id,
               v_account_number,
               v_prosecutor_case_reference, 
               v_last_enforcement,
               v_account_status, 
               v_defendant_account_balance, 
               v_completed_date, 
               v_business_unit_id, 
               v_business_unit_name, 
               v_party_id, 
               v_organisation,
               v_organisation_name,
               v_address_line_1,
               v_postcode,
               v_title, 
               v_forenames, 
               v_surname,  
               v_birth_date, 
               v_national_insurance_number, 
               v_parent_guardian_surname, 
               v_parent_guardian_forenames, 
               v_alias1, 
               v_alias2, 
               v_alias3, 
               v_alias4, 
               v_alias5  
        FROM  v_search_defendant_accounts 
        WHERE defendant_account_id = i;  
    
        -- Verify results in groups of related data
        CASE i
            WHEN 1 THEN
                -- Scenario 1: Verify live defendant account with parent/guardian, with 1 alias

                -- 1. Basic account information
                ASSERT v_defendant_account_id = 1, 'Defendant account ID should be 1';
                ASSERT v_account_number = '25000001E', 'Account number should match';
                ASSERT v_prosecutor_case_reference = 'PCR-1', 'Prosecutor case reference should match';
                ASSERT v_last_enforcement = 'DW', 'Last enforcement ID should match';
                ASSERT v_account_status = 'L', 'Account status should match';
                ASSERT v_defendant_account_balance = 50.00, 'Defendant account balance should match';
                ASSERT v_completed_date::date IS NULL, 'Completed date should be NULL';

                -- 2. Business unit information
                ASSERT v_business_unit_id = 5, 'Business unit ID should be 5';
                ASSERT v_business_unit_name = 'Cambridgeshire', 'Business unit name should match';

                -- 3. Party and debtor information
                ASSERT v_party_id = 1, 'Party ID should match';
                ASSERT v_organisation = FALSE, 'Organisation flag should be FALSE';
                ASSERT v_organisation_name IS NULL, 'Organisation name should be NULL';
                ASSERT v_title = 'Mr', 'Title should match';
                ASSERT v_forenames = 'Fn1', 'Forenames should match';
                ASSERT v_surname = 'Surname1-DA1-Def', 'Surname should match';
                ASSERT v_birth_date::date = '1985-02-14'::date, 'Birth date should match';

                -- 4. Party parent/guardian information
                ASSERT v_parent_guardian_forenames = 'Fn2', 'Parent or Guardian forenames should match';
                ASSERT v_parent_guardian_surname = 'Surname2-DA1-PG', 'Parent or Guardian surname should match';

                -- 5. Address information
                ASSERT v_address_line_1 = 'address 1 DA1 Def', 'Address line 1 should match';
                ASSERT v_postcode = 'AB1 2CC', 'Postcode should match';
                ASSERT v_national_insurance_number = 'NI010101A', 'National Insurance number should match';

                -- 6. Alias information
                ASSERT v_alias1 = 'Fn1-Alias Surname1-DA1-Def-Alias', 'Alias name 1 with person alias with forename and surname name should match';
                ASSERT v_alias2 IS NULL, 'Alias name 2 should be NULL';
                ASSERT v_alias3 IS NULL, 'Alias name 3 should be NULL';
                ASSERT v_alias4 IS NULL, 'Alias name 4 should be NULL';
                ASSERT v_alias5 IS NULL, 'Alias name 5 should be NULL';

            WHEN 2 THEN
                -- Scenario 2: Expect completed defendant account with parent/guardian, with 1 alias

                -- 1. Basic account information
                ASSERT v_defendant_account_id = 2, 'Defendant account ID should be 2';
                ASSERT v_account_number = '25000002A', 'Account number should match';
                ASSERT v_prosecutor_case_reference = 'PCR-2', 'Prosecutor case reference should match';
                ASSERT v_last_enforcement IS NULL, 'Last enforcement ID should be NULL';
                ASSERT v_account_status = 'C', 'Account status should match';
                ASSERT v_defendant_account_balance = 0.00, 'Defendant account balance should match';
                ASSERT v_completed_date::date = '2025-07-01'::date, 'Completed date should match';

                -- 2. Business unit information
                ASSERT v_business_unit_id = 5, 'Business unit ID should be 5';
                ASSERT v_business_unit_name = 'Cambridgeshire', 'Business unit name should match';

                -- 3. Party and debtor information
                ASSERT v_party_id = 3, 'Party ID should match';
                ASSERT v_organisation = FALSE, 'Organisation flag should be FALSE';
                ASSERT v_organisation_name IS NULL, 'Organisation name should be NULL';
                ASSERT v_title = 'Mr', 'Title should match';
                ASSERT v_forenames = 'Fn3', 'Forenames should match';
                ASSERT v_surname = 'Surname3-DA2-Def', 'Surname should match';
                ASSERT v_birth_date::date = '1978-11-23'::date, 'Birth date should match';

                -- 4. Party parent/guardian information
                ASSERT v_parent_guardian_forenames = 'Fn4', 'Parent or Guardian forenames should match';
                ASSERT v_parent_guardian_surname = 'Surname4-DA2-PG', 'Parent or Guardian surname should match';

                -- 5. Address information
                ASSERT v_address_line_1 = 'address 3 DA2 Def', 'Address line 1 should match';
                ASSERT v_postcode = 'AB3 2CC', 'Postcode should match';
                ASSERT v_national_insurance_number = 'NI030303A', 'National Insurance number should match';

                -- 6. Alias information
                ASSERT v_alias1 = 'Fn3-Alias Surname3-DA2-Def-Alias', 'Alias name 1 with person alias with forename and surname name should match';
                ASSERT v_alias2 IS NULL, 'Alias name 2 should be NULL';
                ASSERT v_alias3 IS NULL, 'Alias name 3 should be NULL';
                ASSERT v_alias4 IS NULL, 'Alias name 4 should be NULL';
                ASSERT v_alias5 IS NULL, 'Alias name 5 should be NULL';

            WHEN 3 THEN
                -- Scenario 3: Expect live defendant account with parent/guardian, with 1 alias

                -- 1. Basic account information
                ASSERT v_defendant_account_id = 3, 'Defendant account ID should be 3';
                ASSERT v_account_number = '25000003T', 'Account number should match';
                ASSERT v_prosecutor_case_reference = 'PCR-3', 'Prosecutor case reference should match';
                ASSERT v_last_enforcement IS NULL, 'Last enforcement ID should be NULL';
                ASSERT v_account_status = 'L', 'Account status should match';
                ASSERT v_defendant_account_balance = 150.00, 'Defendant account balance should match';
                ASSERT v_completed_date::date IS NULL, 'Completed date should be NULL';

                -- 2. Business unit information
                ASSERT v_business_unit_id = 8, 'Business unit ID should be 8';
                ASSERT v_business_unit_name = 'Cleveland', 'Business unit name should match';

                -- 3. Party and debtor information
                ASSERT v_party_id = 5, 'Party ID should match';
                ASSERT v_organisation = FALSE, 'Organisation flag should be FALSE';
                ASSERT v_organisation_name IS NULL, 'Organisation name should be NULL';
                ASSERT v_title = 'Mr', 'Title should match';
                ASSERT v_forenames = 'Fn5', 'Forenames should match';
                ASSERT v_surname = 'Surname5-DA3-Def', 'Surname should match';
                ASSERT v_birth_date::date = '1995-12-30'::date, 'Birth date should match';

                -- 4. Party parent/guardian information
                ASSERT v_parent_guardian_forenames = 'Fn6', 'Parent or Guardian forenames should match';
                ASSERT v_parent_guardian_surname = 'Surname6-DA3-PG', 'Parent or Guardian surname should match';

                -- 5. Address information
                ASSERT v_address_line_1 = 'address 5 DA3 Def', 'Address line 1 should match';
                ASSERT v_postcode = 'AB5 2CC', 'Postcode should match';
                ASSERT v_national_insurance_number = 'NI050505A', 'National Insurance number should match';

                -- 6. Alias information
                ASSERT v_alias1 = 'Fn5-Alias Surname5-DA3-Def-Alias', 'Alias name 1 with person alias with forename and surname name should match';
                ASSERT v_alias2 IS NULL, 'Alias name 2 should be NULL';
                ASSERT v_alias3 IS NULL, 'Alias name 3 should be NULL';
                ASSERT v_alias4 IS NULL, 'Alias name 4 should be NULL';
                ASSERT v_alias5 IS NULL, 'Alias name 5 should be NULL';

            WHEN 4 THEN
                -- Scenario 4: Expect live defendant account with parent/guardian, with 1 alias

                -- 1. Basic account information
                ASSERT v_defendant_account_id = 4, 'Defendant account ID should be 4';
                ASSERT v_account_number = '25000004P', 'Account number should match';
                ASSERT v_prosecutor_case_reference = 'PCR-4', 'Prosecutor case reference should match';
                ASSERT v_last_enforcement IS NULL, 'Last enforcement ID should be NULL';
                ASSERT v_account_status = 'L', 'Account status should match';
                ASSERT v_defendant_account_balance = 50.00, 'Defendant account balance should match';
                ASSERT v_completed_date::date IS NULL, 'Completed date should be NULL';

                -- 2. Business unit information
                ASSERT v_business_unit_id = 8, 'Business unit ID should be 8';
                ASSERT v_business_unit_name = 'Cleveland', 'Business unit name should match';

                -- 3. Party and debtor information
                ASSERT v_party_id = 7, 'Party ID should match';
                ASSERT v_organisation = FALSE, 'Organisation flag should be FALSE';
                ASSERT v_organisation_name IS NULL, 'Organisation name should be NULL';
                ASSERT v_title = 'Mr', 'Title should match';
                ASSERT v_forenames = 'Fn7', 'Forenames should match';
                ASSERT v_surname = 'Surname7-DA4-Def', 'Surname should match';
                ASSERT v_birth_date::date = '1995-12-30'::date, 'Birth date should match';

                -- 4. Party parent/guardian information
                ASSERT v_parent_guardian_forenames = 'Fn8', 'Parent or Guardian forenames should match';
                ASSERT v_parent_guardian_surname = 'Surname8-DA4-PG', 'Parent or Guardian surname should match';

                -- 5. Address information
                ASSERT v_address_line_1 = 'address 7 DA4 Def', 'Address line 1 should match';
                ASSERT v_postcode = 'AB7 2CC', 'Postcode should match';
                ASSERT v_national_insurance_number = 'NI070707A', 'National Insurance number should match';

                -- 6. Alias information
                ASSERT v_alias1 = 'Fn7-Alias Surname7-DA4-Def-Alias', 'Alias name 1 with person alias with forename and surname name should match';
                ASSERT v_alias2 IS NULL, 'Alias name 2 should be NULL';
                ASSERT v_alias3 IS NULL, 'Alias name 3 should be NULL';
                ASSERT v_alias4 IS NULL, 'Alias name 4 should be NULL';
                ASSERT v_alias5 IS NULL, 'Alias name 5 should be NULL';

            WHEN 5 THEN
                -- Scenario 5: Expect live defendant account with parent/guardian, with 3 aliases

                -- 1. Basic account information
                ASSERT v_defendant_account_id = 5, 'Defendant account ID should be 5';
                ASSERT v_account_number = '25000005L', 'Account number should match';
                ASSERT v_prosecutor_case_reference = 'PCR-5', 'Prosecutor case reference should match';
                ASSERT v_last_enforcement IS NULL, 'Last enforcement ID should be NULL';
                ASSERT v_account_status = 'L', 'Account status should match';
                ASSERT v_defendant_account_balance = 50.00, 'Defendant account balance should match';
                ASSERT v_completed_date::date IS NULL, 'Completed date should be NULL';

                -- 2. Business unit information
                ASSERT v_business_unit_id = 9, 'Business unit ID should be 9';
                ASSERT v_business_unit_name = 'Cumbria', 'Business unit name should match';

                -- 3. Party and debtor information
                ASSERT v_party_id = 9, 'Party ID should match';
                ASSERT v_organisation = FALSE, 'Organisation flag should be FALSE';
                ASSERT v_organisation_name IS NULL, 'Organisation name should be NULL';
                ASSERT v_title = 'Mr', 'Title should match';
                ASSERT v_forenames = 'Fn9', 'Forenames should match';
                ASSERT v_surname = 'Surname9-DA5-Def', 'Surname should match';
                ASSERT v_birth_date::date = '1995-12-30'::date, 'Birth date should match';

                -- 4. Party parent/guardian information
                ASSERT v_parent_guardian_forenames = 'Fn10', 'Parent or Guardian forenames should match';
                ASSERT v_parent_guardian_surname = 'Surname10-DA5-PG', 'Parent or Guardian surname should match';

                -- 5. Address information
                ASSERT v_address_line_1 = 'address 9 DA5 Def', 'Address line 1 should match';
                ASSERT v_postcode = 'AB9 2CC', 'Postcode should match';
                ASSERT v_national_insurance_number = 'NI090909A', 'National Insurance number should match';

                -- 6. Alias information
                ASSERT v_alias1 = 'Fn9-Alias Surname9-DA5-Def-Alias-1', 'Alias name 1 with person alias with forename and surname name should match';
                ASSERT v_alias2 = 'Fn9-Alias Surname9-DA5-Def-Alias-2', 'Alias name 2 with person alias with forename and surname name should match';
                ASSERT v_alias3 = 'Fn9-Alias Surname9-DA5-Def-Alias-3', 'Alias name 3 with person alias with forename and surname name should match';
                ASSERT v_alias4 IS NULL, 'Alias name 4 should be NULL';
                ASSERT v_alias5 IS NULL, 'Alias name 5 should be NULL';

            WHEN 6 THEN
                -- Scenario 6: Expect completed defendant account with no parent/guardian, without an alias

                -- 1. Basic account information
                ASSERT v_defendant_account_id = 6, 'Defendant account ID should be 6';
                ASSERT v_account_number = '25000006H', 'Account number should match';
                ASSERT v_prosecutor_case_reference = 'PCR-6', 'Prosecutor case reference should match';
                ASSERT v_last_enforcement = 'DW', 'Last enforcement ID should match';
                ASSERT v_account_status = 'C', 'Account status should match';
                ASSERT v_defendant_account_balance = 0.00, 'Defendant account balance should match';
                ASSERT v_completed_date::date = '2025-07-02'::date, 'Completed date should match';
 
                -- 2. Business unit information
                ASSERT v_business_unit_id = 9, 'Business unit ID should be 9';
                ASSERT v_business_unit_name = 'Cumbria', 'Business unit name should match';

                -- 3. Party and debtor information
                ASSERT v_party_id = 11, 'Party ID should match';
                ASSERT v_organisation = FALSE, 'Organisation flag should be FALSE';
                ASSERT v_organisation_name IS NULL, 'Organisation name should be NULL';
                ASSERT v_title = 'Mr', 'Title should match';
                ASSERT v_forenames = 'Fn11', 'Forenames should match';
                ASSERT v_surname = 'Surname11-DA6-Def', 'Surname should match';
                ASSERT v_birth_date::date = '1992-03-17'::date, 'Birth date should match';

                -- 4. Party parent/guardian information
                ASSERT v_parent_guardian_forenames IS NULL, 'Parent or Guardian forenames should be NULL';
                ASSERT v_parent_guardian_surname IS NULL, 'Parent or Guardian surname should be NULL';

                -- 5. Address information
                ASSERT v_address_line_1 = 'address 11 DA6 Def', 'Address line 1 should match';
                ASSERT v_postcode = 'AB11 2CC', 'Postcode should match';
                ASSERT v_national_insurance_number = 'NI111111A', 'National Insurance number should match';

                -- 6. Alias information
                ASSERT v_alias1 IS NULL, 'Alias name 1 should be NULL';
                ASSERT v_alias2 IS NULL, 'Alias name 2 should be NULL';
                ASSERT v_alias3 IS NULL, 'Alias name 3 should be NULL';
                ASSERT v_alias4 IS NULL, 'Alias name 4 should be NULL';
                ASSERT v_alias5 IS NULL, 'Alias name 5 should be NULL';

            WHEN 7 THEN
                -- Scenario 7: Expect live defendant account with no parent/guardian, without an alias

                -- 1. Basic account information
                ASSERT v_defendant_account_id = 7, 'Defendant account ID should be 7';
                ASSERT v_account_number = '25000007D', 'Account number should match';
                ASSERT v_prosecutor_case_reference = 'PCR-7', 'Prosecutor case reference should match';
                ASSERT v_last_enforcement IS NULL, 'Last enforcement ID should be NULL';
                ASSERT v_account_status = 'L', 'Account status should match';
                ASSERT v_defendant_account_balance = 200.00, 'Defendant account balance should match';
                ASSERT v_completed_date::date IS NULL, 'Completed date should be NULL';
 
                -- 2. Business unit information
                ASSERT v_business_unit_id = 10, 'Business unit ID should be 10';
                ASSERT v_business_unit_name = 'Derbyshire', 'Business unit name should match';

                -- 3. Party and debtor information
                ASSERT v_party_id = 12, 'Party ID should match';
                ASSERT v_organisation = FALSE, 'Organisation flag should be FALSE';
                ASSERT v_organisation_name IS NULL, 'Organisation name should be NULL';
                ASSERT v_title = 'Ms', 'Title should match';
                ASSERT v_forenames = 'Fn12', 'Forenames should match';
                ASSERT v_surname = 'Surname12-DA7-Def', 'Surname should match';
                ASSERT v_birth_date::date = '1980-06-25'::date, 'Birth date should match';

                -- 4. Party parent/guardian information
                ASSERT v_parent_guardian_forenames IS NULL, 'Parent or Guardian forenames should be NULL';
                ASSERT v_parent_guardian_surname IS NULL, 'Parent or Guardian surname should be NULL';

                -- 5. Address information
                ASSERT v_address_line_1 = 'address 12 DA7 Def', 'Address line 1 should match';
                ASSERT v_postcode = 'AB12 2CC', 'Postcode should match';
                ASSERT v_national_insurance_number = 'NI121212A', 'National Insurance number should match';

                -- 6. Alias information
                ASSERT v_alias1 IS NULL, 'Alias name 1 should be NULL';
                ASSERT v_alias2 IS NULL, 'Alias name 2 should be NULL';
                ASSERT v_alias3 IS NULL, 'Alias name 3 should be NULL';
                ASSERT v_alias4 IS NULL, 'Alias name 4 should be NULL';
                ASSERT v_alias5 IS NULL, 'Alias name 5 should be NULL';

            WHEN 8 THEN
                -- Scenario 8: Expect bive defendant account for an organisation with no parent/guardian, with 1 alias

                -- 1. Basic account information
                ASSERT v_defendant_account_id = 8, 'Defendant account ID should be 8';
                ASSERT v_account_number = '25000008W', 'Account number should match';
                ASSERT v_prosecutor_case_reference = 'PCR-8', 'Prosecutor case reference should match';
                ASSERT v_last_enforcement IS NULL, 'Last enforcement ID should be NULL';
                ASSERT v_account_status = 'L', 'Account status should match';
                ASSERT v_defendant_account_balance = 60.00, 'Defendant account balance should match';
                ASSERT v_completed_date::date IS NULL, 'Completed date should be NULL';
 
                -- 2. Business unit information
                ASSERT v_business_unit_id = 10, 'Business unit ID should be 10';
                ASSERT v_business_unit_name = 'Derbyshire', 'Business unit name should match';

                -- 3. Party and debtor information
                ASSERT v_party_id = 13, 'Party ID should match';
                ASSERT v_organisation = TRUE, 'Organisation flag should be TRUE';
                ASSERT v_organisation_name = 'OrganisationName13-DA8', 'Organisation name should match';
                ASSERT v_title = 'Mr', 'Title should match';
                ASSERT v_forenames = 'OrgFn13', 'Forenames should match';
                ASSERT v_surname = 'OrgSurname13-DA8-Def', 'Surname should match';
                ASSERT v_birth_date::date = '1986-09-12'::date, 'Birth date should match';

                -- 4. Party parent/guardian information
                ASSERT v_parent_guardian_forenames IS NULL, 'Parent or Guardian forenames should be NULL';
                ASSERT v_parent_guardian_surname IS NULL, 'Parent or Guardian surname should be NULL';

                -- 5. Address information
                ASSERT v_address_line_1 = 'address 13 DA8 Def', 'Address line 1 should match';
                ASSERT v_postcode = 'AB13 2CC', 'Postcode should match';
                ASSERT v_national_insurance_number = 'NI131313A', 'National Insurance number should match';

                -- 6. Alias information
                ASSERT v_alias1 = 'OrganisationName13-DA8-Alias', 'Alias name 1 with organisation alias name should match';
                ASSERT v_alias2 IS NULL, 'Alias name 2 should be NULL';
                ASSERT v_alias3 IS NULL, 'Alias name 3 should be NULL';
                ASSERT v_alias4 IS NULL, 'Alias name 4 should be NULL';
                ASSERT v_alias5 IS NULL, 'Alias name 5 should be NULL';

            WHEN 9 THEN
                -- Scenario 9: Expect live defendant account with no parent/guardian, without an alias

                -- 1. Basic account information
                ASSERT v_defendant_account_id = 9, 'Defendant account ID should be 9';
                ASSERT v_account_number = '25000009S', 'Account number should match';
                ASSERT v_prosecutor_case_reference = 'PCR-9', 'Prosecutor case reference should match';
                ASSERT v_last_enforcement IS NULL, 'Last enforcement ID should be NULL';
                ASSERT v_account_status = 'L', 'Account status should match';
                ASSERT v_defendant_account_balance = 300.00, 'Defendant account balance should match';
                ASSERT v_completed_date::date IS NULL, 'Completed date should be NULL';
 
                -- 2. Business unit information
                ASSERT v_business_unit_id = 11, 'Business unit ID should be 11';
                ASSERT v_business_unit_name = 'Dorset', 'Business unit name should match';

                -- 3. Party and debtor information
                ASSERT v_party_id = 14, 'Party ID should match';
                ASSERT v_organisation = FALSE, 'Organisation flag should be FALSE';
                ASSERT v_organisation_name IS NULL, 'Organisation name should be NULL';
                ASSERT v_title = 'Miss', 'Title should match';
                ASSERT v_forenames = 'Fn14', 'Forenames should match';
                ASSERT v_surname = 'Surname14-DA9-Def', 'Surname should match';
                ASSERT v_birth_date::date = '1993-01-03'::date, 'Birth date should match';

                -- 4. Party parent/guardian information
                ASSERT v_parent_guardian_forenames IS NULL, 'Parent or Guardian forenames should be NULL';
                ASSERT v_parent_guardian_surname IS NULL, 'Parent or Guardian surname should be NULL';

                -- 5. Address information
                ASSERT v_address_line_1 = 'address 14 DA9 Def', 'Address line 1 should match';
                ASSERT v_postcode = 'AB14 2CC', 'Postcode should match';
                ASSERT v_national_insurance_number = 'NI141414A', 'National Insurance number should match';

                -- 6. Alias information
                ASSERT v_alias1 IS NULL, 'Alias name 1 should be NULL';
                ASSERT v_alias2 IS NULL, 'Alias name 2 should be NULL';
                ASSERT v_alias3 IS NULL, 'Alias name 3 should be NULL';
                ASSERT v_alias4 IS NULL, 'Alias name 4 should be NULL';
                ASSERT v_alias5 IS NULL, 'Alias name 5 should be NULL';

            WHEN 10 THEN
                -- Scenario 10: Expect completed defendant account with no parent/guardian, without an alias

                -- 1. Basic account information
                ASSERT v_defendant_account_id = 10, 'Defendant account ID should be 10';
                ASSERT v_account_number = '25000010H', 'Account number should match';
                ASSERT v_prosecutor_case_reference = 'PCR-10', 'Prosecutor case reference should match';
                ASSERT v_last_enforcement IS NULL, 'Last enforcement ID should be NULL';
                ASSERT v_account_status = 'C', 'Account status should match';
                ASSERT v_defendant_account_balance = 0.00, 'Defendant account balance should match';
                ASSERT v_completed_date::date = '2025-07-03'::date, 'Completed date should match';
 
                -- 2. Business unit information
                ASSERT v_business_unit_id = 11, 'Business unit ID should be 11';
                ASSERT v_business_unit_name = 'Dorset', 'Business unit name should match';

                -- 3. Party and debtor information
                ASSERT v_party_id = 15, 'Party ID should match';
                ASSERT v_organisation = FALSE, 'Organisation flag should be FALSE';
                ASSERT v_organisation_name IS NULL, 'Organisation name should be NULL';
                ASSERT v_title = 'Miss', 'Title should match';
                ASSERT v_forenames = 'Fn15', 'Forenames should match';
                ASSERT v_surname = 'Surname15-DA10-Def', 'Surname should match';
                ASSERT v_birth_date::date = '1993-01-04'::date, 'Birth date should match';

                -- 4. Party parent/guardian information
                ASSERT v_parent_guardian_forenames IS NULL, 'Parent or Guardian forenames should be NULL';
                ASSERT v_parent_guardian_surname IS NULL, 'Parent or Guardian surname should be NULL';

                -- 5. Address information
                ASSERT v_address_line_1 = 'address 15 DA10 Def', 'Address line 1 should match';
                ASSERT v_postcode = 'AB15 2CC', 'Postcode should match';
                ASSERT v_national_insurance_number = 'NI151515A', 'National Insurance number should match';

                -- 6. Alias information
                ASSERT v_alias1 IS NULL, 'Alias name 1 should be NULL';
                ASSERT v_alias2 IS NULL, 'Alias name 2 should be NULL';
                ASSERT v_alias3 IS NULL, 'Alias name 3 should be NULL';
                ASSERT v_alias4 IS NULL, 'Alias name 4 should be NULL';
                ASSERT v_alias5 IS NULL, 'Alias name 5 should be NULL';

        END CASE;
         
    END LOOP;   

    RAISE NOTICE 'ALL TESTS COMPLETED AND PASSED: All information retrieved and validated successfully for defendant accounts with all details';
END $$;

-- Clean up test data
DO $$
BEGIN
    RAISE NOTICE '=== Cleaning up test data ===';
    
    DELETE FROM impositions WHERE imposition_id BETWEEN 1 AND 20;
    DELETE FROM results WHERE result_id IN ('Res-1', 'Res-2', 'Res-3', 'Res-4', 'Res-5', 'Res-6', 'Res-7', 'Res-8', 'Res-9', 'Res-10');
    DELETE FROM creditor_transactions WHERE creditor_transaction_id BETWEEN 1 AND 30;
    DELETE FROM creditor_accounts WHERE creditor_account_id BETWEEN 1001 AND 1010;
    DELETE FROM aliases WHERE alias_id BETWEEN 1 AND 13;
    DELETE FROM defendant_account_parties WHERE defendant_account_party_id BETWEEN 1 AND 15;
    DELETE FROM defendant_accounts WHERE defendant_account_id BETWEEN 1 AND 10;
    DELETE FROM parties WHERE party_id BETWEEN 1 AND 30;
    
    RAISE NOTICE 'Test data cleanup completed';
END $$;

\timing