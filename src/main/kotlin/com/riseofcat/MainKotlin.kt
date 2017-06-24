package com.riseofcat

import spark.Spark
import spark.Spark.*

object MainKotlin {
  @JvmStatic fun main(args: Array<String>) {
    System.getenv("PORT")?.let {
      Spark.port(it.toInt())
    }
    get("/") { req, res -> "Hello from Kotlin" }
  }
}
