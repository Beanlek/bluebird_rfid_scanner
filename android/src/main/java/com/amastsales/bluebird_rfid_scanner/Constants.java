package com.amastsales.bluebird_rfid_scanner;

public class Constants {

    public static final String VERSION = "20231205";

    public static final boolean MAIN_D = true;

    public static final boolean HELPER_D = true;

    public static final boolean TAG_LIST_ADAPTER_D = true;

    //<-[20250402]Add Bulk encoding
    public static final String BB_PW = "11112222";
    public static final String PRIVATE_SUFFIX_PW = "0002";
    public static final String REVIVE_SUFFIX_PW = "0000";

    public static class EncodeMode {
        public static final int MASS = 0;
        public static final int PRIVATE = 1;
        public static final int REVIVE = 2;
    }

    public static class EPCItem {
        public String mEPC;
        public String mAccessPW;
        public int mSeqID;

        public EPCItem(String epc, String acPw) {
            mEPC = epc;
            mAccessPW = acPw;
        }
    }
    //[20250402]Add Bulk encoding->

    //<-[20250424]Add other inventory api for test
    public static class InventoryType {
        public static final int NORAML = 0;
        public static final int RSSI_TO_LOCATE = 1;
        public static final int FIND_LOCATE = 2;
        public static final int CUSTOM = 3;
        public static final int RSSI_LIMIT = 4;
        public static final int WITH_PAHSE_FREQ = 5;
    }
    //[20250424]Add other inventory api for test->
}
