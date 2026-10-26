-- PO-10764: expose the defendant's own organisation flag for search results and PDPO logging.
-- Append the column to preserve the existing view column order for CREATE OR REPLACE VIEW.
CREATE OR REPLACE VIEW public.v_search_minor_creditor_accounts AS
SELECT DISTINCT
    ca.creditor_account_id,
    ca.account_number,
    bu.business_unit_id,
    bu.business_unit_name,
    p_ca.party_id,
    p_ca.organisation,
    p_ca.organisation_name,
    p_ca.address_line_1,
    p_ca.postcode,
    p_ca.forenames,
    p_ca.surname,
    i.defendant_account_id,
    p_da.organisation_name AS defendant_organisation_name,
    p_da.forenames AS defendant_forenames,
    p_da.surname AS defendant_surname,
    (
        SELECT COALESCE(SUM(ct.transaction_amount), 0::NUMERIC(18, 2))
        FROM public.creditor_transactions ct
        WHERE ct.creditor_account_id = ca.creditor_account_id
          AND ct.transaction_type = 'PAYMNT'::public.t_creditor_transaction_type_enum
          AND ct.payment_processed = FALSE
    ) AS creditor_account_balance,
    p_da.organisation AS defendant_organisation
FROM public.creditor_accounts ca
JOIN public.business_units bu ON ca.business_unit_id = bu.business_unit_id
JOIN public.parties p_ca ON ca.minor_creditor_party_id = p_ca.party_id
LEFT JOIN public.impositions i ON ca.creditor_account_id = i.creditor_account_id
LEFT JOIN public.defendant_account_parties dap
    ON i.defendant_account_id = dap.defendant_account_id
    AND dap.association_type = 'Defendant'::public.t_association_type_enum
LEFT JOIN public.parties p_da ON dap.party_id = p_da.party_id;
