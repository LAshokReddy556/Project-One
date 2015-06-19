package org.mifosplatform.billing.chargecode.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.hibernate.exception.ConstraintViolationException;
import org.joda.time.LocalDate;
import org.mifosplatform.billing.chargecode.data.ChargeCodeData;
import org.mifosplatform.billing.chargecode.domain.ChargeCodeMaster;
import org.mifosplatform.billing.chargecode.domain.ChargeCodeRepository;
import org.mifosplatform.billing.chargecode.exception.ChargeCodeNotFoundException;
import org.mifosplatform.billing.chargecode.serialization.ChargeCodeCommandFromApiJsonDeserializer;
import org.mifosplatform.billing.discountmaster.data.DiscountMasterData;
import org.mifosplatform.billing.discountmaster.domain.DiscountMaster;
import org.mifosplatform.billing.discountmaster.domain.DiscountMasterRepository;
import org.mifosplatform.billing.planprice.domain.Price;
import org.mifosplatform.billing.planprice.domain.PriceRepository;
import org.mifosplatform.finance.billingorder.commands.InvoiceTaxCommand;
import org.mifosplatform.finance.billingorder.data.BillingOrderData;
import org.mifosplatform.finance.billingorder.service.GenerateBill;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author hugo
 * 
 */
@Service
public class ChargeCodeWritePlatformServiceImp implements ChargeCodeWritePlatformService {

	private final static Logger LOGGER = (Logger) LoggerFactory.getLogger(ChargeCodeWritePlatformServiceImp.class);

	private final PlatformSecurityContext context;
	private final ChargeCodeRepository chargeCodeRepository;
	private final ChargeCodeCommandFromApiJsonDeserializer apiJsonDeserializer;
	private final DiscountMasterRepository discountMasterRepository;
	private final PriceRepository priceRepository;
    private final GenerateBill generateBill;
	@Autowired
	public ChargeCodeWritePlatformServiceImp(final PlatformSecurityContext context,final ChargeCodeRepository chargeCodeRepository,
			final ChargeCodeCommandFromApiJsonDeserializer apiJsonDeserializer,final DiscountMasterRepository discountMasterRepository,
			final PriceRepository priceRepository,final GenerateBill generateBill) {
		
		this.context = context;
		this.chargeCodeRepository = chargeCodeRepository;
		this.apiJsonDeserializer = apiJsonDeserializer;
		this.discountMasterRepository = discountMasterRepository;
		this.priceRepository = priceRepository;
		this.generateBill = generateBill;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * #createChargeCode(org.mifosplatform.infrastructure.core.api.JsonCommand)
	 */
	@Transactional
	@Override
	public CommandProcessingResult createChargeCode(final JsonCommand command) {

		ChargeCodeMaster chargeCode = null;
		try {
			context.authenticatedUser();
			this.apiJsonDeserializer.validaForCreate(command.json());
			chargeCode = ChargeCodeMaster.fromJson(command);
			this.chargeCodeRepository.save(chargeCode);
			return new CommandProcessingResultBuilder().withCommandId(command.commandId())
					  .withEntityId(chargeCode.getId()).build();
		} catch (final DataIntegrityViolationException dve) {
			handleDataIntegrityIssues(command, dve);
			return new CommandProcessingResult(Long.valueOf(-1L));
		}
	}

	private void handleDataIntegrityIssues(final JsonCommand command,
			final DataIntegrityViolationException dve) {
		final Throwable realCause = dve.getMostSpecificCause();
		if (realCause.getMessage().contains("chargecode")) {
			throw new PlatformDataIntegrityException(
					"error.msg.chargecode.duplicate.name", "A code with name'"
							+ command.stringValueOfParameterNamed("chargeCode")
							+ "'already exists", "chargeCode",
					command.stringValueOfParameterNamed("chargeCode"));
		}

		if (realCause.getMessage().contains("chargedescription")) {
			throw new PlatformDataIntegrityException(
					"error.msg.chargecode.duplicate.name",
					"A description with name'"
							+ command
									.stringValueOfParameterNamed("charge_description")
							+ "'already exists", "chargeDescription",
					command.stringValueOfParameterNamed("charge_description"));
		}
		
		if (realCause.getMessage().contains("foreign key constraint")) {
			throw new PlatformDataIntegrityException(
					"error.msg.chargecode.can not.delete or update already used",
					"A code with name'"
							+ command
									.stringValueOfParameterNamed("chargeCode")
							+ "'already used", "chargeCode",
					command.stringValueOfParameterNamed("chargeCode"));
		}
		LOGGER.error(dve.getMessage(), dve);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * #updateChargeCode(org.mifosplatform.infrastructure.core.api.JsonCommand,
	 * java.lang.Long)
	 */
	@Transactional
	@Override
	public CommandProcessingResult updateChargeCode(final JsonCommand command,final Long chargeCodeId) {
		ChargeCodeMaster chargeCode = null;
		try {
			context.authenticatedUser();
			this.apiJsonDeserializer.validaForCreate(command.json());
			chargeCode = retrieveChargeCodeById(chargeCodeId);
			final Map<String, Object> changes = chargeCode.update(command);
			if (!changes.isEmpty()) {
				chargeCodeRepository.saveAndFlush(chargeCode);
			}

			return new CommandProcessingResultBuilder()
					.withCommandId(command.commandId())
					.withEntityId(chargeCode.getId()).with(changes).build();
		} catch (DataIntegrityViolationException dve) {
			if (dve.getCause() instanceof ConstraintViolationException) {
				handleDataIntegrityIssues(command, dve);
			}
			return new CommandProcessingResult(Long.valueOf(-1L));
		}
	}

	private ChargeCodeMaster retrieveChargeCodeById(final Long chargeCodeId) {
		final ChargeCodeMaster chargeCode = this.chargeCodeRepository
				.findOne(chargeCodeId);
		if (chargeCode == null) {
			throw new ChargeCodeNotFoundException(chargeCodeId.toString());
		}
		return chargeCode;
	}
	@Override
	public BigDecimal calculateFinalAmount(ChargeCodeData chargeCodeData,Long clientId,Long priceId) {
		
		Long defaultValue=Long.valueOf(0);
		Date defaultDate= new Date();
		Price price = this.priceRepository.findOne(priceId);
		BigDecimal finalAmount=BigDecimal.ZERO;
		DiscountMaster discountMaster = this.discountMasterRepository.findOne(price.getDiscountId());
		LocalDate endDate=new LocalDate(discountMaster.getStartDate()).plusMonths(1);
		ChargeCodeMaster chargeCode = this.chargeCodeRepository.findOne(chargeCodeData.getId());
		BillingOrderData billingOrderData = new BillingOrderData(defaultValue,defaultValue,defaultValue,clientId,discountMaster.getStartDate(),
				defaultDate,defaultDate,chargeCodeData.getBillFrequencyCode(),chargeCode.getChargeCode(),chargeCode.getChargeType(),chargeCode.getChargeDuration(),
				chargeCode.getChargeType(),defaultDate,price.getPrice(),"",discountMaster.getStartDate(),defaultDate,defaultValue,chargeCode.getTaxInclusive()); 
		
		DiscountMasterData discountMasterData = new DiscountMasterData(discountMaster.getId(),defaultValue,defaultValue,new LocalDate(discountMaster.getStartDate()),
				new LocalDate(),discountMaster.getDiscountType(),discountMaster.getDiscountRate(),"N");
		
		List<InvoiceTaxCommand> invoiceTaxCommands=this.generateBill.calculateDiscountAndTax(billingOrderData, discountMasterData, new LocalDate(discountMaster.getStartDate()),endDate, price.getPrice());
		if(!invoiceTaxCommands.isEmpty()){
		finalAmount = invoiceTaxCommands.get(0).getDiscountedAmount();
		}
		if(chargeCode.getTaxInclusive() !=1){
		for(InvoiceTaxCommand invoiceTaxCommand:invoiceTaxCommands){
			finalAmount = finalAmount.add(invoiceTaxCommand.getTaxAmount()); 
		}
		}
		return finalAmount;
	}

}
