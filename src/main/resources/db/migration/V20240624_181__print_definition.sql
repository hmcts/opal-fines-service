/**
* CGI OPAL Program
*
* MODULE      : print_definition.sql
*
* DESCRIPTION : Creates the PRINT_DEFINITION table for the Print service
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ----------------------------------------------------------------------------
* 26/02/2024    A Dennis    1.0         PO-208 Creates the PRINT_DEFINITION table for the Print service
*
**/
CREATE TABLE print_definition 
(
 print_definition_id    bigint        not null 
,doc_type               varchar(50)   not null 
,doc_description        varchar(255)  not null 
,dest_main              varchar(20)   not null 
,dest_sec1              varchar(20) 
,dest_sec2              varchar(20) 
,format                 varchar(10)   not null 
,auto_mode              varchar(10) 
,expiry_duration        bigint        not null 
,system                 varchar(10)   not null 
,created_date           timestamp
,template_id            varchar(20) 
,address_val_element    varchar(50) 
,doc_doc_id             bigint 
,xslt                   text          not null 
,linked_areas           varchar(150) 
,template_file          varchar(150)    
,CONSTRAINT print_definition_id_pk PRIMARY KEY 
 (
   print_definition_id	
 ) 
);

COMMENT ON COLUMN print_definition.print_definition_id IS 'Primary key created from a sequence';
COMMENT ON COLUMN print_definition.doc_type IS 'The document type (e.g. courtreg_unv)';
COMMENT ON COLUMN print_definition.doc_description IS 'A descriptive name of the document';
COMMENT ON COLUMN print_definition.dest_main IS 'Is the main destination output';
COMMENT ON COLUMN print_definition.dest_sec1 IS 'Is a secondary output destination';
COMMENT ON COLUMN print_definition.dest_sec2 IS 'Is a secondary output destination';
COMMENT ON COLUMN print_definition.format IS 'Is the Apache FOP renderer to use (e.g. PDF, PS, XML)';
COMMENT ON COLUMN print_definition.auto_mode IS 'Used by the Portal for printing';
COMMENT ON COLUMN print_definition.expiry_duration IS 'The duration before the template expires';
COMMENT ON COLUMN print_definition.system IS 'Document relates to (e.g. Libra = L)';
COMMENT ON COLUMN print_definition.created_date IS 'The date / time the record is created';
COMMENT ON COLUMN print_definition.template_id IS 'The Libra Template Identifier';
COMMENT ON COLUMN print_definition.address_val_element IS 'The Libra Template Identifier';
COMMENT ON COLUMN print_definition.doc_doc_id IS 'FK to Libra Document Definition';
COMMENT ON COLUMN print_definition.xslt IS 'The XSLT template file(contents)';
COMMENT ON COLUMN print_definition.linked_areas IS 'Areas as a list of separated values linked to this definition';
COMMENT ON COLUMN print_definition.template_file IS 'The XSLT template file name';
