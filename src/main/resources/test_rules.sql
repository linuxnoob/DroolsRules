-- Generated SQL statements for FD_BUSINESS_RULES

INSERT INTO FD_BUSINESS_RULES (rule_id, department, rule_case, rule_step, rule_destination, created_by, action) VALUES ('UNI_001', 'UNI', 'GROUPID=="GRP_UNI_MED", ACTIVITY=="COM_001"', 'MEDICAL_SKIP_APPROVAL=="NO"', 'STOP', 'SONORA', NULL);

INSERT INTO FD_BUSINESS_RULES (rule_id, department, rule_case, rule_step, rule_destination, created_by, action) VALUES ('UNI_002', 'UNI', 'GROUPID=="GRP_UNI_MED", ACTIVITY=="COM_START"', 'MEDICAL_SKIP_APPROVAL=="YES"', 'COM_002', 'SONORA', NULL);

