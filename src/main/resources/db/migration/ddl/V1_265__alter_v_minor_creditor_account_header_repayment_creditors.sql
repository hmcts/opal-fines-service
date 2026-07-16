/**
* OPAL Program
*
* MODULE      : alter_v_minor_creditor_account_header_repayment_creditors.sql
*
* DESCRIPTION : Update minor creditor account header values for repayment creditors.
*
* VERSION HISTORY:
*
* Date          Author         Version     Nature of Change
* ----------    -----------    --------    ----------------------------------------------------------------------------
* 14/07/2026    C Cho          1.0         PO-2975 - Update repayment creditor header financial values.
*
**/

CREATE OR REPLACE VIEW v_minor_creditor_account_header AS
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
    bu.welsh_language,
        CASE
            WHEN (imp.creditor_account_id IS NOT NULL) THEN true
            ELSE false
        END AS has_associated_defendant,
        CASE
            WHEN (t.repayment IS TRUE) THEN (0)::numeric
            ELSE COALESCE(( SELECT sum(i.imposed_amount) AS sum
                   FROM impositions i
                  WHERE (i.creditor_account_id = t.creditor_account_id)), (0)::numeric)
        END AS awarded,
        CASE
            WHEN (t.repayment IS TRUE) THEN COALESCE(( SELECT ct.transaction_amount
                   FROM creditor_transactions ct
                  WHERE (ct.creditor_account_id = t.creditor_account_id)
                  ORDER BY ct.posted_date ASC
                  LIMIT 1), (0)::numeric)
            ELSE COALESCE(( SELECT sum(ct.transaction_amount) AS sum
                   FROM creditor_transactions ct
                  WHERE ((ct.creditor_account_id = t.creditor_account_id) AND (ct.payment_processed IS TRUE))), (0)::numeric)
        END AS paid_out,
        CASE
            WHEN (t.repayment IS TRUE) THEN (0)::numeric
            ELSE COALESCE(( SELECT sum(ct.transaction_amount) AS sum
                   FROM creditor_transactions ct
                  WHERE ((ct.creditor_account_id = t.creditor_account_id) AND (ct.payment_processed IS FALSE))), (0)::numeric)
        END AS awaiting_payment,
        CASE
            WHEN (t.repayment IS TRUE) THEN (0)::numeric
            ELSE COALESCE(( SELECT (sum(i.imposed_amount) - sum(i.paid_amount))
                   FROM impositions i
                  WHERE (i.creditor_account_id = t.creditor_account_id)), (0)::numeric)
        END AS outstanding,
        bu.business_unit_code
   FROM (((( SELECT ca.creditor_account_id,
            ca.account_number AS creditor_account_number,
            ca.creditor_account_type,
            ca.version_number,
            ca.minor_creditor_party_id,
            ca.business_unit_id,
            ca.repayment
           FROM creditor_accounts ca
          WHERE (ca.creditor_account_type = 'MN'::t_creditor_account_type_enum)) t
     JOIN parties p ON ((t.minor_creditor_party_id = p.party_id)))
     JOIN business_units bu ON ((t.business_unit_id = bu.business_unit_id)))
     LEFT JOIN ( SELECT DISTINCT impositions.creditor_account_id
           FROM impositions) imp ON ((t.creditor_account_id = imp.creditor_account_id)));

COMMENT ON VIEW v_minor_creditor_account_header IS 'Retrieves minor creditor accounts header information';

