package com.test.CommonLog.common.utils;

import java.util.UUID;

public class UUIDUTils {

	public static String getUUID() {
		return UUID.randomUUID().toString().replaceAll("-", "");
	}
}
