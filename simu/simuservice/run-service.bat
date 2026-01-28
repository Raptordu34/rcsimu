@echo off
REM Script de lancement du SimuService avec support de la manette

echo ========================================
echo Lancement de SimuService
echo ========================================

REM Définir le chemin vers la DLL native
set DLL_PATH=%~dp0..\windowsinputapicontroller\build\bin\Debug

echo Chemin de la DLL: %DLL_PATH%
echo.

REM Se positionner dans le répertoire du script pour que les chemins relatifs fonctionnent
set BASE_DIR=%~dp0
echo Repertoire de base: %BASE_DIR%
cd /d "%BASE_DIR%"
echo Repertoire de travail: %CD%
echo.

REM Lancer le service avec les options JVM nécessaires
java -Djava.library.path="%DLL_PATH%" ^
     --enable-native-access=ALL-UNNAMED ^
     -cp "target/classes;target/dependency/*" ^
     fr.ensma.a3.ia.simuservice.SimuServiceLauncher

pause
