SET SQL_SAFE_UPDATES = 0;

INSERT IGNORE INTO job VALUES(null, 'AGINGDISTRIBUTION','Aging Distribution Data','0 30 23 1/1 * ? *', 'Daily once at Midnight', '0001-01-01 00:00:00', '5', NULL, '2014-03-07 14:40:12', NULL, 'AGINGDISTRIBUTIONJobDetaildefault _ DEFAULT', NULL, '0', '0', '1', '0', '0', '1');


drop procedure if exists proc_distrib ;
delimiter //

CREATE  PROCEDURE `proc_distrib`()
BEGIN

SET SQL_SAFE_UPDATES = 0;
delete from b_distribution ;

insert into b_distribution
select @Did:=@Did+1 as Dist_Id,client_id,r1 bid_sn,
bilid as bid,r2 as pid_sn,date(bill_date) bill_date,OBal,round(Due_Amount,2) Due_Amount ,
pid, date(payment_date) payment_date,
round(amount_paid,2) amount_paid,Cl_bal,due_Status,now() Dist_date
from
(  

 select 
	bm.Client_id,bm.r1,bm.id as bilid,bp.r2,bm.Bill_date,
	@ob:=if (@cid=bm.client_id,@cb,0) as OBal,
	if (@bid=bm.id,0,bm.Due_amount) Due_amount,
	if (@pid=bp.id,null,bp.id) pid,
	if (@pid=bp.id,null,bp.payment_date )payment_date ,
    if (@pid=bp.id,0,bp.amount_paid ) amount_paid,
	@cb:=@ob+if (@bid=bm.id,0,bm.Due_amount) - if (@pid=bp.id,0,ifnull(bp.amount_paid,0) ) as Cl_Bal,
	@cid:=bm.client_id as CID,
	@bid:=bm.id as bill_id,         
	if ( ifnull(@ob,0)+if (@bid=bm.id,0,bm.Due_amount) - if (@pid=bp.id,0,bp.amount_paid ) > 0,True,False ) due_Status,
	@pid:=bp.id 
from
(select @ob:=0,@cb:=0,@lastCB:=0,@cid:='',@bid:='',@pid:='') ob 
join
(select @r1:=if(@r0=a.client_id,@r1+1,1) as r1,a.*,@r0:=a.client_id
	from b_bill_master a,(SELECT @r1:=1) b,(SELECT @r0:='') c
	order by a.client_id,a.id) bm
left join
( select @r2:=if(@r0=a.client_id,@r2+1,1) as r2,a.*,@r0:=a.client_id
	from b_payments a,(SELECT @r2:=1) b,(SELECT @r0:='') c
	where a.is_deleted='0'
	order by a.client_id,a.id ) bp
on (bm.Client_id=bp.client_id   and bm.r1=bp.r2 )
where ( @ob:=if (@cid=bm.client_id,@cb,0) +if (@bid=bm.id,0,bm.Due_amount) ) > 0
order by bm.client_id,bm.id,bp.id,bp.payment_date

) Dist  
join (select @Did:=0) d

;
SET SQL_SAFE_UPDATES = 1;
END //

delimiter;

SET SQL_SAFE_UPDATES = 0;

Insert ignore into b_provisioning_actions(id,provision_type,action,provisioning_system,is_enable,is_delete) 
 values(null,'Create Nas','CREATE NAS','Radius','Y','N');

Insert ignore into b_provisioning_actions(id,provision_type,action,provisioning_system,is_enable,is_delete) 
 values(null,'Create RadSevice','CREATE RADSERVICE','Radius','Y','N');

Insert ignore into b_provisioning_actions(id,provision_type,action,provisioning_system,is_enable,is_delete) 
 values(null,'Change Credentials','CHANGE CREDENTIALS','Radius','Y','N');

DELIMITER //
create procedure provisioningActions() 
Begin
IF EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE COLUMN_NAME = 'provision_type'
     and TABLE_NAME = 'b_provisioning_actions'
     and TABLE_SCHEMA = DATABASE())THEN
Alter  table b_provisioning_actions modify column provision_type varchar(30) NOT NULL;
END IF;
END //
DELIMITER ;
call provisioningActions();
Drop procedure IF EXISTS provisioningActions; 

SET SQL_SAFE_UPDATES = 1;

