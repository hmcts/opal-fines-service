/**
* CGI OPAL Program
*
* MODULE      : account_transfers.sql
*
* DESCRIPTION : Creates the ACCOUNT_TRANSFERS table for the Fines model
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ----------------------------------------------------------------------------
* 05/12/2023    A Dennis    1.0         PO-127 Creates the ACCOUNT_TRANSFERS table for the Fines model
*
**/
CREATE TABLE account_transfers 
(
 account_transfer_id     bigint        not null
,business_unit_id        smallint      not null
,defendant_account_id    bigint        not null
,initiated_date          timestamp
,initiated_by            varchar(20)
,printed_date            timestamp
,printed_by              varchar(20)
,document_instance_id    bigint
,destination_lja_id      smallint      not null
,reason                  varchar(100)
,reminder_date           timestamp
,CONSTRAINT account_transfers_pk PRIMARY KEY 
 (
   account_transfer_id	
 ) 
);

ALTER TABLE account_transfers
ADD CONSTRAINT at_defendant_account_id_fk FOREIGN KEY
(
  defendant_account_id 
)
REFERENCES defendant_accounts
(
  defendant_account_id 
);

ALTER TABLE account_transfers
ADD CONSTRAINT at_document_instance_id_fk FOREIGN KEY
(
  document_instance_id 
)
REFERENCES document_instances
(
  document_instance_id 
);

ALTER TABLE account_transfers
ADD CONSTRAINT at_local_justice_area_id_fk FOREIGN KEY
(
  destination_lja_id 
)
REFERENCES local_justice_areas
(
  local_justice_area_id 
);

COMMENT ON COLUMN account_transfers.account_transfer_id IS 'Unique ID of this record';
COMMENT ON COLUMN account_transfers.business_unit_id IS 'ID of the relating business unit';
COMMENT ON COLUMN account_transfers.defendant_account_id IS 'Account Number being transferred out';
COMMENT ON COLUMN account_transfers.initiated_date IS 'Date this transfer was initiated';
COMMENT ON COLUMN account_transfers.initiated_by IS 'User generating the transfer Out';
COMMENT ON COLUMN account_transfers.printed_date IS 'Date the TFO Enforcement Order was printed';
COMMENT ON COLUMN account_transfers.printed_by IS 'ID of the user that printed the TFO Enforcement Order';
COMMENT ON COLUMN account_transfers.document_instance_id IS 'ID of the TFFOUT Enforcement Order document';
COMMENT ON COLUMN account_transfers.destination_lja_id IS 'Destination LJA code';
COMMENT ON COLUMN account_transfers.reason IS 'Reason for the transfer out';
COMMENT ON COLUMN account_transfers.reminder_date IS 'Date that a reminder is sent to the destination court to acknowledge receipt';
