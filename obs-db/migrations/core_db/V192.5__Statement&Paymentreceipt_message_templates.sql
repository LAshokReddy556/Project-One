update b_message_template set template_description='STATEMENT_EMAIL',subject='E-STAEMENT',header='Dear <PARAM1> <br/>', body='Please find enclosed your Account Statement <br/>' where template_description='Bill_Email';
SET @id=(select id from b_message_template where template_description='STATEMENT_EMAIL');
insert ignore into b_message_params values(null,@id, '<PARAM1>', '1');


insert ignore into b_message_template values(null, 'INVOICE_EMAIL', 'TAX INVOICE FROM OBS', 'Dear <PARAM1> <br/>', 'Please find the attached TAX INVOCIE from Obs <br/>', 'Thanks', 'E', '1', '2014-07-30 12:33:42', '1', '2015-06-09 14:10:47', 'N');
SET @id=(select id from b_message_template where template_description='INVOICE_EMAIL');
insert ignore into b_message_params values(null,@id, '<PARAM1>', '1');

insert ignore into b_message_template values(null, 'PAYMENT_EMAIL', 'PAYMENT RECEIPT FROM OBS', 'Dear <PARAM1>', 'Please find the attached PAYMENT RECEIPT from Obs', 'Thanks', 'E', '1', '2014-07-30 12:33:42', '1', '2014-07-30 12:33:42', 'N');
SET @id=(select id from b_message_template where template_description='PAYMENT_EMAIL');
insert ignore into b_message_params values(null,@id, '<PARAM1>', '1');

insert ignore into b_eventaction_mapping values(null, 'Send Payment Receipt', 'Send Payment', 'workflow_events', 'Y', 'N');

