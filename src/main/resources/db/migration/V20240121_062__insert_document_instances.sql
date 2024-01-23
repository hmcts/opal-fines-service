/**
* OPAL Program
*
* MODULE      : insert_document_instances.sql
*
* DESCRIPTION : Inserts rows of data into the DOCUMENT_INSTANCES table. 
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ---------------------------------------------------------------------------------------------------------
* 21/01/2024    A Dennis    1.0         PO-147 Inserts rows of data into the DOCUMENT_INSTANCES table
*
**/
INSERT INTO document_instances
(               
 document_instance_id    
,document_id             
,business_unit_id        
,generated_date          
,generated_by            
,content                          
)
VALUES
(
 500000000
,500000000
,500
,'2024-01-01'
,'10000001'
,'<DocumentInstance>
   <Title>Monthly Statement Of Account</Title>
   <FromDate>2023-12-01</FromDate>
   <ToDate>2023-12-31</ToDate>
 </DocumentInstance>'
);

INSERT INTO document_instances
(               
 document_instance_id    
,document_id             
,business_unit_id        
,generated_date          
,generated_by            
,content                          
)
VALUES
(
 500000001
,500000001
,501
,'2023-12-01'
,'10000002'
,'<DocumentInstance>
   <Title>Monthly Statement Of Account</Title>
   <FromDate>2023-11-01</FromDate>
   <ToDate>2023-11-30</ToDate>
 </DocumentInstance>'
);

INSERT INTO document_instances
(               
 document_instance_id    
,document_id             
,business_unit_id        
,generated_date          
,generated_by            
,content                          
)
VALUES
(
 500000002
,500000002
,502
,'2023-11-01'
,'10000003'
,'<DocumentInstance>
   <Title>Monthly Statement Of Account</Title>
   <FromDate>2023-10-01</FromDate>
   <ToDate>2023-10-31</ToDate>
 </DocumentInstance>'
);

INSERT INTO document_instances
(               
 document_instance_id    
,document_id             
,business_unit_id        
,generated_date          
,generated_by            
,content                          
)
VALUES
(
 500000003
,500000003
,503
,'2023-10-01'
,'10000004'
,'<DocumentInstance>
   <Title>Monthly Statement Of Account</Title>
   <FromDate>2023-09-01</FromDate>
   <ToDate>2023-09-30</ToDate>
 </DocumentInstance>'
);

INSERT INTO document_instances
(               
 document_instance_id    
,document_id             
,business_unit_id        
,generated_date          
,generated_by            
,content                          
)
VALUES
(
 500000004
,500000004
,504
,'2023-09-01'
,'10000005'
,'<DocumentInstance>
   <Title>Monthly Statement Of Account</Title>
   <FromDate>2023-08-01</FromDate>
   <ToDate>2023-08-31</ToDate>
 </DocumentInstance>'
);

INSERT INTO document_instances
(               
 document_instance_id    
,document_id             
,business_unit_id        
,generated_date          
,generated_by            
,content                          
)
VALUES
(
 500000005
,500000005
,505
,'2023-08-01'
,'10000006'
,'<DocumentInstance>
   <Title>Monthly Statement Of Account</Title>
   <FromDate>2023-07-01</FromDate>
   <ToDate>2023-07-31</ToDate>
 </DocumentInstance>'
);

INSERT INTO document_instances
(               
 document_instance_id    
,document_id             
,business_unit_id        
,generated_date          
,generated_by            
,content                          
)
VALUES
(
 500000006
,500000006
,506
,'2023-07-01'
,'10000007'
,'<DocumentInstance>
   <Title>Monthly Statement Of Account</Title>
   <FromDate>2023-06-01</FromDate>
   <ToDate>2023-06-30</ToDate>
 </DocumentInstance>'
);

INSERT INTO document_instances
(               
 document_instance_id    
,document_id             
,business_unit_id        
,generated_date          
,generated_by            
,content                          
)
VALUES
(
 500000007
,500000007
,507
,'2023-06-01'
,'10000008'
,'<DocumentInstance>
   <Title>Monthly Statement Of Account</Title>
   <FromDate>2023-05-01</FromDate>
   <ToDate>2023-05-31</ToDate>
 </DocumentInstance>'
);

INSERT INTO document_instances
(               
 document_instance_id    
,document_id             
,business_unit_id        
,generated_date          
,generated_by            
,content                          
)
VALUES
(
 500000008
,500000008
,508
,'2023-05-01'
,'10000009'
,'<DocumentInstance>
   <Title>Monthly Statement Of Account</Title>
   <FromDate>2023-04-01</FromDate>
   <ToDate>2023-04-31</ToDate>
 </DocumentInstance>'
);

INSERT INTO document_instances
(               
 document_instance_id    
,document_id             
,business_unit_id        
,generated_date          
,generated_by            
,content                          
)
VALUES
(
 500000009
,500000009
,509
,'2023-04-01'
,'10000010'
,'<DocumentInstance>
   <Title>Monthly Statement Of Account</Title>
   <FromDate>2023-03-01</FromDate>
   <ToDate>2023-03-31</ToDate>
 </DocumentInstance>'
);
