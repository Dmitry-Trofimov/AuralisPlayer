# AndroidX Media3 and Compose publish their own consumer R8 rules.
# Keep source and line information for readable crash reports while allowing shrinking/obfuscation.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
