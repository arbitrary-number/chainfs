package com.github.chainfs.v2;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.LegacyAddress;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.SegwitAddress;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.script.Script.ScriptType;

public class UtilityToFindABitcoinAddressFromAPublicKey {

	public static void main(String[] parameters) {

		String thePublicKeyInHexadecimalFormat =
				getTheFirstParameterFromThe(parameters);
		// Replace this value with your actual compressed or uncompressed
		// public key in hexadecimal format.  It doesn't matter what the value is
		// because the code won't validate the format and it can be any length and
		// still work, however it will only return a valid Bitcoin address if the
		// correct format is used.
		// String uncompressedPublicKeyInHexadecimalFormat
		// = "04...whatever it
		// is9a3f0d1d1e1c16d217bc13ed31c404e9b59cb18f8b162b13b85f7154d234e2aa";
		if (thePublicKeyInHexadecimalFormat == null) {
			String theCompressedPublicKeyInHexadecimalFormat =
					"029a3f0d1d1e1c16d217bc13ed31c404e9b59cb18f8b162 " +
					"b13b85f7154d234e2aa";
			thePublicKeyInHexadecimalFormat =
					theCompressedPublicKeyInHexadecimalFormat;
		}
		String theBitcoinAddressInTheLegacyFormat =
				getABitcoinAddressInTheOldFormatFromThePublicKeyInHexadecimalForm(
						thePublicKeyInHexadecimalFormat);
		String theBitcoinAddressInTheSegwitFormat =
				getABitcoinAddressInTheSegWitFormatFromThePublicKeyInHexadecimalForm(
						thePublicKeyInHexadecimalFormat);

		// Currently Bitcoinj only supports addresses starting with one or starting with
		// "bc-one".  It doesn't support addresses starting with three or "bc-one-p".
		// See Appendix One.

		System.out.println("The Bitcoin address in the Legacy format for the " +
						"provided public key \nin " +
						"hexadecimal form is "+ theBitcoinAddressInTheLegacyFormat + ".");
		System.out.println();
		System.out.println("The Bitcoin address in the Segwit format for the " +
				"provided public key \nin " +
				"hexadecimal form is "+ theBitcoinAddressInTheSegwitFormat + ".");
	}

	private static String getTheFirstParameterFromThe(String[] parameters) {
		if (parameters.length == 0) {
			return null;
		}
		return parameters[0];
	}

	public static String
		getABitcoinAddressInTheSegWitFormatFromThePublicKeyInHexadecimalForm(
				String publicKeyInHexadecimalForm) {
		byte[] theBytesOfThePublicKey =
				decodeAHexadecimalPublicKey(publicKeyInHexadecimalForm);
		ECKey ecKey = convertAPublicKeyToAnEllipticCurveKey(theBytesOfThePublicKey);
		NetworkParameters parameters = obtainTheBitcoinMainNetworkParameters();
		String theSegwitAddress =
				getABitcoinAddressInTheSegwitFormat(parameters, ecKey).toString();
		return theSegwitAddress;
	}

	public static String
		getABitcoinAddressInTheTaprootFormatFromThePublicKeyInHexadecimalForm(
				String publicKeyInHexadecimalForm) {
		// This address format is the "Taproot" Bitcoin address format, which
		// is the newest format for Bitcoin addresses.
		byte[] theBytesOfThePublicKey = decodeAHexadecimalPublicKey(publicKeyInHexadecimalForm);
		ECKey ecKey = convertAPublicKeyToAnEllipticCurveKey(theBytesOfThePublicKey);
		NetworkParameters parameters = obtainTheBitcoinMainNetworkParameters();
		String theResultingAddress =
				getABitcoinAddressInTheTaprootFormat(parameters, ecKey).toString();
		return theResultingAddress;
	}

	public static String
		getABitcoinAddressInTheOldFormatFromThePublicKeyInHexadecimalForm(
			String publicKeyInHexadecimalForm) {
		// This "Legacy" address format is the original Bitcoin address format,
		// in other words, it's the "OG" of the Bitcoin address formats.
		byte[] theBytesOfThePublicKey = decodeAHexadecimalPublicKey(publicKeyInHexadecimalForm);
		ECKey ecKey = convertAPublicKeyToAnEllipticCurveKey(theBytesOfThePublicKey);
		NetworkParameters parameters = obtainTheBitcoinMainNetworkParameters();
		String theResultingAddress =
				getABitcoinAddressInTheLegacyP2PKHFormat(parameters, ecKey).toString();
		return theResultingAddress;
	}

	public static String getABitcoinAddressInTheTaprootFormat(
			NetworkParameters parameters, ECKey ecKey) {
		// This address format is a newer standard, that was introduced by:
		// BIP-340 and BIP-341.  The encoding format for this standard
		// is Bech32m.  This format only uses the x axis of the thirty-two
		// byte public key.
		Address theTapRootAddress =
				Address.fromKey(MainNetParams.get(), ecKey, ScriptType.P2TR);
		if (!theTapRootAddress.toString().startsWith("bc1p")) {
			throw new IllegalStateException("\n\nThe resulting address doesn't start " +
					"with bc1p, see here:\n\n" + theTapRootAddress + ".\n");
		}
		return theTapRootAddress.toString();
	}

	public static String getABitcoinAddressInTheSegwitFormat(
			NetworkParameters parameters, ECKey ecKey) {
		// This address format is a newer standard, that was designed under BIP-173.
		String theSegWithAddress = SegwitAddress.fromKey(parameters, ecKey).toString();
		if (!theSegWithAddress.startsWith("bc1")) {
			throw new IllegalStateException("\n\nThe resulting address doesn't start " +
					"with bc1, see here:\n\n" + theSegWithAddress + ".\n");
		}
		return theSegWithAddress;
	}

	public static String getABitcoinAddressInTheLegacyP2PKHFormat(
			NetworkParameters parameters, ECKey ecKey) {
		String theLegacyAddress =
				LegacyAddress.fromKey(parameters, ecKey).toString();
		if (!theLegacyAddress.startsWith("1")) {
			throw new IllegalStateException("The resulting address doesn't start " +
					"with one.");
		}
		return theLegacyAddress;
	}

	public static NetworkParameters obtainTheBitcoinMainNetworkParameters() {
		return MainNetParams.get();
	}

	public static ECKey convertAPublicKeyToAnEllipticCurveKey(byte[] publicKeyBytes) {
		return ECKey.fromPublicOnly(publicKeyBytes);
	}

	public static byte[] decodeAHexadecimalPublicKey(String s) {
		return convertAHexadecimalStringIntoAByteArray(s);
	}

	public static byte[] convertAHexadecimalStringIntoAByteArray(String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) +
					Character.digit(s.charAt(i + 1), 16));
		}
		return data;
	}
}
/*

Appendix One

Bitcoinj only supports two out of the four Bitcoin address formats at this time.
When it does the code would probably be similar to the following:

//		String theBitcoinAddressInTheTarootFormat =
//				getABitcoinAddressInTheTaprootFormatFromThePublicKeyInHexadecimalForm(
//						thePublicKeyInHexadecimalFormat);


//		System.out.println("The Bitcoin address in the Taproot format for the " +
//				"provided public key \nin " +
//				"hexadecimal form is "+ theBitcoinAddressInTheSegwitFormat + ".");


*/
