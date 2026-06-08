/**
* OPAL Program
*
* MODULE      : alter_v_major_creditor_account_header_for_awaiting_payout.sql
*
* DESCRIPTION : Amend v_major_creditor_account_header view, so that it now matches the design detailing how Awaiting Payout is determined.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    -----------------------------------------------------------------------------------------------------------------
* 02/06/2026    P Brumby    1.0         PO-5774 Amend how Awaiting Payout is determined in the database view v_major_creditor_account_header.
*
**/

--
-- Name: v_major_creditor_account_header; Type: VIEW; Schema: public; Owner: -
--

CREATE OR REPLACE VIEW v_major_creditor_account_header AS
 SELECT ca.creditor_account_id,
    ca.account_number AS creditor_account_number,
    ca.creditor_account_type,
    ca.version_number,
    bu.business_unit_id,
    bu.business_unit_name,
    mj.name,
    ( SELECT COALESCE(sum(ct.transaction_amount), (0)::numeric) AS "coalesce"
           FROM creditor_transactions ct
          WHERE ((ct.creditor_account_id = ca.creditor_account_id) AND (ct.posted_date > COALESCE(( SELECT max(ct_last.posted_date) AS max
                   FROM creditor_transactions ct_last
                  WHERE ((ct_last.creditor_account_id = ca.creditor_account_id) AND (ct_last.transaction_type IN ('BACS'::t_creditor_transaction_type_enum, 'CHEQUE'::t_creditor_transaction_type_enum)))), '-infinity'::timestamp without time zone)))) AS awaiting_payout
   FROM ((creditor_accounts ca
     JOIN business_units bu ON ((bu.business_unit_id = ca.business_unit_id)))
     JOIN major_creditors mj ON ((mj.major_creditor_id = ca.major_creditor_id)))
  WHERE (ca.creditor_account_type = 'MJ'::t_creditor_account_type_enum)
UNION
 SELECT ca.creditor_account_id,
    ca.account_number AS creditor_account_number,
    ca.creditor_account_type,
    ca.version_number,
    bu.business_unit_id,
    bu.business_unit_name,
    (ci.item_values ->> 'name'::text) AS name,
    ( SELECT COALESCE(sum(ct.transaction_amount), (0)::numeric) AS "coalesce"
           FROM creditor_transactions ct
          WHERE ((ct.creditor_account_id = ca.creditor_account_id) AND (ct.posted_date > COALESCE(( SELECT max(ct_last.posted_date) AS max
                   FROM creditor_transactions ct_last
                  WHERE ((ct_last.creditor_account_id = ca.creditor_account_id) AND (ct_last.transaction_type IN ('BACS'::t_creditor_transaction_type_enum, 'CHEQUE'::t_creditor_transaction_type_enum)))), '-infinity'::timestamp without time zone)))) AS awaiting_payout
   FROM ((creditor_accounts ca
     JOIN business_units bu ON ((bu.business_unit_id = ca.business_unit_id)))
     JOIN configuration_items ci ON (((bu.business_unit_id = ci.business_unit_id) AND ((ci.item_name)::text = 'CENTRAL_FUND_ACCOUNT'::text))))
  WHERE (ca.creditor_account_type = 'CF'::t_creditor_account_type_enum);

--
-- Name: VIEW v_major_creditor_account_header; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON VIEW v_major_creditor_account_header IS 'Retrieves major creditor account header information';
