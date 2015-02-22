package org.primftpd.util;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Locale;

import org.apache.ftpserver.util.IoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeyInfoProvider
{
	protected final Logger logger = LoggerFactory.getLogger(getClass());

	public String fingerprint(PrivateKey privateKey, String hashAlgo) {
		try {
			MessageDigest md = MessageDigest.getInstance(hashAlgo);
			md.update(privateKey.getEncoded());
			byte[] fingerPrintBytes = md.digest();
			return beautify(fingerPrintBytes);
		} catch (Exception e) {
			logger.error("could not read key: " + e.getMessage(), e);
		}
		return null;
	}

	private static final int BUFFER_SIZE = 4096;

	public PublicKey readPublicKey(FileInputStream fis)
		throws NoSuchAlgorithmException, InvalidKeySpecException,
		IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		IoUtils.copy(fis, baos, BUFFER_SIZE);
		byte[] pubKeyBytes = baos.toByteArray();
		X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(pubKeyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance(KeyGenerator.KEY_ALGO);
		PublicKey publicKey = keyFactory.generatePublic(pubKeySpec);
		return publicKey;
	}

	public PrivateKey readPrivatekey(FileInputStream fis)
		throws NoSuchAlgorithmException, InvalidKeySpecException,
		IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		IoUtils.copy(fis, baos, BUFFER_SIZE);
		byte[] privKeyBytes = baos.toByteArray();
		PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privKeyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance(KeyGenerator.KEY_ALGO);
		PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);
		return privateKey;
	}

	protected String beautify(byte[] fingerPrintBytes)
	{
		StringBuilder fingerPrint = new StringBuilder();
		for (int i=0; i<fingerPrintBytes.length; i++) {
			byte b = fingerPrintBytes[i];
			String hexString = Integer.toHexString(b);
			if (hexString.length() > 2) {
			hexString = hexString.substring(
			hexString.length() - 2,
			hexString.length());
			} else if (hexString.length() < 2) {
				hexString = "0" + hexString;
			}
			fingerPrint.append(hexString.toUpperCase(Locale.ENGLISH));
			if (i != fingerPrintBytes.length -1) {
				fingerPrint.append(":");
			}
			if (i > 0 && i % 10 == 0) {
				// force line breaks in UI
				fingerPrint.append("<br/>");
			}
		}
		return fingerPrint.toString();
	}
}