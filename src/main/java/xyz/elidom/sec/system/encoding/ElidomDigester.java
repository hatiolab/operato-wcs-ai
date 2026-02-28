package xyz.elidom.sec.system.encoding;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Helper for working with the MessageDigest API. (org.springframework.security.crypto.password.Digester)
 *
 * Performs the configured number of iterations of the hashing algorithm per digest to aid
 * in protecting against brute force attacks.
 *
 * @author Keith Donald
 * @author Luke Taylor
 */
final class ElidomDigester {
	/**
	 * the digest algorithm; for example, "SHA-1" or "SHA-256".
	 */
	private final String algorithm;

	private int iterations;

	/**
	 * Create a new Digester.
	 * @param algorithm the digest algorithm; for example, "SHA-1" or "SHA-256".
	 * @param iterations the number of times to apply the digest algorithm to the input
	 */
	ElidomDigester(String algorithm, int iterations) {
		// eagerly validate the algorithm
		createDigest(algorithm);
		this.algorithm = algorithm;
		setIterations(iterations);
	}

	byte[] digest(byte[] value) {
		MessageDigest messageDigest = createDigest(this.algorithm);
		for (int i = 0; i < this.iterations; i++) {
			value = messageDigest.digest(value);
		}
		return value;
	}

	void setIterations(int iterations) {
		if (iterations <= 0) {
			throw new IllegalArgumentException("Iterations value must be greater than zero");
		}
		this.iterations = iterations;
	}

	private static MessageDigest createDigest(String algorithm) {
		try {
			return MessageDigest.getInstance(algorithm);
		}
		catch (NoSuchAlgorithmException ex) {
			throw new IllegalStateException("No such hashing algorithm", ex);
		}
	}
}
