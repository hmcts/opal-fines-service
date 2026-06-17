/**
* OPAL Program
*
* MODULE      : alter_v_minor_creditor_account_header_add_business_unit_code.sql
*
* DESCRIPTION : Amend v_minor_creditor_account_header so header summary consumers can access the business unit code.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    -----------------------------------------------------------------------------------------------------------------
* 17/06/2026    OpenAI      1.0         PO-7409 Add business_unit_code to the v_minor_creditor_account_header view.
*
**/

--
-- Name: v_minor_creditor_account_header; Type: VIEW; Schema: public; Owner: -
--

CREATE OR REPLACE VIEW public.v_minor_creditor_account_header AS
 SELECT DISTINCT t.creditor_account_id,
    t.creditor_account_number,
    t.creditor_account_type,
    t.version_number,
    p.party_id,
    p.title,
    p.forenames,
    p.surname,
    p.organisation,
    p.organisation_name,
    bu.business_unit_id,
    bu.business_unit_name,
    bu.business_unit_code,
    bu.welsh_language,
        CASE
            WHEN (imp.creditor_account_id IS NOT NULL) THEN true
            ELSE false
        END AS has_associated_defendant,
    COALESCE(( SELECT sum(i.imposed_amount) AS sum
           FROM public.impositions i
          WHERE (i.creditor_account_id = t.creditor_account_id)), (0)::numeric) AS awarded,
    COALESCE(( SELECT sum(ct.transaction_amount) AS sum
           FROM public.creditor_transactions ct
          WHERE ((ct.creditor_account_id = t.creditor_account_id) AND (ct.payment_processed IS TRUE))),
        (0)::numeric) AS paid_out,
    COALESCE(( SELECT sum(ct.transaction_amount) AS sum
           FROM public.creditor_transactions ct
          WHERE ((ct.creditor_account_id = t.creditor_account_id) AND (ct.payment_processed IS FALSE))),
        (0)::numeric) AS awaiting_payment,
    COALESCE(( SELECT (sum(i.imposed_amount) - sum(i.paid_amount))
           FROM public.impositions i
          WHERE (i.creditor_account_id = t.creditor_account_id)), (0)::numeric) AS outstanding
   FROM (((( SELECT ca.creditor_account_id,
            ca.account_number AS creditor_account_number,
            ca.creditor_account_type,
            ca.version_number,
            ca.minor_creditor_party_id,
            ca.business_unit_id
           FROM public.creditor_accounts ca
          WHERE (ca.creditor_account_type = 'MN'::public.t_creditor_account_type_enum)) t
     JOIN public.parties p ON ((t.minor_creditor_party_id = p.party_id)))
     JOIN public.business_units bu ON ((t.business_unit_id = bu.business_unit_id)))
     LEFT JOIN ( SELECT DISTINCT i.creditor_account_id
           FROM public.impositions i
          WHERE (i.creditor_account_id IS NOT NULL)) imp ON ((t.creditor_account_id = imp.creditor_account_id)));

--
-- Name: VIEW v_minor_creditor_account_header; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON VIEW public.v_minor_creditor_account_header IS 'Retrieves minor creditor accounts header information';
