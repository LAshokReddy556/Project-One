
CREATE TABLE IF NOT EXISTS `m_office_agreement` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `office_id` bigint(20) NOT NULL,
  `agreement_status` varchar(20) NOT NULL,
  `start_date` datetime DEFAULT NULL,
  `end_date` datetime DEFAULT NULL,
  `createdby_id` int(5) DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  `lastmodified_date` datetime DEFAULT NULL,
  `lastmodifiedby_id` int(5) DEFAULT NULL,
  `is_deleted` char(1) NOT NULL DEFAULT 'N',
  PRIMARY KEY (`id`),
  KEY `fk_office_agreement` (`office_id`),
  CONSTRAINT `fk_office_agreement` FOREIGN KEY (`office_id`) REFERENCES `m_office` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
  ) ENGINE=InnoDB  DEFAULT CHARSET=utf8;
  
  CREATE TABLE IF NOT EXISTS `m_office_agreement_detail` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `agreement_id` bigint(20) NOT NULL,
  `source` bigint(30) NOT NULL,
  `share_amount` decimal(6,2) NOT NULL,
  `share_type` varchar(10) NOT NULL,
  `start_date` datetime DEFAULT NULL,
  `end_date` datetime DEFAULT NULL,
  `createdby_id` bigint(20) DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  `lastmodified_date` datetime DEFAULT NULL,
  `lastmodifiedby_id` bigint(20) DEFAULT NULL,
  `is_deleted` char(1) NOT NULL DEFAULT 'N',
    PRIMARY KEY (`id`),
  UNIQUE KEY `agreement_dtl_ai_ps_mc_uniquekey` (`agreement_id`,`source`),
  KEY `fk_agreement` (`agreement_id`),
  CONSTRAINT `fk_agreement_2` FOREIGN KEY (`agreement_id`) REFERENCES `m_office_agreement` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

Drop procedure if exists officeInfo;
DELIMITER //
create procedure officeInfo()
BEGIN
IF NOT EXISTS(SELECT * FROM INFORMATION_SCHEMA.COLUMNS
WHERE COLUMN_NAME ='is_collective' 
AND TABLE_NAME='m_office_additional_info'
AND TABLE_SCHEMA=DATABASE())THEN
Alter table m_office_additional_info add column is_collective char(2) DEFAULT 'N';
END IF;
END // 
DELIMITER ;
call officeInfo();
Drop procedure if exists officeInfo;


Drop procedure if exists officeAddress;
DELIMITER //
create procedure officeAddress()
BEGIN
IF EXISTS(SELECT * FROM INFORMATION_SCHEMA.COLUMNS
WHERE COLUMN_NAME ='company_logo' 
AND TABLE_NAME='b_office_address'
AND TABLE_SCHEMA=DATABASE())THEN
ALTER TABLE b_office_address CHANGE COLUMN  company_logo company_logo VARCHAR(500)  DEFAULT NULL;
END IF;
END // 
DELIMITER ;
call officeAddress();
Drop procedure if exists officeAddress;

set foreign_key_checks=0;

insert ignore into `stretchy_report`(`id`,`report_name`,`report_type`,`report_subtype`,`report_category`,`report_sql`,`description`,`core_report`,`use_report`) 
values (null,'Agent Commission Report','Table',null,'Client' ,'select * from v_agent_commission where `invoice_date`  between ''${startDate}'' and ''${endDate}''','Agent Commission Report',1,1);

SET @id=(select id from stretchy_report where report_name='Agent Commission Report');

insert ignore into stretchy_report_parameter(report_id,parameter_id,report_parameter_name)values (@id,1,'From Date');

insert ignore into stretchy_report_parameter(report_id,parameter_id,report_parameter_name)values (@id,2,'To Date');

CREATE TABLE IF NOT EXISTS `b_office_commission` (
  `id` int(20) NOT NULL AUTO_INCREMENT,
  `charge_id` int(20) NOT NULL DEFAULT '0',
  `office_id` bigint(20) NOT NULL,
  `invoice_date` datetime NOT NULL,
  `source` int(10),
  `share_amount` decimal(6,2),
  `share_type` varchar(10),
  `comm_source` varchar(100) CHARACTER SET utf8 DEFAULT NULL,
  `amt` double(19,2) DEFAULT NULL,
  `created_dt` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8;

INSERT IGNORE INTO job VALUES(null, 'RESELLERCOMMISSION', 'Reseller Commission', '0 0 12 1/1 * ? *', 'Daily once at Midnight', '2015-01-26 15:59:45', '5', NULL, '2015-01-26 15:59:45', NULL, 'RESELLERCOMMISSIONJobDetaildefault _ DEFAULT', NULL, '0', '0', '1', '0', '0', '1');

SET @id=(select id from job where name='RESELLERCOMMISSION');

INSERT IGNORE INTO job_parameters VALUES(null ,@id, 'processDate', 'DATE', 'NOW()', '26 January 2015', 'Y', NULL);
INSERT IGNORE INTO job_parameters VALUES(null, @id, 'reportName', 'COMBO', NULL, 'Reseller Commission', 'Y', NULL);





DROP PROCEDURE IF EXISTS proc_office_commission;
DELIMITER //
CREATE PROCEDURE proc_office_commission()
BEGIN
insert into b_office_commission
select null id,bc.id charge_id,
mc.office_id,bi.invoice_date,-- bc.charge_type,
oad.source,oad.share_amount,oad.share_type ,mcv.code_value comm_source,
case 
when oad.share_type='Flat' and bc.charge_type='NRC' then round(oad.share_amount,2)
when oad.share_type='Percentage' and bc.charge_type='NRC' then round(bc.netcharge_amount * oad.share_amount / 100,2) 
when oad.share_type='Percentage' and bc.charge_type <>'NRC' then round(bc.netcharge_amount * oad.share_amount / 100,2) 
else round(oad.share_amount,2) end amt ,now() created_dt
from b_charge bc
join b_invoice bi on (bc.invoice_id=bi.id)
join m_client mc on (bc.client_id=mc.id)
join m_office_agreement oa on (oa.office_id = mc.office_id)
join m_office_agreement_detail oad on (oad.agreement_id = oa.id and oad.is_deleted='N'
 and oad.source = 
 case when bc.charge_type ='NRC' then (select id from m_code_value cv where cv.code_value='Hardware') 
 when bc.charge_type ='RC' then (select id from m_code_value cv where cv.code_value='Subscriptions')  else (select id from m_code_value cv where cv.code_value='On-demand') end)
join m_code_value mcv on (mcv.id = oad.source and mcv.code_id=(select id from m_code mc where mc.code_name='Source Category'))
where 
bc.id not in 
 (select charge_id from b_office_commission)
and bc.id >= 
 ifnull( (select max(charge_id) from b_office_commission),0);
END //


