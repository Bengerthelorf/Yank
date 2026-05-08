-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keep @kotlinx.serialization.Serializable class * { *; }
-keepclassmembers class **$$serializer { *; }
-keepclassmembers class homes.snaix.app.yank.domain.schema.** {
    *** Companion;
}
-keepclasseswithmembers class homes.snaix.app.yank.domain.schema.** {
    kotlinx.serialization.KSerializer serializer(...);
}
