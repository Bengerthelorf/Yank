package homes.snaix.app.yank.domain.schema

import kotlinx.serialization.json.Json

// encodeDefaults = true so the polymorphic discriminator survives encoding
// when a subclass relies on a default-valued field for round-trip identity.
val RecognitionJson: Json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}
