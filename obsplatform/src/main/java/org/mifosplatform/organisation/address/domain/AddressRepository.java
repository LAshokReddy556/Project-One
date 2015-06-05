package org.mifosplatform.organisation.address.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AddressRepository  extends JpaRepository<Address, Long>,JpaSpecificationExecutor<Address>{

	@Query("from Address address where address.clientId=:clientId and address.addressNo =:newPropertyCode and deleted='n'")
	Address findOneByAddressNo(@Param("clientId") Long clientId, @Param("newPropertyCode") String newPropertyCode);
	
	
	@Query("from Address address where address.clientId=:clientId and address.addressKey ='PRIMARY' and deleted='n'")
	Address findOneByClientId(@Param("clientId") Long clientId);
}
