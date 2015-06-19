package org.mifosplatform.billing.discountmaster.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DiscountDetailRepository extends

JpaRepository<DiscountDetails, Long>,
JpaSpecificationExecutor<DiscountDetails>{
   @Query("from DiscountDetails discountDetails where discountDetails.categoryType = :categoryId and discountDetails.discountMaster = :discountMaster and discountDetails.isDeleted ='N' ")
	DiscountDetails findOneByCategoryAndDiscountId(@Param("categoryId")String categoryId, @Param("discountMaster")DiscountMaster discountMaster);
	

}
