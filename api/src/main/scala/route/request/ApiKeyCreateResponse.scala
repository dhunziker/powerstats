package ai.powerstats.api
package route.request

import ai.powerstats.common.db.model.ApiKey

case class ApiKeyCreateResponse(key: String, apiKey: ApiKey)
