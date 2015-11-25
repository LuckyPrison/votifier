package com.vexsoftware.votifier.crypto;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAKeyGenParameterSpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.xml.bind.DatatypeConverter;

import com.ulfric.lib.api.module.SimpleModule;
import com.vexsoftware.votifier.Votifier;

public class RSAModule extends SimpleModule {

	protected static Rsa impl = Rsa.EMPTY;

	public RSAModule()
	{
		super("rsa", "RSA key module", "blakeman8192, Kramer, and Packet", "1.0.0-REL");
	}

	@Override
	public void postEnable()
	{
		RSAModule.impl = new Rsa()
		{
			@Override
			public KeyPair generate(int bits) throws Exception
			{
				Votifier.getInstance().log("Votifier is generating an RSA key pair...");

				KeyPairGenerator keygen = KeyPairGenerator.getInstance("RSA");

				RSAKeyGenParameterSpec spec = new RSAKeyGenParameterSpec(bits, RSAKeyGenParameterSpec.F4);

				keygen.initialize(spec);

				return keygen.generateKeyPair();
			}

			@Override
			public void save(File directory, KeyPair keyPair) throws Exception
			{
				PrivateKey privateKey = keyPair.getPrivate();
				PublicKey publicKey = keyPair.getPublic();

				// Store the public key.
				X509EncodedKeySpec publicSpec = new X509EncodedKeySpec(
						publicKey.getEncoded());
				FileOutputStream out = new FileOutputStream(directory + "/public.key");
				out.write(DatatypeConverter.printBase64Binary(publicSpec.getEncoded())
						.getBytes());
				out.close();

				// Store the private key.
				PKCS8EncodedKeySpec privateSpec = new PKCS8EncodedKeySpec(
						privateKey.getEncoded());
				out = new FileOutputStream(directory + "/private.key");
				out.write(DatatypeConverter.printBase64Binary(privateSpec.getEncoded())
						.getBytes());
				out.close();
			}

			@Override
			public KeyPair load(File directory) throws Exception
			{
				// Read the public key file.
				File publicKeyFile = new File(directory + "/public.key");
				FileInputStream in = new FileInputStream(directory + "/public.key");
				byte[] encodedPublicKey = new byte[(int) publicKeyFile.length()];
				in.read(encodedPublicKey);
				encodedPublicKey = DatatypeConverter.parseBase64Binary(new String(
						encodedPublicKey));
				in.close();

				// Read the private key file.
				File privateKeyFile = new File(directory + "/private.key");
				in = new FileInputStream(directory + "/private.key");
				byte[] encodedPrivateKey = new byte[(int) privateKeyFile.length()];
				in.read(encodedPrivateKey);
				encodedPrivateKey = DatatypeConverter.parseBase64Binary(new String(
						encodedPrivateKey));
				in.close();

				// Instantiate and return the key pair.
				KeyFactory keyFactory = KeyFactory.getInstance("RSA");
				X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(
						encodedPublicKey);
				PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
				PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(
						encodedPrivateKey);
				PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);
				return new KeyPair(publicKey, privateKey);
			}

			@Override
			public byte[] decrypt(byte[] data, PrivateKey key) throws Exception
			{
				Cipher cipher = Cipher.getInstance("RSA");
				cipher.init(Cipher.DECRYPT_MODE, key);
				return cipher.doFinal(data);
			}
		};

		File rsaDirectory = new File(Votifier.getInstance().getDataFolder() + "/rsa");

		/*
		 * Create RSA directory and keys if it does not exist; otherwise, read
		 * keys.
		 */
		try
		{
			if (!rsaDirectory.exists())
			{
				rsaDirectory.mkdir();
				Votifier.getInstance().setKeyPair(RSAKeygen.generate(2048));
				RSAIO.save(rsaDirectory, Votifier.getInstance().getKeyPair());
			}
			else
			{
				Votifier.getInstance().setKeyPair(RSAIO.load(rsaDirectory));
			}
		}
		catch (Exception ex)
		{
			this.getOwningPlugin().warn("Error reading RSA keys");

			this.log(ex.toString());

			Votifier.getInstance().gracefulExit();
		}
	}

	@Override
	public void postDisable()
	{
		RSAModule.impl = Rsa.EMPTY;
	}

	protected interface Rsa
	{
		Rsa EMPTY = new Rsa() { };

		default KeyPair generate(int bits) throws Exception { return null; }

		default void save(File directory, KeyPair keyPair) throws Exception { }

		default KeyPair load(File directory) throws Exception { return null; }

		default byte[] decrypt(byte[] data, PrivateKey key) throws Exception { return null; }
	}

}