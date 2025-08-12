package org.ps.properties

import com.typesafe.config.{Config, ConfigFactory}

object ApplicationProperties {

  private val conf: Config = ConfigFactory.load()

  val vonageApiKey: String = conf.getString("vonage-api-key")
  val vonageApiSecret: String = conf.getString("vonage-api-secret")

}
