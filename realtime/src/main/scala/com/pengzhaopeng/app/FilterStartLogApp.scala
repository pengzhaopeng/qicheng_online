package com.pengzhaopeng.app

import java.lang
import java.sql.{Connection, ResultSet}

import com.pengzhaopeng.constans.GmallConstant
import com.pengzhaopeng.utils.{DataSourceUtil, SqlProxy, _}
import com.pengzhaopeng.utils.jdbc_mysql.{DataSourceUtil, QueryCallback, SqlProxy}
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.kafka010._
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.streaming.{Seconds, StreamingContext}

import scala.collection.mutable

/**
  * description
  * 1.清洗启动日志 用redis去重 并保存到 hbase 中
  * author 鹏鹏鹏先森
  * date 2020/6/9 0:21
  * Version 1.0
  */
object FilterStartLogApp {

  private val batchDuration = 5

  def main(args: Array[String]): Unit = {

    //读取配置文件
    val properties: PropertiesUtil = new PropertiesUtil("config.properties")

    //设置hadoop用户
    System.setProperty("HADOOP_USER_NAME", "dog")

    //设置spark参数
    //设置spark参数
    val conf = new SparkConf().setAppName(this.getClass.getSimpleName)
      .set("spark.streaming.kafka.maxRatePerPartition", "100")
      .set("spark.streaming.backpressure.enabled", "true")
      .set("spark.streaming.stopGracefullyOnShutdown", "true")
      .setMaster("local[*]")
    val ssc = new StreamingContext(conf, Seconds(batchDuration))
    val sparkContext: SparkContext = ssc.sparkContext

    //设置kafka参数
    val topics = Array(GmallConstant.KAFKA_STARTUP)
    val groupId = GmallConstant.KAFKA_GROUP_STARTUP
    val kafkaMap: Map[String, Object] = Map[String, Object](
      "bootstrap.servers" -> topics,
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> groupId,
      "auto.offset.reset" -> "earliest",
      "enable.auto.commit" -> (false: lang.Boolean)
    )

    //查询mysql中是否有偏移量
    val sqlProxy = new SqlProxy()
    val client: Connection = DataSourceUtil.getConnection
    val offsetMap = new mutable.HashMap[TopicPartition, Long]()
    val queryOffsetSql: String =
      s"""
         |select
         |  *
         |from `offset_manager`
         |where `groupid`=?
       """.stripMargin
    try {
      sqlProxy.executeQuery(client, queryOffsetSql, Array(groupId), new QueryCallback {
        override def process(rs: ResultSet): Unit = {
          while (rs.next()) {
            val model = new TopicPartition(rs.getString(2), rs.getInt(3))
            val offset: Long = rs.getLong(4)
            offsetMap.put(model, offset)
          }
          rs.close()
        }
      })
    } catch {
      case e: Exception => e.printStackTrace()
    } finally {
      sqlProxy.shutdown(client)
    }

    //设置kafka消费的数据的参数 判断班底是否有偏移量 有则根据偏移量继续消费 无则重新消费
    val stream: InputDStream[ConsumerRecord[String, String]] = if (offsetMap.isEmpty) {
      KafkaUtils.createDirectStream(
        ssc,
        LocationStrategies.PreferConsistent,
        ConsumerStrategies.Subscribe[String, String](topics, kafkaMap))
    } else {
      KafkaUtils.createDirectStream(
        ssc,
        LocationStrategies.PreferConsistent,
        ConsumerStrategies.Subscribe[String, String](topics, kafkaMap, offsetMap))
    }

    //业务处理

    //处理完业务逻辑后手动提交 offset 维护到本地 mysql 中
    stream.foreachRDD(rdd => {
      if (!rdd.isEmpty()) {
        val sqlProxy = new SqlProxy()
        val client: Connection = DataSourceUtil.getConnection
        try {
          val offsetRanges: Array[OffsetRange] = rdd.asInstanceOf[HasOffsetRanges].offsetRanges
          for (elem <- offsetRanges) {
            sqlProxy.executeUpdate(
              client,
              s"""
                 |replace into `offset_manager` (`groupid`,`topic`,`partition`,`untilOffset`)
                 | values(?,?,?,?)
             """.stripMargin,
              Array(groupId, elem.topic, elem.partition, elem.untilOffset))
            //            println("偏移量："+ groupid, elem.topic, elem.partition, elem.untilOffset)
          }
        } catch {
          case e: Exception => e.printStackTrace()
        } finally {
          sqlProxy.shutdown(client)
        }

      }
    })

    //优雅停止
    ssc.start()
    ssc.awaitTermination()
  }
}
