package com.njuguna.dailyselfie.common.util;

import com.google.common.io.BaseEncoding;

import java.nio.ByteBuffer;
import java.util.UUID;

public final class IDUtils {

	public static synchronized String generateGUID() {
		return UUID.randomUUID().toString();
	}
	public static synchronized String generateCompactGUID() {
		return UUID.randomUUID().toString().replaceAll("-", "");
	}

	public static synchronized String generateBase64GUID() {
		UUID uuid = UUID.randomUUID();
		ByteBuffer buffer = ByteBuffer.wrap(new byte[16]);
		buffer.putLong(uuid.getMostSignificantBits());
		buffer.putLong(uuid.getLeastSignificantBits());
		String code = BaseEncoding.base64Url().omitPadding().encode(buffer.array());

		// check is the first character is an underscore that does not seem to be liked by CBL or hyphen and recurse to get another one
		if ((code.substring(0,1).equals("_")) || (code.substring(0,1).equals("-"))) {
			code = generateBase64GUID();
		}
		return code;
	}

	public static synchronized String generateBase64GUIDwNum(long num) {
		if (num < 1) { throw new IllegalArgumentException("num cannot be less than 1"); }
        return Base62.encode(num) + generateBase64GUID();
	}

}
