package org.mifosplatform.finance.paymentsgateway.domain;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang3.RandomUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import java.io.ByteArrayOutputStream;
import java.security.Provider;

public class Aes256Encryption {

	private static final String CIPHER_ALG = "PBEWITHMD5AND256BITAES-CBC-OPENSSL";
	private static final Provider CIPHER_PROVIDER = new BouncyCastleProvider();
	private static final String PREFIX = "Salted__";
	private static final String UTF_8 = "UTF-8";
	private BASE64Encoder encoder = new BASE64Encoder();
	private BASE64Decoder decoder = new BASE64Decoder();
	
	public String encrypt(String plainText, char[] password) throws Exception {  
        byte[] salt = RandomUtils.nextBytes(8);  
      
        Cipher cipher = createCipher(Cipher.ENCRYPT_MODE, salt, password);  
        byte[] cipherText = cipher.doFinal(plainText.getBytes(UTF_8));  
      
        ByteArrayOutputStream baos = new ByteArrayOutputStream(cipherText.length + 16);  
        baos.write(PREFIX.getBytes(UTF_8));  
        baos.write(salt);  
        baos.write(cipherText);  
      
        return encoder.encode(baos.toByteArray());  
      }  
      
      public String decrypt(String cipherText, char[] password) throws Exception {  
        byte[] input = decoder.decodeBuffer(cipherText);  
      
        String prefixText = new String(input, 0, 8, UTF_8);  
        Validate.isTrue(prefixText.equals(PREFIX), "Invalid prefix: ", prefixText);  
      
        byte[] salt = new byte[8];  
        System.arraycopy(input, 8, salt, 0, salt.length);  
      
        Cipher cipher = createCipher(Cipher.DECRYPT_MODE, salt, password);  
        byte[] plainText = cipher.doFinal(input, 16, input.length - 16);  
      
        return new String(plainText, UTF_8);  
      }  
      
      private Cipher createCipher(int cipherMode, byte[] salt, char[] password)  
          throws Exception {  
      
        PBEKeySpec pbeSpec = new PBEKeySpec(password);  
        SecretKeyFactory keyFact = SecretKeyFactory.getInstance(CIPHER_ALG, CIPHER_PROVIDER);  
        PBEParameterSpec defParams = new PBEParameterSpec(salt, 0);  
      
        Cipher cipher = Cipher.getInstance(CIPHER_ALG, CIPHER_PROVIDER);  
        cipher.init(cipherMode, keyFact.generateSecret(pbeSpec), defParams);  
        return cipher;  
      }  

}
