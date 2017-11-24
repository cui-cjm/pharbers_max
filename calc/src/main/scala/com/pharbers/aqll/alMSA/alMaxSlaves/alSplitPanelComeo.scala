package com.pharbers.aqll.alMSA.alMaxSlaves

import javax.activation.MimetypesFileTypeMap

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.pharbers.alCalcMemory.alprecess.alsplitstrategy.server_info
import com.pharbers.aqll.alCalaHelp.alMaxDefines.alMaxRunning
import com.pharbers.aqll.alCalcMemory.aljobs.alJob.{max_jobs, max_split_csv_jobs}
import com.pharbers.aqll.alCalcOther.alMessgae.alWebSocket
import com.pharbers.aqll.alMSA.alCalcMaster.alMasterTrait.alCameoSplitPanel.{split_panel_end, split_panel_start_impl, split_panel_timeout}
import com.pharbers.aqll.alCalcMemory.aljobs.aljobtrigger.alJobTrigger._
import com.pharbers.aqll.common.alFileHandler.fileConfig.{fileBase, outPut}

import scala.collection.immutable.Map
import scala.concurrent.duration._

/**
  * Created by alfredyang on 12/07/2017.
  */
object alSplitPanelComeo {
    def props(item: alMaxRunning,
              originSender: ActorRef,
              owner: ActorRef,
              counter: ActorRef) = Props(new alSplitPanelComeo(item, originSender, owner, counter))
}

class alSplitPanelComeo(item: alMaxRunning,
                        originSender : ActorRef,
                        owner : ActorRef,
                        counter : ActorRef) extends Actor with ActorLogging {

    override def postRestart(reason: Throwable) : Unit = {
        // TODO : 计算次数，重新计算
        counter ! canIReStart(reason)
    }

    override def receive: Receive = {
        case split_panel_timeout() => {
            log.debug("timeout occur")
            shutSlaveCameo(split_panel_timeout())
        }

        case split_panel_end(item, parent, subs) => {
            owner forward split_panel_end(item, parent, subs)
            shutSlaveCameo(split_panel_end(item, parent, subs))
        }

        case split_panel_start_impl(item) => {
            val file = fileBase + "fea9f203d4f593a96f0d6faa91ba24ba" + outPut + item.tid
            val result = if("application/octet-stream" == new MimetypesFileTypeMap().getContentType(file)){
                println("split scv file ==> " + file)
                max_split_csv_jobs(file).result
            } else {
                println("split excel file ==> " + file)
                max_jobs(file).result
            }

            try {
                val (parent, subs) = result.map (x => x).getOrElse(throw new Exception("cal error"))
                self ! split_panel_end(item, parent.asInstanceOf[String], subs.asInstanceOf[List[String]])
            } catch {
                case _ : Exception => self ! split_panel_end(item, "", Nil)
            }
        }

        case canDoRestart(reason: Throwable) =>
            super.postRestart(reason)
            self ! split_panel_start_impl(item)

        case cannotRestart(reason: Throwable) => {
            val msg = Map(
                "type" -> "error",
                "error" -> s"error with actor=${self}, reason=${reason}"
            )
            alWebSocket(item.uid).post(msg)
            self ! split_panel_end(item, "", Nil)
        }
    }

    import scala.concurrent.ExecutionContext.Implicits.global
    val timeoutMessager = context.system.scheduler.scheduleOnce(10 minute) {
        self ! split_panel_timeout()
    }

    def shutSlaveCameo(msg : AnyRef) = {
        originSender ! msg
        log.debug("stopping split excel cameo")
        timeoutMessager.cancel()
        context.stop(self)
    }
}