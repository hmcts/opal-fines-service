/**
* CGI OPAL Program
*
* MODULE      : results.sql
*
* DESCRIPTION : Creates the RESULTS table for the Fines model
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ----------------------------------------------------------------------------
* 03/12/2023    A Dennis    1.0         PO-127 Creates the RESULTS table for the Fines model
*
**/
CREATE TABLE results 
(
 result_id                         varchar(6)   not null
,result_title                      varchar(50)  not null
,result_title_cy                   varchar(50)  not null
,result_type                       varchar(10)  not null
,active                            boolean      not null
,imposition                        boolean      not null
,imposition_category               varchar(30)
,imposition_allocation_priority    smallint
,imposition_accruing               boolean
,imposition_creditor               varchar(10)
,enforcement                       boolean      not null
,enforcement_override              boolean      not null
,further_enforcement_warn          boolean      not null
,further_enforcement_disallow      boolean      not null
,enforcement_hold                  boolean      not null
,requires_enforcer                 boolean      not null
,generates_hearing                 boolean      not null
,collection_order                  boolean      not null
,extend_ttp_disallow               boolean      not null
,extend_ttp_preserve_last_enf      boolean      not null
,prevent_payment_card              boolean      not null
,lists_monies                      boolean      not null
,user_entries                      json
,CONSTRAINT result_id_pk PRIMARY KEY 
 (
   result_id	
 ) 
);

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
COMMENT ON COLUMN results.user_entries IS 'The parameters required to be input by the user when applyig this result. New field. Hard-coded in legacy GoB';
