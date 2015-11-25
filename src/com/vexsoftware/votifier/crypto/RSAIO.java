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

import java.io.File;
import java.security.KeyPair;

/**
 * Static utility methods for saving and loading RSA key pairs.
 * 
 * @author Blake Beaupain
 */
public class RSAIO {

	/**
	 * Saves the key pair to the disk.
	 * 
	 * @param directory
	 *            The directory to save to
	 * @param keyPair
	 *            The key pair to save
	 * @throws Exception
	 *             If an error occurs
	 */
	public static void save(File directory, KeyPair keyPair) throws Exception
	{
		RSAModule.impl.save(directory, keyPair);
	}

	/**
	 * Loads an RSA key pair from a directory. The directory must have the files
	 * "public.key" and "private.key".
	 * 
	 * @param directory
	 *            The directory to load from
	 * @return The key pair
	 * @throws Exception
	 *             If an error occurs
	 */
	public static KeyPair load(File directory) throws Exception
	{
		return RSAModule.impl.load(directory);
	}

}
