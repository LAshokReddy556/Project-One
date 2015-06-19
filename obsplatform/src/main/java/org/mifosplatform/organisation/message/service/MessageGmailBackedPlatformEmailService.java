
package org.mifosplatform.organisation.message.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.HtmlEmail;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.joda.time.LocalDate;
import org.json.JSONException;
import org.json.JSONObject;
import org.mifosplatform.infrastructure.configuration.domain.Configuration;
import org.mifosplatform.infrastructure.configuration.domain.ConfigurationConstants;
import org.mifosplatform.infrastructure.configuration.domain.ConfigurationRepository;
import org.mifosplatform.infrastructure.configuration.exception.ConfigurationPropertyNotFoundException;
import org.mifosplatform.infrastructure.core.service.DateUtils;
import org.mifosplatform.organisation.message.data.BillingMessageDataForProcessing;
import org.mifosplatform.organisation.message.domain.BillingMessage;
import org.mifosplatform.organisation.message.domain.BillingMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * @author ashokreddy
 *
 */
@Service
public class MessageGmailBackedPlatformEmailService implements MessagePlatformEmailService {
	

	private final BillingMessageRepository messageDataRepository;
	private final ConfigurationRepository repository;

	private String authuser;
	private String encodedPassword;
	private String authpwd;
	private String hostName;
	private int portNumber;
	private String port;
	private String starttlsValue;
	private String setContentString;
	private Configuration configuration;
	private HttpClient client = new DefaultHttpClient();
	
	@Autowired

	public MessageGmailBackedPlatformEmailService(BillingMessageRepository messageDataRepository,final ConfigurationRepository repository) {
		
		this.messageDataRepository = messageDataRepository;
		this.repository=repository;
	}
	
	public void smtpDataProcessing() {
		try {
			
			configuration = repository.findOneByName(ConfigurationConstants.CONFIG_PROPERTY_SMTP);
			
			if( null == configuration || null == configuration.getValue()){
				throw new ConfigurationPropertyNotFoundException(ConfigurationConstants.CONFIG_PROPERTY_SMTP);
			}
			
			String value = configuration.getValue();
			JSONObject object = new JSONObject(value);
			
			authuser = (String) object.get("mailId");
			encodedPassword = (String) object.get("password");
			authpwd = new String(Base64.decodeBase64(encodedPassword));
			hostName = (String) object.get("hostName");
			port = object.getString("port");
			if (port.isEmpty()) {
				portNumber = Integer.parseInt("25");
			} else {
				portNumber = Integer.parseInt(port);
			}
			starttlsValue = (String) object.get("starttls");
			setContentString = (String) object.get("setContentString");
			
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

	@SuppressWarnings("deprecation")
	@Override
	public String sendToUserEmail(BillingMessageDataForProcessing emailDetail) {
		
		smtpDataProcessing();
		if(configuration != null){
			
			 //1) get the session object      
		     Properties properties = System.getProperties();  
		     properties.setProperty("mail.smtp.host", hostName);   
		     properties.put("mail.smtp.ssl.trust",hostName);
		     properties.put("mail.smtp.auth", "true");  
		     properties.put("mail.smtp.starttls.enable", starttlsValue);//put as false
		     properties.put("mail.smtp.starttls.required", starttlsValue);//put as false


		     Session session = Session.getDefaultInstance(properties,   
		             new javax.mail.Authenticator() {   
		         protected PasswordAuthentication getPasswordAuthentication() {   
		             return new PasswordAuthentication(authuser,authpwd);    }   });       

		     //2) compose message      
			try {
				MimeMessage message = new MimeMessage(session);
				message.setFrom(new InternetAddress(authuser));
				message.addRecipient(Message.RecipientType.TO,new InternetAddress(emailDetail.getMessageTo()));
				message.setSubject(emailDetail.getSubject());

				StringBuilder messageBuilder = new StringBuilder()
						.append(emailDetail.getHeader())
						.append(emailDetail.getBody())
						.append(emailDetail.getFooter());

				// 3) create MimeBodyPart object and set your message text
				MimeBodyPart messageBodyPart = new MimeBodyPart();
				messageBodyPart.setContent(messageBuilder.toString(),setContentString);
				
				// 4) create new MimeBodyPart object and set DataHandler object to this object
				MimeBodyPart mimeBodyAttachPart = new MimeBodyPart();
				String filelocation = emailDetail.getAttachment();// change accordingly
				
				if (filelocation != null) {
					DataSource source = new FileDataSource(filelocation);
					mimeBodyAttachPart.setDataHandler(new DataHandler(source));
					mimeBodyAttachPart.setFileName(source.getName());
				}
				
				// 5) create Multipart object and add MimeBodyPart objects to this object
				Multipart multipart = new MimeMultipart();
				multipart.addBodyPart(messageBodyPart);
				
				if(filelocation != null){
					multipart.addBodyPart(mimeBodyAttachPart);
				}
				
				// 6) set the multiplart object to the message object
				message.setContent(multipart);

				// 7) send message
				Transport.send(message);
				System.out.println("message sent Successfully....");
				BillingMessage billingMessage = this.messageDataRepository.findOne(emailDetail.getId());
				if (billingMessage.getStatus().contentEquals("N")) {
					billingMessage.updateStatus();
				}
				this.messageDataRepository.save(billingMessage);
				return "success";

			}catch(Exception e){
				 System.out.println("message sending failed :" + e.getMessage());

		    	 handleCodeDataIntegrityIssues(null, e);
			     return e.getMessage();
		     }
		        
		}else{	
			throw new ConfigurationPropertyNotFoundException("SMTP GlobalConfiguration Property Not Found"); 		
		}

		
	}

	private void handleCodeDataIntegrityIssues(Object object, Exception dve) {
		// TODO Autogenerated method stub
		/*http://ukapi.smppcloud.com/api/v3/sendsms/plain
			?user=asianetott&password=AsiaWelcome321&sender=ASIANT
			&SMSText=api%20test&GSM=919946999111*/
	}

	@Override
	public String sendToUserMobile(String message, Long id, String messageTo, String messageBody) {
		
		try {
			String output = "Failure", senderName = null, senderMessage = null, contentType = null, areaCode = null;
			
			Configuration configuration = this.repository.findOneByName(ConfigurationConstants.CONFIG_PROPERTY_SMS);
			
			if( null == configuration || configuration.getValue().isEmpty() || !configuration.isEnabled()){
				throw new ConfigurationPropertyNotFoundException(ConfigurationConstants.CONFIG_PROPERTY_SMS);
			}
			
			JSONObject object = new JSONObject(configuration.getValue());
			
			String urlString = object.getString("url");
			String method = object.getString("method"); 
			
			if(object.has("contentType")) contentType = object.getString("contentType");
			if(object.has("senderName")) senderName = object.getString("senderName");
			if(object.has("senderMessage")) senderMessage = object.getString("senderMessage");
			if(object.has("areaCode")) areaCode = object.getString("areaCode");
			
			if(null!=method && method.equalsIgnoreCase("POST")){

				String decodedString,retval = "";
			
				URL url = new URL(urlString);
				HttpURLConnection urlconnection = (HttpURLConnection) url.openConnection();
				urlconnection.setRequestMethod(method);
				
				if(null != contentType && !contentType.isEmpty()){
					urlconnection.setRequestProperty("ContentType", contentType);
				}
				
				urlconnection.setDoOutput(true);
				OutputStreamWriter out = new OutputStreamWriter(urlconnection.getOutputStream());
				out.write(message);
				out.close();
				BufferedReader in = new BufferedReader(new InputStreamReader(urlconnection.getInputStream()));
				
				while ((decodedString = in.readLine()) != null) {
					retval += decodedString;
				}
				in.close();
				System.out.println("Message Sending to " + messageTo + " and Response=" + retval);
				retval = retval.substring(0, 3);
				
				if(retval.equalsIgnoreCase("OK:")) output = "SUCCESS";
				
			} else if (null!=method && method.equalsIgnoreCase("GET")) {
				
				String encodedMessage = URLEncoder.encode(messageBody);
				
				//String url = "http://202.62.67.34/smpp.sms?username=obs&password=9989275041&from=RADIUS&to=" + messageTo + "&text=" + encodedMessage;
				String url = urlString + "&to=" + messageTo + "&text=" + encodedMessage;
				
				HttpGet get = new HttpGet(url);
				
				HttpResponse response = client.execute(get);
				
				InputStream is = response.getEntity().getContent();
				BufferedReader br = new BufferedReader(new InputStreamReader(is));
				String result = "";
				String line = null;
				while ((line = br.readLine()) != null) {
				result += line;
				}
				System.out.println(result);
				if(result.contains(messageTo)) output = "SUCCESS";
			}
			
			if (null != output && output.equalsIgnoreCase("SUCCESS")) {
				BillingMessage billingMessage = this.messageDataRepository.findOne(id);
				if (billingMessage.getStatus().contentEquals("N")) {
					billingMessage.updateStatus();
				}
				this.messageDataRepository.save(billingMessage);
			}
			return "success";
			
			

		} catch (UnsupportedEncodingException e) {
			System.out.println("UnsupportedEncodingException : "
					+ e.getMessage() + " . encoding pattern not supported.");
			return e.getMessage();
		} catch (MalformedURLException e) {
			System.out.println("MalformedURLException : " + e.getMessage()
					+ " . URL is not located.");
			return e.getMessage();
		} catch (IOException e) {
			System.out.println("IOException : " + e.getMessage() + ".");
			return e.getMessage();
		} catch (Exception e) {
			System.out.println("JSONException : " + e.getMessage() + ".");
			return e.getMessage();
		}
	}


	@SuppressWarnings("deprecation")
	@Override
	public String createEmail(String pdfFileName, String emailId) {
		
		smtpDataProcessing();

		if(configuration != null){
			
			Date date=DateUtils.getDateOfTenant();
			String dateTime=date.getHours()+""+date.getMinutes();
		    String fileName="ReportEmail_"+new LocalDate().toString().replace("-","")+"_"+dateTime+".pdf";
		    Properties props = new Properties();
		    props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.starttls.enable", starttlsValue);
			props.put("mail.smtp.host", hostName);
			props.put("mail.smtp.port", portNumber);

			Session session = Session.getInstance(props,new javax.mail.Authenticator() {
			      protected PasswordAuthentication getPasswordAuthentication() {
			        return new PasswordAuthentication(authuser, authpwd);
			      }
			});

			try {

				Message message = new MimeMessage(session);
			    message.setFrom(new InternetAddress(emailId));
			    message.setRecipients(Message.RecipientType.TO,
			       InternetAddress.parse(emailId));
			    message.setSubject("ReportEmail");
					
				MimeBodyPart messageBodyPart = new MimeBodyPart();

			    Multipart multipart = new MimeMultipart();
			  
			    String file = pdfFileName;
			    DataSource source = new FileDataSource(file);
			    messageBodyPart.setDataHandler(new DataHandler(source));
			    messageBodyPart.setFileName(fileName);
			    multipart.addBodyPart(messageBodyPart);
			    message.setContent(multipart);
			    System.out.println("Sending");
				Transport.send(message);
			    System.out.println("Done");
			    return "Success";
		    } catch (Exception e) {
		    	System.out.println("Sending Failed");
		    	return "Email sending Failed";
			}		
		}else{			
			throw new ConfigurationPropertyNotFoundException("SMTP GlobalConfiguration Property Not Found"); 		

		}
		 
		    
	}

	@Override
	public String sendGeneralMessage(String emailId, String body ,String subject) {

		smtpDataProcessing();
		
		if(configuration != null){
			
			HtmlEmail email = new HtmlEmail();
			
			email.setAuthenticator(new DefaultAuthenticator(authuser, authpwd));
			email.setHostName(hostName);
				try{
					String sendToEmail = emailId;
					email.setStartTLSRequired(starttlsValue.equalsIgnoreCase("true"));
					email.setFrom(authuser);
					email.setSmtpPort(portNumber);
					email.setSubject(subject);
					email.addTo(sendToEmail);
					email.setHtmlMsg(body);	
					email.send();
					return "success";
					
				}catch (Exception e) {
					handleCodeDataIntegrityIssues(null, e);
					return e.getMessage();
				}
				
		}else{			
			throw new ConfigurationPropertyNotFoundException("SMTP GlobalConfiguration Property Not Found"); 			
		}
		       
	}
	@Override
	public String sendTicketMessage(BillingMessageDataForProcessing emailDetail) {

		
		smtpDataProcessing();
		
		if(configuration != null){
			
			HtmlEmail email = new HtmlEmail();
			
			email.setAuthenticator(new DefaultAuthenticator(authuser, authpwd));
			email.setHostName(hostName);
				try{
					String sendToEmail = emailDetail.getMessageTo();
					email.setStartTLSRequired(starttlsValue.equalsIgnoreCase("true"));
					email.setFrom(authuser);
					email.setSmtpPort(portNumber);
					email.setSubject(emailDetail.getSubject());
					email.addTo(sendToEmail);
					email.setHtmlMsg(emailDetail.getHeader()+"<br/>"+emailDetail.getBody());	
					email.send();
					//return "success";
					BillingMessage billingMessage = this.messageDataRepository.findOne(emailDetail.getId());
					if (billingMessage.getStatus().contentEquals("N")) {
						billingMessage.updateStatus();
					}
					this.messageDataRepository.save(billingMessage);
					return "success";
					
				}catch (Exception e) {
					handleCodeDataIntegrityIssues(null, e);
					return e.getMessage();
				}
				
		}else{			
			throw new ConfigurationPropertyNotFoundException("SMTP GlobalConfiguration Property Not Found"); 			
		}
		       
	}
}