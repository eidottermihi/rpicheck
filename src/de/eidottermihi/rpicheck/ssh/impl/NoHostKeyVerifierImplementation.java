/**
 * 
 */
package de.eidottermihi.rpicheck.ssh.impl;

import java.security.PublicKey;

import net.schmizz.sshj.transport.verification.HostKeyVerifier;

/**
 * Always returns true.
 * 
 * @author Michael
 * 
 */
public class NoHostKeyVerifierImplementation implements HostKeyVerifier {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.schmizz.sshj.transport.verification.HostKeyVerifier#verify(java.lang
	 * .String, int, java.security.PublicKey)
	 */
	public boolean verify(String hostname, int port, PublicKey key) {
		return true;
	}

}
