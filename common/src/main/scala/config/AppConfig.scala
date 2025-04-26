package ai.powerstats.common
package config

final case class AppConfig(ui: Ui,
                           api: Api,
                           mailjet: Mailjet,
                           database: Database)
