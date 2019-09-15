import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.security.MessageDigest ;
import java.security.Signature;
import javax.crypto.Cipher;
import java.util.*;
import java.nio.charset.*;

public class Cryptography
{

	private static final String ALGORITHM = "RSA";
	private static MessageDigest md = null;
	private static final Charset ISO_8859_1 = StandardCharsets.ISO_8859_1;

	public static byte[] encrypt(byte[] publicKey, byte[] inputData)
			throws Exception 
	{
		PublicKey key = KeyFactory.getInstance(ALGORITHM)
				.generatePublic(new X509EncodedKeySpec(publicKey));

		Cipher cipher = Cipher.getInstance(ALGORITHM);
		cipher.init(Cipher.ENCRYPT_MODE, key);

		byte[] encryptedBytes = cipher.doFinal(inputData);

		return encryptedBytes;
	}

	public static String encrypt(String key, String input)
	{
		try
		{
			byte[] temp = encrypt(toBytes(key),toBytes(input));
			return toString(temp);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		return null;
	}

	public static String decrypt(byte[] key, String input)
	{
		try
		{
			byte[] temp = decrypt(key,toBytes(input));
			return toString(temp);
		}
		catch(Exception e)
		{
			return "";
			//e.printStackTrace();
		}

		//return null;
	}

	public static String decrypt(String key, String input)
	{
		try
		{
			byte[] temp = decrypt(toBytes(key),toBytes(input));
			return toString(temp);
		}
		catch(Exception e)
		{
			return "";
			//e.printStackTrace();
		}

		//return null;
	}

	public static byte[] decrypt(byte[] privateKey, byte[] inputData)
			throws Exception
	{

		PrivateKey key = KeyFactory.getInstance(ALGORITHM)
				.generatePrivate(new PKCS8EncodedKeySpec(privateKey));

		Cipher cipher = Cipher.getInstance(ALGORITHM);
		cipher.init(Cipher.DECRYPT_MODE, key);

		byte[] decryptedBytes = cipher.doFinal(inputData);

		return decryptedBytes;
	}

	public static KeyPair generateKeyPair()
			throws NoSuchAlgorithmException, NoSuchProviderException {

		KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ALGORITHM);

		SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");

		// 512 is keysize
		keyGen.initialize(512, random);

		KeyPair generateKeyPair = keyGen.generateKeyPair();
		return generateKeyPair;
	}

	public static String toString(byte[] ar)
	{
		try
		{
			// return Base64.getEncoder().encodeToString(ar).trim();
			return new String(ar, ISO_8859_1);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	public static byte[] toBytes(String s)
	{
		// return Base64.getDecoder().decode(s);
		//return s.getBytes("UTF-8");
		try
		{
			// int l = 4 - (s.length()%4);
			// for(int i=0;i<l;++i)
			// 	s+=" ";

			// return Base64.getDecoder().decode(s);
			return s.getBytes(ISO_8859_1);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	public static String sign(String plainText, PrivateKey privateKey)
	{
		try
		{
		    Signature privateSignature = Signature.getInstance("SHA256withRSA");
		    privateSignature.initSign(privateKey);
		    privateSignature.update(plainText.getBytes());

		    byte[] signature = privateSignature.sign();

		    return toString(signature);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	public static Boolean verify(String plainText, String signature, String publicKey)
	{
		try
		{
			PublicKey key = KeyFactory.getInstance(ALGORITHM)
				.generatePublic(new X509EncodedKeySpec(toBytes(publicKey)));
		    Signature publicSignature = Signature.getInstance("SHA256withRSA");
		    publicSignature.initVerify(key);
		    publicSignature.update(plainText.getBytes());

		    byte[] signatureBytes = toBytes(signature);

		    return publicSignature.verify(signatureBytes);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			//return false;
		}
		return null;
	}

	public static void main(String[] args) throws Exception {

		KeyPair generateKeyPair = generateKeyPair();
		byte[] publicKey = generateKeyPair.getPublic().getEncoded();
		byte[] privateKey = generateKeyPair.getPrivate().getEncoded();

		String s = "1234 678";
		// for(int i=0;i<publicKey.length;++i)
			// System.out.println(publicKey[i]);

		// System.out.println(toString(toBytes(s)));
		byte[] ar = {65,66,67};
		byte[] ar2 = toBytes(toString(publicKey));
		System.out.println(Arrays.equals(publicKey, ar2));
		String test = encrypt(toString(publicKey), "whyyy");
		String dec = decrypt(toString(privateKey), test);
		System.out.println(dec);
		// byte[] encryptedData = encrypt(publicKey,
				// "hi there".getBytes());

		// byte[] decryptedData = decrypt(privateKey, encryptedData);

		String sig = sign("hey there",generateKeyPair.getPrivate());
		boolean verif = verify("hey there",sig,toString(publicKey));

		System.out.println(sig);
		System.out.println(verif);

		// String pub = 
		// System.out.println(pub.length());

	}

}