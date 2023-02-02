package com.passportsingle;
public class PrinterCommands {
    public static final byte[] INIT = {27, 64, 27, 82, 0};
    public static final byte[] CHARSET = {27, 82, 0};

	public static final byte[] FEED_PAPER = {27, 100, 3};
	public static final byte[] STARINIT = {0x1b, 0x1d, 0x61, 0x01};
	public static final byte[] CUSTOM_FEED_PAPER_AND_CUT = {27, 105};
	
    public static byte[] FEED_LINE = {10};
 
    public static byte[] SELECT_FONT_A = {27, 30, 70, 0};
 
    public static byte[] SET_BAR_CODE_HEIGHT = {29, 104, 100};
    public static byte[] PRINT_BAR_CODE_1 = {29, 107, 2};
    public static byte[] SEND_NULL_BYTE = {0x00};
 
    public static byte[] KICK = {27, 112, 48, 55, 121};
    public static byte[] CUSTOM_KICK = {0x1b, 0x70, 0x01, 0x10, 0x12};

    public static byte[] SELECT_PRINT_SHEET = {0x1B, 0x63, 0x30, 0x02};
    public static byte[] FEED_PAPER_AND_CUT = {0x1D, 0x56, 66, 0x00};
 
    public static byte[] SELECT_CYRILLIC_CHARACTER_CODE_TABLE = {0x1B, 0x74, 0x11};
 
    public static byte[] SELECT_BIT_IMAGE_MODE = {0x1B, 0x2A, 33, -128, 0};
    public static byte[] SET_LINE_SPACING_24 = {0x1B, 0x33, 24};
    public static byte[] SET_LINE_SPACING_30 = {0x1B, 0x33, 30};
 
    public static byte[] TRANSMIT_DLE_PRINTER_STATUS = {0x10, 0x04, 0x01};
    public static byte[] TRANSMIT_DLE_OFFLINE_PRINTER_STATUS = {0x10, 0x04, 0x02};
    public static byte[] TRANSMIT_DLE_ERROR_STATUS = {0x10, 0x04, 0x03};
    public static byte[] TRANSMIT_DLE_ROLL_PAPER_SENSOR_STATUS = {0x10, 0x04, 0x04};

	public static byte[] STANDRD_MODE = {27, 83};
}