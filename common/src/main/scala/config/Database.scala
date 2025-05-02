package dev.powerstats.common
package config

case class Database(url: String,
                    user: String,
                    password: String,
                    maxPoolSize: Int,
                    schema: String,
                    writeLimit: Int,
                    batchSize: Int)
