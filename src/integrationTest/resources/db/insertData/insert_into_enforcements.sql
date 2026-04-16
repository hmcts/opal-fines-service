/**
* OPAL Program
*
* MODULE      : insert_into_enforcements.sql
*
* DESCRIPTION : Insert test data for enforcements integration tests
*
* VERSION HISTORY:
*
* Date          Author       Version     Nature of Change
* ----------    --------     --------    ---------------------------------------------------------------------------------------------------------
* 10/04/2026    S WALL        1.0         PO2255 Insert test data for enforcements integration tests
*
**/

INSERT INTO public.enforcements(
    enforcement_id, defendant_account_id, posted_date, posted_by, result_id, reason, enforcer_id, jail_days, result_responses, warrant_reference, case_reference, hearing_date, hearing_court_id, posted_by_name, earliest_release_date, enforcement_account_type)
VALUES (
           91000000007001,
           99000000000001,
           now(),
           'L080JG',
           'REGF',
           'Test enforcement',
           null,
           null,
           null,
           '001/25/00001',
           null,
           null,
           null,
           'opal-test',
           null,
           null),
       (
           91000000007002,
           99000000000002,
           TIMESTAMP '2000-01-01',
           'L080JG',
           'REGF',
           'Test enforcement',
           null,
           null,
           null,
           '001/25/00001',
           null,
           null,
           null,
           'opal-test',
           null,
           null
    );