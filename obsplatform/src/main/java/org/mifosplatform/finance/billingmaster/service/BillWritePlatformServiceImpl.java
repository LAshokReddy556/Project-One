package org.mifosplatform.finance.billingmaster.service;

import java.io.File;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRRuntimeException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;

import org.mifosplatform.finance.billingmaster.domain.BillDetail;
import org.mifosplatform.finance.billingmaster.domain.BillMaster;
import org.mifosplatform.finance.billingmaster.domain.BillMasterRepository;
import org.mifosplatform.finance.billingorder.exceptions.BillingOrderNoRecordsFoundException;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.service.FileUtils;
import org.mifosplatform.infrastructure.core.service.TenantAwareRoutingDataSource;
import org.mifosplatform.organisation.message.domain.BillingMessage;
import org.mifosplatform.organisation.message.domain.BillingMessageRepository;
import org.mifosplatform.organisation.message.domain.BillingMessageTemplate;
import org.mifosplatform.organisation.message.domain.BillingMessageTemplateRepository;
import org.mifosplatform.organisation.message.exception.BillingMessageTemplateNotFoundException;
import org.mifosplatform.portfolio.client.domain.Client;
import org.mifosplatform.portfolio.client.domain.ClientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * @author ranjith
 *
 */
@Service
public class BillWritePlatformServiceImpl implements BillWritePlatformService {
	
	private final static Logger LOGGER = LoggerFactory.getLogger(BillWritePlatformServiceImpl.class);
	private final BillMasterRepository billMasterRepository;
	private final TenantAwareRoutingDataSource dataSource;
    private final ClientRepository clientRepository;
    private final BillingMessageTemplateRepository messageTemplateRepository;
    private final BillingMessageRepository messageDataRepository;

	
	@Autowired
	public BillWritePlatformServiceImpl(final BillMasterRepository billMasterRepository,final TenantAwareRoutingDataSource dataSource,
			final ClientRepository clientRepository,final BillingMessageTemplateRepository messageTemplateRepository,
		    final BillingMessageRepository messageDataRepository) {

		this.dataSource = dataSource;
		this.billMasterRepository = billMasterRepository;
		this.clientRepository = clientRepository;
		this.messageTemplateRepository = messageTemplateRepository;
		this.messageDataRepository = messageDataRepository;
		
	}

	@Override
	public CommandProcessingResult updateBillMaster(final List<BillDetail> billDetails, final BillMaster billMaster, final BigDecimal clientBalance) {
		
		try{
		BigDecimal chargeAmount = BigDecimal.ZERO;
		BigDecimal adjustmentAmount = BigDecimal.ZERO;
		BigDecimal paymentAmount = BigDecimal.ZERO;
		BigDecimal dueAmount = BigDecimal.ZERO;
		BigDecimal taxAmount = BigDecimal.ZERO;
		BigDecimal oneTimeSaleAmount = BigDecimal.ZERO;
		BigDecimal serviceTransferAmount =BigDecimal.ZERO;
		
		for (final BillDetail billDetail : billDetails) {
			if ("SERVICE_CHARGES".equalsIgnoreCase(billDetail.getTransactionType())) {
				if (billDetail.getAmount() != null)
					chargeAmount = chargeAmount.add(billDetail.getAmount());
				
			} else if ("TAXES".equalsIgnoreCase(billDetail.getTransactionType())) {
				if (billDetail.getAmount() != null)
					taxAmount = taxAmount.add(billDetail.getAmount());

			} else if ("ADJUSTMENT".equalsIgnoreCase(billDetail.getTransactionType())) {
				if (billDetail.getAmount() != null)
					adjustmentAmount = adjustmentAmount.add(billDetail.getAmount());
				
			} else if (billDetail.getTransactionType().contains("PAYMENT")) {
				if (billDetail.getAmount() != null)
					paymentAmount = paymentAmount.add(billDetail.getAmount());

			} else if (billDetail.getTransactionType().contains("ONETIME_CHARGES")) {
				if (billDetail.getAmount() != null)
					oneTimeSaleAmount = oneTimeSaleAmount.add(billDetail.getAmount());

			}else if (billDetail.getTransactionType().contains("SERVICE_TRANSFER")) {
				if (billDetail.getAmount() != null)
					serviceTransferAmount = serviceTransferAmount.add(billDetail.getAmount());
			}
			
		}
	  dueAmount = chargeAmount.add(taxAmount).add(oneTimeSaleAmount).add(clientBalance)
			      .add(serviceTransferAmount).subtract(paymentAmount).subtract(adjustmentAmount);

	  billMaster.setChargeAmount(chargeAmount.add(oneTimeSaleAmount).add(serviceTransferAmount));
	  billMaster.setAdjustmentAmount(adjustmentAmount);
	  billMaster.setTaxAmount(taxAmount);
	  billMaster.setPaidAmount(paymentAmount);
	  billMaster.setDueAmount(dueAmount);
	  billMaster.setPreviousBalance(clientBalance);
	  this.billMasterRepository.save(billMaster);
	  return new CommandProcessingResult(billMaster.getId(),billMaster.getClientId());
	}catch(DataIntegrityViolationException dve){
		LOGGER.error("unable to retrieve data" + dve.getLocalizedMessage());
		return CommandProcessingResult.empty();
	}
}

	@Transactional
	@Override
	public void generateStatementPdf(final Long billId) throws SQLException {
		
		try {
			final String fileLocation = FileUtils.MIFOSX_BASE_DIR;
			/** Recursively create the directory if it does not exist **/
			if (!new File(fileLocation).isDirectory()) {
				new File(fileLocation).mkdirs();
			}
			BillMaster billMaster=this.billMasterRepository.findOne(billId);
			
			final String statementDetailsLocation = fileLocation + File.separator + "StatementPdfFiles"; 
			if (!new File(statementDetailsLocation).isDirectory()) {
				new File(statementDetailsLocation).mkdirs();
			}
			final String printStatementLocation = statementDetailsLocation + File.separator + "Bill_" + billMaster.getId() + ".pdf";
			final String jpath = fileLocation+File.separator+"jasper"; 
			final String jfilepath =jpath+File.separator+"Bill_Mainreport.jasper";
			final Connection connection = this.dataSource.getConnection();
		
			Map<String, Object> parameters = new HashMap<String, Object>();
			final Integer id = Integer.valueOf(billMaster.getId().toString());
			parameters.put("param1", id);
			parameters.put("SUBREPORT_DIR",jpath+""+File.separator);
			final JasperPrint jasperPrint = JasperFillManager.fillReport(jfilepath, parameters, connection);
			JasperExportManager.exportReportToPdfFile(jasperPrint, printStatementLocation);
			billMaster.setFileName(printStatementLocation);
			this.billMasterRepository.save(billMaster);
			connection.close();
			System.out.println("Filling report successfully...");
			
		} catch (final DataIntegrityViolationException ex) {
			 LOGGER.error("Filling report failed..." + ex.getLocalizedMessage());
			 System.out.println("Filling report failed...");
			 ex.printStackTrace();
		} catch (final JRException  | JRRuntimeException e) {
			LOGGER.error("Filling report failed..." + e.getLocalizedMessage());
			System.out.println("Filling report failed...");
			e.printStackTrace();
		} catch (final Exception e) {
			LOGGER.error("Filling report failed..." + e.getLocalizedMessage());
			System.out.println("Filling report failed...");
			e.printStackTrace();
		}
	}

	@Transactional
	@Override
	public String generateInovicePdf(final Long invoiceId)  {
		
		final String fileLocation = FileUtils.MIFOSX_BASE_DIR ;
		/** Recursively create the directory if it does not exist **/
		if (!new File(fileLocation).isDirectory()) {
			new File(fileLocation).mkdirs();
		}
		final String InvoiceDetailsLocation = fileLocation + File.separator +"InvoicePdfFiles";
		if (!new File(InvoiceDetailsLocation).isDirectory()) {
			 new File(InvoiceDetailsLocation).mkdirs();
		}
		final String printInvoiceLocation = InvoiceDetailsLocation +File.separator + "Invoice_" + invoiceId + ".pdf";
		try {
			
			final String jpath = fileLocation+File.separator+"jasper"; 
			final String jasperfilepath =jpath+File.separator+"Invoicereport.jasper";
			final Integer id = Integer.valueOf(invoiceId.toString());
			final Connection connection = this.dataSource.getConnection();
			Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("param1", id);
		   final JasperPrint jasperPrint = JasperFillManager.fillReport(jasperfilepath, parameters, connection);
		   JasperExportManager.exportReportToPdfFile(jasperPrint,printInvoiceLocation);
	       connection.close();
	       System.out.println("Filling report successfully...");
	       
		   }catch (final DataIntegrityViolationException ex) {
			 LOGGER.error("Filling report failed..." + ex.getLocalizedMessage());
			 System.out.println("Filling report failed...");
			 ex.printStackTrace();
		   } catch (final JRException  | JRRuntimeException e) {
			LOGGER.error("Filling report failed..." + e.getLocalizedMessage());
			System.out.println("Filling report failed...");
		 	e.printStackTrace();
		  } catch (final Exception e) {
			LOGGER.error("Filling report failed..." + e.getLocalizedMessage());
			System.out.println("Filling report failed...");
			e.printStackTrace();
		}
		return printInvoiceLocation;	
   }
	
	
	@Transactional
	@Override
	public String generatePaymentPdf(final Long paymentId)  {
		
		final String fileLocation = FileUtils.MIFOSX_BASE_DIR ;
		/** Recursively create the directory if it does not exist **/
		if (!new File(fileLocation).isDirectory()) {
			new File(fileLocation).mkdirs();
		}
		final String PaymentDetailsLocation = fileLocation + File.separator +"PaymentPdfFiles";
		if (!new File(PaymentDetailsLocation).isDirectory()) {
			 new File(PaymentDetailsLocation).mkdirs();
		}
		final String printPaymentLocation = PaymentDetailsLocation +File.separator + "Payment_" + paymentId + ".pdf";
		try {
			
			final String jpath = fileLocation+File.separator+"jasper"; 
			final String jasperfilepath =jpath+File.separator+"Paymentreport.jasper";
			final Integer id = Integer.valueOf(paymentId.toString());
			final Connection connection = this.dataSource.getConnection();
			Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("param1", id);
		   final JasperPrint jasperPrint = JasperFillManager.fillReport(jasperfilepath, parameters, connection);
		   JasperExportManager.exportReportToPdfFile(jasperPrint,printPaymentLocation);
	       connection.close();
	       System.out.println("Filling report successfully...");
	       
		   }catch (final DataIntegrityViolationException ex) {
			 LOGGER.error("Filling report failed..." + ex.getLocalizedMessage());
			 System.out.println("Filling report failed...");
			 ex.printStackTrace();
		   } catch (final JRException  | JRRuntimeException e) {
			LOGGER.error("Filling report failed..." + e.getLocalizedMessage());
			System.out.println("Filling report failed...");
		 	e.printStackTrace();
		  } catch (final Exception e) {
			LOGGER.error("Filling report failed..." + e.getLocalizedMessage());
			System.out.println("Filling report failed...");
			e.printStackTrace();
		}
		return printPaymentLocation;	
	}
	
	@Transactional
	@Override
	public void sendPdfToEmail(final String printFileName, final Long clientId,final String templateName) {
		
		//context.authenticatedUser();
		final Client client = this.clientRepository.findOne(clientId);
		final String clientEmail = client.getEmail();
		if(clientEmail == null){
			final String msg = "Please provide email first";
			throw new BillingOrderNoRecordsFoundException(msg, client);
		}
		final BillingMessageTemplate messageTemplate = this.messageTemplateRepository.findByTemplateDescription(templateName);
		if(messageTemplate !=null){
		  String header = messageTemplate.getHeader().replace("<PARAM1>", client.getDisplayName().isEmpty()?client.getFirstname():client.getDisplayName());
		  BillingMessage  billingMessage = new BillingMessage(header, messageTemplate.getBody(), messageTemplate.getFooter(), clientEmail, clientEmail, 
		    		messageTemplate.getSubject(), "N", messageTemplate, messageTemplate.getMessageType(), printFileName);
		    this.messageDataRepository.save(billingMessage);
	    }else{
	    	throw new BillingMessageTemplateNotFoundException(templateName);
	    }
	  }
	}
	
/*	@Override
	public String generatePdf(final BillDetailsData billDetails,final List<FinancialTransactionsData> datas) {

		final String fileLocation = FileUtils.MIFOSX_BASE_DIR + File.separator
				+ "Print_invoice_Details";
		
		*//** Recursively create the directory if it does not exist **//*
		if (!new File(fileLocation).isDirectory()) {
			new File(fileLocation).mkdirs();
		}
		final String printInvoicedetailsLocation = fileLocation + File.separator
				+ "invoice" + billDetails.getId() + ".pdf";

		BillMaster billMaster = this.billMasterRepository.findOne(billDetails
				.getId());
		billMaster.setFileName(printInvoicedetailsLocation);
		this.billMasterRepository.save(billMaster);

		try {

			Document document = new Document();

			final PdfWriter writer = PdfWriter.getInstance(document,
					new FileOutputStream(printInvoicedetailsLocation));
			document.open();
			final PdfContentByte pdfContentByte = writer.getDirectContent();
			final Font b = new Font(Font.BOLD + Font.BOLD, 8);
			final Font b1 = new Font(Font.BOLD + Font.UNDERLINE + Font.BOLDITALIC
					+ Font.TIMES_ROMAN, 6);

			pdfContentByte.beginText();

			final PdfPTable table = new PdfPTable(11);
			table.setWidthPercentage(100);

			PdfPCell cell1 = new PdfPCell((new Paragraph("Bill Invoice",
					FontFactory.getFont(FontFactory.HELVETICA, 12, Font.BOLD))));
			cell1.setColspan(11);
			cell1.setHorizontalAlignment(Element.ALIGN_CENTER);
			cell1.setPadding(10.0f);
			table.addCell(cell1);
			PdfPCell cell = new PdfPCell();
			cell.setColspan(2);
			final Paragraph para = new Paragraph("Name           :", b1);
			final Paragraph addr = new Paragraph("Address        :", b);
			final Paragraph branch = new Paragraph("Branch       :", b);
			branch.setSpacingBefore(25);

			cell.addElement(para);
			cell.addElement(addr);
			cell.addElement(branch);
			cell.disableBorderSide(PdfPCell.RIGHT);
			table.addCell(cell);
			PdfPCell cell0 = new PdfPCell();

			final Paragraph add0 = new Paragraph("" + billDetails.getClientName(), b);
			final Paragraph add1 = new Paragraph("" + billDetails.getAddrNo() + ""
					+ billDetails.getStreet(), b);
			add1.setSpacingBefore(10);
			final Paragraph add2 = new Paragraph("" + billDetails.getCity() + ""
					+ billDetails.getState() + "" + billDetails.getCountry()
					+ "" + billDetails.getZip(), b);
			cell0.setColspan(4);
			cell0.disableBorderSide(PdfPCell.LEFT);
			cell0.addElement(add0);
			cell0.addElement(add1);
			cell0.addElement(add2);
			table.addCell(cell0);

			Image image = Image.getInstance(FileUtils.MIFOSX_BASE_DIR  + File.separator + billDetails.getCompanyLogo());
			image.scaleAbsolute(90, 90);
			PdfPCell cell2 = new PdfPCell();
			cell2.addElement(image);
			cell2.disableBorderSide(PdfPCell.TOP);
			cell2.disableBorderSide(PdfPCell.BOTTOM);
			cell2.disableBorderSide(PdfPCell.LEFT);
			cell2.disableBorderSide(PdfPCell.RIGHT);
			cell2.setColspan(2);
			table.addCell(cell2);
			PdfPCell cell02 = new PdfPCell();
			final Paragraph addr1 = new Paragraph(billDetails.getAddr1(),
					FontFactory.getFont(FontFactory.HELVETICA, 8, Font.BOLD,
							new CMYKColor(0, 255, 255, 17)));
			final Paragraph addr2 = new Paragraph(billDetails.getAddr2(), b);
			final Paragraph addr3 = new Paragraph(billDetails.getOffCity() + "," + billDetails.getOffState(), b);
			final Paragraph addr4 = new Paragraph(billDetails.getOffCountry()+ "-" + billDetails.getOffZip(), b);
			final Paragraph addr5 = new Paragraph(" Tel: " + billDetails.getPhnNum(), b);
			final Paragraph addr6 = new Paragraph(billDetails.getEmailId(), b);
			cell02.addElement(addr1);
			cell02.addElement(addr2);
			cell02.addElement(addr3);
			cell02.addElement(addr4);
			cell02.addElement(addr5);
			cell02.addElement(addr6);

			cell02.disableBorderSide(PdfPCell.TOP);
			cell02.disableBorderSide(PdfPCell.BOTTOM);
			cell02.disableBorderSide(PdfPCell.LEFT);
			cell2.disableBorderSide(PdfPCell.RIGHT);
			cell02.setColspan(3);
			table.addCell(cell02);
			PdfPCell cell3 = new PdfPCell();
			// cell3.setPadding (1.0f);
			final Paragraph BillId = new Paragraph("Client Id:   "
					+ billDetails.getClientId(), b);
			cell3.setColspan(6);
			cell3.addElement(BillId);
			cell3.disableBorderSide(PdfPCell.RIGHT);
			table.addCell(cell3);
			PdfPCell cell12 = new PdfPCell();
			final Paragraph billNo = new Paragraph("BillNo:" + billDetails.getId(), b);
			final Paragraph billDate = new Paragraph("Bill Date:"
					+ billDetails.getBillDate(), b);
			
			final Paragraph BillPeriod = new Paragraph("Bill Period:"
					+ billDetails.getBillPeriod(), b);
			
			final Paragraph dueDate = new Paragraph("Due Date:"
					+ billDetails.getDueDate(), b);
			
			cell12.disableBorderSide(PdfPCell.LEFT);
			cell12.addElement(billNo);
			cell12.addElement(billDate);
			cell12.addElement(BillPeriod);
			cell12.setColspan(5);
			cell12.addElement(dueDate);
			table.addCell(cell12);
			PdfPCell cell4 = new PdfPCell();

			final Paragraph previousbal = new Paragraph("Previous Balance", b);
			final Paragraph previousamount = new Paragraph(""
					+ billDetails.getPreviousBalance(), b);
			cell4.setColspan(2);
			cell4.addElement(previousbal);
			cell4.addElement(previousamount);
			cell4.disableBorderSide(PdfPCell.TOP);
			
			table.addCell(cell4);
			pdfContentByte.setTextMatrix(390, 405);

			PdfPCell cell5 = new PdfPCell();
			final Paragraph adjstment = new Paragraph("Adjustment Amount", b);
			final Paragraph adjstmentamount = new Paragraph(""
					+ billDetails.getAdjustmentAmount(), b);
			cell5.setColspan(2);
			cell5.addElement(adjstment);
			cell5.addElement(adjstmentamount);
			cell5.disableBorderSide(PdfPCell.TOP);
			cell5.disableBorderSide(PdfPCell.LEFT);
			table.addCell(cell5);

			PdfPCell cell6 = new PdfPCell();
			final Paragraph paid_amount = new Paragraph("Payments", b);
			final Paragraph amount = new Paragraph("" + billDetails.getPaidAmount(),
					b);
			cell6.setColspan(2);
			cell6.addElement(paid_amount);
			cell6.addElement(amount);
			cell6.disableBorderSide(PdfPCell.TOP);
			cell6.disableBorderSide(PdfPCell.LEFT);
			table.addCell(cell6);

			PdfPCell cell7 = new PdfPCell();
			final Paragraph charge_amount = new Paragraph("Charge Amount", b);
			final Paragraph chargeamount = new Paragraph(""
					+ billDetails.getChargeAmount(), b);
			cell7.setColspan(2);
			cell7.addElement(charge_amount);
			cell7.addElement(chargeamount);

			cell7.disableBorderSide(PdfPCell.TOP);
			cell7.disableBorderSide(PdfPCell.LEFT);
			
			table.addCell(cell7);

			PdfPCell cell8 = new PdfPCell();
			final Paragraph due_amount = new Paragraph("Due Amount", b);
			final Paragraph dueamount = new Paragraph(
					"" + billDetails.getDueAmount(), b);
			cell8.setColspan(3);
			cell8.addElement(due_amount);
			cell8.addElement(dueamount);

			cell8.disableBorderSide(PdfPCell.TOP);
			cell8.disableBorderSide(PdfPCell.LEFT);
			
			table.addCell(cell8);

			PdfPCell cell9 = new PdfPCell();
			cell9.setColspan(6);
			final Paragraph billDetail = new Paragraph("Current Bill Details", b);
			cell9.setPadding(10.0f);
			cell9.setPaddingLeft(100.0f);
			cell9.addElement(billDetail);
			cell9.disableBorderSide(PdfPCell.TOP);
			cell9.disableBorderSide(PdfPCell.BOTTOM);
			cell9.disableBorderSide(PdfPCell.LEFT);
			cell9.disableBorderSide(PdfPCell.RIGHT);
			table.addCell(cell9);

			PdfPCell cell10 = new PdfPCell();
			cell10.setColspan(5);
			final Paragraph message = new Paragraph("Promotional Message", b);
			cell10.setPadding(10.0f);
			cell10.setPaddingLeft(100.0f);
			cell10.addElement(message);
			cell10.disableBorderSide(PdfPCell.TOP);
			cell10.disableBorderSide(PdfPCell.BOTTOM);
			cell10.disableBorderSide(PdfPCell.LEFT);
			cell10.disableBorderSide(PdfPCell.RIGHT);
			table.addCell(cell10);

			PdfPCell cell26 = new PdfPCell();
			cell26.setColspan(1);
			final Paragraph charge = new Paragraph("Id", b);

			cell26.addElement(charge);

			cell26.disableBorderSide(PdfPCell.RIGHT);

			PdfPCell cell28 = new PdfPCell();
			cell28.setColspan(1);
			Paragraph amountValue = new Paragraph("Amount", b);

			cell28.addElement(amountValue);
			
			cell28.disableBorderSide(PdfPCell.LEFT);
			cell28.disableBorderSide(PdfPCell.RIGHT);

			PdfPCell cell27 = new PdfPCell();
			cell27.setColspan(1);
			final Paragraph dateValue = new Paragraph("Date", b);

			cell27.addElement(dateValue);
			
			cell27.disableBorderSide(PdfPCell.LEFT);
			cell27.disableBorderSide(PdfPCell.RIGHT);

			PdfPCell cell23 = new PdfPCell();
			cell23.setColspan(3);
			Paragraph transId = new Paragraph("Transaction", b);

			cell23.addElement(transId);
		
			cell23.disableBorderSide(PdfPCell.LEFT);
			cell23.disableBorderSide(PdfPCell.RIGHT);

			BigDecimal totalAmount = BigDecimal.ZERO;

			for (final FinancialTransactionsData data : datas) {
				final Paragraph id = new Paragraph("" + data.getTransactionId(), b);

				cell26.addElement(id);

				final Paragraph transactionType = new Paragraph(""
						+ data.getTransactionType(), b);
				cell23.addElement(transactionType);
				final Paragraph date = new Paragraph("" + data.getTransDate(), b);
				cell27.addElement(date);
				final Paragraph tranAmount = new Paragraph("" + data.getAmount(), b);

				cell28.addElement(tranAmount);
				totalAmount = totalAmount.add(data.getAmount());

			}

			table.addCell(cell26);
			table.addCell(cell23);
			table.addCell(cell27);
			table.addCell(cell28);
			PdfPCell cell24 = new PdfPCell();
			cell24.setColspan(1);
			cell24.disableBorderSide(PdfPCell.TOP);
			cell24.disableBorderSide(PdfPCell.BOTTOM);
			table.addCell(cell24);
			PdfPCell cell25 = new PdfPCell();
			Paragraph proMessage = new Paragraph("" + billDetails.getMessage(),
					b);
			cell25.addElement(proMessage);
			cell25.setColspan(4);
			cell25.setPadding(70f);
			table.addCell(cell25);

			pdfContentByte.endText();
			document.add(table);
			document.close();

			// This option is to open the PDF on Server. Instead we have given
			// Financial Statement Download Option
			
			 * Runtime.getRuntime().exec(
			 * "rundll32 url.dll,FileProtocolHandler "
			 * +printInvoicedetailsLocation);
			 

		} catch (Exception e) {
		}
		return printInvoicedetailsLocation;

	}*/	
	
