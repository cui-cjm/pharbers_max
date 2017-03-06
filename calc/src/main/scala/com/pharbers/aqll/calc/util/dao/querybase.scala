/**
  * For Database Query
  * Created By Alfred Yang
  */

package com.pharbers.aqll.calc.util.dao

import com.mongodb.casbah.query.dsl.QueryExpressionObject
import com.mongodb.casbah.Imports._
import com.mongodb.MongoCredential._
import com.pharbers.aqll.calc.util.Const
import com.pharbers.aqll.calc.util.dao.Msd._

object _data_connection {
	def conn_name : String = Const.DB

	val server = new ServerAddress(DBHost,DBPort)
	val credentials = MongoCredential.createScramSha1Credential(username, conn_name ,password.toCharArray)
	val _conn = MongoClient(server, List(credentials))

	var _conntion : Map[String, MongoCollection] = Map.empty
	def getCollection(coll_name : String) : MongoCollection = {
		if (!_conntion.contains(coll_name)) _conntion += (coll_name -> _conn(conn_name)(coll_name))

		_conntion.get(coll_name).get
	}

	def resetCollection(coll_name : String) : Unit = getCollection(coll_name).drop

	def isExisted(coll_name : String) : Boolean = !(getCollection(coll_name).isEmpty)

	def releaseConntions = _conntion = Map.empty
}

trait IDatabaseContext {
	var coll_name : String = null

	protected def openConnection : MongoCollection =
		_data_connection._conn(_data_connection.conn_name)(coll_name)
	protected def closeConnection = null
}

class ALINQ[T] {
	var w : T => Boolean = x => true
	var ls : List[T] = Nil

	def in(l: List[T]) : ALINQ[T] = {
		ls = l
		this
	}

	def where(f: T => Boolean) : ALINQ[T] = {
		w = f
		this
	}

	def select[U](cr: (T) => U) : IQueryable[U] = {
		var nc = new Linq_List[U]
		for (i <- ls) {
			if (w(i)) nc = (nc :+ cr(i)).asInstanceOf[Linq_List[U]]
		}
		nc
	}

	def contains : Boolean = {
		for (i <- ls) {
			if (w(i)) true
		}
		false
	}

	def count : Int = ls.count(w)
}

object from {
	def apply[T]() : ALINQ[T] = new ALINQ[T]
	def db() : AMongoDBLINQ = new AMongoDBLINQ
}

class AMongoDBLINQ extends IDatabaseContext {
	var w : DBObject = null

	def in(l: String) : AMongoDBLINQ = {
		coll_name = l
		this
	}

	def where(args: Any* ) : AMongoDBLINQ = {
		w = new MongoDBObject
		for (arg <- args) {
			arg match {
				case a: (String, AnyRef) => w += a
				case a: DBObject => w = w ++ a
				case _ => w
			}
		}
		this
	}

	def select[U](cr: (MongoDBObject) => U) : IQueryable[U] = {

		val mongoColl = openConnection
		val ct = mongoColl.find(w)
		var nc = new Linq_List[U]
		for (i <- ct) {
			nc = (nc :+ cr(i)).asInstanceOf[Linq_List[U]]
		}
		nc
	}

	def contains : Boolean = {
		!(select (x => x).empty)
	}

	def selectTop[U](n : Int)(o : String)(cr : (MongoDBObject) => U) : IQueryable[U] = {
		val mongoColl = openConnection
		val ct = mongoColl.find(w).sort(MongoDBObject(o -> -1)).limit(n)
		var nc = new Linq_List[U]
		for (i <- ct) {
			nc = (nc :+ cr(i)).asInstanceOf[Linq_List[U]]
		}
		nc
	}

	def selectOneByOne[U](o: String)(cr: (MongoDBObject) => U): MongoCursor = {
		val mongoColl = openConnection
		mongoColl.find(w).sort(MongoDBObject(o -> -1))
	}

	def selectSkipTop[U](skip : Int)(take : Int)(o : String)(cr : (MongoDBObject) => U) : IQueryable[U] = {
		val mongoColl = openConnection
		val ct = mongoColl.find(w).sort(MongoDBObject(o -> -1)).skip(skip).limit(take)
		var nc = new Linq_List[U]
		for (i <- ct) {
			nc = (nc :+ cr(i)).asInstanceOf[Linq_List[U]]
		}
		nc
	}

	def count : Int = openConnection.count(w)
}
