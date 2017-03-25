md out\handmade

javac -d out\handmade -cp "lib\*;artifacts\*;" src\ru\ifmo\ctddev\panin\implementor\*.java

jar -cfm Implementor.jar manifests\implementorManifest.MF -C out\handmade\ ru\ifmo\ctddev\panin\implementor\