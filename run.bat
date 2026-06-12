@echo off

REM Supprimer le dossier build s'il existe
if exist build rmdir /s /q build

REM Créer le dossier build
mkdir build

REM Compiler les fichiers Java
javac --release 21 -cp "lib/*" -d build src\main\java\*.java

REM Vérifier si la compilation a échoué
if %errorlevel% neq 0 (
    echo Erreur de compilation
    pause
    exit /b 1
)

REM Créer le fichier JAR
jar cvf framework.jar -C build .

echo framework.jar genere avec succes

pause