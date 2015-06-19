/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.finance.paymentsgateway.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.Validate;
import org.bouncycastle.jce.provider.BouncyCastleProvider;


public class Aes256GibberishEncryption {

	private static final String CIPHER_ALG = "PBEWITHMD5AND256BITAES-CBC-OPENSSL";  
    private static final Provider CIPHER_PROVIDER = new BouncyCastleProvider();  
    private static final String PREFIX = "Salted__";  
    private static final String UTF_8 = "UTF-8";  
    
	public String encrypt(String plainText, char[] password)
			throws InvalidKeyException, NoSuchAlgorithmException,
			NoSuchPaddingException, InvalidAlgorithmParameterException,
			InvalidKeySpecException, IllegalBlockSizeException,
			BadPaddingException, IOException {

		byte[] salt = RandomUtils.nextBytes(8);

		Cipher cipher = createCipher(Cipher.ENCRYPT_MODE, salt, password);
		byte[] cipherText = cipher.doFinal(plainText.getBytes(UTF_8));

		ByteArrayOutputStream baos = new ByteArrayOutputStream(cipherText.length + 16);
		baos.write(PREFIX.getBytes(UTF_8));
		baos.write(salt);
		baos.write(cipherText);

		return DatatypeConverter.printBase64Binary(baos.toByteArray());
	}  
      
	public static String decrypt(String cipherText, char[] password)
			throws IOException, InvalidKeyException, NoSuchAlgorithmException,
			NoSuchPaddingException, InvalidAlgorithmParameterException,
			InvalidKeySpecException, IllegalBlockSizeException,
			BadPaddingException {

		byte[] input = DatatypeConverter.parseBase64Binary(cipherText);

		String prefixText = new String(input, 0, 8, UTF_8);
		Validate.isTrue(prefixText.equals(PREFIX), "Invalid prefix: ", prefixText);

		byte[] salt = new byte[8];
		System.arraycopy(input, 8, salt, 0, salt.length);

		Cipher cipher = createCipher(Cipher.DECRYPT_MODE, salt, password);
		byte[] plainText = cipher.doFinal(input, 16, input.length - 16);

		return new String(plainText, UTF_8);
	}  
      
      private static Cipher createCipher(int cipherMode, byte[] salt, char[] password) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, InvalidKeySpecException  
          {  
      
        PBEKeySpec pbeSpec = new PBEKeySpec(password);  
        SecretKeyFactory keyFact = SecretKeyFactory.getInstance(CIPHER_ALG, CIPHER_PROVIDER);  
        PBEParameterSpec defParams = new PBEParameterSpec(salt, 0);  
      
        Cipher cipher = Cipher.getInstance(CIPHER_ALG, CIPHER_PROVIDER);  
        cipher.init(cipherMode, keyFact.generateSecret(pbeSpec), defParams);  
        return cipher;  
      } 

}
