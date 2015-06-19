package org.mifosplatform.organisation.message.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.mifosplatform.infrastructure.core.domain.AbstractAuditableCustom;
import org.mifosplatform.useradministration.domain.AppUser;

/**
 * Entity class
 * 
 * @author ashokreddy
 *
 */

@Entity
@Table(name = "b_message_data")
public class BillingMessage extends AbstractAuditableCustom<AppUser, Long> {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Column(name = "message_to")
	private String messageTo;
	
	@Column(name = "message_from")
	private String messageFrom;
	
	@Column(name = "status")
	private String status;
	
	@Column(name = "header")
	private String header;

	@Column(name = "body")
	private String body;
	
	@Column(name = "footer")
	private String footer;
	
	@Column(name = "subject")
	private String subject;
	
	@Column(name = "message_type")
	private char messageType;
	
	@Column(name = "attachment")
	private String attachment;
	
	@Column(name = "description")
	private String description;
	
	@ManyToOne
    @JoinColumn(name="msgtemplate_id")
    private BillingMessageTemplate billingMessageTemplate;
	
	/*@Column(name ="template_id")
	private Long templateId;*/
	
	public BillingMessage(){
		//default-constructor
	}

	public BillingMessage(final String header, final String body, final String footer,
			final  String messageFrom, final String messageTo, final String subject,
			final String status, final BillingMessageTemplate billingMessageTemplate, 
			final char messageType, final String attachment) {
        
		this.header=header;
		this.body=body;
		this.footer=footer;
		this.messageFrom=messageFrom;
		this.messageTo=messageTo;
		this.subject=subject;
		this.status=status;
	    this.billingMessageTemplate=billingMessageTemplate;
		this.messageType=messageType;
		this.attachment=attachment;
		//this.templateId = null;
	}

	public char getMessageType() {
		return messageType;
	}

	public String getAttachment() {
		return attachment;

	}

	public String getMessageTo() {
		return messageTo;
	}

	public void setMessageTo(String messageTo) {
		this.messageTo = messageTo;
	}

	public String getMessageFrom() {
		return messageFrom;
	}

	public void setMessageFrom(String messageFrom) {
		this.messageFrom = messageFrom;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getFooter() {
		return footer;
	}

	public void setFooter(String footer) {
		this.footer = footer;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public BillingMessageTemplate getBillingMessageTemplate() {
		return billingMessageTemplate;
	}

	public void setupdate(BillingMessageTemplate billingMessageTemplate) {
		this.billingMessageTemplate = billingMessageTemplate;
	}

	public void updateStatus() {		
		this.status="Y";	
	}

	public void setAttachment(String attachment) {
		this.attachment = attachment;
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public BillingMessage(final String header, final String body, final String footer,
			final  String messageFrom, final String messageTo, final String subject,
			final String status, final Long templateId, 
			final char messageType, final String attachment) {
        
		this.header=header;
		this.body=body;
		this.footer=footer;
		this.messageFrom=messageFrom;
		this.messageTo=messageTo;
		this.subject=subject;
		this.status=status;
	//	this.templateId=templateId;
		this.messageType=messageType;
		this.attachment=attachment;
		this.billingMessageTemplate = null;
	}

	/*public Long getTemplateId() {
		return templateId;
	}

	public void setTemplateId(Long templateId) {
		this.templateId = templateId;
	}*/
	
	
	
}
