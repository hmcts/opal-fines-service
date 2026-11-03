UPDATE public.reports
SET template_name = 'test-template-name.doc',
    report_parameters = '[{"name":"businessUnitIds","prompt":"Business units","type":"menu-autocomplete","mandatory":true},{"name":"reportType","prompt":"Report type","type":"text-60","mandatory":true,"min":1,"max":60},{"name":"isPaymentMade","prompt":"Payment made","type":"boolean","mandatory":true},{"name":"reportMode","prompt":"Report mode","type":"text-60","mandatory":true,"min":1,"max":60},{"name":"sinceDate","prompt":"Since date","type":"date","mandatory":true},{"name":"includeAdult","prompt":"Include adults","type":"boolean","mandatory":true},{"name":"includeYouth","prompt":"Include youths","type":"boolean","mandatory":true},{"name":"includeCompany","prompt":"Include companies","type":"boolean","mandatory":true}]'::json,
    can_manually_create = TRUE
WHERE report_id = 'operational_report_payment';
