package dev.powerstats.common
package db.model

import java.time.LocalDate

case class Meet(federation: String,
                date: LocalDate,
                meetCountry: String,
                meetState: Option[String],
                meetTown: Option[String],
                meetName: String)
