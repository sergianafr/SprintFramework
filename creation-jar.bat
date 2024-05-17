@echo off
set "work_dir=C:\Users\SERGIANA\Documents\Study\S4\Mr-Naina\SprintFramework"
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
for /F "delims=" %%i in (libs.txt) do set "classpath=%%i"
@REM echo "%classpath%"
:: Exécuter la commande javac
javac -d "%temp%" -cp "%classpath%" @sources.txt

@REM del sources.txt
@REM del libs.txt

rem creation fichier jar
cd temp
if exist "%work_dir%/%jar%" (
    del /f /q "%work_dir%/%jar%"
)
jar cf "../%jar%" *
echo compilation into jar done





