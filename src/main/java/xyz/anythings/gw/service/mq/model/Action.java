package xyz.anythings.gw.service.mq.model;

public enum Action {
	GatewayInitRequest(Values.GatewayInitRequest),
	GatewayInitRequestAck(Values.GatewayInitRequestAck),
	GatewayInitResponse(Values.GatewayInitResponse),
	GatewayInitResponseAck(Values.GatewayInitResponseAck),
	GatewayInitReport(Values.GatewayInitReport),
	GatewayInitReportAck(Values.GatewayInitReportAck),
	IndicatorInitReport(Values.IndicatorInitReport),
	IndicatorInitReportAck(Values.IndicatorInitReportAck),
	
	IndicatorOnRequest(Values.IndicatorOnRequest),
	IndicatorOnRequestAck(Values.IndicatorOnRequestAck),
	IndicatorOnResponse(Values.IndicatorOnResponse),
	IndicatorOnResponseAck(Values.IndicatorOnResponseAck),
	
	IndicatorOffRequest(Values.IndicatorOffRequest),
	IndicatorOffRequestAck(Values.IndicatorOffRequestAck),
	IndicatorOffRResponse(Values.IndicatorOffResponse),
	IndicatorOffRResponseAck(Values.IndicatorOffResponseAck),

	LedOnRequest(Values.LedOnRequest),
	LedOnRequestAck(Values.LedOnRequestAck),
	LedOffRequest(Values.LedOffRequest),
	LedOffRequestAck(Values.LedOffRequestAck),
	
	GatewayDepRequest(Values.GatewayDepRequest),
	GatewayDepRequestAck(Values.GatewayDepRequestAck),
	GatewayDepResponse(Values.GatewayDepResponse),
	GatewayDepResponseAck(Values.GatewayDepResponseAck),

	IndicatorDepRequest(Values.IndicatorDepRequest),
	IndicatorDepRequestAck(Values.IndicatorDepRequestAck),
	IndicatorDepResponse(Values.IndicatorDepResponse),
	IndicatorDepResponseAck(Values.IndicatorDepResponseAck),

	IndicatorStatusReport(Values.IndicatorStatusReport),
	IndicatorStatusReportAck(Values.IndicatorStatusReportAck),

	TimesyncRequest(Values.TimesyncRequest),
	TimesyncRequestAck(Values.TimesyncRequestAck),
	TimesyncResponse(Values.TimesyncResponse),
	TimesyncResponseAck(Values.TimesyncResponseAck),

	ErrorReport(Values.ErrorReport),
	ErrorReportAck(Values.ErrorReportAck),

	MiddlewareConnInfoModRequest(Values.MiddlewareConnInfoModRequest),
	MiddlewareConnInfoModRequestAck(Values.MiddlewareConnInfoModRequestAck),
	MiddlewareConnInfoModResponse(Values.MiddlewareConnInfoModResponse),
	MiddlewareConnInfoModResponseAck(Values.MiddlewareConnInfoModResponseAck),
	
	EquipStatusReport(Values.EquipStatusReport),
	EquipRefreshRequest(Values.EquipRefreshRequest),
	
	ScanBoxReport(Values.ScanBoxReport),
	ScanBoxReportAck(Values.ScanBoxReportAck),
	BoxDepartReport(Values.ScanBoxReport),
	BoxDepartReportAck(Values.ScanBoxReportAck),
	BoxArriveReport(Values.BoxArriveReport),
	BoxArriveReportAck(Values.BoxArriveReportAck),
	WeightResultReport(Values.WeightResultReport),
	WeightResultReportAck(Values.WeightResultReportAck),
	BoxWaitReport(Values.BoxWaitReport),
	BoxWaitReportAck(Values.BoxWaitReportAck),
	BoxSortReport(Values.BoxSortReport),
	BoxSortReportAck(Values.BoxSortReportAck)
	;
	
    private String action;

    private Action(String action) {
        this.action = action;
    }

    public String getAction() {
        return action;
    }

    public static class Values {
        public static final String GatewayInitRequest = "GW_INIT_REQ";
        public static final String GatewayInitRequestAck = "GW_INIT_REQ_ACK";
        public static final String GatewayInitResponse = "GW_INIT_RES";
        public static final String GatewayInitResponseAck = "GW_INIT_RES_ACK";
        public static final String GatewayInitReport = "GW_INIT_RPT";
        public static final String GatewayInitReportAck = "GW_INIT_RPT_ACK";
        public static final String IndicatorInitReport = "IND_INIT_RPT";
        public static final String IndicatorInitReportAck = "IND_INIT_RPT_ACK";

        public static final String IndicatorOnRequest = "IND_ON_REQ";
        public static final String IndicatorOnRequestAck = "IND_ON_REQ_ACK";
        public static final String IndicatorOnResponse = "IND_ON_RES";
        public static final String IndicatorOnResponseAck = "IND_ON_RES_ACK";
        
        public static final String IndicatorOffRequest = "IND_OFF_REQ";
        public static final String IndicatorOffRequestAck = "IND_OFF_REQ_ACK";
        public static final String IndicatorOffResponse = "IND_OFF_RES";
        public static final String IndicatorOffResponseAck = "IND_OFF_RES_ACK";

        public static final String LedOnRequest = "LED_ON_REQ";
        public static final String LedOnRequestAck = "LED_ON_REQ_ACK";
        public static final String LedOffRequest = "LED_OFF_REQ";
        public static final String LedOffRequestAck = "LED_OFF_REQ_ACK";
        
        public static final String GatewayDepRequest = "GW_DEP_REQ";
        public static final String GatewayDepRequestAck = "GW_DEP_REQ_ACK";
        public static final String GatewayDepResponse = "GW_DEP_RES";
        public static final String GatewayDepResponseAck = "GW_DEP_RES_ACK";

        public static final String IndicatorDepRequest = "IND_DEP_REQ";
        public static final String IndicatorDepRequestAck = "IND_DEP_REQ_ACK";
        public static final String IndicatorDepResponse = "IND_DEP_RES";
        public static final String IndicatorDepResponseAck = "IND_DEP_RES_ACK";
        
        public static final String IndicatorAlternation = "IND_ALT";
        public static final String IndicatorAlternationAck = "IND_ALT_ACK";

        public static final String IndicatorStatusReport = "IND_STATUS_RPT";
        public static final String IndicatorStatusReportAck = "IND_STATUS_RPT_ACK";
        
        public static final String TimesyncRequest = "TIMESYNC_REQ";
        public static final String TimesyncRequestAck = "TIMESYNC_REQ_ACK";
        public static final String TimesyncResponse = "TIMESYNC_RES";
        public static final String TimesyncResponseAck = "TIMESYNC_RES_ACK";

        public static final String ErrorReport = "ERR_RPT";
        public static final String ErrorReportAck = "ERR_RPT_ACK";

        public static final String MiddlewareConnInfoModRequest = "MW_MOD_IP_REQ";
        public static final String MiddlewareConnInfoModRequestAck = "MW_MOD_IP_REQ_ACK";
        public static final String MiddlewareConnInfoModResponse = "MW_MOD_IP_RES";
        public static final String MiddlewareConnInfoModResponseAck = "MW_MOD_IP_RES_ACK";
        
        public static final String DeviceCommand = "DEVICE_CMD";
        public static final String EquipStatusReport = "EQUIP_STATUS";
        public static final String EquipRefreshRequest = "EQUIP_REFRESH";
        
        // ECS
        public static final String ScanBoxReport = "SCAN_BOX_RPT";
        public static final String ScanBoxReportAck = "SCAN_BOX_RPT_ACK";
        public static final String BoxDepartReport = "BOX_DEPART_RPT";
        public static final String BoxDepartReportAck = "BOX_DEPART_RPT_ACK";
        public static final String BoxArriveReport = "BOX_ARRIVE_RPT";
        public static final String BoxArriveReportAck = "BOX_ARRIVE_RPT_ACK";
        public static final String WeightResultReport = "WEIGHT_RESULT_RPT";
        public static final String WeightResultReportAck = "WEIGHT_RESULT_RPT_ACK";
        public static final String BoxWaitReport = "BOX_WAIT_RPT";
        public static final String BoxWaitReportAck = "BOX_WAIT_RPT_ACK";
        public static final String BoxSortReport = "BOX_SORT_RPT";
        public static final String BoxSortReportAck = "BOX_SORT_RPT_ACK";
    }
}
