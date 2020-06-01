package com.pengzhaopeng.utils

import redis.clients.jedis.{Jedis, JedisPool, JedisPoolConfig}

/**
  * description 
  * author 鹏鹏鹏先森
  * date 2020/5/27 0:29
  * Version 1.0
  */
object RedisUtil {
  var jedisPool: JedisPool = null

  def getJedisClient: Jedis = {
    if (jedisPool == null) {
      //println("开辟一个连接池")
      val config = new PropertiesUtil("config.properties")
      val host: String = config.readProperty("redis.host")
      val port = config.readProperty("redis.port")

      val jedisPoolConfig = new JedisPoolConfig()
      jedisPoolConfig.setMaxTotal(100) //最大连接数
      jedisPoolConfig.setMaxIdle(20) //最大空闲
      jedisPoolConfig.setMinIdle(20) //最小空闲
      jedisPoolConfig.setBlockWhenExhausted(true) //忙碌时是否等待
      jedisPoolConfig.setMaxWaitMillis(500) //忙碌时等待时长 毫秒
      jedisPoolConfig.setTestOnBorrow(true) //每次获得连接的进行测试

      jedisPool = new JedisPool(jedisPoolConfig, host, port.toInt)
    }
    //    println(s"jedisPool.getNumActive = ${jedisPool.getNumActive}")
    //   println("获得一个连接")
    jedisPool.getResource
  }

}
