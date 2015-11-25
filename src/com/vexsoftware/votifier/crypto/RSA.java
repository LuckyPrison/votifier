/*
 * Copyright (C) 2011 Vex Software LLC
 * This file is part of Votifier.
 * 
 * Votifier is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Votifier is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Votifier.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.vexsoftware.votifier.crypto;

import java.security.PrivateKey;

/**
 * Static RSA utility methods for encrypting and decrypting blocks of
 * information.
 * 
 * @author Blake Beaupain
 */
public class RSA {

	/**
	 * Decrypts a block of data.
	 * 
	 * @param data
	 *            The data to decrypt
	 * @param key
	 *            The key to decrypt with
	 * @return The decrypted data
	 * @throws Exception
	 *             If an error occurs
	 */
	public static byte[] decrypt(byte[] data, PrivateKey key) throws Exception
	{
		return RSAModule.impl.decrypt(data, key);
	}

}
