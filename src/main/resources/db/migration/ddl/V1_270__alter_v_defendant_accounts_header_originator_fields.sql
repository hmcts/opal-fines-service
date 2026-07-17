/**
* OPAL Program
*
* MODULE      : alter_v_defendant_accounts_header_originator_fields.sql
*
* DESCRIPTION : Add originator fields to defendant account header view.
*
* VERSION HISTORY:
*
* Date          Author         Version     Nature of Change
* ----------    -----------    --------    ----------------------------------------------------------------------------
* 17/07/2026    C Cho          1.0         PO-2976 - Add originator_type and originator_name to defendant account header view.
*
**/

CREATE OR REPLACE VIEW public.v_defendant_accounts_header AS
 SELECT DISTINCT da.defendant_account_id,
    da.version_number,
    da.account_number,
    da.prosecutor_case_reference,
    da.account_status,
    da.account_type,
    da.amount_paid AS paid_written_off,
    da.account_balance,
    da.amount_imposed,
    public.f_arrears(da.defendant_account_id) AS arrears,
    dap_def.defendant_account_party_id,
        CASE
            WHEN (dap_def.debtor IS TRUE) THEN dap_def.association_type
            ELSE NULL::public.t_association_type_enum
        END AS debtor_type,
    p_def.party_id,
    p_def.title,
    p_def.forenames,
    p_def.surname,
    p_def.birth_date,
    p_def.organisation,
    p_def.organisation_name,
    bu.business_unit_id,
    bu.business_unit_name,
    bu.business_unit_code,
    fpo.ticket_number,
    dap_pg.defendant_account_party_id AS parent_guardian_account_party_id,
        CASE
            WHEN (dap_pg.defendant_account_id IS NOT NULL) THEN true
            ELSE false
        END AS has_parent_guardian,
        CASE
            WHEN (dap_pg.debtor IS TRUE) THEN dap_pg.association_type
            ELSE NULL::public.t_association_type_enum
        END AS parent_guardian_debtor_type,
        CASE
            WHEN (dt.defendant_account_id IS NOT NULL) THEN true
            ELSE false
        END AS has_consolidated_accounts,
    da.originator_type,
    da.originator_name
   FROM ((((((public.defendant_accounts da
     JOIN public.business_units bu ON ((da.business_unit_id = bu.business_unit_id)))
     JOIN public.defendant_account_parties dap_def ON (((da.defendant_account_id = dap_def.defendant_account_id) AND (dap_def.association_type = 'Defendant'::public.t_association_type_enum))))
     JOIN public.parties p_def ON ((dap_def.party_id = p_def.party_id)))
     LEFT JOIN public.fixed_penalty_offences fpo ON ((da.defendant_account_id = fpo.defendant_account_id)))
     LEFT JOIN public.defendant_account_parties dap_pg ON (((da.defendant_account_id = dap_pg.defendant_account_id) AND (dap_pg.association_type = 'Parent/Guardian'::public.t_association_type_enum))))
     LEFT JOIN ( SELECT DISTINCT defendant_transactions.defendant_account_id
           FROM public.defendant_transactions
          WHERE ((defendant_transactions.transaction_type = 'CONSOL'::public.t_defendant_transaction_type_enum) AND (defendant_transactions.associated_record_type = 'defendant_accounts'::public.t_associated_record_type_enum))) dt ON ((da.defendant_account_id = dt.defendant_account_id)));

COMMENT ON VIEW public.v_defendant_accounts_header IS 'Retrieves defendant account header information for the Defendant Header Summary';
