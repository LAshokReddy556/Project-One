SET SQL_SAFE_UPDATES=0;
SET foreign_key_checks=0;

SET @offId=(SELECT id FROM stretchy_parameter where parameter_label='Office');
SET @startDate=(SELECT id FROM stretchy_parameter where parameter_label='startDate');
SET @endDate=(SELECT id FROM stretchy_parameter where parameter_label='endDate');
SET @modeId=(SELECT id FROM stretchy_parameter where parameter_label='Payment Mode');

update stretchy_report set report_name='Paymode Collection Chart',report_category='Invoice&collections',report_sql='select 
    mcv.code_value as Paymode,
    sum(ifnull(pay.amount_paid, 0)) as Amount
from
    m_office off
        JOIN
    m_client clnt ON off.id = clnt.office_id
        JOIN
    b_payments pay ON clnt.id = pay.client_id
        JOIN 
   m_code_value mcv ON mcv.id=pay.paymode_id
 where (off.id = ''${officeId}'' OR -1 = ''${officeId}'' ) AND  DATE_FORMAT(pay.payment_date,''%Y-%m-%d'')  between ''${startDate}'' AND ''${endDate}''
group by pay.payment_date,mcv.code_value
order by pay.payment_date' where report_name='PaymodeCollection Chart';

SET @ID=(SELECT id FROM stretchy_report where report_name='Paymode Collection Chart');
insert ignore into stretchy_report_parameter(report_id,parameter_id,report_parameter_name)values (@ID,@startDate,'startDate');
insert ignore into stretchy_report_parameter(report_id,parameter_id,report_parameter_name)values (@ID,@endDate,'endDate');

SET @CID=(SELECT id FROM stretchy_report where report_name='Collection_Day_wise_Details');
update stretchy_report set report_sql='select
    off.office_type,
    off.name as officeName,
   DATE_FORMAT(pay.payment_date,''%Y-%m-%d'') as Date,
    mcv.code_value as payMode,
    clnt.display_name as clientName,
    sum(ifnull(pay.amount_paid, 0)) as Collection
from
    m_office off
        join
    m_client clnt ON off.id = clnt.office_id
        join
    b_payments pay ON clnt.id = pay.client_id
      join 
   m_code_value mcv ON mcv.id=pay.paymode_id
where (off.id = ''${officeId}''or -1 = ''${officeId}'')
and (pay.paymode_id = ''${paymode_id}'' or -1 = ''${paymode_id}'') 
group by pay.payment_date,pay.paymode_id
order by off.office_type , pay.payment_date' where report_name='Collection_Day_wise_Details';
insert ignore into stretchy_report_parameter(report_id,parameter_id,report_parameter_name)values (@CID,@offId,'Office');
insert ignore into stretchy_report_parameter(report_id,parameter_id,report_parameter_name)values (@CID,@modId,'Payment Mode');

  Insert ignore into m_role_permission 

Select (Select id from m_role where name='selfcare') as rid,b.id from m_permission b where b.code = 'READ_CHARGECODE';

Insert ignore into m_role_permission 
Select (Select id from m_role where name='selfcare') as rid,b.id from m_permission b where b.code = 'DELETERECURRINGBILLING_PAYMENTGATEWAY';

Insert ignore into m_role_permission 
Select (Select id from m_role where name='selfcare') as rid,b.id from m_permission b where b.code = 'UPDATE_CLIENT';

Insert ignore into m_role_permission 
Select (Select id from m_role where name='selfcare') as rid,b.id from m_permission b where b.code = 'DISCONNECT_ORDER';

