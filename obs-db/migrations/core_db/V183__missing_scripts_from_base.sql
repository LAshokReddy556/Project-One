-- Create Tables
SET SQL_SAFE_UPDATES = 0;
CREATE TABLE IF NOT EXISTS `b_billing_rules` (
  `id` int(20) NOT NULL AUTO_INCREMENT,
  `billing_rule` varchar(30) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `b_client_register` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `username` varchar(100) NOT NULL,
  `generate_key` varchar(33) NOT NULL,
  `status` varchar(10) NOT NULL,
  `payment_data` varchar(500) DEFAULT 'NULL',
  `payment_status` varchar(15) DEFAULT 'INACTIVE',
  `createdby_id` bigint(20) DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  `lastmodifiedby_id` bigint(20) DEFAULT NULL,
  `lastmodified_date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `generate_key` (`generate_key`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='latin1_swedish_ci';



-- Modify Columns
alter table b_client_balance modify `balance_amount` decimal(24,4) NOT NULL default '0.0000';
alter table b_clientuser modify `auth_pin` varchar(20) default NULL; 
alter table b_login_history modify `status` varchar(10) default NULL;
alter table b_message_data modify `msgtemplate_id` bigint(20) default NULL;
alter table b_bill_details modify  `plan_code` varchar(50) DEFAULT NULL;
alter table b_message_data modify `header` varchar(255) default NULL;
alter table b_message_template modify `template_description` varchar(50) NOT NULL;
alter table b_office_commission modify `source` int(10) NOT NULL;
alter table b_office_commission modify `share_amount` decimal(6,2) NOT NULL;
alter table b_office_commission modify `share_type` varchar(10) NOT NULL;
alter table b_payments modify `is_sub_payment` char(2) default 'N';
alter table b_process_request_detail modify `hardware_id` varchar(50) default NULL;
alter table b_process_request_detail modify `sent_message` text default NULL;
alter table b_provisioning_actions modify `action` varchar(25) NOT NULL;
alter table b_charge_tax modify `tax_value` BIGINT(20) default NULL;



-- Add Columns
Drop procedure IF EXISTS addb_groupcoloumns;
DELIMITER //
create procedure addb_groupcoloumns() 
Begin
IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE COLUMN_NAME = 'address_no'
     and TABLE_NAME = 'b_group'
     and TABLE_SCHEMA = DATABASE())THEN
alter  table b_group add column `address_no` varchar(150) default NULL;
alter  table b_group add column `street` varchar(150) default NULL;
alter  table b_group add column `zip` varchar(20) default NULL;
alter  table b_group add column `city` varchar(200) default NULL;
alter  table b_group add column `state` varchar(200) default NULL;
alter  table b_group add column `country` varchar(200) default NULL;
END IF;
END //
DELIMITER ;
call addb_groupcoloumns();
Drop procedure IF EXISTS addb_groupcoloumns;

Drop procedure IF EXISTS addb_itemsalecoloumns;
DELIMITER //
create procedure addb_itemsalecoloumns() 
Begin
IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE COLUMN_NAME = 'agent_id'
     and TABLE_NAME = 'b_itemsale'
     and TABLE_SCHEMA = DATABASE())THEN
alter  table b_itemsale add column `agent_id` bigint(20) default '0';
END IF;
END //
DELIMITER ;
call addb_itemsalecoloumns();
Drop procedure IF EXISTS addb_itemsalecoloumns;

Drop procedure IF EXISTS plancodekeyDrop; 
DELIMITER //
create procedure plancodekeyDrop() 
Begin
  IF  EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS WHERE
`CONSTRAINT_CATALOG` = 'def' AND `TABLE_SCHEMA` = DATABASE() 
AND `TABLE_NAME` = 'b_bill_details' AND CONSTRAINT_NAME='fk_bdtl_pc')THEN 
ALTER TABLE b_bill_details DROP FOREIGN KEY `fk_bdtl_pc`;
ALTER TABLE b_bill_details DROP FOREIGN KEY `fk_bdtl_sc`;
END IF;
END //
DELIMITER ;
call plancodekeyDrop();
Drop procedure IF EXISTS plancodekeyDrop;

Drop procedure IF EXISTS contractDurationCode; 
DELIMITER //
create procedure contractDurationCode() 
Begin
IF NOT EXISTS (
     SELECT * FROM information_schema.TABLE_CONSTRAINTS
     WHERE  CONSTRAINT_NAME = 'dur_uni_code' and TABLE_NAME = 'b_contract_period'
     and TABLE_SCHEMA = DATABASE())THEN
     alter table b_contract_period modify `contract_type` varchar(50) NOT NULL;
ALTER TABLE `b_contract_period` ADD CONSTRAINT UNIQUE KEY `dur_uni_code` (`contract_type`,`contract_duration`);
END IF;
END //
DELIMITER ;
call contractDurationCode();
Drop procedure IF EXISTS contractDurationCode;
  
Drop procedure IF EXISTS addb_invoicecolumnonetimesale;
DELIMITER //
create procedure addb_invoicecolumnonetimesale() 
Begin
IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE COLUMN_NAME = 'invoice_id'
     and TABLE_NAME = 'b_onetime_sale'
     and TABLE_SCHEMA = DATABASE())THEN
alter  table b_onetime_sale add column `invoice_id` bigint(20) DEFAULT NULL;
END IF;
END //
DELIMITER ;
call addb_invoicecolumnonetimesale();
Drop procedure IF EXISTS addb_invoicecolumnonetimesale;


Drop procedure IF EXISTS addb_loginhistorycolumn;
DELIMITER //
create procedure addb_loginhistorycolumn() 
Begin
IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE COLUMN_NAME = 'session_lastupdate'
     and TABLE_NAME = 'b_login_history'
     and TABLE_SCHEMA = DATABASE())THEN
alter  table b_login_history add column `session_lastupdate` datetime default NULL;
END IF;
END //
DELIMITER ;
call addb_loginhistorycolumn();
Drop procedure IF EXISTS addb_loginhistorycolumn;

Drop procedure IF EXISTS addb_message_datacolumns;
DELIMITER //
create procedure addb_loginhistorycolumn() 
Begin
IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE COLUMN_NAME = 'attachment'
     and TABLE_NAME = 'b_message_data'
     and TABLE_SCHEMA = DATABASE())THEN
alter  table b_message_data add column `attachment` varchar(200) default NULL;
alter  table b_message_data add column `template_id` bigint(20) default NULL;
END IF;
END //
DELIMITER ;
call addb_loginhistorycolumn();
Drop procedure IF EXISTS addb_loginhistorycolumn;

Drop procedure IF EXISTS addb_templateColumn;
DELIMITER //
create procedure addb_templateColumn() 
Begin
IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE COLUMN_NAME = 'is_deleted'
     and TABLE_NAME = 'b_template'
     and TABLE_SCHEMA = DATABASE())THEN
alter  table b_template add `is_deleted` char(1) default 'N';
END IF;
END //
DELIMITER ;
call addb_templateColumn();
Drop procedure IF EXISTS addb_templateColumn;

Drop procedure IF EXISTS addm_officeagreementdetailColumn;
DELIMITER //
create procedure addm_officeagreementdetailColumn() 
Begin
IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE COLUMN_NAME = 'status'
     and TABLE_NAME = 'm_office_agreement_detail'
     and TABLE_SCHEMA = DATABASE())THEN
alter  table m_office_agreement_detail add `status` bigint(20) default '1';
END IF;
END //
DELIMITER ;
call addm_officeagreementdetailColumn();
Drop procedure IF EXISTS addm_officeagreementdetailColumn;

Drop procedure IF EXISTS modifyb_ticketdetailsconstraint;
DELIMITER //
create procedure modifyb_ticketdetailsconstraint() 
Begin
IF EXISTS(
SELECT * FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS WHERE
`CONSTRAINT_CATALOG` = 'def' AND `TABLE_SCHEMA` = DATABASE() 
AND `TABLE_NAME` = 'b_ticket_details' AND CONSTRAINT_NAME='fk_tdl_usr')THEN
ALTER TABLE b_ticket_details DROP FOREIGN KEY `fk_tdl_usr`;
ALTER TABLE `b_ticket_details` ADD CONSTRAINT `fk_tdl_usr`
 FOREIGN KEY ( `createdby_id` ) REFERENCES `m_appuser` ( `id` ) ON DELETE NO ACTION ON UPDATE NO ACTION;
END IF;

END //
DELIMITER ;
call modifyb_ticketdetailsconstraint();
Drop procedure IF EXISTS modifyb_ticketdetailsconstraint;

Drop procedure IF EXISTS modifyb_ticketdetailsconstraint;
DELIMITER //
create procedure modifyb_ticketdetailsconstraint() 
Begin
IF EXISTS(
SELECT * FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS WHERE
`CONSTRAINT_CATALOG` = 'def' AND `TABLE_SCHEMA` = DATABASE() 
AND `TABLE_NAME` = 'b_ticket_details' AND CONSTRAINT_NAME='fk_tdl_usr')THEN
ALTER TABLE b_ticket_details DROP FOREIGN KEY `fk_tdl_usr`;
ALTER TABLE `b_ticket_details` ADD CONSTRAINT `fk_tdl_usr`
 FOREIGN KEY ( `createdby_id` ) REFERENCES `m_appuser` ( `id` ) ON DELETE NO ACTION ON UPDATE NO ACTION;
END IF;
END //
DELIMITER ;
call modifyb_ticketdetailsconstraint();
Drop procedure IF EXISTS modifyb_ticketdetailsconstraint;

Drop procedure IF EXISTS modifyb_ticketmasterconstraint;
DELIMITER //
create procedure modifyb_ticketmasterconstraint() 
Begin
IF EXISTS(
SELECT * FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS WHERE
`CONSTRAINT_CATALOG` = 'def' AND `TABLE_SCHEMA` = DATABASE() 
AND `TABLE_NAME` = 'b_ticket_master' AND CONSTRAINT_NAME='fk_tm_assgn')THEN
ALTER TABLE b_ticket_master DROP FOREIGN KEY `fk_tm_assgn`;
ALTER TABLE b_ticket_master DROP FOREIGN KEY `fk_tm_usr`;
ALTER TABLE `b_ticket_master` ADD CONSTRAINT `fk_tm_assgn`
 FOREIGN KEY ( `assigned_to` ) REFERENCES `m_appuser` ( `id` ) ON DELETE NO ACTION ON UPDATE NO ACTION;
 ALTER TABLE `b_ticket_master` ADD CONSTRAINT `fk_tm_usr`
 FOREIGN KEY ( `createdby_id` ) REFERENCES `m_appuser` ( `id` ) ON DELETE NO ACTION ON UPDATE NO ACTION;
END IF;

END //
DELIMITER ;
call modifyb_ticketmasterconstraint();
Drop procedure IF EXISTS modifyb_ticketmasterconstraint;
Drop procedure IF EXISTS addsubnettoippool;
DELIMITER //
create procedure addsubnettoippool() 
Begin
IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE COLUMN_NAME = 'subnet'
     and TABLE_NAME = 'b_ippool_details'
     and TABLE_SCHEMA = DATABASE())THEN
Alter table b_ippool_details add column subnet bigint(10) DEFAULT NULL;
END IF;
IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE COLUMN_NAME = 'notes'
     and TABLE_NAME = 'b_ippool_details'
     and TABLE_SCHEMA = DATABASE())THEN
Alter table b_ippool_details add column notes varchar(60) DEFAULT NULL;
END IF;
IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE COLUMN_NAME = 'type'
     and TABLE_NAME = 'b_ippool_details'
     and TABLE_SCHEMA = DATABASE())THEN
Alter table b_ippool_details add column type bigint(20) DEFAULT NULL;
END IF;
END //
DELIMITER ;
call addsubnettoippool();
Drop procedure IF EXISTS addsubnettoippool;

Drop procedure if exists changedatauploadtablename;
DELIMITER //
CREATE PROCEDURE changedatauploadtablename()
BEGIN
IF EXISTS(Select * from INFORMATION_SCHEMA.TABLES where  TABLE_SCHEMA = DATABASE() and TABLE_NAME ='uploads_status') then
IF NOT EXISTS(Select * from INFORMATION_SCHEMA.TABLES where  TABLE_SCHEMA = DATABASE() and TABLE_NAME ='b_data_uploads') then
  SET @ddl = CONCAT('RENAME table uploads_status to b_data_uploads');
  PREPARE STMT FROM @ddl;
  EXECUTE STMT;
END IF; 
END IF;
END //
DELIMITER ;
call changedatauploadtablename();
truncate table b_eventaction_mapping;
Drop procedure if exists changedatauploadtablename;
Drop procedure IF EXISTS eventactionmappingunique; 
DELIMITER //
create procedure eventactionmappingunique() 
Begin
IF NOT EXISTS (SELECT * FROM information_schema.TABLE_CONSTRAINTS
     WHERE 
      TABLE_NAME = 'b_eventaction_mapping'   AND CONSTRAINT_NAME='event_action_name_code'
     and TABLE_SCHEMA = DATABASE())THEN
ALTER TABLE `b_eventaction_mapping` ADD CONSTRAINT `event_action_name_code` UNIQUE (`event_name`,`action_name`);
END IF;
END //
DELIMITER ;
call eventactionmappingunique();
Drop procedure IF EXISTS eventactionmappingunique;



Drop procedure IF EXISTS configurationalter; 
DELIMITER //
create procedure configurationalter() 
Begin
IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE COLUMN_NAME = 'name' and COLUMN_KEY = 'UNI' and IS_NULLABLE ='NO'
     and TABLE_NAME = 'c_configuration'
     and TABLE_SCHEMA = DATABASE())THEN
ALTER TABLE `c_configuration` MODIFY name varchar(50) NOT NULL;
ALTER TABLE `c_configuration` ADD CONSTRAINT `name_config` UNIQUE (`name`);
END IF;
END //
DELIMITER ;
call configurationalter();
Drop procedure IF EXISTS configurationalter;


/* Depricated Tables
1.b_plan_qualifier
2.b_partner_settlement
*/


-- Procedures
DROP PROCEDURE IF EXISTS  custom_validation;
DELIMITER //
CREATE PROCEDURE  `custom_validation`(p_userid INT,p_clientid INT,
jsonstr VARCHAR(1000),event_name VARCHAR(200), out err_code INT,out err_msg VARCHAR(4000))
SWL_return:
BEGIN
If event_name='Event Order' then
begin
 Declare veventid int;
 Declare fmttype varchar(10);
 Declare optype varchar(10); 
 Declare isnewplan varchar(10); 
 Declare vprice double(20,2);
 Declare cbalance double(20,2);
 SET @optype= common_schema.extract_json_value(jsonstr, '//optType');
 Select price into vprice from b_mod_pricing where event_id=veventid and format_type=fmttype and Opt_type=optype and is_deleted='N' ;
 SET @cbalance=IFNULL((select balance_amount from b_client_balance where client_id=p_clientid),0);
if cbalance-vprice<0 then 
        SET err_code = 0;
        SET err_msg = '';
else
        SET err_code = 0;
        SET err_msg = '';
end if;

end;


ELSEIF event_name='Rental' then
begin

  Declare v_allow2 INT default 0;
 Select count(0) into v_allow2 from b_onetime_sale where client_id=p_clientid and is_deleted = 'N';
if v_allow2 > 0 then 
      
        SET err_code = 0;
        SET err_msg = '';
        select concat("erro ",err_code);
else
        SET err_code = 0;
        SET err_msg  = '';
         
end if;

end;

ELSEIF event_name='Order Booking' then
   begin
   DECLARE v_status INT ;
   Declare v_allow INT default 0;
   Declare vofficetype INT ;
   Declare vcontractPeriod int;
   Declare vpaytermCode varchar(20);  
   SET err_code = 0;
   SET err_msg = NULL ;
   
   
Select count(0) into v_allow 
from b_orders where  client_id =p_clientid and order_status =1 ;

if (v_allow>0 and  common_schema.extract_json_value(jsonstr,'//isNewplan') = 'true')  then
  SET err_code = 0;
  SET err_msg = '' ;  
else
  Select count(0) into v_allow 
  from b_orders where plan_id=common_schema.extract_json_value(jsonstr, '//planCode') and client_id =p_clientid and order_status =3;
  if (v_allow>0 and  common_schema.extract_json_value(jsonstr,'//isNewplan') = 'true')   then
    SET err_code = 0;
    SET err_msg = '' ;  
  else
  Select count(0) into v_allow 
 from b_orders where  client_id =p_clientid and order_status =4;
  if v_allow>0 then
    SET err_code = 0;
    SET err_msg = '' ;  
  end if;
    end if; 
end if;
select concat("end ",err_code);
if (err_code <> 0) then LEAVE SWL_return;
    end if;
    end;
else
        SET err_code = 0;
        SET err_msg = '';
end if;
 
END//
DELIMITER ;

-- ------

DROP PROCEDURE IF EXISTS  workflow_events;
DELIMITER //
CREATE PROCEDURE `workflow_events`(IN  clientid     INT,
										IN  eventname    varchar(60),
										IN  actionname   varchar(60),
										IN  resourceid   varchar(20),
										OUT strjson      text,
										OUT result       char(5))
BEGIN
		  DECLARE orderid       int(20) DEFAULT 0;
		  DECLARE paidamt       decimal(20, 2) DEFAULT 0;
		  DECLARE planid        int(5) DEFAULT 0;
		  DECLARE orderaction   varchar(25) DEFAULT 0;
		  DECLARE paymodeid     int(5) DEFAULT 0;
		  DECLARE assignedid     int(5) DEFAULT 0;
		  DECLARE receiptno     varchar(20) DEFAULT 0;
		  DECLARE emailid     varchar(120) DEFAULT 0;
		  DECLARE finish BOOLEAN DEFAULT 0;
		  DECLARE CONTINUE HANDLER FOR NOT FOUND SET finish = TRUE;
	If (eventname='Create Ticket' or eventname='Add Comment' or eventname='Close Ticket') and actionname='Send Email' then
		Select assigned_to into assignedid from  b_ticket_master  WHERE id = resourceid; 
		set emailid='rakesh.adlagatta@gmail.com';
		SET result = 'true';
		SET strjson = Concat("Email_Id:",emailid);
    else
        SET result = 'true';
		SET strjson =Concat("result:",true);
	End if;	
    
	END //
DELIMITER ;


	
-- data
Update m_permission set grouping='client&orders' where grouping like '%Client&orders%';
Update m_permission set grouping='inventory' where grouping like '%Inventory%';
Update m_permission set grouping='client&orders' where grouping like '%Ordering%';
Update m_permission set grouping='organisation' where grouping like '%Organization%';
insert ignore into m_permission values(null,'Organisation','CREATE_IPPOOLMANAGEMENT','IPPOOLMANAGEMENT','CREAT',0);
insert ignore into m_permission values(null,'client&orders','DISCONNECT_ORDER','ORDER','DISCONNECT',0);
insert ignore into `m_appuser`(`id`,`is_deleted`,`office_id`,`staff_id`,`username`,`firstname`,`lastname`,`password`,`email`,`firsttime_login_remaining`,`nonexpired`,`nonlocked`,`nonexpired_credentials`,`enabled`) values (0,1,1,null,'jobs','jobs','obs','1c03139549fd730b00ed181d9f1b27ec99eb20a02d9b31586a3f3db6a022d737','info@openbillingsystem.com',1,0,1,1,0);
update m_appuser set id=0 where username='jobs';

insert ignore into `r_enum_value`(`enum_name`,`enum_id`,`enum_message_property`,`enum_value`) values ('order_status',1,'Active','Active');
insert ignore into `r_enum_value`(`enum_name`,`enum_id`,`enum_message_property`,`enum_value`) values ('order_status',2,'Cancelled','Cancelled');
insert ignore into `r_enum_value`(`enum_name`,`enum_id`,`enum_message_property`,`enum_value`) values ('order_status',3,'Disconnected','Disconnected');
insert ignore into `r_enum_value`(`enum_name`,`enum_id`,`enum_message_property`,`enum_value`) values ('order_status',4,'PENDING','PENDING');
insert ignore into `r_enum_value`(`enum_name`,`enum_id`,`enum_message_property`,`enum_value`) values ('order_status',5,'TERMINATED','TERMINATED');
insert ignore into `r_enum_value`(`enum_name`,`enum_id`,`enum_message_property`,`enum_value`) values ('order_status',6,'SUSPENDED','SUSPENDED');
insert ignore into `r_enum_value`(`enum_name`,`enum_id`,`enum_message_property`,`enum_value`) values ('order_status',7,'REACTIVE','REACTIVE');


 insert ignore into b_office_bank values(null, 'Open Billing System', '2044860011', '2044860008', 'Hyderabad', 'KVB(karur vysya bank)', 'Kondapur', 'KVBL0004801 ');
insert ignore into b_office_address values(null, 'OBS', 'OPen Billing System', 'Kondapur', 'Hyderabad', 'Andhra Pradesh', 'India', '30214', '858 935 8804', NULL, 'info@openbillingsystem.com', 'companyLogo.jpg', NULL, NULL, '1');

insert ignore into r_enum_value VALUES ('device_swap',1,'Replacement','Replacement');



-- insert ignore into c_paymentgateway_conf values(null,'is-paypal',1,'{"clientId":AZqG2RCYDJtB9b1J3Qz-uZIzrg9uFTh_RjV8NaupF3RXoXJVzKhI3kqDvSvm,"secretCode" : "EJURWhCrRD1e580Wpk2gRRs56ZNyGUduwaCtDSAvKv_qpaoN9GePsmIjsndP"}');
-- insert ignore into c_paymentgateway_conf values(null,'is-paypal-for-ios',1,'{"clientId":AZqG2RCYDJtB9b1J3Qz-uZIzrg9uFTh_RjV8NaupF3RXoXJVzKhI3kqDvSvm,"secretCode" : "EJURWhCrRD1e580Wpk2gRRs56ZNyGUduwaCtDSAvKv_qpaoN9GePsmIjsndP"}');

