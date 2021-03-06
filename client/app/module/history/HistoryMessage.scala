package module.history

import com.pharbers.bmmessages.CommonMessage
import play.api.libs.json.JsValue


abstract class MsgHistoryCommand extends CommonMessage("history", HistoryModule)
object HistoryMessage {
	case class MsgQueryHistorySelect(data: JsValue) extends MsgHistoryCommand
	case class MsgQueryHistoryCount(data: JsValue) extends MsgHistoryCommand
	case class MsgQueryHistoryByPage(data: JsValue) extends MsgHistoryCommand
}
