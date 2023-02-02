package com.pos.passport.model;

public class EmailSetting {
	
	private static boolean enabled;
	public static boolean bookkeeper;

	private static String smtpServer = "";
	private static int smtpPort = -1;
	private static String smtpUsername = "";
	private static String smtpPasword = "";
	private static String smtpEmail = "";
	private static String smtpSubject = "";

	public static String blurb = "";
	public static boolean isEnabled() {
		return enabled;
	}
	public static void setEnabled(boolean enabled) {
		EmailSetting.enabled = enabled;
	}
	public static String getSmtpServer() {
		return smtpServer;
	}
	public static void setSmtpServer(String smtpServer) {
		EmailSetting.smtpServer = smtpServer;
	}
	public static String getSmtpUsername() {
		return smtpUsername;
	}
	public static void setSmtpUsername(String smtpUsername) {
		EmailSetting.smtpUsername = smtpUsername;
	}
	public static String getSmtpSubject() {
		return smtpSubject;
	}
	public static void setSmtpSubject(String smtpSubject) {
		EmailSetting.smtpSubject = smtpSubject;
	}
	public static String getSmtpEmail() {
		return smtpEmail;
	}
	public static void setSmtpEmail(String smtpEmail) {
		EmailSetting.smtpEmail = smtpEmail;
	}
	public static String getSmtpPasword() {
		return smtpPasword;
	}
	public static void setSmtpPasword(String smtpPasword) {
		EmailSetting.smtpPasword = smtpPasword;
	}
	public static int getSmtpPort() {
		return smtpPort;
	}
	public static void setSmtpPort(int smtpPort) {
		EmailSetting.smtpPort = smtpPort;
	}

	public static void clear() {
		enabled = false;
		bookkeeper = false;
		smtpServer = "";
		smtpPort = -1;
		smtpUsername = "";
		smtpPasword = "";
		smtpEmail = "";
		smtpSubject = "";
		blurb = "";

	}

}
