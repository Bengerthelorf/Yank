-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class **$$serializer { *; }
-keepclassmembers class homes.snaix.app.yank.domain.schema.** {
    *** Companion;
}
-keepclasseswithmembers class homes.snaix.app.yank.domain.schema.** {
    kotlinx.serialization.KSerializer serializer(...);
}
