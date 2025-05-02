package dev.powerstats.api
package route.request

import dev.powerstats.common.db.model.ApiKey

case class ApiKeyCreateResponse(key: String, apiKey: ApiKey)
