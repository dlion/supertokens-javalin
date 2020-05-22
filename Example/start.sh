./gradlew build

./gradlew copyJars

mkdir out

mv ./build/dependencies/* ./out/

mv ./build/libs/* ./out/

java -classpath "./out/*" example.Main