(cd ../ && ./gradlew build)

./gradlew build

./gradlew copyJars

rm -rf out

mkdir out

mv ./build/dependencies/* ./out/

mv ./build/libs/* ./out/

java -classpath "./out/*" example.Main