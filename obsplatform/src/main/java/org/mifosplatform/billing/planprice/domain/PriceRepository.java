package org.mifosplatform.billing.planprice.domain;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PriceRepository extends JpaRepository<Price, Long>,
JpaSpecificationExecutor<Price>{

	@Query("from Price price where price.planCode =:planId and price.serviceCode =:serviceCode and price.contractPeriod =:duration and price.chargeCode = :chargeCode and price.isDeleted='n'")
	List<Price> findOneByPlanAndService(@Param("planId")Long planId,@Param("serviceCode") String serviceCode,@Param("duration") String duration,@Param("chargeCode") String chargeCode);

}
