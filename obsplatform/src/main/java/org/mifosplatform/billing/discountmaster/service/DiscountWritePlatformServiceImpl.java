package org.mifosplatform.billing.discountmaster.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.mifosplatform.billing.discountmaster.domain.DiscountDetailRepository;
import org.mifosplatform.billing.discountmaster.domain.DiscountDetails;
import org.mifosplatform.billing.discountmaster.domain.DiscountMaster;
import org.mifosplatform.billing.discountmaster.domain.DiscountMasterRepository;
import org.mifosplatform.billing.discountmaster.exception.DiscountMasterNotFoundException;
import org.mifosplatform.billing.discountmaster.serialization.DiscountCommandFromApiJsonDeserializer;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

/**
 * @author hugo
 * 
 */
@Service
public class DiscountWritePlatformServiceImpl implements
		DiscountWritePlatformService {

	private final static Logger LOGGER = LoggerFactory.getLogger(DiscountWritePlatformServiceImpl.class);
	private final PlatformSecurityContext context;
	private final DiscountCommandFromApiJsonDeserializer apiJsonDeserializer;
	private final DiscountMasterRepository discountMasterRepository;
	private final FromJsonHelper fromApiJsonHelper;
	private final DiscountDetailRepository discountDetailRepository;
	/**
	 * @param context
	 * @param apiJsonDeserializer
	 * @param discountMasterRepository
	 */
	@Autowired
	public DiscountWritePlatformServiceImpl(final PlatformSecurityContext context,final DiscountCommandFromApiJsonDeserializer apiJsonDeserializer,
			final DiscountMasterRepository discountMasterRepository,final FromJsonHelper fromApiJsonHelper,
			final DiscountDetailRepository detailRepository) {
		
		this.context = context;
		this.apiJsonDeserializer = apiJsonDeserializer;
		this.fromApiJsonHelper = fromApiJsonHelper;
		this.discountMasterRepository = discountMasterRepository;
		this.discountDetailRepository = detailRepository;
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * #createNewDiscount(org.mifosplatform.infrastructure.core.api.JsonCommand)
	 */
	@Transactional
	@Override
	public CommandProcessingResult createNewDiscount(final JsonCommand command) {

		try {

			this.context.authenticatedUser();
			this.apiJsonDeserializer.validateForCreate(command.json());
			DiscountMaster discountMaster = DiscountMaster.fromJson(command);
			final JsonArray discountPricesArray = command.arrayOfParameterNamed("discountPrices").getAsJsonArray();
			discountMaster=assembleDiscountDetails(discountPricesArray,discountMaster); 
			this.discountMasterRepository.save(discountMaster);
			return new CommandProcessingResultBuilder().withCommandId(command.commandId())
					        .withEntityId(discountMaster.getId()).build();
			
		} catch (DataIntegrityViolationException dve) {
			handleCodeDataIntegrityIssues(command, dve);
			return new CommandProcessingResult(Long.valueOf(-1));
		}

	}

	private DiscountMaster assembleDiscountDetails(JsonArray discountPricesArray, DiscountMaster discountMaster) {
		
			String[]  discountPrices = null;
			discountPrices = new String[discountPricesArray.size()];
			if(discountPricesArray.size() > 0){
				for(int i = 0; i < discountPricesArray.size(); i++){
					discountPrices[i] = discountPricesArray.get(i).toString();
				}
		
			for (final String discountPrice : discountPrices) {
				final JsonElement element = fromApiJsonHelper.parse(discountPrice);
				final String categoryId = fromApiJsonHelper.extractStringNamed("categoryId", element);
				final BigDecimal discountRate = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("discountRate", element);
				DiscountDetails discountDetails = new DiscountDetails(categoryId, discountRate);
				discountMaster.addDetails(discountDetails);
				
			}	 
		}	
		
		return discountMaster;
	}

	private void handleCodeDataIntegrityIssues(final JsonCommand command,final DataIntegrityViolationException dve) {
		
		final Throwable realCause = dve.getMostSpecificCause();
		if (realCause.getMessage().contains("discountcode")) {
			final String name = command.stringValueOfParameterNamed("discountCode");
			throw new PlatformDataIntegrityException("error.msg.discount.duplicate.name","A discount with Code'" + name + "'already exists",
					"discountCode", name);
			
		}else if(realCause.getMessage().contains("discountid_with_category_uniquekey")){
			final String name = command.stringValueOfParameterNamed("discountCode");
			throw new PlatformDataIntegrityException("error.msg.customer.category.discount.duplicate.name","this customer category discount already defined",
					"discountCode", name);
		}

		LOGGER.error(dve.getMessage(), dve);
		throw new PlatformDataIntegrityException("error.msg.could.unknown.data.integrity.issue","Unknown data integrity issue with resource: "
						+ realCause.getMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see #updateDiscount(java.lang.Long,
	 * org.mifosplatform.infrastructure.core.api.JsonCommand)
	 */
	//@Transactional
	@Override
	public CommandProcessingResult updateDiscount(final Long entityId,final JsonCommand command) {

		try {

			this.context.authenticatedUser();
			this.apiJsonDeserializer.validateForCreate(command.json());
			DiscountMaster discountMaster = discountRetrieveById(entityId);
			List<DiscountDetails> details=new ArrayList<>(discountMaster.getDiscountDetails());
			final JsonArray discountPricesArray = command.arrayOfParameterNamed("discountPrices").getAsJsonArray();
			    String[] states =null;
			    states=new String[discountPricesArray.size()];
			    for(int i=0; i<discountPricesArray.size();i++){
			    	states[i] =discountPricesArray.get(i).toString();
			    }
				 for (String discountPrice : states) {
					  
					 final JsonElement element = fromApiJsonHelper.parse(discountPrice);
						final String categoryId = fromApiJsonHelper.extractStringNamed("categoryId", element);
						final Long id = fromApiJsonHelper.extractLongNamed("id", element);
						final BigDecimal discountRate = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("discountRate", element);
						if(id != null){
						DiscountDetails discountDetails =this.discountDetailRepository.findOne(id);
						
						if(discountDetails != null){
							discountDetails.setCategoryType(categoryId);
							discountDetails.setDiscountRate(discountRate);
							this.discountDetailRepository.saveAndFlush(discountDetails);
							if(details.contains(discountDetails)){
							   details.remove(discountDetails);
							}
						}
						}else {
							DiscountDetails newDetails = new DiscountDetails(categoryId, discountRate);
							discountMaster.addDetails(newDetails);
						}
						
				  }
					 discountMaster.getDiscountDetails().removeAll(details);
			final Map<String, Object> changes = discountMaster.update(command);
			this.discountMasterRepository.saveAndFlush(discountMaster);
			
			return new CommandProcessingResultBuilder().withCommandId(command.commandId())
					       .withEntityId(discountMaster.getId()).with(changes).build();

		} catch (DataIntegrityViolationException dve) {
				handleCodeDataIntegrityIssues(command, dve);
			return new CommandProcessingResult(Long.valueOf(-1));

		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see #deleteDiscount(java.lang.Long)
	 */
	@Transactional
	@Override
	public CommandProcessingResult deleteDiscount(final Long entityId) {

		DiscountMaster discountMaster = null;

		try {
			this.context.authenticatedUser();
			discountMaster = this.discountRetrieveById(entityId);
			discountMaster.delete();
			this.discountMasterRepository.save(discountMaster);
			return new CommandProcessingResult(entityId);

		} catch (final DataIntegrityViolationException dve) {
			
			throw new PlatformDataIntegrityException("error.msg.could.unknown.data.integrity.issue",
					"Unknown data integrity issue with resource: "+ dve.getMessage());
		}

	}

	private DiscountMaster discountRetrieveById(final Long entityId) {

		DiscountMaster discountMaster = this.discountMasterRepository.findOne(entityId);
		if (discountMaster == null) {
			throw new DiscountMasterNotFoundException(entityId);
		}
		return discountMaster;
	}
}
