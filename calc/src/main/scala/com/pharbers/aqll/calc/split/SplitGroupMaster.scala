package com.pharbers.aqll.calc.split

import akka.actor.ActorLogging
import akka.actor.Actor
import akka.actor.Props
import akka.actor.ActorRef
import com.pharbers.aqll.calc.excel.model.integratedData
import scala.collection.mutable.ArrayBuffer
import com.pharbers.aqll.calc.excel.model.integratedData
import com.pharbers.aqll.calc.excel.model.modelRunData
import com.pharbers.aqll.calc.common.DefaultData
import com.pharbers.aqll.calc.datacala.algorithm.maxCalcData
import com.pharbers.aqll.calc.datacala.algorithm.maxSumData
import com.pharbers.aqll.calc.util.DateUtil
import com.pharbers.aqll.calc.datacala.common.BaseMaxDataArgs
import com.pharbers.aqll.calc.datacala.common.AdminHospDataBaseArgs
import com.pharbers.aqll.calc.datacala.common.IntegratedDataArgs
import com.pharbers.aqll.calc.datacala.module.MaxModule
import com.pharbers.aqll.calc.datacala.common.ModelRunDataArgs
import com.pharbers.aqll.calc.util.MD5

object SplitGroupMaster {
	def props(a : ActorRef) = Props(new SplitGroupMaster(a))
	
	case class groupintegrated(lst : List[integratedData])
	case class mappingend() extends broadcastingDefines
}

trait CalcPramMaster {
    def funcalc: modelRunData => Double
}

class SplitGroupMaster(aggregator : ActorRef) extends Actor with ActorLogging {
    
    
    
	val inte_lst : ArrayBuffer[integratedData] = ArrayBuffer.empty
  	val subFun = aggregator ! SplitAggregator.aggmapsubscrbe(self)
  	val maxSum : scala.collection.mutable.Map[String, (Double, Double, Double)] = scala.collection.mutable.Map.empty
//  	val r : ArrayBuffer[List[(String, Double, Double)] => Option[(Int, Int, (Double, Double))]] = ArrayBuffer.empty
  	val r : ArrayBuffer[List[(String, Double, Double)] => Option[(Int, Int, (Double, Double), (String, String, String), (String, String), String)]] = ArrayBuffer.empty
	
  	def receive = {
		case SplitAggregator.msg_container(group, lst) => {
	        inte_lst ++= lst
		}
		case SplitMaxBroadcasting.mappingiteratorhashed(mrd) => {
			sender() ! SplitMaxBroadcasting.mappingiteratornext()
			iteratorMrd(mrd)
		}
		case SplitMaxBroadcasting.mappingiterator(mrd) => iteratorMrd(mrd)
		case SplitMaxBroadcasting.mappingeof() => aggregator ! SplitWorker.requestaverage(maxSum.toList) 
		case SplitGroupMaster.mappingend() => aggregator ! SplitWorker.requestaverage(maxSum.toList)
		case SplitEventBus.average(avg) => {
		    // TODO 这里不应该是按照年月来分组计算最终结果，不然在结果查询的时候某些条件将无法满足查询还是要改，目前是按照年+月+最小产品单位（或医院phacode来分组）
			val result = r.map (f => f(avg)).filterNot(_ == None).map(_.get).groupBy(x => (x._1, x._2, x._5._2)).map (x =>
								(MD5.md5(x._1._1.toString + x._1._2.toString + x._1._3), 
								(DateUtil.getDateLong(x._1._1, x._1._2), x._2.map (_._3._1).sum, x._2.map (_._3._2).sum, x._2.map(_._4).distinct, x._2.map(_._5).distinct, x._2.map(_._6).head )))
            aggregator ! SplitWorker.postresult(result)
	    }
		case _ => Unit
	}
	
	def iteratorMrd(mrd : modelRunData) = {
		val tmp = inte_lst.find (iter => mrd.uploadYear == iter.uploadYear 
									 && mrd.uploadMonth == iter.uploadMonth 
									 && mrd.minimumUnitCh == iter.minimumUnitCh 
									 && mrd.hospId == iter.hospNum) 
									 
		tmp match {
			case Some(x) => {
				mrd.sumValue = x.sumValue
				mrd.volumeUnit = x.volumeUnit
			}
			case None => Unit
		}
		
		r += (SplitResultFunc(mrd)(_))
		
		if (mrd.ifPanelTouse == "1") {
			maxSum += mrd.segment -> 
				maxSum.find(p => p._1 == mrd.segment)
					.map (x => (x._2._1 + mrd.sumValue, x._2._2 + mrd.volumeUnit, x._2._3 + mrd.selectvariablecalculation.get._2))
					.getOrElse ((mrd.sumValue, mrd.volumeUnit,  mrd.selectvariablecalculation.get._2))
		}
	}
}