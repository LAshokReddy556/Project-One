INSERT IGNORE INTO `stretchy_parameter` values(null, 'year', 'year', 'year', 'select', 'number', '2015', '', 'Y', '', 'select distinct year4 from dim_date where year4 between 2010 and 2020', NULL, 'Single');

INSERT IGNORE INTO `stretchy_report` 
(`id`,`report_name`,`report_type`,`report_subtype`,`report_category`,`report_sql`,`description`,`core_report`,`use_report`) 
VALUES 
(null,'Net Revenue Report for Year','Table',NULL,'Client','
select * from netrevenue_vw_2015 ',
'Net Revenue Report for 2015',1,1);

SET @ID=(select id from stretchy_report where report_name='Net Revenue Report for Year');
SET @PID=(select id from stretchy_parameter where parameter_name='year');

INSERT IGNORE INTO `stretchy_report_parameter` (`report_id`,`parameter_id`,`report_parameter_name`) 
VALUES (@ID,@PID,'year');


CREATE OR REPLACE  VIEW netrevenue_vw_2015 AS 
select 
    c.id AS client_id,
    dt.year4 AS year4,
    dt.month_number AS mon,
    dt.year_month_abbreviation AS year_mon,
    cast(ifnull((select 
                        round((sum(biop.invoice_amount) - ifnull(sum(bpop.amount_paid), 0)),
                                    2) AS op_bal
                    from
                        (b_invoice biop
                        left join b_payments bpop ON (((biop.client_id = bpop.client_id) and bpop.payment_date < any (select fdm.date_value))))
                    where
                        ((biop.invoice_date < fdm.date_value) and (biop.client_id = c.id))),
                0)
        as char charset utf8) AS op_bal,
    cast(round(sum(bi.invoice_amount), 2) as char charset utf8) AS inv_amt,
    cast(sum(round(ifnull(bp.amount_paid, 0), 2)) as char charset utf8) AS pmt_amt,
    ldm.date_value AS month_end,
    cast(round((select 
                        (ifnull(sum(bicl.invoice_amount), 0) - ifnull(sum(bpcl.amount_paid), 0)) AS cl_bal
                    from
                        (b_invoice bicl
                        left join b_payments bpcl ON (((bicl.client_id = bpcl.client_id) and bpcl.payment_date <= any (select ldm.date_value))))
                    where
                        ((bicl.invoice_date <= ldm.date_value) and (bicl.client_id = c.id))),
                2)
        as char charset utf8) AS cl_bal
from
    (((((m_client c
    join dim_date dt ON (((dt.year4 = 2015) and (dt.month_number <= month(now())))))
    join dim_date fdm ON (((fdm.year_month_number = dt.year_month_number) and (fdm.is_first_day_in_month = 'Yes'))))
    join dim_date ldm ON (((dt.year_month_number = ldm.year_month_number) and (ldm.is_last_day_in_month = 'Yes'))))
    join b_invoice bi ON (((c.id = bi.client_id) and (bi.invoice_date = dt.date_value))))
    left join b_payments bp ON (((c.id = bp.client_id) and (dt.year_month_number = concat(year(bp.payment_date), '-', month(bp.payment_date))))))
group by bi.client_id , dt.year_month_abbreviation
order by c.id , dt.year4 , dt.month_number
