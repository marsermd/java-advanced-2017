md out\handmade

javac -d out\handmade -cp "C:\Users\marsermd\forGit\java-advanced-2017\lib\*;C:\Users\marsermd\forGit\java-advanced-2017\artifacts\*;" src\ru\ifmo\ctddev\panin\implementor\*.java

jar -cfm Implementor.jar manifests\implementorManifest.MF -C out\handmade\ ru\ifmo\ctddev\panin\implementor\