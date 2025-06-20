package dev.powerstats.common
package db.model

import java.time.LocalDate

case class PersonalBest(name: String,
                        sex: String,
                        equipment: String,
                        best3SquatKg: Option[Float],
                        best3BenchKg: Option[Float],
                        best3DeadliftKg: Option[Float],
                        totalKg: Option[Float],
                        dots: Option[Float],
                        wilks: Option[Float],
                        glossbrenner: Option[Float],
                        goodlift: Option[Float])
