/**
* CGI OPAL Program
*
* MODULE      : results.sql
*
* DESCRIPTION : Creates the RESULTS table for the Fines model as per script from Capita
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    -------------------------------------------------------------------------------
* 05/09/2024    A Dennis    1.0         PO-701 Creates the RESULTS table for the Fines model as per script from Capita
*
**/
CREATE TABLE results(
    result_id varchar(6) NOT NULL,
    result_title varchar(60) NOT NULL,
    result_title_cy varchar(60),
    result_type varchar(10) NOT NULL,
    active boolean NOT NULL,
    imposition boolean NOT NULL,
    imposition_category varchar(40),
    imposition_allocation_priority smallint,
    imposition_accruing boolean NOT NULL,
    imposition_creditor varchar(10),
    enforcement boolean NOT NULL,
    enforcement_override boolean NOT NULL,
    further_enforcement_warn boolean NOT NULL,
    further_enforcement_disallow boolean NOT NULL,
    enforcement_hold boolean NOT NULL,
    requires_enforcer boolean NOT NULL,
    generates_hearing boolean NOT NULL,
    generates_warrant boolean NOT NULL,
    collection_order boolean NOT NULL,
    extend_ttp_disallow boolean NOT NULL,
    extend_ttp_preserve_last_enf boolean NOT NULL,
    prevent_payment_card boolean NOT NULL,
    lists_monies boolean NOT NULL,
    result_parameters json);

ALTER TABLE results
    ADD CONSTRAINT results_id_pk 
        PRIMARY KEY (result_id),
    ADD CONSTRAINT results_result_type_cc
        CHECK (result_type IN ('Result','Action')),
    ADD CONSTRAINT results_imposition_category_cc
        CHECK (imposition_category IN ('Fines','Court Charge','Victim Surcharge','Witness Expenses '||CHR(38)||' Central Fund','Crown Prosecution Costs','Costs','Compensation','Legal Aid')),
    ADD CONSTRAINT results_imposition_creditor_cc
        CHECK (imposition_creditor IN ('CF','CPS','!CPS','Any'));

COMMENT ON COLUMN results.result_id IS 'Unique ID of this result';
COMMENT ON COLUMN results.result_title IS 'Result title';
COMMENT ON COLUMN results.result_title_cy IS 'Result title';
COMMENT ON COLUMN results.result_type IS 'Indicates if this is an acutal result or just an action. New field. Hard-coded in legacy GoB';
COMMENT ON COLUMN results.active IS 'Indicates if this result can be applied to new accounts. New field. Hard-coded in legacy GoB';
COMMENT ON COLUMN results.imposition IS 'Indicates if this result creates an imposition. New field. Hard-coded in legacy GoB';
COMMENT ON COLUMN results.imposition_category IS 'Financial category that monies for this imposition are reported under. New field. Hard-coded in legacy GoB';
COMMENT ON COLUMN results.imposition_allocation_priority IS 'Determines the order in which monies received are allocated to this impsition with respect to other impositions on the same account. New field. Hard-coded in legacy GoB';
COMMENT ON COLUMN results.imposition_accruing IS 'Indicates if this result is an imposition that accrues with time. New field. Hard-coded in legacy GoB';
COMMENT ON COLUMN results.imposition_creditor IS 'Indicates the creditor to be used for the imposition. Can be either Central (Central Fund Account), DPP (Crown Prosection Service), !DPP (a creditor other than DPP) or Any (any creditor). New field. Hard-coded in legacy GoB';
COMMENT ON COLUMN results.enforcement IS 'Indicates if this result is an enforcement result. New field. Hard-coded in legacy GoB';
COMMENT ON COLUMN results.enforcement_override IS 'Indicates if this result can be used as an enforcement override. New field. Hard-coded in legacy GoB';
COMMENT ON COLUMN results.further_enforcement_warn IS 'Indicates if a warning should be issued when applying an enforcement action while this is the last enforcement on the account. New field. Hard-coded in legacy GoB';
COMMENT ON COLUMN results.further_enforcement_disallow IS 'Indicates if the system should prevent applying an enforcement action while this is the last enforcement on the account. New field. Hard-coded in legacy GoB';
COMMENT ON COLUMN results.enforcement_hold IS 'Indicates if this action places a hold on enforcement which requires it to be explicity removed to continue further enforcement on the account. New field. Hard-coded in legacy GoB';
COMMENT ON COLUMN results.requires_enforcer IS 'Indicates if this result requires the user to also specify an enforcer. New field. Hard-coded in legacy GoB';
COMMENT ON COLUMN results.generates_hearing IS 'Indicates if applying this action should attempt to schedule an enforcement hearing for the account debtor. New field. Hard-coded in legacy GoB';
COMMENT ON COLUMN results.collection_order IS 'Indicates if this result is a collection order result. New field. Hard-coded in legacy GoB';
COMMENT ON COLUMN results.extend_ttp_disallow IS 'Indicates if this result should prevent extension of payment terms. New field. Hard-coded in legacy GoB';
COMMENT ON COLUMN results.extend_ttp_preserve_last_enf IS 'Indicates if this should be preserved as the last enforcement on an account after extending payment instead of clearing it. New field. Hard-coded in legacy GoB';
COMMENT ON COLUMN results.prevent_payment_card IS 'Indicates if this result should prevent requesting a payment card if it is the last enforcement on an account. New field. Hard-coded in legacy GoB';
COMMENT ON COLUMN results.lists_monies IS 'this result cause the account to be reported on List Monies Under Warrant if a payment is received while this is the last enforcement action on the account. New field. Hard-coded in legacy GoB'; 
COMMENT ON COLUMN results.result_parameters IS 'The parameters required to be input by the user when applyig this result. New field. Hard-coded in legacy GoB';
