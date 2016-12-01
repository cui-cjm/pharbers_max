package com.pharbers.aqll.calc.split

import akka.actor.Actor
import akka.actor.Props
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Terminated
import com.pharbers.aqll.calc.maxmessages.{excelJobStart,excelJobEnd}
import com.pharbers.aqll.calc.maxmessages.startReadExcel
import com.pharbers.aqll.calc.excel.CPA.CpaMarket

/**
 * enum
 */
object JobCategories {
    object cpaMarketJob extends JobDefines(0, "CpaMarket")
    object cpaProductJob extends JobDefines(1, "CpaProduct")
    object phaMarketJob extends JobDefines(2, "PhaMarket")
    object ppaProductJob extends JobDefines(3, "PhaProduct")
}

sealed case class JobDefines(t : Int, des : String)

object SplitReception {
	def props = Props[SplitReception]
}

class SplitReception extends Actor with ActorLogging with CreateSplitMaster {
	import SplitReception._
	var masters = Seq[ActorRef]()     // supervisor
	
	def receive = {
		case excelJobStart(filename, cat) => {
		    val act = context.actorOf(SplitMaster.props)
		    masters = masters :+ act
		    context.watch(act)
			act ! startReadExcel(filename, cat)
		}
		case excelJobEnd(filename) => {
		    println(filename)
		}
		case Terminated(a) => {
		    println("-*-*-*-*-*-*-*-")
		    context.unwatch(a)
		    masters = masters.filterNot (_ == a)
		    // job完成，提醒用户
		}
		case _ => ???
	}
}

trait CreateSplitMaster { this : Actor => 
	def CreateSplitMaster = context.actorOf(SplitReception.props)
}