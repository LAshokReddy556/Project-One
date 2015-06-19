CREATE TABLE IF NOT EXISTS `b_item_price` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `item_id` int(20) NOT NULL,
  `region_id` varchar(30) NOT NULL,
  `price` decimal(22,6) NOT NULL,
  `is_deleted` char(1) DEFAULT 'N',
  PRIMARY KEY (`id`),
  UNIQUE KEY `itemid_with_region_uniquekey` (`item_id`,`region_id`),
  KEY `fk_item_price_id` (`item_id`),
  CONSTRAINT `fk_item_price_id` FOREIGN KEY (`item_id`) REFERENCES `b_item_master` (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8;


-- Price Region 
insert ignore into b_priceregion_master (id,priceregion_code,priceregion_name,createdby_id,created_date,is_deleted) 
 VALUES (null,'Default','Default Region',null,null,'N');
 
 insert ignore	 into b_priceregion_detail (priceregion_id,country_id,state_id,is_deleted)
select prm.id,0,0,'N' from b_priceregion_master prm where prm.priceregion_code ='Default';


Drop procedure IF EXISTS regionIdToItemAudit; 
DELIMITER //
create procedure regionIdToItemAudit() 
Begin
  IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE COLUMN_NAME = 'region_id'
     and TABLE_NAME = 'b_item_audit'
     and TABLE_SCHEMA = DATABASE())THEN
ALTER TABLE b_item_audit ADD region_id VARCHAR(20) NOT NULL AFTER itemmaster_id ;
END IF;
END //
DELIMITER ;
call regionIdToItemAudit();
Drop procedure IF EXISTS regionIdToItemAudit;


DELIMITER //
create procedure addsessionupdatecolumn() 
Begin
IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE COLUMN_NAME = 'session_lastupdate'
     and TABLE_NAME = 'b_login_history'
     and TABLE_SCHEMA = DATABASE())THEN
Alter  table b_login_history add column session_lastupdate datetime DEFAULT NULL;
END IF;
END //
DELIMITER ;
call addsessionupdatecolumn();
Drop procedure IF EXISTS addsessionupdatecolumn; 

ALTER TABLE b_item_audit MODIFY COLUMN region_id VARCHAR(20) DEFAULT NULL;


Drop procedure IF EXISTS addInvoiceInDeviceSale; 
DELIMITER //
create procedure addInvoiceInDeviceSale() 
Begin
IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE COLUMN_NAME = 'invoice_id'
     and TABLE_NAME = 'b_onetime_sale'
     and TABLE_SCHEMA = DATABASE())THEN
alter table b_onetime_sale ADD column `invoice_id` bigint(20) DEFAULT NULL after `bill_id`;
alter table b_onetime_sale ADD INDEX `fk_ots_iid` (`invoice_id`) ;
END IF;
END //
DELIMITER ;
call addInvoiceInDeviceSale();
Drop procedure IF EXISTS addInvoiceInDeviceSale; 



set @id=(select id from b_priceregion_master where priceregion_code='Default'); 
insert ignore into b_item_price (id,item_id,region_id,price,is_deleted) 
   select null,id,@id,unit_price,'N' from b_item_master;
  

