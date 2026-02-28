package xyz.elidom.mw.print.message;

public enum Action {
	ZplPrint(Values.ZplPrint),
	PdfPrint(Values.PdfPrint),
	ZplPrintAck(Values.ZplPrint),
	PdfPrintAck(Values.PdfPrint);
	
    private String action;

    private Action(String action) {
        this.action = action;
    }

    public String getAction() {
        return action;
    }

    public static class Values {
        public static final String ZplPrint = "ZPL_PRINT";
        public static final String PdfPrint = "PDF_PRINT";
        
        public static final String ZplPrintAck = "ZPL_PRINT_ACK";
        public static final String PdfPrintAck = "PDF_PRINT_ACK";
    }
}