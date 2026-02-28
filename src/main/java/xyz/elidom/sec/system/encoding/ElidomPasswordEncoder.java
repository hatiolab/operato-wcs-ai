package xyz.elidom.sec.system.encoding;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

//import org.springframework.security.crypto.codec.Base64;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.security.crypto.codec.Utf8;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * ElidomPasswordEncoder
 * 
 * @author shortstop
 */
@Component
public class ElidomPasswordEncoder implements PasswordEncoder {
//	/**
//	 * salt generator
//	 */
//	private StringKeyGenerator saltGenerator = new Base64StringKeyGenerator();
//
//	private boolean encodeHashAsBase64;
//
//	private ElidomDigester digester;
//	
//	/**
//	 * 기본 알고리즘은 SHA-256
//	 */
//	public ElidomPasswordEncoder() {
//		this("SHA-256");
//	}
//
//	/**
//	 * The digest algorithm to use Supports the named
//	 * <a href="https://java.sun.com/j2se/1.4.2/docs/guide/security/CryptoSpec.html#AppA">
//	 * Message Digest Algorithms</a> in the Java environment.
//	 * @param algorithm
//	 */
//	public ElidomPasswordEncoder(String algorithm) {
//		this.digester = new ElidomDigester(algorithm, 1);
//	}
//
//	public void setEncodeHashAsBase64(boolean encodeHashAsBase64) {
//		this.encodeHashAsBase64 = encodeHashAsBase64;
//	}
//
//	/**
//	 * Encodes the rawPass using a MessageDigest. If a salt is specified it will be merged
//	 * with the password before encoding.
//	 * @param rawPassword The plain text password
//	 * @return Hex string of password digest (or base64 encoded string if
//	 * encodeHashAsBase64 is enabled.
//	 */
//	@Override
//	public String encode(CharSequence rawPassword) {
//		String salt = this.saltGenerator.generateKey();
//		return digest(salt, rawPassword);
//	}
//
//	private String digest(String salt, CharSequence rawPassword) {
//		String saltedPassword = rawPassword + salt;
//		byte[] digest = this.digester.digest(Utf8.encode(saltedPassword));
//		String encoded = encode(digest);
//		return salt + encoded;
//	}
//
//	private String encode(byte[] digest) {
//		if (this.encodeHashAsBase64) {
//			return Utf8.decode(Base64.getEncoder().encode(digest));
//		}
//		return new String(Hex.encode(digest));
//	}
//
//	/**
//	 * Takes a previously encoded password and compares it with a rawpassword after mixing
//	 * in the salt and encoding that value
//	 * @param rawPassword plain text password
//	 * @param encodedPassword previously encoded password
//	 * @return true or false
//	 */
//	@Override
//	public boolean matches(CharSequence rawPassword, String encodedPassword) {
//		String salt = extractSalt(encodedPassword);
//		String rawPasswordEncoded = digest(salt, rawPassword);
//		return this.equalPassword(encodedPassword.toString(), rawPasswordEncoded);
//	}
//
//	/**
//	 * Sets the number of iterations for which the calculated hash value should be
//	 * "stretched". If this is greater than one, the initial digest is calculated, the
//	 * digest function will be called repeatedly on the result for the additional number
//	 * of iterations.
//	 * @param iterations the number of iterations which will be executed on the hashed
//	 * password/salt value. Defaults to 1.
//	 */
//	public void setIterations(int iterations) {
//		this.digester.setIterations(iterations);
//	}
//
//	private String extractSalt(String prefixEncodedPassword) {
//		return prefixEncodedPassword;
//	}
//	
//	/**
//	 * Constant time comparison to prevent against timing attacks.
//	 * 
//	 * @param expected
//	 * @param actual
//	 * @return
//	 */
//	private boolean equalPassword(String expected, String actual) {
//		byte[] expectedBytes = bytesUtf8(expected);
//		byte[] actualBytes = bytesUtf8(actual);
//		return MessageDigest.isEqual(expectedBytes, actualBytes);
//	}
//
//	private static byte[] bytesUtf8(String s) {
//		// need to check if Utf8.encode() runs in constant time (probably not).
//		// This may leak length of string.
//		return (s != null) ? Utf8.encode(s) : null;
//	}
	
	private final String algorithm;
	private int iterations = 1;
	private boolean encodeHashAsBase64 = false;

	public ElidomPasswordEncoder() {
		this("SHA-256");
	}
	
	/**
	 * The digest algorithm to use Supports the named
	 * <a href="http://java.sun.com/j2se/1.4.2/docs/guide/security/CryptoSpec.html#AppA">
	 * Message Digest Algorithms</a> in the Java environment.
	 *
	 * @param algorithm
	 */
	public ElidomPasswordEncoder(String algorithm) {
		this(algorithm, false);
	}

	/**
	 * Convenience constructor for specifying the algorithm and whether or not to enable
	 * base64 encoding
	 *
	 * @param algorithm
	 * @param encodeHashAsBase64
	 * @throws IllegalArgumentException if an unknown
	 */
	public ElidomPasswordEncoder(String algorithm, boolean encodeHashAsBase64) throws IllegalArgumentException {
		this.algorithm = algorithm;
		setEncodeHashAsBase64(encodeHashAsBase64);
		// Validity Check
		getMessageDigest();
	}
	
	public boolean getEncodeHashAsBase64() {
		return encodeHashAsBase64;
	}
	
	/**
	 * The encoded password is normally returned as Hex (32 char) version of the hash
	 * bytes. Setting this property to true will cause the encoded pass to be returned as
	 * Base64 text, which will consume 24 characters.
	 *
	 * @param encodeHashAsBase64 set to true for Base64 output
	 */
	public void setEncodeHashAsBase64(boolean encodeHashAsBase64) {
		this.encodeHashAsBase64 = encodeHashAsBase64;
	}
	
	/**
	 * Encodes the rawPass using a MessageDigest. If a salt is specified it will be merged
	 * with the password before encoding.
	 *
	 * @param rawPass The plain text password
	 * @param salt The salt to sprinkle
	 * @return Hex string of password digest (or base64 encoded string if
	 * encodeHashAsBase64 is enabled.
	 */
	@Override
	public String encode(CharSequence rawPass) {
		Object salt = null;
		String saltedPass = mergePasswordAndSalt((String)rawPass, salt, false);

		MessageDigest messageDigest = getMessageDigest();

		byte[] digest = messageDigest.digest(Utf8.encode(saltedPass));

		// "stretch" the encoded value if configured to do so
		for (int i = 1; i < this.iterations; i++) {
			digest = messageDigest.digest(digest);
		}

		if (getEncodeHashAsBase64()) {
			// return Utf8.decode(Base64.encode(digest));
			return Utf8.decode(Base64.getEncoder().encode(digest));
		} else {
			return new String(Hex.encode(digest));
		}
	}

	/**
	 * Get a MessageDigest instance for the given algorithm. Throws an
	 * IllegalArgumentException if <i>algorithm</i> is unknown
	 *
	 * @return MessageDigest instance
	 * @throws IllegalArgumentException if NoSuchAlgorithmException is thrown
	 */
	protected final MessageDigest getMessageDigest() throws IllegalArgumentException {
		try {
			return MessageDigest.getInstance(this.algorithm);
		}
		catch (NoSuchAlgorithmException e) {
			throw new IllegalArgumentException(
					"No such algorithm [" + this.algorithm + "]");
		}
	}
	
	/**
	 * Takes a previously encoded password and compares it with a rawpassword after mixing
	 * in the salt and encoding that value
	 *
	 * @param encPass previously encoded password
	 * @param rawPass plain text password
	 * @param salt salt to mix into password
	 * @return true or false
	 */
	@Override
	public boolean matches(CharSequence encPass, String rawPass) {
		String pass1 = "" + encPass;
		// String pass2 = encodePassword(rawPass, salt);
		// return this.equals(pass1, pass2);
		return this.equals(pass1, rawPass);
	}

	public String getAlgorithm() {
		return this.algorithm;
	}

	/**
	 * Sets the number of iterations for which the calculated hash value should be
	 * "stretched". If this is greater than one, the initial digest is calculated, the
	 * digest function will be called repeatedly on the result for the additional number
	 * of iterations.
	 *
	 * @param iterations the number of iterations which will be executed on the hashed
	 * password/salt value. Defaults to 1.
	 */
	public void setIterations(int iterations) {
		Assert.isTrue(iterations > 0, "Iterations value must be greater than zero");
		this.iterations = iterations;
	}
	
	/**
	 * Used by subclasses to generate a merged password and salt <code>String</code>.
	 * <P>
	 * The generated password will be in the form of <code>password{salt}</code>.
	 * </p>
	 * <p>
	 * A <code>null</code> can be passed to either method, and will be handled correctly.
	 * If the <code>salt</code> is <code>null</code> or empty, the resulting generated
	 * password will simply be the passed <code>password</code>. The <code>toString</code>
	 * method of the <code>salt</code> will be used to represent the salt.
	 * </p>
	 *
	 * @param password the password to be used (can be <code>null</code>)
	 * @param salt the salt to be used (can be <code>null</code>)
	 * @param strict ensures salt doesn't contain the delimiters
	 *
	 * @return a merged password and salt <code>String</code>
	 *
	 * @throws IllegalArgumentException if the salt contains '{' or '}' characters.
	 */
	protected String mergePasswordAndSalt(String password, Object salt, boolean strict) {
		if (password == null) {
			password = "";
		}

		if (strict && (salt != null)) {
			if ((salt.toString().lastIndexOf("{") != -1)
					|| (salt.toString().lastIndexOf("}") != -1)) {
				throw new IllegalArgumentException("Cannot use { or } in salt.toString()");
			}
		}

		if ((salt == null) || "".equals(salt)) {
			return password;
		}
		else {
			return password + "{" + salt.toString() + "}";
		}
	}
	
	/**
	 * Constant time comparison to prevent against timing attacks.
	 * @param expected
	 * @param actual
	 * @return
	 */
	boolean equals(String expected, String actual) {
		byte[] expectedBytes = bytesUtf8(expected);
		byte[] actualBytes = bytesUtf8(actual);
		int expectedLength = expectedBytes == null ? -1 : expectedBytes.length;
		int actualLength = actualBytes == null ? -1 : actualBytes.length;
		if (expectedLength != actualLength) {
			return false;
		}

		int result = 0;
		for (int i = 0; i < expectedLength; i++) {
			result |= expectedBytes[i] ^ actualBytes[i];
		}
		return result == 0;
	}

	private byte[] bytesUtf8(String s) {
		if (s == null) {
			return null;
		}

		return Utf8.encode(s);
	}
}
