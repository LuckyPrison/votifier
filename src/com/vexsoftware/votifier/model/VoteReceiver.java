/*
 * Copyright (C) 2012 Vex Software LLC
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

package com.vexsoftware.votifier.model;

import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import javax.crypto.BadPaddingException;

import com.ulfric.lib.api.server.Events;
import com.ulfric.lib.api.task.Tasks;
import com.vexsoftware.votifier.Votifier;
import com.vexsoftware.votifier.crypto.RSA;

/**
 * The vote receiving server.
 * 
 * @author Blake Beaupain
 * @author Kramer Campbell
 */
public class VoteReceiver extends Thread {

	private final Votifier plugin;

	/** The host to listen on. */
	private final String host;

	/** The port to listen on. */
	private final int port;

	/** The server socket. */
	private ServerSocket server;

	/** The running flag. */
	private boolean running = true;

	/**
	 * Instantiates a new vote receiver.
	 * 
	 * @param host
	 *            The host to listen on
	 * @param port
	 *            The port to listen on
	 */
	protected VoteReceiver(final Votifier plugin, String host, int port) throws Exception {
		this.plugin = plugin;
		this.host = host;
		this.port = port;

		initialize();
	}

	private void initialize() throws Exception {
		try {
			this.server = new ServerSocket();
			this.server.bind(new InetSocketAddress(this.host, this.port));
		} catch (Exception ex) {
			this.plugin.warn("Error initializing vote receiver. Please verify that the configured");
			this.plugin.warn("IP address and port are not already in use. This is a common problem");
			this.plugin.warn("with hosting services and, if so, you should check with your hosting provider.");
			this.plugin.log(ex.toString());
			throw new Exception(ex);
		}
	}

	/**
	 * Shuts the vote receiver down cleanly.
	 */
	public void shutdown() {
		this.running = false;
		if (this.server == null)
			return;
		try {
			this.server.close();
		} catch (Exception ex) {
			this.plugin.warn("Unable to shut down vote receiver cleanly.");
		}
	}

	@Override
	public void run() {

		// Main loop.
		while (this.running) {
			try {
				Socket socket = this.server.accept();
				socket.setSoTimeout(5000); // Don't hang on slow connections.
				BufferedWriter writer = new BufferedWriter(
						new OutputStreamWriter(socket.getOutputStream()));
				InputStream in = socket.getInputStream();

				// Send them our version.
				writer.write("VOTIFIER " + this.plugin.getVersion());
				writer.newLine();
				writer.flush();

				// Read the 256 byte block.
				byte[] block = new byte[256];
				in.read(block, 0, block.length);

				// Decrypt the block.
				block = RSA.decrypt(block, Votifier.getInstance().getKeyPair()
						.getPrivate());
				int position = 0;

				// Perform the opcode check.
				String opcode = readString(block, position);
				position += opcode.length() + 1;
				if (!opcode.equals("VOTE")) {
					// Something went wrong in RSA.
					in.close();
					writer.close();
					socket.close();
					throw new Exception("Unable to decode RSA");
				}

				// Parse the block.
				String serviceName = readString(block, position);
				position += serviceName.length() + 1;
				String username = readString(block, position);
				position += username.length() + 1;
				String address = readString(block, position);
				position += address.length() + 1;
				String timeStamp = readString(block, position);
				position += timeStamp.length() + 1;

				// Create the vote.
				final Vote vote = new Vote();
				vote.setServiceName(serviceName);
				vote.setUsername(username);
				vote.setAddress(address);
				vote.setTimeStamp(timeStamp);

				if (this.plugin.isDebug())
					this.plugin.debug("Received vote record -> " + vote);

				// Call event in a synchronized fashion to ensure that the
				// custom event runs in the
				// the main server thread, not this one.
				Tasks.run(() -> Events.call(new VotifierEvent(vote)));

				

				// Clean up.
				writer.close();
				in.close();
				socket.close();
			} catch (SocketException ex) {
				this.plugin.warn("Protocol error. Ignoring packet - " + ex.getLocalizedMessage());
			} catch (BadPaddingException ex) {
				this.plugin.warn("Unable to decrypt vote record. Make sure that that your public key");
				this.plugin.warn("matches the one you gave the server list.");
				this.plugin.log(ex.toString());
			} catch (Throwable t) {
				this.plugin.warn("Exception caught while receiving a vote notification");
				this.plugin.log(t.toString());
			}
		}
	}

	/**
	 * Reads a string from a block of data.
	 * 
	 * @param data
	 *            The data to read from
	 * @return The string
	 */
	private String readString(byte[] data, int offset) {
		StringBuilder builder = new StringBuilder();
		for (int i = offset; i < data.length; i++) {
			if (data[i] == '\n')
				break; // Delimiter reached.
			builder.append((char) data[i]);
		}
		return builder.toString();
	}
}
