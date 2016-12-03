package com.pharbers.aqll.calc.split

import com.pharbers.aqll.calc.common.DefaultData.capLoadXmlPath
import com.pharbers.aqll.calc.common.DefaultData.phaLoadXmlPath
import com.pharbers.aqll.calc.excel.core.row_cpamarketinteractparser
import com.pharbers.aqll.calc.excel.core.row_cpaproductinteractparser
import com.pharbers.aqll.calc.excel.core.row_phamarketinteractparser
import com.pharbers.aqll.calc.excel.core.row_phaproductinteractparser
import com.pharbers.aqll.calc.maxmessages.end
import com.pharbers.aqll.calc.maxmessages.startReadExcel

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Props
import akka.cluster.routing.ClusterRouterPool
import akka.cluster.routing.ClusterRouterPoolSettings
import akka.routing.RoundRobinPool
import com.pharbers.aqll.calc.maxmessages.cancel
import com.pharbers.aqll.calc.maxresult.Insert
import com.pharbers.aqll.calc.maxresult.InserAdapter
import java.util.Date

object SplitMaster {
	def props = Props[SplitMaster]
	
	val num_count = 10
}

class SplitMaster extends Actor with ActorLogging 
	with CreateSplitWorker 
	with CreateSplitEventBus
	with CreateSplitAggregator {

	import SplitMaster._
	import JobCategories._
	
	val bus = CreateSplitEventBus
	val agg = CreateSplitAggregator(bus)
	val router = CreateSplitWorker(agg, bus)
	var fileName = ""
	
	val ready : Receive = {
		case startReadExcel(filename, cat) => {
		    fileName = filename.substring(filename.lastIndexOf("""/""")+1, filename.length())
	        context.become(spliting)
		    (cat.t match {
		        case 0 => {
		            row_cpamarketinteractparser(capLoadXmlPath.cpamarketxmlpath_en, 
		                                           capLoadXmlPath.cpamarketxmlpath_ch, 
		                                           router)
		        }
		        case 1 => {
		            row_cpaproductinteractparser(capLoadXmlPath.cpaproductxmlpath_en, 
		                                            capLoadXmlPath.cpaproductxmlpath_ch, 
		                                            router)
		        }
		        case 2 => {
		            row_phamarketinteractparser(phaLoadXmlPath.phamarketxmlpath_en, 
		                                           phaLoadXmlPath.phamarketxmlpath_ch, 
		                                           router)
		        }
		        case 3 => {
		            row_phaproductinteractparser(phaLoadXmlPath.phaproductxmlpath_en, 
		                                            phaLoadXmlPath.phaproductxmlpath_ch, 
		                                            router)
		        }
		    }).startParse(filename, 1)
		    bus.publish(SplitEventBus.excelEnded())
		}
		case _ => {
		    println("exception")
		}
	}

	val spliting : Receive = {
	    case startReadExcel(filename, cat) => println("one master only start one cal process at one time")
	    
	    case SplitAggregator.aggregatefinalresult(mr) => {
	        val time = new Date().getTime
	    	Insert.maxResultInsert(mr)(InserAdapter(fileName,"BMS",time))
	    	/**
	    	 * 入数据库
	    	 */
	    	context.stop(self)
	    }
		case cancel() => {
		    println(s"cancel() $self")
		}
		case end() => {
		    println(s"end() $self")
		}
	    
	    case _ => {
	        println("sssssssssss")
	    }
	}
	
	def receive = ready
}

trait CreateSplitWorker { this : Actor =>
	def CreateSplitWorker(a : ActorRef, b : SplitEventBus) = {
	    context.actorOf(
            ClusterRouterPool(RoundRobinPool(10), ClusterRouterPoolSettings(    
                totalInstances = 50, maxInstancesPerNode = SplitMaster.num_count,
                allowLocalRoutees = true, useRole = None)).props(SplitWorker.props(a, b)),
              name = "worker-router")
	}
}

trait CreateSplitEventBus { this : Actor => 
	def CreateSplitEventBus = new SplitEventBus(SplitMaster.num_count)
}

trait CreateSplitAggregator { this : Actor => 
    def CreateSplitAggregator(b : SplitEventBus) = {
    	val a = context.actorOf(SplitAggregator.props(SplitMaster.num_count, b, self))
    	context.watch(a)
    	a
    }
}