package ai.powerstats.common
package config

final case class AppConfig(api: Api,
                           mailjet: Mailjet,
                           database: Database)
