package org.mifosplatform.finance.billingmaster.service;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import org.mifosplatform.finance.billingmaster.domain.BillDetail;
import org.mifosplatform.finance.billingmaster.domain.BillMaster;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;

public interface BillWritePlatformService {
	
	CommandProcessingResult updateBillMaster(List<BillDetail> billDetails,BillMaster billMaster, BigDecimal previousBal);
	
	void generateStatementPdf(Long billId) throws SQLException;

	String generateInovicePdf(Long billId) ;

	String generatePaymentPdf(Long paymentId);

	void sendPdfToEmail(String printFileName, Long clientId,String templateName);

}
