package org.mifosplatform.organisation.voucher.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 
 * @author ashokreddy
 *
 */
public interface VoucherDetailsRepository extends JpaRepository<VoucherDetails, Long>,
		JpaSpecificationExecutor<VoucherDetails> {
	
	//and voucherDetails.clientId is null
	@Query("from VoucherDetails voucherDetails where voucherDetails.pinNo =:pinNumber and voucherDetails.status = 'NEW' and voucherDetails.isDeleted = 'N'")
	VoucherDetails findOneByPinNumber(@Param("pinNumber") String pinNumber);
	

}
