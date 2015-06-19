Drop procedure IF EXISTS messageDataDescription; 
DELIMITER //
create procedure messageDataDescription() 
Begin
IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE COLUMN_NAME = 'description'
     and TABLE_NAME = 'b_message_data'
     and TABLE_SCHEMA = DATABASE())THEN
Alter table b_message_data ADD COLUMN description  TEXT DEFAULT NULL;
END IF;
END //
DELIMITER ;
call messageDataDescription();
Drop procedure IF EXISTS messageDataDescription;


INSERT IGNORE INTO b_message_template(template_description,subject,body,message_type) values ('SMS_NOTIFY_PAYMENT','Notify Payment','We confirm the receipts of your payment <Amount> with Dated: <Payment Date>, done towards the Services.','M');

INSERT IGNORE INTO b_message_template(template_description,subject,body,message_type) values ('SMS_NOTIFY_RECONNECTION','Notify Reconnection','Thanks for subscribing to our services. 
Your service with <Service name> is activated on <Reconnection Date>.','M');

INSERT IGNORE INTO b_message_template(template_description,subject,body,message_type) values ('SMS_NOTIFY_DISCONNECTION','Notify Disconnection','Your service with <Service name> is disconnected on <Disconnection Date>.Please call us or do the renew or Top-up through your selfcare portal.','M');

INSERT IGNORE INTO b_message_template(template_description,subject,body,message_type) values ('SMS_NOTIFY_ACTIVATION','Notify Activation','Thanks for subscribing to our services. 
Your service with <Service name> is activated on <Activation Date>.','M');

INSERT IGNORE INTO b_message_template(template_description,subject,body,message_type) values ('SMS_NEW_CUSTOMER','New Customer','Thanks for subscribing to our services. 
Your service with <Service name> is activated on <Activation Date>.','M');

INSERT IGNORE INTO b_message_template(template_description,subject,body,message_type) values ('SMS_PAYMENT_RECEIPT','Payment Receipt','Thank you for making your purchase for OBS.
 Result : <A1>,
 Description : <A2>,
 Amount : <A3>,     ReceiptNo : <A4>, 
 Card Type :<A5>, 
 Card Number : <A6>.','M');

INSERT IGNORE INTO b_message_template(template_description,subject,body,message_type) values ('SMS_CREATE_USER','Create User','OBS User Account has been successfully created .You can login using the following credentials username: <PARAM1> ,password: <PARAM2>','M');

INSERT IGNORE INTO b_message_template(template_description,subject,body,message_type) values ('SMS_PROVISION_CREDENTIALS','ProvisionCredentials','Your Beenius Subscriber Account has been successfully created And Following are the Beenius Account Details. 
 subscriberUid : <PARAM2> , 
 Authpin : <PARAM3> .','M');

INSERT IGNORE INTO b_message_template(template_description,subject,body,message_type) values ('SMS_NEW_SELFCARE_PASSWORD','Selfcare New Password','The password for your SelfCare User Portal Account- <PARAM1>  was reset. Password : <PARAM2>','M');

INSERT IGNORE INTO b_message_template(template_description,subject,body,message_type) values ('SMS_CREATE_SELFCARE','Selfcare','Your Selfcare User Account has been successfully created,Following are the User login Details.
 userName : <PARAM1>,
 password : <PARAM2>','M');


INSERT IGNORE INTO b_eventaction_mapping values(null,'Notify Payment Receipt','Notify_SMS_Payment','workflow_events','Y','N');
INSERT IGNORE INTO b_eventaction_mapping values(null,'Order reconnection','Notify_SMS_Reconnection','workflow_events','Y','N');
INSERT IGNORE INTO b_eventaction_mapping values(null,'Order disconnection','Notify_SMS_Disconnection','workflow_events','Y','N');
INSERT IGNORE INTO b_eventaction_mapping values(null,'Order activation','Notify_SMS_Activation','workflow_events','Y','N');


















