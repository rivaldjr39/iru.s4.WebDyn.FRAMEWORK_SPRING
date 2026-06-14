#!/bin/bash

rm -rf build
mkdir -p build

javac --release 21 -cp "lib/*" -d build src/main/java/*.java

if [ $? -ne 0 ]; then
    echo "Erreur de compilation"
    exit 1
fi

jar cvf framework.jar -C build .

echo "framework.jar généré avec succès"