Drop procedure IF EXISTS vendormobilenum; 
DELIMITER //
create procedure vendormobilenum() 
Begin
  IF EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE COLUMN_NAME = 'vendor_mobile'
     and TABLE_NAME = 'b_vendor_management'
     and TABLE_SCHEMA = DATABASE())THEN
ALTER TABLE `b_vendor_management` MODIFY `vendor_mobile` varchar(20) NOT NULL;
END IF;
IF EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE COLUMN_NAME = 'vendor_landline'
     and TABLE_NAME = 'b_vendor_management'
     and TABLE_SCHEMA = DATABASE())THEN
ALTER TABLE `b_vendor_management` MODIFY `vendor_landline` varchar(20) DEFAULT NULL;
END IF;
END //
DELIMITER ;
call vendormobilenum();
Drop procedure IF EXISTS vendormobilenum;
