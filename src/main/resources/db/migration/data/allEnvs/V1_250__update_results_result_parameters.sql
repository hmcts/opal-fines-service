/**
* OPAL Program
*
* MODULE      : update_results_result_parameters.sql
*
* DESCRIPTION : Update results.results_parameters for specified enforcement actions
*               so that enforcement processing aligns with updated configuration
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    -----------------------------------------------------------------------------------------------------------------
* 09/07/2026    C Cho       1.0         PO-2979 Update results_parameters (Enforcement Actions)
*
**/

UPDATE results
SET    result_parameters = '[{"name": "reason", "prompt": "Reason", "type": "text-60", "mandatory": true, "min": 1, "max": 24, "language_dependent": false, "hint": "For example, LIVE UC or AMEND UC +400.00"}, {"name": "enforcer", "prompt": "Enforcer", "type": "menu-autocomplete", "mandatory": true, "min": 1, "max": 1, "language_dependent": true, "apidata": "enforcers"}]'
WHERE  result_id = 'ABDC';

UPDATE results
SET    result_parameters = '[{"name": "reason", "prompt": "Reason", "type": "text-60", "mandatory": true, "min": 1, "max": 24, "language_dependent": false, "hint": "For example, include employer name"}, {"name": "normaldeductionrate", "prompt": "Normal deduction rate", "type": "decimal", "mandatory": true, "min": 1, "max": 99999, "language_dependent": false}, {"name": "protectedearningsrate", "prompt": "Protected earnings rate", "type": "decimal", "mandatory": true, "min": 1, "max": 99999, "language_dependent": false}, {"name": "payperiod", "prompt": "Pay period", "type": "menu-radio", "mandatory": true, "min": 1, "max": 1, "language_dependent": false, "options": ["Weekly; Fortnightly", "Monthly"]}]'
WHERE  result_id = 'AEO';

UPDATE results
SET    result_parameters = '[{"name": "reason", "prompt": "Reason", "type": "text-60", "mandatory": true, "min": 1, "max": 24, "language_dependent": false, "hint": "For example, include employer name"}]'
WHERE  result_id = 'AEOC';

UPDATE results
SET    result_parameters = '[{"name": "reason", "prompt": "Reason", "type": "text-60", "mandatory": true, "min": 1, "max": 24, "language_dependent": false}, {"name": "hearingdate", "prompt": "Adjourned to", "type": "date", "mandatory": true, "min": 1, "max": "No Limit", "language_dependent": false, "hint": "For example, 31/01/2023"}, {"name": "courtcode", "prompt": "Court code", "type": "menu-autocomplete", "mandatory": true, "min": 1, "max": 999, "language_dependent": false, "apidata": "courts"}, {"name": "enforcer", "prompt": "Enforcer", "type": "menu-autocomplete", "mandatory": true, "min": 1, "max": 1, "language_dependent": true, "apidata": "enforcers"}]'
WHERE  result_id = 'BWTD';

UPDATE results
SET    result_parameters = '[{"name": "reason", "prompt": "Reason", "type": "text-60", "mandatory": true, "min": 1, "max": 24, "language_dependent": false}, {"name": "baildirection", "prompt": "Date and time (Bail direction)", "type": "text-1000", "mandatory": false, "min": 0, "max": 500, "language_dependent": false}, {"name": "courtdetails", "prompt": "Court details", "type": "text-100", "mandatory": true, "min": 1, "max": 100, "language_dependent": false}, {"name": "enforcer", "prompt": "Enforcer", "type": "menu-autocomplete", "mandatory": true, "min": 1, "max": 1, "language_dependent": true, "apidata": "enforcers"}]'
WHERE  result_id = 'BWTU';

UPDATE results
SET    result_parameters = '[{"name": "reason", "prompt": "Reason", "type": "text-60", "mandatory": true, "min": 1, "max": 24, "language_dependent": false, "hint": "For example, vehicle details held on account"}, {"name": "effectivedate", "prompt": "Effective from date", "type": "date", "mandatory": false, "min": 0, "max": "No Limit", "language_dependent": true, "hint": "For example, 31/01/2023"}, {"name": "enforcer", "prompt": "Enforcer", "type": "menu-autocomplete", "mandatory": true, "min": 1, "max": 1, "language_dependent": true, "apidata": "enforcers"}]'
WHERE  result_id = 'CLAMPO';

UPDATE results
SET    result_parameters = '[{"name": "reason", "prompt": "Reason", "type": "text-60", "mandatory": true, "min": 1, "max": 24, "language_dependent": false, "hint": "For example, granted by court"}, {"name": "collectiontype", "prompt": "Collection type", "type": "menu-radio", "mandatory": true, "min": 1, "max": 1, "language_dependent": false, "options": ["Wages; Benefits; Not applicable"]}, {"name": "reserveterms", "prompt": "Reserve terms (if applicable)", "type": "text-60", "mandatory": false, "min": 0, "max": 55, "language_dependent": true, "hint": "For example, to pay within 14 days"}]'
WHERE  result_id = 'COLLO';

UPDATE results
SET    result_parameters = '[{"name": "reason", "prompt": "Reason", "type": "text-60", "mandatory": true, "min": 1, "max": 24, "language_dependent": false}]'
WHERE  result_id = 'CONF';

UPDATE results
SET    result_parameters = '[{"name": "reason", "prompt": "Reason", "type": "text-60", "mandatory": true, "min": 1, "max": 24, "language_dependent": false}, {"name": "prisonid", "prompt": "Prison", "type": "menu-autocomplete", "mandatory": false, "min": 0, "max": 1, "language_dependent": false, "hint": "Enter name of prison", "apidata": "prisons"}, {"name": "commitaldays", "prompt": "Number of days of committal", "type": "integer", "mandatory": true, "min": 1, "max": 99999, "language_dependent": false, "hint": "Enter days, for example 90 not 3 months"}, {"name": "consecconcurrent", "prompt": "Select how it will be served", "type": "menu-radio", "mandatory": true, "min": 1, "max": 1, "language_dependent": false, "options": ["Consecutive; Concurrent"]}, {"name": "detailsconsecutive", "prompt": "Details", "type": "text-100", "mandatory": false, "min": 0, "max": 100, "language_dependent": false}, {"name": "processserver", "prompt": "Custodian or process server", "type": "text-60", "mandatory": false, "min": 0, "max": 50, "language_dependent": false}, {"name": "basisofcommital", "prompt": "Basis of committal", "type": "text-1000", "mandatory": true, "min": 1, "max": 1000, "language_dependent": false}, {"name": "reasonnoaltused", "prompt": "Reason no alternative used", "type": "text-1000", "mandatory": false, "min": 0, "max": 1000, "language_dependent": false}, {"name": "enforcer", "prompt": "Enforcer", "type": "menu-autocomplete", "mandatory": true, "min": 1, "max": 1, "language_dependent": true, "apidata": "enforcers"}]'
WHERE  result_id = 'CW';

UPDATE results
SET    result_parameters = '[{"name": "reason", "prompt": "Reason", "type": "text-60", "mandatory": true, "min": 1, "max": 24, "language_dependent": false, "hint": "For example, failed to comply with suspended committal"}, {"name": "hearingdate", "prompt": "Date of hearing", "type": "date", "mandatory": true, "min": 1, "max": "No Limit", "language_dependent": false, "hint": "For example, 31/01/2023"}, {"name": "courtcode", "prompt": "Court code", "type": "menu-autocomplete", "mandatory": true, "min": 1, "max": 999, "language_dependent": false, "apidata": "courts"}]'
WHERE  result_id = 'CWN';

UPDATE results
SET    result_parameters = '[{"name": "reason", "prompt": "Reason", "type": "text-60", "mandatory": true, "min": 1, "max": 24, "language_dependent": false}, {"name": "enforcer", "prompt": "Enforcer", "type": "menu-autocomplete", "mandatory": true, "min": 1, "max": 1, "language_dependent": true, "apidata": "enforcers"}]'
WHERE  result_id = 'DW';

UPDATE results
SET    result_parameters = '[{"name": "reason", "prompt": "Reason", "type": "text-60", "mandatory": true, "min": 1, "max": 24, "language_dependent": false}]'
WHERE  result_id = 'FSN';

UPDATE results
SET    result_parameters = '[{"name": "reason", "prompt": "Reason", "type": "text-60", "mandatory": true, "min": 1, "max": 24, "language_dependent": false}]'
WHERE  result_id = 'HTT';

UPDATE results
SET    result_parameters = '[{"name": "reason", "prompt": "Reason", "type": "text-60", "mandatory": true, "min": 1, "max": 24, "language_dependent": false}]'
WHERE  result_id = 'INTL';

UPDATE results
SET    result_parameters = '[{"name": "reason", "prompt": "Reason", "type": "text-60", "mandatory": true, "min": 1, "max": 24, "language_dependent": false, "hint": "For example, order made by the court"}, {"name": "supervisor", "prompt": "Supervisor", "type": "menu-radio", "mandatory": true, "min": 1, "max": 1, "language_dependent": false, "options": ["Probation officer; An officer of a provider of probation services; A member of the youth offending team; Other"]}, {"name": "detailsifother", "prompt": "Details (if other)", "type": "text-100", "mandatory": false, "min": 0, "max": 100, "language_dependent": false}, {"name": "prisondetention", "prompt": "Select type of supervision ", "type": "menu-radio", "mandatory": true, "min": 1, "max": 1, "language_dependent": false, "options": ["Prison; Detention"]}]'
WHERE  result_id = 'MPSO';

UPDATE results
SET    result_parameters = '[{"name": "reason", "prompt": "Reason", "type": "text-60", "mandatory": true, "min": 1, "max": 24, "language_dependent": false}]'
WHERE  result_id = 'NAP';

UPDATE results
SET    result_parameters = '[{"name": "reason", "prompt": "Reason", "type": "text-60", "mandatory": true, "min": 1, "max": 24, "language_dependent": false}, {"name": "hearingdate", "prompt": "Date of hearing", "type": "date", "mandatory": false, "min": 0, "max": "No Limit", "language_dependent": false}, {"name": "courtcode", "prompt": "Court code", "type": "integer", "mandatory": true, "min": 1, "max": 999, "language_dependent": false}]'
WHERE  result_id = 'NAWT';

UPDATE results
SET    result_parameters = '[{"name": "reason", "prompt": "Reason", "type": "text-60", "mandatory": true, "min": 1, "max": 24, "language_dependent": false}, {"name": "courtdetails", "prompt": "Court details", "type": "text-100", "mandatory": false, "min": 0, "max": 100, "language_dependent": false}, {"name": "enforcer", "prompt": "Enforcer", "type": "menu-autocomplete", "mandatory": true, "min": 1, "max": 1, "language_dependent": true, "apidata": "enforcers"}]'
WHERE  result_id = 'NBWT';

UPDATE results
SET    result_parameters = '[{"name": "reason", "prompt": "Reason", "type": "text-60", "mandatory": true, "min": 1, "max": 24, "language_dependent": false}]'
WHERE  result_id = 'NOENF';

UPDATE results
SET    result_parameters = '[{"name": "earliestreleasedate", "prompt": "Earliest release date", "type": "date", "mandatory": false, "min": 0, "max": "No Limit", "language_dependent": false, "hint": "For example, 31/01/2023"}, {"name": "prisonandprisonnumber", "prompt": "Prisoner number", "type": "text-60", "mandatory": false, "min": 0, "max": 24, "language_dependent": false, "hint": "For example, A1234AA"}]'
WHERE  result_id = 'PRIS';

UPDATE results
SET    result_parameters = '[{"name": "reason", "prompt": "Reason", "type": "text-60", "mandatory": true, "min": 1, "max": 24, "language_dependent": false}]'
WHERE  result_id = 'REGF';

UPDATE results
SET    result_parameters = '[{"name": "reason", "prompt": "Reason", "type": "text-60", "mandatory": true, "min": 1, "max": 24, "language_dependent": false}]'
WHERE  result_id = 'REM';

UPDATE results
SET    result_parameters = '[{"name": "reason", "prompt": "Reason", "type": "text-60", "mandatory": true, "min": 1, "max": 24, "language_dependent": false}, {"name": "hearingdate", "prompt": "Date of hearing", "type": "date", "mandatory": false, "min": 0, "max": "No Limit", "language_dependent": false, "hint": "For example, 31/01/2023"}, {"name": "courtcode", "prompt": "Court code", "type": "menu-autocomplete", "mandatory": true, "min": 1, "max": 999, "language_dependent": false, "apidata": "courts"}]'
WHERE  result_id = 'REW';

UPDATE results
SET    result_parameters = '[{"name": "reason", "prompt": "Reason", "type": "text-60", "mandatory": true, "min": 1, "max": 24, "language_dependent": false}, {"name": "timeofrelease", "prompt": "Time to be released", "type": "text-60", "mandatory": false, "min": 0, "max": 10, "language_dependent": false, "hint": "For example, 02.30 or 14.30 and midnight is 00:00"}, {"name": "enforcer", "prompt": "Enforcer", "type": "menu-autocomplete", "mandatory": true, "min": 1, "max": 1, "language_dependent": true, "apidata": "enforcers"}]'
WHERE  result_id = 'S136';

UPDATE results
SET    result_parameters = '[{"name": "reason", "prompt": "Reason", "type": "text-60", "mandatory": true, "min": 1, "max": 24, "language_dependent": false, "hint": "For example, culpable neglect or wilful refusal"}, {"name": "datesuspcom", "prompt": "Date of suspended committal", "type": "date", "mandatory": false, "min": 0, "max": "No Limit", "language_dependent": false, "hint": "For example, 31/01/2023"}, {"name": "previouspaymentterms", "prompt": "Previous payment terms", "type": "text-100", "mandatory": false, "min": 0, "max": 100, "language_dependent": false}, {"name": "replydate", "prompt": "Date reply to be received by", "type": "date", "mandatory": false, "min": 0, "max": "No Limit", "language_dependent": false, "hint": "For example, 31/01/2023"}]'
WHERE  result_id = 'S18';

UPDATE results
SET    result_parameters = '[{"name": "reason", "prompt": "Reason", "type": "text-60", "mandatory": true, "min": 1, "max": 24, "language_dependent": false, "hint": "For example, culpable neglect or wilful refusal"}, {"name": "daysindefault", "prompt": "Days in default", "type": "integer", "mandatory": true, "min": 1, "max": 99999, "language_dependent": false, "hint": "Enter in days, for example 90 rather than 3 months"}, {"name": "totalamount", "prompt": "Total amount to pay", "type": "decimal", "mandatory": false, "min": 0, "max": 99999, "language_dependent": false}, {"name": "paymentterms", "prompt": "Payment terms", "type": "text-1000", "mandatory": true, "min": 1, "max": 200, "language_dependent": true}]'
WHERE  result_id = 'SC';

UPDATE results
SET    result_parameters = '[{"name": "reason", "prompt": "Reason", "type": "text-60", "mandatory": true, "min": 1, "max": 24, "language_dependent": false}, {"name": "hearingdate", "prompt": "Date of hearing", "type": "date", "mandatory": false, "min": 0, "max": "No Limit", "language_dependent": false, "hint": "For example, 31/01/2023"}, {"name": "courtcode", "prompt": "Court code", "type": "menu-autocomplete", "mandatory": true, "min": 1, "max": 999, "language_dependent": false, "apidata": "courts"}, {"name": "prisondetention", "prompt": "Select a supervision order", "type": "menu-radio", "mandatory": true, "min": 1, "max": 1, "language_dependent": false, "options": ["Prison; Detention"]}]'
WHERE  result_id = 'SUMM';

UPDATE results
SET    result_parameters = '[{"name": "reason", "prompt": "Reason", "type": "text-60", "mandatory": true, "min": 1, "max": 24, "language_dependent": false}, {"name": "noofhours", "prompt": "Number of hours", "type": "integer", "mandatory": true, "min": 1, "max": 999, "language_dependent": false, "hint": "As ordered in court. For example, 14"}, {"name": "consecconcurrent", "prompt": "Select how it will be served", "type": "menu-checkbox", "mandatory": false, "min": 0, "max": 2, "language_dependent": true, "hint": "As ordered in court", "options": ["Consecutive; Concurrent"]}, {"name": "completiondate", "prompt": "Completion date", "type": "date", "mandatory": false, "min": 0, "max": "No Limit", "language_dependent": false, "hint": "For example, 31/01/2023"}, {"name": "supervisor", "prompt": "Select supervisor", "type": "menu-radio", "mandatory": true, "min": 1, "max": 1, "language_dependent": true, "options": ["A probation officer; An officer of a provider of probation services; Other"]}, {"name": "detailsifother", "prompt": "Details (if other)", "type": "text-100", "mandatory": false, "min": 0, "max": 100, "language_dependent": false}, {"name": "supervisingcourt", "prompt": "Name of supervising court", "type": "text-60", "mandatory": false, "min": 0, "max": 60, "language_dependent": true}]'
WHERE  result_id = 'UPWO';

UPDATE results
SET    result_parameters = '[{"name": "reason", "prompt": "Reason", "type": "text-60", "mandatory": true, "min": 1, "max": 24, "language_dependent": false}]'
WHERE  result_id = 'WDN';