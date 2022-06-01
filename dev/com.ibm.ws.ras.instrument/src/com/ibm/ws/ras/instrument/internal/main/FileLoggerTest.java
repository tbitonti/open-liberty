package com.ibm.ws.ras.instrument.internal.main;

import java.io.File;

public class FileLoggerTest {
	
	public static final String LOG_NAME_1 = "test/LTITest1_.log";
	public static final String LOG_PREFIX_1 = "test1: ";
	
	public static final String LOG_NAME_2 = "test/LTITest2_.log";
	public static final String LOG_PREFIX_2 = "test2: ";
	
	public static void main(String[] args) {
		FileLogger logger_1 = FileLogger.create( new File(LOG_NAME_1), LOG_PREFIX_1, FileLogger.AUTOFLUSH);
		test(logger_1);
		
		FileLogger logger_2 = FileLogger.create( new File(LOG_NAME_2), LOG_PREFIX_2, !FileLogger.AUTOFLUSH);
		test(logger_2);
	}

	private static byte[] populate(int length) {
		byte[] bytes = new byte[length];
		
		for ( int byteNo = 0; byteNo < length; byteNo++ ) {
			bytes[byteNo] = (byte) byteNo;
		}		
		
		return bytes;
	}
	
	public static final byte[] TEST_BYTES_0 = populate(0);
	public static final byte[] TEST_BYTES_1 = populate(256);	
	public static final byte[] TEST_BYTES_2 = populate(35);
	
	public static final String BANNER = "----------------------------------------";
	
	public static void test(FileLogger logger) {
		logger.log("Basic Output");
		logger.log(BANNER);
		logger.log("Text");
		logger.log("ClassName", "Text");
		logger.log("ClassName", "MethodName", "Text");
		logger.log("ClassName", "MethodName", "Text", "Value");
		logger.log(BANNER);
		
		logger.log("Byte Output");
		logger.log(BANNER);
		logger.log("Text", TEST_BYTES_0);
		logger.log("ClassName", "Text", TEST_BYTES_1);
		logger.log("ClassName", "MethodName", "Text", TEST_BYTES_2);
		logger.log(BANNER);

		logger.log("Stack Output (Internal)");
		logger.log(BANNER);
		logger.logStack("Text");
		logger.logStack("ClassName", "Text");
		logger.logStack("ClassName", "MethodName", "Text");
		logger.log(BANNER);

		Throwable th = new Throwable("Dummy");

		logger.log("Stack Output (External)");
		logger.log(BANNER);
		logger.logStack("Text", th);
		logger.logStack("ClassName", "Text", th);
		logger.logStack("ClassName", "MethodName", "Text", th);
		logger.log(BANNER);		
	}
}
