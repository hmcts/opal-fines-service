/**
*
* OPAL Program
*
* MODULE      : update_template.sql
*
* DESCRIPTION : Update template name to "Collection Order - Authorised Powers"
*
* VERSION HISTORY:
*
* Date          Author       Version     Nature of Change 
* ----------    --------     --------    --------------------------------------
* 30/07/2024    I Readman    1.0         PO-570 Update incorrect template name
*
**/  
UPDATE templates SET template_name = 'Collection Order - Authorised Powers' WHERE template_id = 500000004;
