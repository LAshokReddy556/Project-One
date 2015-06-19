SET SQL_SAFE_UPDATES = 0;
SET foreign_key_checks = 0;
-- Charge Codes
delete from b_charge_codes where charge_code='NONE';
insert ignore into `b_charge_codes`(`id`,`charge_code`,`charge_description`,`charge_type`,`charge_duration`,`duration_type`,`tax_inclusive`,`billfrequency_code`) values (null,'MSC','Monthly Subscription','RC',1,'Month(s)',0,'Monthly');
insert ignore into `b_charge_codes`(`id`,`charge_code`,`charge_description`,`charge_type`,`charge_duration`,`duration_type`,`tax_inclusive`,`billfrequency_code`) values (null,'QSC','Quaterly Subscription','RC',3,'Month(s)',0,'Quaterly');
insert ignore into `b_charge_codes`(`id`,`charge_code`,`charge_description`,`charge_type`,`charge_duration`,`duration_type`,`tax_inclusive`,`billfrequency_code`) values (null,'HSC','Half Yearly Subscription','RC',6,'Month(s)',0,'Halfyearly');
insert ignore into `b_charge_codes`(`id`,`charge_code`,`charge_description`,`charge_type`,`charge_duration`,`duration_type`,`tax_inclusive`,`billfrequency_code`) values (null,'YSC','Yearly Subscription','RC',12,'Month(s)',0,'yearly');
insert ignore into `b_charge_codes`(`id`,`charge_code`,`charge_description`,`charge_type`,`charge_duration`,`duration_type`,`tax_inclusive`,`billfrequency_code`) values (null,'OTC','One Time','NRC',1,'Month(s)',0,'Once');




insert ignore into m_permission values(null,'client&order','SUSPEND_ORDER','ORDER','SUSPEND',0);
insert ignore into m_permission values(null,'organisation','MOVEITEM_MRN','MRN','MOVEITEM',0);

-- Contract Periods
insert ignore into `b_contract_period`(`id`,`contract_period`,`contract_duration`,`contract_type`,`is_deleted`) values (null,'Perpetual',0,'None','N');
insert ignore into `b_contract_period`(`id`,`contract_period`,`contract_duration`,`contract_type`,`is_deleted`) values (null,'1 Month',1,'Month(s)','N');
insert ignore into `b_contract_period`(`id`,`contract_period`,`contract_duration`,`contract_type`,`is_deleted`) values (null,'3 Months',3,'Month(s)','N');
insert ignore into `b_contract_period`(`id`,`contract_period`,`contract_duration`,`contract_type`,`is_deleted`) values (null,'6 Months',6,'Month(s)','N');
insert ignore into `b_contract_period`(`id`,`contract_period`,`contract_duration`,`contract_type`,`is_deleted`) values (null,'1 Year',12,'Month(s)','N');

-- Billing rules 

insert ignore into `b_billing_rules`(`id`,`billing_rule`) values (null,'Prorata & DC');
insert ignore into `b_billing_rules`(`id`,`billing_rule`) values (null,'Prorata & NODC');
insert ignore into `b_billing_rules`(`id`,`billing_rule`) values (null,'Full Month & NODC');

insert ignore into `b_service`(`id`,`service_code`,`service_description`,`service_type`,`status`,`is_deleted`,`service_unittype`,`is_optional`,`is_auto`) values (0,'None','None','TV','ACtive','Y',null,'N','Y');
update  b_service set id=0 where service_code='None'; 
-- Discount Codes

insert ignore into `b_discount_master`(`id`,`discount_code`,`discount_description`,`discount_type`,`discount_rate`,`start_date`,`discount_status`,`is_delete`) values (null,'None','None','Flat',0,'2015-04-16 00:00:00','ACTIVE','N');

insert ignore  into `b_service`(`id`,`service_code`,`service_description`,`service_type`,`status`,`is_deleted`,`service_unittype`,`is_optional`,`is_auto`) values (0,'None','None','TV','ACtive','Y',null,'N','Y');
update  b_service set id=0 where service_code='None'; 


-- Event Action Mapping
truncate table b_eventaction_mapping;
insert ignore into `b_eventaction_mapping`(`id`,`event_name`,`action_name`,`process`,`is_deleted`,`is_synchronous`) values (null,'Create Client','Send Mail','workflow_events','Y','N');
insert ignore into `b_eventaction_mapping`(`id`,`event_name`,`action_name`,`process`,`is_deleted`,`is_synchronous`) values (null,'Create Client','SEND SMS','workflow_events','Y','N');
insert ignore into `b_eventaction_mapping`(`id`,`event_name`,`action_name`,`process`,`is_deleted`,`is_synchronous`) values (null,'Order activation','Invoice','workflow_events','Y','Y');
insert ignore into `b_eventaction_mapping`(`id`,`event_name`,`action_name`,`process`,`is_deleted`,`is_synchronous`) values (null,'Order Booking','Invoice','workflow_events','Y','Y');
insert ignore into `b_eventaction_mapping`(`id`,`event_name`,`action_name`,`process`,`is_deleted`,`is_synchronous`) values (null,'Close Client','SEND PROVISION','workflow_events','Y','N');
insert ignore into `b_eventaction_mapping`(`id`,`event_name`,`action_name`,`process`,`is_deleted`,`is_synchronous`) values (null,'Create Ticket','Send Email','workflow_events','Y','N');
insert ignore into `b_eventaction_mapping`(`id`,`event_name`,`action_name`,`process`,`is_deleted`,`is_synchronous`) values (null,'Create Live Event','Active Live Event','workflow_events','Y','N');
insert ignore into `b_eventaction_mapping`(`id`,`event_name`,`action_name`,`process`,`is_deleted`,`is_synchronous`) values (null,'Create Payment','Renewal','workflow_events','Y','N');



-- Message Templates
delete from b_message_template where template_description ='SELFCARE REGISTRATIO';
insert ignore into `b_message_template`(`id`,`template_description`,`subject`,`header`,`body`,`footer`,`message_type`,`createdby_id`,`created_date`,`lastmodifiedby_id`,`lastmodified_date`,`is_deleted`) values (null,'TICKET_TEMPLATE','TICKET','TICKET','create ticket','Thank You','E',1,null,1,'2015-04-16 12:10:37','N');
insert ignore into `b_message_template`(`id`,`template_description`,`subject`,`header`,`body`,`footer`,`message_type`,`createdby_id`,`created_date`,`lastmodifiedby_id`,`lastmodified_date`,`is_deleted`) values (null,'Bill_Email','TAX INVOICE from Obs','Bill','Please find the attached TAX INVOCIE from Spicenet','Thanks','E',1,'2014-07-30 12:33:42',1,'2014-07-30 12:33:42','N');
insert ignore into `b_message_template`(`id`,`template_description`,`subject`,`header`,`body`,`footer`,`message_type`,`createdby_id`,`created_date`,`lastmodifiedby_id`,`lastmodified_date`,`is_deleted`) values (null,'CREATE SELFCARE','OBS Selfcare','Dear <PARAM1>','Your Selfcare User Account has been successfully created,Following are the User login Details. <br/> userName : <PARAM2> , <br/> password : <PARAM3> .','Thankyou','E',null,null,null,null,'N');
insert ignore into `b_message_template`(`id`,`template_description`,`subject`,`header`,`body`,`footer`,`message_type`,`createdby_id`,`created_date`,`lastmodifiedby_id`,`lastmodified_date`,`is_deleted`) values (null,'SELFCARE REGISTRATION','Register Confirmation','Hai','Your Registration has been successfully completed.To approve this Registration please click on this link: <br/> URL : <PARAM1>.','Thankyou','E',null,null,null,null,'N');
insert ignore into `b_message_template`(`id`,`template_description`,`subject`,`header`,`body`,`footer`,`message_type`,`createdby_id`,`created_date`,`lastmodifiedby_id`,`lastmodified_date`,`is_deleted`) values (null,'NEW SELFCARE PASSWORD','Reset Password','Dear <PARAM1>','The password for your SelfCare User Portal Account- <PARAM2>  was reset. . <br/> Password : <PARAM3>.','Thankyou','E',null,null,null,null,'N');
insert ignore into `b_message_template`(`id`,`template_description`,`subject`,`header`,`body`,`footer`,`message_type`,`createdby_id`,`created_date`,`lastmodifiedby_id`,`lastmodified_date`,`is_deleted`) values (null,'PROVISION CREDENTIALS','OBS Provision Credentials','Dear <PARAM1>','Your OBS Subscriber Account has been successfully created And Following are the Account Details.  <br/> subscriberUid : <PARAM2> , <br/>  Authpin : <PARAM3> .','Thankyou','E',null,null,null,null,'N');
insert ignore into `b_message_template`(`id`,`template_description`,`subject`,`header`,`body`,`footer`,`message_type`,`createdby_id`,`created_date`,`lastmodifiedby_id`,`lastmodified_date`,`is_deleted`) values (null,'CREATE USER','OBS User Creation','Dear <PARAM1>','OBS User Account has been successfully created .You can login using the following credentials. 
 userName : <PARAM2> , 
 password : <PARAM3> .','Thankyou','E',null,null,null,null,'N');
insert ignore into `b_message_template`(`id`,`template_description`,`subject`,`header`,`body`,`footer`,`message_type`,`createdby_id`,`created_date`,`lastmodifiedby_id`,`lastmodified_date`,`is_deleted`) values (null,'PAYMENT_RECEIPT','Payment Confirmation','Dear <PARAM1><br/><br/>','Thank you for making your purchase for OBS.<br/><br/>
 This is a confirmation of your payment.<br/><br/> Result : <PARAM2>,<br/> Description : <PARAM3>,<br/>Amount : <PARAM4>,<br/>
 ReceiptNo : <PARAM5>.<br/>','Thankyou','E',null,null,null,null,'N');
insert ignore into `b_message_template`(`id`,`template_description`,`subject`,`header`,`body`,`footer`,`message_type`,`createdby_id`,`created_date`,`lastmodifiedby_id`,`lastmodified_date`,`is_deleted`) values (null,'Reminder For Expiry','service expiry','Dear <Param1>','Your service with <Param2> is going to expired on <Param3>.
Please renew or top-up to avoid service disconnection. Please call us or do the renew through your selfcare portal <Param4>','Thanks <br/> <Param5> <br/> <Param6>','E',1,'2015-04-14 18:23:13',1,'2015-04-14 18:25:24','N');
insert ignore into `b_message_template`(`id`,`template_description`,`subject`,`header`,`body`,`footer`,`message_type`,`createdby_id`,`created_date`,`lastmodifiedby_id`,`lastmodified_date`,`is_deleted`) values (null,'Newly_Activated_Customers','New_customers','Dear <Param1>','Thanks for subscribing to our services. 
Your service with <Param2> is activated on <Param3>.','Thanks <br/> <Param4> <br/> <Param5>','E',1,'2015-04-14 18:54:51',1,'2015-04-14 18:54:51','N');
insert ignore into `b_message_template`(`id`,`template_description`,`subject`,`header`,`body`,`footer`,`message_type`,`createdby_id`,`created_date`,`lastmodifiedby_id`,`lastmodified_date`,`is_deleted`) values (null,'Disconnected_Customers','Disconnected Customers','Dear <Param1>','Your service with <Param2> is disconnected on <Param3>.
Please call us or do the renew or Top-up through your selfcare portal <Param4>','Thanks <br/> <Param5> <br/> <Param6>','E',1,'2015-04-14 18:56:51',1,'2015-04-14 18:56:51','N');
insert ignore into `b_message_template`(`id`,`template_description`,`subject`,`header`,`body`,`footer`,`message_type`,`createdby_id`,`created_date`,`lastmodifiedby_id`,`lastmodified_date`,`is_deleted`) values (null,'Renew_or_Top-up_Customer','Renew or Top-up Customer','Dear <Param1>','Thanks for subscribing to our services. 
Your service with <Param2> is activated on <Param3>.','Thanks <br/> <Param4> <br/> <Param5>','E',1,'2015-04-14 18:59:11',1,'2015-04-14 18:59:11','N');

-- Provisioning Actions

insert ignore into `b_provisioning_actions`(`id`,`provision_type`,`action`,`provisioning_system`,`is_enable`,`is_delete`) values (null,'Create Client','CLIENT ACTIVATION','CMS','N','N');
insert ignore into `b_provisioning_actions`(`id`,`provision_type`,`action`,`provisioning_system`,`is_enable`,`is_delete`) values (null,'Close Client','TERMINATE','CMS','N','N');
insert ignore into `b_provisioning_actions`(`id`,`provision_type`,`action`,`provisioning_system`,`is_enable`,`is_delete`) values (null,'Event Order','PROVISION IT','Radius','N','N');
insert ignore into `b_provisioning_actions`(`id`,`provision_type`,`action`,`provisioning_system`,`is_enable`,`is_delete`) values (null,'Release Device','RELEASE DEVICE','Radius','N','N');
insert ignore into `b_provisioning_actions`(`id`,`provision_type`,`action`,`provisioning_system`,`is_enable`,`is_delete`) values (null,'Create Agent','CREATE AGENT','CMS','N','N');
insert ignore into `b_provisioning_actions`(`id`,`provision_type`,`action`,`provisioning_system`,`is_enable`,`is_delete`) values (null,'Create Nas','CREATE NAS','Radius','N','N');
insert ignore into `b_provisioning_actions`(`id`,`provision_type`,`action`,`provisioning_system`,`is_enable`,`is_delete`) values (null,'Create RadSevice','CREATE RADSERVICE','Radius','N','N');
insert ignore into `b_provisioning_actions`(`id`,`provision_type`,`action`,`provisioning_system`,`is_enable`,`is_delete`) values (null,'Change Credentials','CHANGE CREDENTIALS','Radius','N','N');
insert ignore into `b_provisioning_actions`(`id`,`provision_type`,`action`,`provisioning_system`,`is_enable`,`is_delete`) values (null,'Update RadService','UPDATE RADSERVICE','Radius','N','N');
insert ignore into `b_provisioning_actions`(`id`,`provision_type`,`action`,`provisioning_system`,`is_enable`,`is_delete`) values (null,'Remove RadService','REMOVE RADSERVICE','Radius','N','N');
insert ignore into `b_provisioning_actions`(`id`,`provision_type`,`action`,`provisioning_system`,`is_enable`,`is_delete`) values (null,'Update RadSevice','UPDATE RADSERVICE','Radius','N','N');
insert ignore into `b_provisioning_actions`(`id`,`provision_type`,`action`,`provisioning_system`,`is_enable`,`is_delete`) values (null,'Remove RadSevice','REMOVE RADSERVICE','Radius','N','N');



 
 -- config
 delete from c_configuration where name='Forcible Balance Check';
insert ignore into c_configuration VALUES (null,'balance-check',0,null);

-- Price Region 
/*insert ignore into b_priceregion_master (id,priceregion_code,priceregion_name,createdby_id,created_date,is_deleted) 
 VALUES (null,'Default','Default Region',null,null,'N');
 
 insert ignore	 into b_priceregion_detail (priceregion_id,country_id,state_id,is_deleted)
select prm.id,0,0,'N' from b_priceregion_master prm where prm.priceregion_code ='Default';
*/
-- Views
-----------
  
-- Financial Transaction view 
CREATE OR REPLACE VIEW  `fin_trans_vw` AS SELECT  `m_appuser`.`username` AS `username`, `b_invoice`.`client_id` AS `client_id`, `b_invoice`.`id` AS `transId`,if(( `b_charge`.`charge_type` = 'NRC'),'Once','Periodic') AS `tran_type`,cast( `b_invoice`.`invoice_date` AS date) AS `transDate`,'INVOICE' AS `transType`,if(( `b_invoice`.`invoice_amount` > 0), `b_invoice`.`invoice_amount`,0) AS `dr_amt`,if(( `b_invoice`.`invoice_amount` < 0),abs( `b_invoice`.`invoice_amount`),0) AS `cr_amt`,1 AS `flag` FROM (( `b_invoice` JOIN  `m_appuser`) JOIN  `b_charge`) WHERE (( `b_invoice`.`createdby_id` =  `m_appuser`.`id`) AND ( `b_invoice`.`id` =  `b_charge`.`invoice_id`) AND ( `b_invoice`.`invoice_date` <= now())) UNION ALL SELECT  `m_appuser`.`username` AS `username`, `b_adjustments`.`client_id` AS `client_id`, `b_adjustments`.`id` AS `transId`,(SELECT  `m_code_value`.`code_value` FROM  `m_code_value` WHERE (( `m_code_value`.`code_id` = 12) AND ( `b_adjustments`.`adjustment_code` =  `m_code_value`.`id`))) AS `tran_type`,cast( `b_adjustments`.`adjustment_date` AS date) AS `transdate`,'ADJUSTMENT' AS `transType`,0 AS `dr_amt`,(CASE  `b_adjustments`.`adjustment_type` WHEN 'CREDIT' THEN  `b_adjustments`.`adjustment_amount` END) AS `cr_amount`,1 AS `flag` FROM ( `b_adjustments` JOIN  `m_appuser`) WHERE (( `b_adjustments`.`adjustment_date` <= now()) AND ( `b_adjustments`.`adjustment_type` = 'CREDIT') AND ( `b_adjustments`.`createdby_id` =  `m_appuser`.`id`)) UNION ALL SELECT  `m_appuser`.`username` AS `username`, `b_adjustments`.`client_id` AS `client_id`, `b_adjustments`.`id` AS `transId`,(SELECT  `m_code_value`.`code_value` FROM  `m_code_value` WHERE (( `m_code_value`.`code_id` = 12) AND ( `b_adjustments`.`adjustment_code` =  `m_code_value`.`id`))) AS `tran_type`,cast( `b_adjustments`.`adjustment_date` AS date) AS `transdate`,'ADJUSTMENT' AS `transType`,(CASE  `b_adjustments`.`adjustment_type` WHEN 'DEBIT' THEN  `b_adjustments`.`adjustment_amount` END) AS `dr_amount`,0 AS `cr_amt`,1 AS `flag` FROM ( `b_adjustments` JOIN  `m_appuser`) WHERE (( `b_adjustments`.`adjustment_date` <= now()) AND ( `b_adjustments`.`adjustment_type` = 'DEBIT') AND ( `b_adjustments`.`createdby_id` =  `m_appuser`.`id`)) UNION ALL SELECT  `m_appuser`.`username` AS `username`, `b_payments`.`client_id` AS `client_id`, `b_payments`.`id` AS `transId`,(SELECT  `m_code_value`.`code_value` FROM  `m_code_value` WHERE (( `m_code_value`.`code_id` = 11) AND ( `b_payments`.`paymode_id` =  `m_code_value`.`id`))) AS `tran_type`,cast( `b_payments`.`payment_date` AS date) AS `transDate`,'PAYMENT' AS `transType`,0 AS `dr_amt`, `b_payments`.`amount_paid` AS `cr_amount`, `b_payments`.`is_deleted` AS `flag` FROM ( `b_payments` JOIN  `m_appuser`) WHERE (( `b_payments`.`createdby_id` =  `m_appuser`.`id`) AND ( `b_payments`.`payment_date` <= now())) UNION ALL SELECT  `m_appuser`.`username` AS `username`, `b_payments`.`client_id` AS `client_id`, `b_payments`.`id` AS `transId`,(SELECT  `m_code_value`.`code_value` FROM  `m_code_value` WHERE (( `m_code_value`.`code_id` = 11) AND ( `b_payments`.`paymode_id` =  `m_code_value`.`id`))) AS `tran_type`,cast( `b_payments`.`payment_date` AS date) AS `transDate`,'PAYMENT CANCELED' AS `transType`, `b_payments`.`amount_paid` AS `dr_amt`,0 AS `cr_amount`, `b_payments`.`is_deleted` AS `flag` FROM ( `b_payments` JOIN  `m_appuser`) WHERE (( `b_payments`.`is_deleted` = 1) AND ( `b_payments`.`createdby_id` =  `m_appuser`.`id`) AND ( `b_payments`.`payment_date` <= now())) UNION ALL SELECT `ma`.`username` AS `username`,`bjt`.`client_id` AS `client_id`,`bjt`.`id` AS `transId`,'Event Journal' AS `tran_type`,cast(`bjt`.`jv_date` AS date) AS `transDate`,'JOURNAL VOUCHER' AS `transType`,ifnull(`bjt`.`debit_amount`,0) AS `dr_amt`,ifnull(`bjt`.`credit_amount`,0) AS `cr_amount`,1 AS `flag` FROM ( `b_jv_transactions` `bjt` JOIN  `m_appuser` `ma` ON(((`bjt`.`createdby_id` = `ma`.`id`) AND (`bjt`.`jv_date` <= now())))) ORDER BY 1,2;

-- Movie all
CREATE OR REPLACE VIEW  `mvAll_vw` AS select  `b_media_asset`.`id` AS `mediaId`, `b_media_asset`.`title` AS `title`, `b_media_asset`.`category_id` AS `category`, `b_media_asset`.`image` AS `image`, `b_media_asset`.`rating` AS `rating`, `b_mod_master`.`event_description` AS `eventDescription`, `b_mod_pricing`.`Opt_type` AS `optType`, `b_mod_pricing`.`event_id` AS `eventId`, `b_mod_pricing`.`format_type` AS `quality`,`c`.`code_value` AS `clientType`, `b_mod_pricing`.`price` AS `price` from (((( `b_mod_master` left join  `b_mod_detail` on(( `b_mod_master`.`id` =  `b_mod_detail`.`event_id`))) left join  `b_mod_pricing` on(( `b_mod_master`.`id` =  `b_mod_pricing`.`event_id`))) left join  `b_media_asset` on(( `b_media_asset`.`id` =  `b_mod_detail`.`media_id`))) left join  `m_code_value` `c` on(( `b_mod_pricing`.`client_typeid` = `c`.`id`))) where (date_format(now(),'%Y-%m-%d') between date_format( `b_mod_master`.`event_start_date`,'%Y-%m-%d') and ifnull(date_format( `b_mod_master`.`event_end_date`,'%Y-%m-%d'),'2090-12-31'));

-- mvComing_view
CREATE OR REPLACE  VIEW  `mvComing_vw` AS select `m`.`id` AS `mediaId`,`m`.`title` AS `title`,`m`.`image` AS `image`,`m`.`rating` AS `rating`,0 AS `eventId`,'C' AS `assetTag` from  `b_media_asset` `m` where (`m`.`category_id` = 19);

-- Discountmovies
CREATE OR REPLACE VIEW  `mvDiscount_vw` AS select `m`.`id` AS `mediaId`,`m`.`title` AS `title`,`m`.`image` AS `image`,`m`.`rating` AS `rating`,`ed`.`event_id` AS `eventId`,count(`ed`.`media_id`) AS `media_count` from ((( `b_media_asset` `m` join  `b_mod_detail` `ed` on((`ed`.`media_id` = `m`.`id`))) join  `b_mod_master` `em` on((`em`.`id` = `ed`.`event_id`))) join  `b_mod_pricing` `ep` on((`em`.`id` = `ep`.`event_id`))) where (`ep`.`discount_id` >= 1) group by `m`.`id` having (count(distinct `ed`.`event_id`) >= 1);

-- Highrated movies
CREATE OR REPLACE VIEW    `mvHighRate_vw` AS select `m`.`id` AS `mediaId`,`m`.`title` AS `title`,`m`.`image` AS `image`,`m`.`rating` AS `rating`,`ed`.`event_id` AS `eventId`,count(`ed`.`media_id`) AS `media_count` from (( `b_media_asset` `m` left join  `b_mod_detail` `ed` on((`ed`.`media_id` = `m`.`id`))) left join  `b_mod_master` `em` on((`em`.`id` = `ed`.`event_id`))) where (`m`.`rating` >= 4.5) group by `m`.`id`,`m`.`title`;
-- Release movies
CREATE OR REPLACE VIEW  `mvNewRelease_vw` AS select `m`.`id` AS `mediaId`,`m`.`title` AS `title`,`m`.`image` AS `image`,`m`.`rating` AS `rating`,`ed`.`event_id` AS `eventId`,count(`ed`.`media_id`) AS `media_count` from (( `b_media_asset` `m` left join  `b_mod_detail` `ed` on((`ed`.`media_id` = `m`.`id`))) left join  `b_mod_master` `em` on((`em`.`id` = `ed`.`event_id`))) where (`m`.`release_date` <= (now() + interval -(3) month)) group by `m`.`id`;

-- Promotion movies
CREATE OR REPLACE VIEW  `mvPromotion_vw` AS select `ed`.`event_id` AS `event_id`,`ma`.`id` AS `mediaId`,`ma`.`title` AS `title`,`ma`.`image` AS `image`,`ed`.`event_id` AS `eventId`,`ma`.`rating` AS `rating` from ( `b_media_asset` `ma` join  `b_mod_detail` `ed` on((`ed`.`media_id` = `ma`.`id`))) where `ed`.`event_id` in (select `ed`.`event_id` from ( `b_mod_master` `em` join  `b_mod_detail` `ed` on((`em`.`id` = `ed`.`event_id`))) group by `ed`.`event_id` having (count(`ed`.`event_id`) > 1));


-- Watched movies
CREATE OR REPLACE VIEW  `mvWatched_vw` AS select `m`.`id` AS `mediaId`,`m`.`title` AS `title`,`m`.`image` AS `image`,`m`.`rating` AS `rating`,'W' AS `assetTag`,`m`.`release_date` AS `release_date`,`ed`.`event_id` AS `eventId`,count(`eo`.`id`) AS `COUNT(eo.id)` from (( `b_media_asset` `m` join  `b_mod_detail` `ed` on((`m`.`id` = `ed`.`media_id`))) join  `b_modorder` `eo` on((`eo`.`event_id` = `ed`.`event_id`))) order by 6 desc;
 -- Event ORder View 
CREATE OR REPLACE VIEW `event_orders_vw` AS SELECT `ma`.`content_provider` AS `CONTENT PROVIDER`, `ma`.`type` AS `TYPE`, `ma`.`title` AS `TITLE`, `ml`.`language_id` AS `LANGUAGE ID`, `ml`.`format_type` AS `FORMAT TYPE`, `ml`.`location` AS `LOCATION`,`em`.`event_name` AS `EVENT NAME`,`em`.`status` AS `STATUS`,cast(`em`.`event_validity` AS date) AS `EVENT VALIDITY`,cast(`eo`.`event_bookeddate` AS date) AS `EVENT BOOKED DATE`  FROM ((((`b_mediaasset_location` `ml` JOIN `b_media_asset` `ma` ON ((`ml`.`media_id` = `ma`.`id`))) JOIN `b_mod_detail` `ed` ON ((`ed`.`media_id` = `ma`.`id`))) JOIN `b_mod_master` `em` ON ((`ed`.`event_id` = `em`.`id`))) JOIN `b_modorder` `eo` ON ((`eo`.`event_id` = `em`.`id`)));
-- top movies view
CREATE OR REPLACE VIEW `top_movies_vw` AS SELECT `ma`.`title` AS `TITLE`, `ma`.`id` AS `ID`, `ma`.`type` AS `TYPE`, cast(`ma`.`release_date` AS date) AS `RELEASE DATE`,`ma`.`content_provider` AS `CONTENT PROVIDER`,`ma`.`rating` AS `RATING`,`ma`.`rating_count` AS `RATING COUNT`,count(`eo`.`id`) AS `ORDER CNT`  FROM (((`b_media_asset` `ma` JOIN `b_mod_detail` `ed` ON ((`ma`.`id` = `ed`.`media_id`))) JOIN `b_mod_master` `em` ON ((`em`.`id` = `ed`.`event_id`))) JOIN `b_modorder` `eo` ON ((`ed`.`event_id` = `eo`.`event_id`))) ORDER BY 8 DESC;

SET SQL_SAFE_UPDATES = 1;
SET foreign_key_checks = 1;





