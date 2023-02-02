package com.passportsingle;

import java.util.Hashtable;

import org.ksoap2.serialization.KvmSerializable;
import org.ksoap2.serialization.PropertyInfo;

public class StageTrans implements KvmSerializable {
    
	public String TransactionType;
	public String Amount;
	public String ClerkId;
	public String OrderNumber;
	public String Dba;
	public String SoftwareName;
	public String SoftwareVersion;
	public String TransactionId;
	public String ForceDuplicate;
	public String EntryMode;
	public String TaxAmount;
	
	public Object getProperty(int arg0) {
		switch (arg0) {
		case 0:
			return TransactionType;
		case 1:
			return Amount;
		case 3:
			return ClerkId;
		case 4:
			return OrderNumber;
		case 5:
			return Dba;
		case 6:
			return SoftwareName;
		case 7:
			return SoftwareVersion;
		case 8:
			return TransactionId;
		case 9:
			return ForceDuplicate;
		case 10:
			return EntryMode;
		case 11:
			return TaxAmount;
		default:
			break;
		}
		return null;
	}

	public int getPropertyCount() {
		return 3;
	}

	@Override
	public void getPropertyInfo(int index, Hashtable arg1, PropertyInfo info) {
		switch (index) {
		case 0:
			info.type = PropertyInfo.STRING_CLASS;
			info.name = "TransactionType";
			break;
		case 1:
			info.type = PropertyInfo.STRING_CLASS;
			info.name = "Amount";
			break;
		case 3:
			info.type = PropertyInfo.STRING_CLASS;
			info.name = "ClerkId";
			break;
		case 4:
			info.type = PropertyInfo.STRING_CLASS;
			info.name = "OrderNumber";
			break;
		case 5:
			info.type = PropertyInfo.STRING_CLASS;
			info.name = "Dba";
			break;
		case 6:
			info.type = PropertyInfo.STRING_CLASS;
			info.name = "SoftwareName";
			break;
		case 7:
			info.type = PropertyInfo.STRING_CLASS;
			info.name = "SoftwareVersion";
			break;
		case 8:
			info.type = PropertyInfo.STRING_CLASS;
			info.name = "TransactionId";
			break;
		case 9:
			info.type = PropertyInfo.STRING_CLASS;
			info.name = "ForceDuplicate";
			break;
		case 10:
			info.type = PropertyInfo.STRING_CLASS;
			info.name = "EntryMode";
			break;
		case 11:
			info.type = PropertyInfo.STRING_CLASS;
			info.name = "TaxAmount";
			break;
		default:
			break;
		}

	}

	public void setProperty(int index, Object value) {
		switch (index) {
		case 0:
			TransactionType = value.toString();
			break;
		case 1:
			Amount = value.toString();
			break;
		case 3:
			ClerkId = value.toString();
			break;
		case 4:
			OrderNumber = value.toString();
			break;
		case 5:
			Dba = value.toString();
			break;
		case 6:
			SoftwareName = value.toString();
			break;
		case 7:
			SoftwareVersion = value.toString();
			break;
		case 8:
			TransactionId = value.toString();
			break;
		case 9:
			ForceDuplicate = value.toString();
			break;
		case 10:
			EntryMode = value.toString();
			break;
		case 11:
			TaxAmount = value.toString();
			break;
		default:
			break;
		}
	}

}