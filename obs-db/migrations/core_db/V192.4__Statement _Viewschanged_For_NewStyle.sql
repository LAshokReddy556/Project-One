
CREATE OR REPLACE VIEW  billdetails_v
 AS 
select 
    b.client_id AS client_id,
    a.id AS transId,
    b.invoice_date AS transDate,
    'SERVICE_CHARGES' AS transType,
    a.netcharge_amount AS amount,
    concat(date_format(a.charge_start_date, '%Y-%m-%d'),
            ' to ',
            date_format(a.charge_end_date, '%Y-%m-%d')) AS description,
    c.plan_id AS plan_code,
    a.charge_type AS service_code
from
    ((b_charge a
    join b_invoice b)
    join b_orders c)
where
    ((a.invoice_id = b.id) and (a.order_id = c.id) and isnull(a.bill_id) and (b.invoice_date <= now()) and (a.priceline_id >= 1)) 
union all select 
    b.client_id AS client_id,
    a.id AS transId,
    date_format(b.invoice_date, '%Y-%m-%d') AS transDate,
    'TAXES' AS transType,
    a.tax_amount AS amount,
    a.tax_code AS description,
    NULL AS plan_code,
    c.charge_type AS service_code
from
    ((b_charge_tax a
    join b_invoice b)
    join b_charge c)
where
    ((a.invoice_id = b.id) and (a.charge_id = c.id) and isnull(a.bill_id) and (b.invoice_date <= now())) 
union all select 
    b_adjustments.client_id AS client_id,
    b_adjustments.id AS transId,
    date_format(b_adjustments.adjustment_date,
            '%Y-%m-%d') AS transDate,
    'ADJUSTMENT' AS transType,
    (case b_adjustments.adjustment_type
        when 'DEBIT' then -(b_adjustments.adjustment_amount)
        when 'CREDIT' then (b_adjustments.adjustment_amount)
    end) AS amount,
    b_adjustments.remarks AS remarks,
    b_adjustments.adjustment_type AS adjustment_type,
    NULL AS service_code
from
    b_adjustments
where
    (isnull(b_adjustments.bill_id) and (b_adjustments.adjustment_date <= now())) 
union all select 
    pa.client_id AS client_id,
    pa.id AS transId,
    date_format(pa.payment_date, '%Y-%m-%d') AS transDate,
    concat('PAYMENT', ' - ', p.code_value) AS transType,
    pa.amount_paid AS invoiceAmount,
    pa.Remarks AS remarks,
    p.code_value AS code_value,
    NULL AS service_code
from
    (b_payments pa
    join m_code_value p)
where
    (isnull(pa.bill_id) and (pa.payment_date <= now()) and (pa.paymode_id = p.id)) 
union all select 
    b.client_id AS client_id,
    a.id AS transId,
    date_format(c.sale_date, '%Y-%m-%d') AS transDate,
    'ONETIME_CHARGES' AS transType,
    a.netcharge_amount AS amount,
    c.charge_code AS charge_code,
    c.item_id AS item_id,
    a.charge_type AS service_code
from
    ((b_charge a
    join b_invoice b)
    join b_onetime_sale c)
where
    ((a.invoice_id = b.id) and (a.order_id = c.id) and isnull(a.bill_id) and (c.sale_date <= now()) and (c.invoice_id = b.id) and (a.priceline_id = 0)) 
union all select 
    b.client_id AS client_id,
    a.id AS transId,
    b.invoice_date AS transDate,
    'SERVICE_TRANSFER' AS transType,
    a.netcharge_amount AS amount,
    concat(date_format(a.charge_start_date, '%Y-%m-%d'),
            ' to ',
            date_format(a.charge_end_date, '%Y-%m-%d')) AS description,
    ph.property_code AS plan_code,
    a.charge_type AS service_code
from
    ((b_charge a
    join b_invoice b)
    join b_property_history ph)
where
    ((a.invoice_id = b.id) and (a.order_id = ph.id) and isnull(a.bill_id) and (b.invoice_date <= now()) and (a.priceline_id = -(1)));



CREATE OR REPLACE view bmaster_vw
AS 
select distinct
    bm.id AS billId,
    bm.Bill_No AS billNo,
    bm.Client_id AS clientId,
    cast(bm.Bill_date as date) AS billDate,
    -- bm.Bill_startdate AS billStartdate,
    -- bm.Bill_enddate AS billEnddate,
    bm.Due_date AS dueDate,
    bm.Previous_balance AS previousBalance,
    bm.Charges_amount AS chargesAmount,
    bm.Adjustment_amount AS adjustmentAmount,
    round((bm.Tax_amount), 2) AS taxAmount,
    bm.Paid_amount AS paidAmount,
    bm.Due_amount AS dueAmount,
    -- round((bm.Due_amount + bm.Tax_amount), 2) totalBillAmount,
   -- bm.filename AS filename,
    bm.Promotion_description AS Description,
    sum(bc.discount_amount) AS discountAmount,
    ca.address_id AS addressId,
    ca.address_key AS addressKey,
    ca.address_no AS addressNo,
    ca.street AS street,
    ca.zip AS zip,
    ca.city AS city,
    ca.state AS state,
    ca.country AS country,
    concat(ca.street,
            ',',
            ca.city,
            ',',
            ca.state,
            ',',
            ca.country,
            '-',
            ca.zip) as address,
    c.account_no AS accountNo,
    c.external_id AS externalId,
    c.status_enum AS status,
    c.activation_date AS activationDate,
    c.office_id AS officeId,
    c.firstname AS firstname,
    c.middlename AS middlename,
    c.lastname AS lastname,
    c.display_name AS displayName,
    c.email as emailId,
    c.image_key AS imageKey,
    c.category_type AS categoryType,
    mo.name as officeName
from
    b_bill_master bm
        join
    m_client c ON (bm.Client_id = c.id)
     join
    m_office mo ON (c.office_id=mo.id)
        left join
    b_client_address ca ON ((ca.client_id = c.id) and address_key = 'PRIMARY' and  ca.is_deleted ='n')
        left join
    b_charge bc ON (bm.id = bc.bill_id and bc.discount_amount > 0) 
group by bm.id 
;


CREATE OR REPLACE VIEW `netrevenue_vw_2015`
AS
select `dt`.`year4` AS `year4`,`dt`.`year_month_abbreviation` AS `year_mon`,`dt`.`month_number` AS `mon`,`c`.`id` AS `client_id`,ifnull((select round((sum(`biop`.`invoice_amount`) - ifnull(sum(`bpop`.`amount_paid`),0)),2) AS `op_bal` from (`b_invoice` `biop` left join `b_payments` `bpop` on((`biop`.`client_id` = `bpop`.`client_id`))) where ((`biop`.`invoice_date` < `fdm`.`date_value`) and (`bpop`.`payment_date` < `fdm`.`date_value`) and (`biop`.`client_id` = `c`.`id`))),0) AS `op_bal`,`bi`.`invoice_amount` AS `inv_amt`,round(ifnull(`bp`.`amount_paid`,0),2) AS `pmt_amt`,`ldm`.`date_value` AS `date_value`,round((select (ifnull(sum(`bicl`.`invoice_amount`),0) - ifnull(sum(`bpcl`.`amount_paid`),0)) AS `cl_bal` from (`b_invoice` `bicl` left join `b_payments` `bpcl` on((`bicl`.`client_id` = `bpcl`.`client_id`))) where ((`bicl`.`invoice_date` <= `ldm`.`date_value`) and (`bpcl`.`payment_date` <= `ldm`.`date_value`) and (`bicl`.`client_id` = `c`.`id`))),2) AS `cl_bal`
from (((((`m_client` `c` join `dim_date` `dt` on(((`dt`.`year4` = 2015) and (`dt`.`month_number` <= month(now()))))) join `dim_date` `fdm` on(((`fdm`.`year_month_number` = `dt`.`year_month_number`) and (`fdm`.`is_first_day_in_month` = 'Yes')))) join `dim_date` `ldm` on(((`dt`.`year_month_number` = `ldm`.`year_month_number`) and (`ldm`.`is_last_day_in_month` = 'Yes')))) join `b_invoice` `bi` on(((`c`.`id` = `bi`.`client_id`) and (`dt`.`year_month_number` = concat(year(`bi`.`invoice_date`),'-',month(`bi`.`invoice_date`)))))) left join `b_payments` `bp` on(((`c`.`id` = `bp`.`client_id`) and (`dt`.`year_month_number` = concat(year(`bp`.`payment_date`),'-',month(`bp`.`payment_date`))))))
group by `bi`.`client_id`,`dt`.`year_month_abbreviation` order by `c`.`id`,`dt`.`year4`,`dt`.`month_number`;



