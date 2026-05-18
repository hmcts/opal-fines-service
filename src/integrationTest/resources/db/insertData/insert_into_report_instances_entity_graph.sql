/**
* OPAL Program
*
* MODULE      : insert_into_report_instances_entity_graph.sql
*
* DESCRIPTION : Inserts rows of data into the REPORT_INSTANCES table for Integration Tests.
*
* VERSION HISTORY:
*
* Date        Author    Version  Nature of Change
* ----------  --------  -------  -----------------------------------------------------------------------------------------
* 11/05/2026  A REEVES  1.0      PO-2254 insert data to test report-instances API GET endpoint
**/
INSERT INTO reports (
    report_id,
    report_title,
    report_group,
    report_parameters,
    audited_report,
    supports_multi_bu,
    is_bespoke_journey,
    shown_as_worklist,
    retention_period,
    supported_file_types,
    can_manually_create
) VALUES
      (
          'full_report_single_bu',
          'Operational report: single BU',
          'Operational Reports',
          '{"param1":"string_value","param2":123}',
          FALSE,
          FALSE,
          FALSE,
          FALSE,
          '14',
          ARRAY['CSV', 'PDF','XML']::r_supported_file_type_enum[],
          TRUE
      ),
      (
          'full_report_multi_bus',
          'Operational report: multi BUs',
          'Operational Reports',
          '{"param1":"string_value","param2":123}',
          FALSE,
          TRUE,
          FALSE,
          FALSE,
          '14',
          ARRAY['CSV', 'PDF','XML']::r_supported_file_type_enum[],
          TRUE
      ),
      (
          'no_supported_filetypes',
          'Operational report: no supported filetypes',
          'Operational Reports',
          NULL,
          FALSE,
          FALSE,
          FALSE,
          FALSE,
          '14',
          ARRAY[]::r_supported_file_type_enum[],
          TRUE
      );


INSERT INTO business_units (
    business_unit_id,
    business_unit_name,
    business_unit_type,
    welsh_language
) VALUES (
          1,
          'BU no1',
          'Accounting Division',
          false
         ),(
          2,
          'BU no2 - Welsh',
          'Accounting Division',
          true
);

INSERT INTO report_instances (
    report_instance_id,
    report_id,
    business_unit_id,
    audit_sequence,
    created_timestamp,
    requested_by,
    requested_by_name,
    report_parameters,
    requested_at,
    generation_status,
    scheduled_deletion_timestamp,
    report_name,
    no_of_records,
    errors
) VALUES (
          123,
          'full_report_single_bu',
          ARRAY[1],
          1,
          '2026-05-11 17:30:00',
          1001,
          'Report Person',
          '{"param1":"A string parameter value", "param2":987}',
          '2026-05-10 17:30:00',
          'READY',
          '2026-05-25 17:30:00',
          null,
          10,
          null
    ), (
         234,
         'full_report_single_bu',
         ARRAY[1],
         1,
         null,
         1001,
         'Report Person',
         null,
         '2026-05-10 17:30:00',
         'REQUESTED',
         '2026-05-25 17:30:00',
         null,
         10,
         null
    ), (
         345,
         'full_report_multi_bus',
         ARRAY[1, 2],
         1,
         null,
         1001,
         'Report Person',
         '{"param1":"A string parameter value", "param2":987}',
         '2026-05-10 17:30:00',
         'IN_PROGRESS',
         '2026-05-25 17:30:00',
         'Report instance name override',
         0,
         null
    ), (
         400,
         'full_report_single_bu',
         ARRAY[1],
         1,
         null,
         1001,
         'Report Person',
         '{"param1":"A string parameter value", "param2":987}',
         '2026-05-10 17:30:00',
         'ERROR',
         '2026-05-25 17:30:00',
         null,
         0,
         '{"operationId":"ERROR-ID","error":"Generation failed"}'
    ), (
         567,
         'no_supported_filetypes',
         ARRAY[1],
         1,
         null,
         1001,
         'Report Person',
         null,
         '2026-05-10 17:30:00',
         'READY',
         '2026-05-25 17:30:00',
         null,
         0,
         null
    );