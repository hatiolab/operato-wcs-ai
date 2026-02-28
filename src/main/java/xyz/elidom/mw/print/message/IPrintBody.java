package xyz.elidom.mw.print.message;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;

import xyz.elidom.mw.rabbitmq.message.api.IMwMsgBody;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use= JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "action"
)
@JsonSubTypes({
    @Type(value = ZplPrintBody.class, name = Action.Values.ZplPrint),
    @Type(value = PdfPrintBody.class, name = Action.Values.PdfPrint)
})
public interface IPrintBody extends IMwMsgBody {
	void setAction(String action);
	String getAction();
}
