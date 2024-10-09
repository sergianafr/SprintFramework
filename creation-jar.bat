@echo off
setlocal enabledelayedexpansion

set "work_dir=C:\Users\SERGIANA\Documents\Study\L2\S4\Mr-Naina\SprintFramework"
set "temp=%work_dir%\temp"
set "src=%work_dir%\src"
set "lib=%work_dir%\lib"
set "jar=framework.jar"

if exist "%temp%" ( 
    rd /s /q "%temp%"
)
mkdir "%temp%"
rem compilation fichiers .java

dir /s /B "%src%\*.java" > sources.txt
:: Créer une liste de tous les fichiers .jar dans le répertoire lib et ses sous-répertoires
dir /s /B "%lib%\*.jar" > libs.txt
:: Construire le classpath
set "classpath="

:: Boucle pour ajouter chaque ligne de libs.txt à la variable classpath
for /F "delims=" %%i in (libs.txt) do (
    if defined classpath (
        set "classpath=!classpath!;%%i"
    ) else (
        set "classpath=%%i"
    )
)

:: Afficher le classpath pour vérification
echo !classpath!

:: Exécuter la commande javac avec le classpath
javac -d "%temp%" -cp "!classpath!" @sources.txt

del sources.txt
del libs.txt

rem creation fichier jar
cd temp
if exist "%work_dir%/%jar%" (
    del /f /q "%work_dir%/%jar%"
)
jar cf "../%jar%" *
echo compilation into jar done





