/**
* CGI OPAL Program
*
* MODULE      : mis_debtors.sql
*
* DESCRIPTION : Creates the MIS_DEBTORS table for the Fines model
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ----------------------------------------------------------------------------
* 05/12/2023    A Dennis    1.0         PO-127 Creates the MIS_DEBTORS table for the Fines model
*
**/
CREATE TABLE mis_debtors 
(
 mis_debtor_id          bigint          not null
,business_unit_id       smallint        not null
,debtor_name            varchar(100)    not null
,account_category       varchar(1)
,arrears_category       varchar(1)
,account_number         varchar(1)      not null
,account_start_date     timestamp       not null
,terms_type             varchar(1)      not null
,instalment_amount      decimal(18,2)
,lump_sum               decimal(18,2)
,terms_date             timestamp
,days_in_jail           smallint
,date_last_movement     timestamp
,last_enforcement       varchar(6)
,arrears                decimal(18,2)
,amount_imposed         decimal(18,2)  not null
,amount_paid            decimal(18,2)  not null
,amount_outstanding     decimal(18,2)  not null
,CONSTRAINT mis_debtors_pk PRIMARY KEY 
 (
   mis_debtor_id	
 ) 
);

COMMENT ON COLUMN mis_debtors.mis_debtor_id IS 'Unique ID of this record';
COMMENT ON COLUMN mis_debtors.business_unit_id IS 'ID of the relating till to which this till belongs';
COMMENT ON COLUMN mis_debtors.debtor_name IS 'Debtor full name';
COMMENT ON COLUMN mis_debtors.account_category IS 'Account category';
COMMENT ON COLUMN mis_debtors.arrears_category IS 'Arrears category';
COMMENT ON COLUMN mis_debtors.account_number IS 'Account number';
COMMENT ON COLUMN mis_debtors.account_start_date IS 'Account start date';
COMMENT ON COLUMN mis_debtors.terms_type IS 'Terms type indicating if paying by a date or by instalments';
COMMENT ON COLUMN mis_debtors.instalment_amount IS 'Instalment amount if applicable';
COMMENT ON COLUMN mis_debtors.lump_sum IS 'Initial lump sum';
COMMENT ON COLUMN mis_debtors.terms_date IS 'Pay-by date or instalments start date';
COMMENT ON COLUMN mis_debtors.days_in_jail IS 'Days in jail if in default';
COMMENT ON COLUMN mis_debtors.date_last_movement IS 'Date of last movement on the account';
COMMENT ON COLUMN mis_debtors.last_enforcement IS 'Last enforcement action';
COMMENT ON COLUMN mis_debtors.arrears IS 'arrears';
COMMENT ON COLUMN mis_debtors.amount_imposed IS 'Amount imposed';
COMMENT ON COLUMN mis_debtors.amount_paid IS 'Amount paid so far';
COMMENT ON COLUMN mis_debtors.amount_outstanding IS 'Amount still to pay';
