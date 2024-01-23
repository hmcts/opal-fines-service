/**
* OPAL Program
*
* MODULE      : insert_documents.sql
*
* DESCRIPTION : Inserts rows of data into the DOCUMENTS table. 
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ---------------------------------------------------------------------------------------------------------
* 21/01/2024    A Dennis    1.0         PO-147 Inserts rows of data into the DOCUMENTS table
*
**/
INSERT INTO documents
(               
 document_id            
,recipient              
,document_language      
,signature_source       
,priority               
,header_type            
,document_elements           
)
VALUES
(
 500000000
,'P'
,'EN'
,'Area'
,1
,'AP'
,'{"num_of_lines": 10,
"created_by": "Amanda Kesh"}'
);

INSERT INTO documents
(               
 document_id            
,recipient              
,document_language      
,signature_source       
,priority               
,header_type            
,document_elements           
)
VALUES
(
 500000001
,'P'
,'EN'
,'Area'
,1
,'AP'
,'{"num_of_lines": 11,
"created_by": "Bono Brul"}'
);

INSERT INTO documents
(               
 document_id            
,recipient              
,document_language      
,signature_source       
,priority               
,header_type            
,document_elements           
)
VALUES
(
 500000002
,'O'
,'EN'
,'LJA'
,2
,'EO'
,'{"num_of_lines": 12,
"created_by": "Izzy Can"}'
);

INSERT INTO documents
(               
 document_id            
,recipient              
,document_language      
,signature_source       
,priority               
,header_type            
,document_elements           
)
VALUES
(
 500000003
,'O'
,'EN'
,'LJA'
,3
,'MC'
,'{"num_of_lines": 103,
"created_by": "Donna Simms"}'
);

INSERT INTO documents
(               
 document_id            
,recipient              
,document_language      
,signature_source       
,priority               
,header_type            
,document_elements           
)
VALUES
(
 500000004
,'P'
,'EN'
,'Area'
,1
,'A'
,'{"num_of_lines": 14,
"created_by": "Kirsty Rimmes"}'
);

INSERT INTO documents
(               
 document_id            
,recipient              
,document_language      
,signature_source       
,priority               
,header_type            
,document_elements           
)
VALUES
(
 500000005
,'P'
,'CY'
,'Area'
,2
,'MC'
,'{"num_of_lines": 5,
"created_by": "Freda Gore"}'
);

INSERT INTO documents
(               
 document_id            
,recipient              
,document_language      
,signature_source       
,priority               
,header_type            
,document_elements           
)
VALUES
(
 500000006
,'P'
,'EN'
,'Area'
,2
,'MF'
,'{"num_of_lines": 16,
"created_by": "Tom Sonma"}'
);

INSERT INTO documents
(               
 document_id            
,recipient              
,document_language      
,signature_source       
,priority               
,header_type            
,document_elements           
)
VALUES
(
 500000007
,'P'
,'EN'
,'LJA'
,2
,'MA'
,'{"num_of_lines": 29,
"created_by": "Rod Bany"}'
);

INSERT INTO documents
(               
 document_id            
,recipient              
,document_language      
,signature_source       
,priority               
,header_type            
,document_elements           
)
VALUES
(
 500000008
,'P'
,'EN'
,'LJA'
,2
,'ME'
,'{"num_of_lines": 120,
"created_by": "Keisha Malley"}'
);

INSERT INTO documents
(               
 document_id            
,recipient              
,document_language      
,signature_source       
,priority               
,header_type            
,document_elements           
)
VALUES
(
 500000009
,'P'
,'EN'
,'LJA'
,3
,'MA'
,'{"num_of_lines": 67,
"created_by": "Tama Leito"}'
);
