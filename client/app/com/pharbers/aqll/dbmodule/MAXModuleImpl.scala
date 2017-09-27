package com.pharbers.aqll.dbmodule

import javax.inject.Singleton

import com.pharbers.mongodbDriver.MongoDB.MongoDBImpl

//import com.pharbers.mongodbDriver.MongoDB.MongoDBImpl
import com.pharbers.dbManagerTrait.dbInstanceManager
import com.pharbers.token.tokenImpl.TokenImplTrait

/**
  * Created by alfredyang on 01/06/2017.
  */
@Singleton
class MAXDBManager extends dbInstanceManager

@Singleton
class MAXTokenInjectModule extends TokenImplTrait