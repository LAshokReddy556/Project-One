package org.mifosplatform.logistics.itemdetails.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ItemDetailsRepository extends JpaRepository<ItemDetails, Long>, JpaSpecificationExecutor<ItemDetails>{

	@Query("from ItemDetails item where item.serialNumber = :macId")
	ItemDetails getInventoryItemDetailBySerialNum(@Param("macId") String macId);
	
	
}


