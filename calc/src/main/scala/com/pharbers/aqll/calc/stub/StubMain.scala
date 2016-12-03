package com.pharbers.aqll.calc.stub

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import akka.cluster.Cluster
import akka.actor.Props
import com.pharbers.aqll.calc.split.SplitReception
import com.pharbers.aqll.calc.common.DefaultData
import com.pharbers.aqll.calc.excel.Manage.AdminHospitalDataBase
import com.pharbers.aqll.calc.maxmessages.excelJobStart

object StubMain extends App {
    val config = ConfigFactory.load("split-master")
	val system = ActorSystem("calc", config) 
	
	if(system.settings.config.getStringList("akka.cluster.roles").contains("splitmaster")) {
    	Cluster(system).registerOnMemberUp {
		    val end_point = system.actorOf(Props[SplitReception], "splitreception")
		    import com.pharbers.aqll.calc.split.JobCategories._
	        end_point ! excelJobStart("""config/test/201601-07-CPA-HTN市场数据待上传.xlsx""", cpaMarketJob)
    	}
  	}  
}