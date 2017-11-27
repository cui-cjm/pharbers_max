package com.pharbers.aqll.alMSA.alClusterLister

import akka.actor.{Actor, ActorLogging, Address, RootActorPath}
import akka.agent.Agent
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import com.pharbers.aqll.alMSA.alCalcAgent.alPropertyAgent.{refundNodeForRole, takeNodeForRole}
import com.pharbers.aqll.alStart.alEntry.alActorTest.system

import scala.concurrent.stm.Ref
import scala.concurrent.stm.atomic
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by alfredyang on 11/07/2017.
  */
class alMaxClusterLister extends Actor with ActorLogging {
    
    val cluster = Cluster(context.system)
    
    override def preStart() = {
        cluster.subscribe(self, initialStateMode = InitialStateAsEvents,
            classOf[MemberUp], classOf[MemberRemoved], classOf[UnreachableMember], classOf[ReachableMember])
    }
    
    override def postStop() = {
        cluster.unsubscribe(self)
    }
    
    override def receive = {
        case MemberJoined(member) => log.info("Member Joined")
        
        case MemberUp(member) => {

            val a = context.actorSelection("akka.tcp://calc@127.0.0.1:2551/user/agent-reception")
            member.roles.map (x => a ! refundNodeForRole(x))
//            member.roles.map {x =>
//                if (system.settings.config.getStringList("akka.cluster.roles").contains(x)){
//                    a ! refundNodeForRole(x)
//                }
//            }
        }
        
        case MemberRemoved(member, previousStatus) => {
            val a = context.actorSelection("akka.tcp://calc@127.0.0.1:2551/user/agent-reception")
            member.roles.map (x => a ! takeNodeForRole(x))
        }
    }
}
