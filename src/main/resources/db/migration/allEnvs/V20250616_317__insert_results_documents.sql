/**
* OPAL Program
*
* MODULE      : insert_results_documents.sql
*
* DESCRIPTION : Insert data into RESULTS_DOCUMENTS table
*
* VERSION HISTORY:
*
* Date          Author       Version     Nature of Change
* ----------    --------     --------    ---------------------------------------------------------------------------------------------------------
* 16/06/2025    C Cho        1.0         PO-1880 Insert reference data for results documents
*
**/

DELETE FROM result_documents;

INSERT INTO result_documents (result_id,document_id,cy_document_id)
VALUES ('ABDC','ABDD','CY_ABDD');
INSERT INTO result_documents (result_id,document_id,cy_document_id)
VALUES ('ABDC','ABD',NULL);
INSERT INTO result_documents (result_id,document_id,cy_document_id)
VALUES ('AEO','AEOLD',NULL);
INSERT INTO result_documents (result_id,document_id,cy_document_id)
VALUES ('AEOC','AEOFD','CY_AEOFD');
INSERT INTO result_documents (result_id,document_id,cy_document_id)
VALUES ('AEOC','AEOF',NULL);
INSERT INTO result_documents (result_id,document_id,cy_document_id)
VALUES ('BWTD','ENFWTB',NULL);
INSERT INTO result_documents (result_id,document_id,cy_document_id)
VALUES ('BWTD','BACKLET','CY_BACKLET');
INSERT INTO result_documents (result_id,document_id,cy_document_id)
VALUES ('BWTU','ENFWTU',NULL);
INSERT INTO result_documents (result_id,document_id,cy_document_id)
VALUES ('BWTU','BACKLET','CY_BACKLET');
INSERT INTO result_documents (result_id,document_id,cy_document_id)
VALUES ('CERTBR','CERTBR',NULL);
INSERT INTO result_documents (result_id,document_id,cy_document_id)
VALUES ('CLAMPO','CLAMPO',NULL);
INSERT INTO result_documents (result_id,document_id,cy_document_id)
VALUES ('CLAMPO','CLAMPOC',NULL);
INSERT INTO result_documents (result_id,document_id,cy_document_id)
VALUES ('COLLO','FCOLLO','CY_FCOLLO');
INSERT INTO result_documents (result_id,document_id,cy_document_id)
VALUES ('CONSOL','CONSOL','CY_CONSOL');
INSERT INTO result_documents (result_id,document_id,cy_document_id)
VALUES ('CREDRM','UNPRELET',NULL);
INSERT INTO result_documents (result_id,document_id,cy_document_id)
VALUES ('CTPAYF','CTPAYSF',NULL);
INSERT INTO result_documents (result_id,document_id,cy_document_id)
VALUES ('CW','COMMWT',NULL);
INSERT INTO result_documents (result_id,document_id,cy_document_id)
VALUES ('CWN','ESCISS','CY_ESCISS');
INSERT INTO result_documents (result_id,document_id,cy_document_id)
VALUES ('DW','DWAR',NULL);
INSERT INTO result_documents (result_id,document_id,cy_document_id)
VALUES ('DW','BACKLETDW','CY_BACKLETDW');
INSERT INTO result_documents (result_id,document_id,cy_document_id)
VALUES ('FCOMP','COMPLETT','CY_COMPLETT');
INSERT INTO result_documents (result_id,document_id,cy_document_id)
VALUES ('FSN','FFSN','CY_FFSN');
INSERT INTO result_documents (result_id,document_id,cy_document_id)
VALUES ('MPSO','FSO',NULL);
INSERT INTO result_documents (result_id,document_id,cy_document_id)
VALUES ('NBWT','ENFWTN',NULL);
INSERT INTO result_documents (result_id,document_id,cy_document_id)
VALUES ('NBWT','BACKLET','CY_BACKLET');
INSERT INTO result_documents (result_id,document_id,cy_document_id)
VALUES ('REGF','FREGNR',NULL);
INSERT INTO result_documents (result_id,document_id,cy_document_id)
VALUES ('REM','FINREMB','CY_FINREMB');
INSERT INTO result_documents (result_id,document_id,cy_document_id)
VALUES ('S136','COMMWTP',NULL);
INSERT INTO result_documents (result_id,document_id,cy_document_id)
VALUES ('S18','ESCFNOT',NULL);
INSERT INTO result_documents (result_id,document_id,cy_document_id)
VALUES ('SC','SUSNOT','CY_SUSNOT');
INSERT INTO result_documents (result_id,document_id,cy_document_id)
VALUES ('SUMM','ENFSUM','CY_ENFSUM');
INSERT INTO result_documents (result_id,document_id,cy_document_id)
VALUES ('SUMA','ENFSUM','CY_ENFSUM');
INSERT INTO result_documents (result_id,document_id,cy_document_id)
VALUES ('TFOFP','FINOR','CY_FINOR');
INSERT INTO result_documents (result_id,document_id,cy_document_id)
VALUES ('TFOOUT','TFOOUT',NULL);
INSERT INTO result_documents (result_id,document_id,cy_document_id)
VALUES ('TFORD','FINOT','CY_FINOT');
INSERT INTO result_documents (result_id,document_id,cy_document_id)
VALUES ('TFORD','FINOTA',NULL);
INSERT INTO result_documents (result_id,document_id,cy_document_id)
VALUES ('TFOREM','TFOAR',NULL);
INSERT INTO result_documents (result_id,document_id,cy_document_id)
VALUES ('TTPAY','TTPLET','CY_TTPLET');
INSERT INTO result_documents (result_id,document_id,cy_document_id)
VALUES ('UPWO','UPWO','CY_UPWO');