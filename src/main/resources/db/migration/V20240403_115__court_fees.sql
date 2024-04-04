/**
* CGI OPAL Program
*
* MODULE      : court_fees.sql
*
* DESCRIPTION : Creates the COURT_FEES table for the Fines model
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ----------------------------------------------------------------------------
* 03/04/2024    A Dennis    1.0         PO-200 Creates the COURT_FEES table for the Fines model
*
**/
CREATE TABLE court_fees 
(
 court_fee_id       bigint          not null
,business_unit_id   smallint        not null
,court_fee_code     varchar(10)     not null
,description        varchar(50)     not null
,amount             decimal(18,2)   not null
,stats_code         varchar(10)     not null
,CONSTRAINT court_fees_pk PRIMARY KEY 
 (
  court_fee_id	
 )  
);

ALTER TABLE court_fees
ADD CONSTRAINT cf_business_unit_id_fk FOREIGN KEY
(
  business_unit_id 
)
REFERENCES business_units
(
  business_unit_id 
);

COMMENT ON COLUMN court_fees.court_fee_id IS 'Unique ID of this record';
COMMENT ON COLUMN court_fees.business_unit_id IS 'ID of the relating business unit';
COMMENT ON COLUMN court_fees.court_fee_code IS 'Court fee code unique to the business unit';
COMMENT ON COLUMN court_fees.description IS 'Description of the service for which this court fee is payable';
COMMENT ON COLUMN court_fees.amount IS 'The amount payable for this service';
COMMENT ON COLUMN court_fees.stats_code IS 'The statistic code that this fee is counted under';
