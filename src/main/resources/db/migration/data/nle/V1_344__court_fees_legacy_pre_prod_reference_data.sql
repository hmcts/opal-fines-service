/**
* CGI OPAL Program
*
* MODULE      : court_fees_legacy_pre_prod_reference_data.sql
*
* DESCRIPTION : Dev-only data for PO-10936 court fee scenarios.
*
* VERSION HISTORY:
*
* Date          Author         Version     Nature of Change
* ----------    -----------    --------    ------------------------------------------------------------------------------------
* 08/09/2026    P Brumby       1.0         PO-10936 Load sample court fee reference data from Legacy pre-prod environment
*
**/

-- ---------------------------------------------------------------------------
-- NLE-only seeded data for PO-10936.
--

-- Clear any existing PO-10936 data for repeatable builds.

DELETE FROM public.court_fees
 WHERE court_fee_id BETWEEN 99109360000001 AND 99109360000013;

-- ---------------------------------------------------------------------------
-- Court fees records.
-- ---------------------------------------------------------------------------

INSERT INTO public.court_fees (
    court_fee_id,
    business_unit_id,
    court_fee_code,
    description,
    amount,
    stats_code
) VALUES
    (99109360000001, 45, 'STECH', 'S Tech Fee', 150.00, '101'),
    (99109360000002, 45, 'CODE0', 'Fee Type 0', 12.00, '102'),
    (99109360000003, 45, 'CODE1', 'Fee Type 1', 200.00, '103'),
    (99109360000004, 45, 'CODE2', 'Fee Type 2', 100.50, '104'),
    (99109360000005, 45, 'CODE3', 'Fee Type 3', 250.00, '105'),
    (99109360000006, 36, 'STECH', 'S Tech Fee', 150.00, '1'),
    (99109360000007, 36, 'CODEE0', 'Fee Type 0', 12.00, '2'),
    (99109360000008, 36, 'CODE1', 'Fee Type 1', 200.00, '3'),
    (99109360000009, 36, 'CODE2', 'Fee Type 2', 100.50, '4'),
    (99109360000010, 73, 'APPWAR', 'Court Fee For APPWAR Test CLD-5836', 20.00, '101'),
    (99109360000011, 73, 'TET123', 'Test Court Fee Code', 30.00, '1'),
    (99109360000012, 73, 'TST123', 'TEST COURT FEE 2', 40.00, '2'),
    (99109360000013, 73, 'TEST01', 'Fee Code Descripion', 20.00, 'AA1234');
