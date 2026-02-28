package xyz.anythings.gw.service.mq.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use= JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "action"
)
@JsonSubTypes({
    @Type(value = GatewayInitRequest.class, name = Action.Values.GatewayInitRequest),
    @Type(value = GatewayInitRequestAck.class, name = Action.Values.GatewayInitRequestAck),
    @Type(value = GatewayInitResponse.class, name = Action.Values.GatewayInitResponse),
    @Type(value = GatewayInitResponseAck.class, name = Action.Values.GatewayInitResponseAck),
    @Type(value = GatewayInitReport.class, name = Action.Values.GatewayInitReport),
    @Type(value = GatewayInitReportAck.class, name = Action.Values.GatewayInitReportAck),
    @Type(value = IndicatorInitReport.class, name = Action.Values.IndicatorInitReport),
    @Type(value = IndicatorInitReportAck.class, name = Action.Values.IndicatorInitReportAck),

    @Type(value = IndicatorOnRequest.class, name = Action.Values.IndicatorOnRequest),
    @Type(value = IndicatorOnRequestAck.class, name = Action.Values.IndicatorOnRequestAck),
    @Type(value = IndicatorOnResponse.class, name = Action.Values.IndicatorOnResponse),
    @Type(value = IndicatorOnResponseAck.class, name = Action.Values.IndicatorOnResponseAck),
    
    @Type(value = IndicatorOffRequest.class, name = Action.Values.IndicatorOffRequest),
    @Type(value = IndicatorOffRequestAck.class, name = Action.Values.IndicatorOffRequestAck),
    @Type(value = IndicatorOffResponse.class, name = Action.Values.IndicatorOffResponse),
    @Type(value = IndicatorOffResponseAck.class, name = Action.Values.IndicatorOffResponseAck),
    
    @Type(value = LedOnRequest.class, name = Action.Values.LedOnRequest),
    @Type(value = LedOnRequestAck.class, name = Action.Values.LedOnRequestAck),
    @Type(value = LedOffRequest.class, name = Action.Values.LedOffRequest),
    @Type(value = LedOffRequestAck.class, name = Action.Values.LedOffRequestAck),
        
    @Type(value = GatewayDepRequest.class, name = Action.Values.GatewayDepRequest),
    @Type(value = GatewayDepRequestAck.class, name = Action.Values.GatewayDepRequestAck),
    @Type(value = GatewayDepResponse.class, name = Action.Values.GatewayDepResponse),
    @Type(value = GatewayDepResponseAck.class, name = Action.Values.GatewayDepResponseAck),

    @Type(value = IndicatorDepRequest.class, name = Action.Values.IndicatorDepRequest),
    @Type(value = IndicatorDepRequestAck.class, name = Action.Values.IndicatorDepRequestAck),
    @Type(value = IndicatorDepResponse.class, name = Action.Values.IndicatorDepResponse),
    @Type(value = IndicatorDepResponseAck.class, name = Action.Values.IndicatorDepResponseAck),

    @Type(value = IndicatorStatusReport.class, name = Action.Values.IndicatorStatusReport),
    @Type(value = IndicatorStatusReportAck.class, name = Action.Values.IndicatorStatusReportAck),

    @Type(value = TimesyncRequest.class, name = Action.Values.TimesyncRequest),
    @Type(value = TimesyncRequestAck.class, name = Action.Values.TimesyncRequestAck),
    @Type(value = TimesyncResponse.class, name = Action.Values.TimesyncResponse),
    @Type(value = TimesyncResponseAck.class, name = Action.Values.TimesyncResponseAck),

    @Type(value = ErrorReport.class, name = Action.Values.ErrorReport),
    @Type(value = ErrorReportAck.class, name = Action.Values.ErrorReportAck),

    @Type(value = MiddlewareConnInfoModRequest.class, name = Action.Values.MiddlewareConnInfoModRequest),
    @Type(value = MiddlewareConnInfoModRequestAck.class, name = Action.Values.MiddlewareConnInfoModRequestAck),
    @Type(value = MiddlewareConnInfoModResponse.class, name = Action.Values.MiddlewareConnInfoModResponse),
    @Type(value = MiddlewareConnInfoModResponseAck.class, name = Action.Values.MiddlewareConnInfoModResponseAck)
})
public interface IMessageBody {
	void setAction(String action);
	String getAction();
}
