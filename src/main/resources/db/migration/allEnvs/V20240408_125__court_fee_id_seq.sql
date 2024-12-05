/**
* OPAL Program
*
* MODULE      : court_fee_id_seq.sql
*
* DESCRIPTION : Creates the Sequence to be used to generate the Primary key for the table COURT_FEES. 
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ---------------------------------------------------------------------------------------------------------
* 08/04/2024    A Dennis    1.0         PO-264 Creates the Sequence to be used to generate the Primary key for the table COURT_FEES
*
**/
CREATE SEQUENCE IF NOT EXISTS court_fee_id_seq INCREMENT 1 MINVALUE 1 NO MAXVALUE START WITH 1 CACHE 20 OWNED BY court_fees.court_fee_id;
