INSERT INTO document_instances (
    document_instance_id, document_id, business_unit_id, generated_date, generated_by,
    associated_record_type, associated_record_id, status, document_content
) VALUES (
    26220001, 'ABD', 78, TIMESTAMP '2026-01-06 09:00:00', 'hist-doc-user',
    'defendant_accounts', '262200', 'New', '<doc><account>262200</account></doc>'
) ON CONFLICT (document_instance_id) DO NOTHING;
