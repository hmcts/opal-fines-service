/**
* OPAL Program
*
* MODULE      : insert_into_reports.sql
*
* DESCRIPTION : Insert test data for reports table for integration tests
*
* VERSION HISTORY:
*
* Date          Author            Version     Nature of Change
* ----------    --------          --------    --------------------------------------------------------------------------------------------------------
* 24/04/2026    Krishna Sapkota   1.0         PO-2250 Insert test data for reports integration tests
*
**/


-- Insert test report data for integration tests
INSERT INTO reports (report_id,
                     report_title,
                     report_group,
                     report_parameters,
                     audited_report,
                     supports_multi_bu,
                     is_bespoke_journey,
                     shown_as_worklist,
                     retention_period,
                     permission,
                     supported_file_types,
                     can_manually_create)
VALUES ('operational_report_enforcement',
        'Operational report (by enforcement)',
        'Operational Reports',
        NULL,
        FALSE,
        FALSE,
        FALSE,
        FALSE,
        'P14D',
        NULL,
        ARRAY ['CSV', 'PDF']::r_supported_file_type_enum[],
        TRUE),
       ('operational_report_payment',
        'Operational report (by payment)',
        'Operational Reports',
        NULL,
        FALSE,
        FALSE,
        FALSE,
        FALSE,
        'P14D',
        NULL,
        ARRAY ['CSV', 'PDF']::r_supported_file_type_enum[],
        TRUE),
       ('test_report_with_params',
        'Test Report with Parameters',
        'Test Reports',
        '{
          "fromDate": "date",
          "toDate": "date",
          "status": "string"
        }',
        TRUE,
        TRUE,
        TRUE,
        TRUE,
        'P30D',
        'REPORTS_VIEW',
        ARRAY ['CSV', 'PDF', 'XML']::r_supported_file_type_enum[],
        FALSE)
ON CONFLICT (report_id) DO UPDATE SET report_title         = EXCLUDED.report_title,
                                      report_group         = EXCLUDED.report_group,
                                      report_parameters    = EXCLUDED.report_parameters,
                                      audited_report       = EXCLUDED.audited_report,
                                      supports_multi_bu    = EXCLUDED.supports_multi_bu,
                                      is_bespoke_journey   = EXCLUDED.is_bespoke_journey,
                                      shown_as_worklist    = EXCLUDED.shown_as_worklist,
                                      retention_period     = EXCLUDED.retention_period,
                                      permission           = EXCLUDED.permission,
                                      supported_file_types = EXCLUDED.supported_file_types,
                                      can_manually_create  = EXCLUDED.can_manually_create;


