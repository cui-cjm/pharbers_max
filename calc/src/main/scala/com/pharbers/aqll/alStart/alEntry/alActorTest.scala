package com.pharbers.aqll.alStart.alEntry

import akka.actor.ActorSystem
import akka.cluster.Cluster
import com.pharbers.aqll.alCalaHelp.alMaxDefines.alCalcParmary
import com.pharbers.aqll.alMSA.alCalcMaster.alMaxDriver.pushGeneratePanelJobs
import com.pharbers.aqll.alStart.alHttpFunc.alUploadItem
import com.typesafe.config.ConfigFactory

object alActorTest extends App {

    val config = ConfigFactory.load("split-test")
    val system : ActorSystem = ActorSystem("calc", config)
    val cp = new alCalcParmary("fea9f203d4f593a96f0d6faa91ba24ba", "jeorch")
    // implicit val timeout = Timeout(30 minute)

    if(system.settings.config.getStringList("akka.cluster.roles").contains("splittest")) {
        Cluster(system).registerOnMemberUp {
            val a = system.actorSelection("akka.tcp://calc@127.0.0.1:2551/user/portion-actor")

            val gycx_file_local = "/home/clock/workSpace/blackMirror/dependence/program/generatePanel/file/Client/GYCX/1705 GYC.xlsx"
            val cpa_file_local = "/home/clock/workSpace/blackMirror/dependence/program/generatePanel/file/Client/CPA/1705 CPA.xlsx"

            for(i <- 1 to 50) {
                a ! pushGeneratePanelJobs(alUploadItem("generatePanel","user"+i,cpa_file_local,gycx_file_local,"201705"))
            }

        }
    }


}
