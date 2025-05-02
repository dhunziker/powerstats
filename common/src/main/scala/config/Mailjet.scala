package dev.powerstats.common
package config

case class Mailjet(baseUrl: String,
                   apiKey: String,
                   secretKey: String,
                   fromAddress: String,
                   fromName: String)
