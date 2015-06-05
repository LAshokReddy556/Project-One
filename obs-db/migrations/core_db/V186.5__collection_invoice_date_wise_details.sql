SET SQL_SAFE_UPDATES = 0;
SET foreign_key_checks = 0;
-- collection_date_wise_details --
insert ignore into stretchy_report values(null, 'Collection_Date_Wise_Details', 'Table', '', 'Invoice&Collections', 'select
    mcv1.code_value as office_type,
    off.name as office_Name,
    clnt.id as Client_Id,
    clnt.display_name as Client_Name,
 DATE_FORMAT(pay.payment_date,''%Y-%m-%d'') as Date,
    mcv.code_value as Paymode,
    TRUNCATE(sum(ifnull(pay.amount_paid, 0)),2) as Amount_Collection
from
    m_office off
      Left join
    m_client clnt ON off.id = clnt.office_id
     Left join
    b_payments pay ON clnt.id = pay.client_id
     Left join
   m_code_value mcv ON mcv.id=pay.paymode_id
    Left join
    m_code_value mcv1 ON  off.office_type = mcv1.id
 where (off.id = ''${officeId}'' or -1 = ''${officeId}'') and (pay.paymode_id = ''${paymode_id}'' or -1 = ''${paymode_id}'' ) and pay.payment_date between ''${startDate}'' and ''${endDate}''
group by pay.payment_date,pay.paymode_id', 'Collections From Clients---Day wise details', '0', '1');

  UPDATE stretchy_report
  SET report_sql ='select
    mcv1.code_value as office_type,
    off.name as office_Name,
    clnt.id as Client_Id,
    clnt.display_name as Client_Name,
 DATE_FORMAT(pay.payment_date,''%Y-%m-%d'') as Date,
    mcv.code_value as Paymode,
    TRUNCATE(sum(ifnull(pay.amount_paid, 0)),2) as Amount_Collection
from
    m_office off
      Left join
    m_client clnt ON off.id = clnt.office_id
     Left join
    b_payments pay ON clnt.id = pay.client_id
     Left join
   m_code_value mcv ON mcv.id=pay.paymode_id
    Left join
    m_code_value mcv1 ON  off.office_type = mcv1.id
where (off.id = ''${officeId}'' or -1 = ''${officeId}'') and (pay.paymode_id = ''${paymode_id}'' or -1 = ''${paymode_id}'' ) and pay.payment_date between ''${startDate}'' and ''${endDate}''
group by pay.payment_date,pay.paymode_id'
where report_name='Collection_Date_Wise_Details';

-- collection_date_wise_details parameters --

SET @id=(select id from stretchy_report where report_name='Collection_Date_Wise_Details');
SET @offId=(SELECT id FROM stretchy_parameter where parameter_label='Office');
insert ignore into stretchy_report_parameter(report_id,parameter_id,report_parameter_name)values (@id,@offId,'Office');

SET @id = (select id from stretchy_report where report_name='Collection_Date_Wise_Details');
SET @payModeId=(SELECT id FROM stretchy_parameter where parameter_label='Payment Mode');
insert ignore into stretchy_report_parameter(report_id,parameter_id,report_parameter_name)values (@id,@payModeId,'Payment Mode');

SET @id=(select id from stretchy_report where report_name='Collection_Date_Wise_Details');
SET @DATEID=(SELECT id FROM stretchy_parameter where parameter_label='StartDate');
insert ignore into stretchy_report_parameter(report_id,parameter_id,report_parameter_name)values (@id,@DATEID,'StartDate');

SET @id=(select id from stretchy_report where report_name='Collection_Date_Wise_Details');
SET @DATEID=(SELECT id FROM stretchy_parameter where parameter_label='EndDate');
insert ignore into stretchy_report_parameter(report_id,parameter_id,report_parameter_name)values (@id,@DATEID,'EndDate');


-- Invoice_Date_wise_Details --

 insert ignore into stretchy_report values(null, 'Invoice_Date_Wise_Details', 'Table', '', 'Invoice&Collections', 'SELECT
             clnt.id as ''Client Id'',
              cast(clnt.display_name as char charset utf8) as Client_Name,   
             DATE_FORMAT(inv.invoice_date, ''%Y-%m-%d'') as Invoice_date,
              charge.charge_type as chargetype,      
              cast(TRUNCATE(charge.charge_amount,2) as char charset utf8) as charge_Amount,
              cast(TRUNCATE(charge.discount_amount,2) as char charset utf8) as discount,
               cast(TRUNCATE(ctx.tax_amount,2) as char charset utf8) as tax_Amount,
              cast(inv.invoice_amount as char charset utf8) as invoice_Amount
FROM 
      m_office off
      JOIN
      m_client clnt ON off.id = clnt.office_id
      JOIN
      b_invoice inv  ON clnt.id = inv.client_id
       JOIN
      b_charge charge ON inv.id = charge.invoice_id AND charge.client_id = inv.client_id
       LEFT JOIN
      b_charge_tax ctx ON charge.invoice_id = ctx.invoice_id 
    where (off.id = ''${officeId}'' or -1 = ''${officeId}'')  AND inv.invoice_date between ''${startDate}'' and ''${endDate}''
 GROUP BY inv.invoice_date,inv.client_id', 'Invoice_Date_Wise_Details', '0', '1');

  UPDATE stretchy_report
  SET report_sql ='SELECT
             clnt.id as ''Client Id'',
              cast(clnt.display_name as char charset utf8) as Client_Name,   
             DATE_FORMAT(inv.invoice_date, ''%Y-%m-%d'') as Invoice_date,
              charge.charge_type as chargetype,      
              cast(TRUNCATE(charge.charge_amount,2) as char charset utf8) as charge_Amount,
              cast(TRUNCATE(charge.discount_amount,2) as char charset utf8) as discount,
               cast(TRUNCATE(ctx.tax_amount,2) as char charset utf8) as tax_Amount,
              cast(inv.invoice_amount as char charset utf8) as invoice_Amount
FROM 
      m_office off
      JOIN
      m_client clnt ON off.id = clnt.office_id
      JOIN
      b_invoice inv  ON clnt.id = inv.client_id
       JOIN
      b_charge charge ON inv.id = charge.invoice_id AND charge.client_id = inv.client_id
       LEFT JOIN
      b_charge_tax ctx ON charge.invoice_id = ctx.invoice_id 
    where (off.id = ''${officeId}'' or -1 = ''${officeId}'')  AND inv.invoice_date between ''${startDate}'' and ''${endDate}''
 GROUP BY inv.invoice_date,inv.client_id'
 where report_name='Invoice_Date_Wise_Details' ;

-- invoice_date_wise_details parameters --

SET @id=(select id from stretchy_report where report_name='Invoice_Date_Wise_Details');
SET @offId=(SELECT id FROM stretchy_parameter where parameter_label='Office');
insert ignore into stretchy_report_parameter(report_id,parameter_id,report_parameter_name)values (@id,@offId,'Office');

SET @id=(select id from stretchy_report where report_name='Invoice_Date_Wise_Details');
SET @DATEID=(SELECT id FROM stretchy_parameter where parameter_label='StartDate');
insert ignore into stretchy_report_parameter(report_id,parameter_id,report_parameter_name)values (@id,@DATEID,'StartDate');

SET @id=(select id from stretchy_report where report_name='Invoice_Date_Wise_Details');
SET @DATEID=(SELECT id FROM stretchy_parameter where parameter_label='EndDate');
insert ignore into stretchy_report_parameter(report_id,parameter_id,report_parameter_name)values (@id,@DATEID,'EndDate');
SET SQL_SAFE_UPDATES = 0;
SET foreign_key_checks = 0;