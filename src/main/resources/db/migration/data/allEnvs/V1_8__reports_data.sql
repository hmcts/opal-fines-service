
INSERT INTO public.reports (report_id, report_title, report_group, audited_report, report_parameters, supports_multi_bu, is_bespoke_journey, shown_as_worklist, retention_period, permission, supported_file_types, can_manually_create) VALUES
	('tfo_in_register', 'Transfer of Fine Orders In Register', 'Fines', true, NULL, false, false, false, NULL, NULL, NULL, false),
	('fp_register', 'Fixed Penalty Register', 'Fines', true, NULL, false, false, false, NULL, NULL, NULL, false),
	('list_amendments', 'List Amendments', 'Account Management', true, '[{ "name":"amendment_date", "prompt":"Amendment Date", "type":"date" }]', false, false, false, NULL, NULL, NULL, false),
	('list_extend_ttp', 'List of Extensions of Time to Pay', 'Account Management', true, NULL, false, false, false, NULL, NULL, NULL, false),
	('warrant_register', 'Warrant Register', 'Account Management', true, '[{ "name":"enforcer", "prompt":"Enforcer", "type":"enforcers", "min":1, "max":1 }]', false, false, false, NULL, NULL, NULL, false),
	('operational_report_enforcement', 'Operational report (by enforcement)', 'Operational Reports', false, NULL, false, false, false, 'P14D', NULL, '{CSV,PDF}', true),
	('operational_report_payment', 'Operational report (by payment)', 'Operational Reports', false, NULL, false, false, false, 'P14D', NULL, '{CSV,PDF}', true);

