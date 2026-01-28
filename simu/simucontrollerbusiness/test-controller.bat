@echo off
REM Test rapide de la manette (sans WebSocket)

echo ========================================
echo Test du controleur (logique metier)
echo ========================================
echo.
echo Branchez votre manette et appuyez sur une touche...
pause

REM Définir les chemins
set BASE_DIR=%~dp0
set DLL_PATH=%BASE_DIR%..\windowsinputapicontroller\build\bin\Debug
set DRIVER_JAR=%BASE_DIR%..\windowsinputapicontroller\target\windowsinputapicontroller-1.0-SNAPSHOT.jar
set BUSINESS_CLASSES=%BASE_DIR%target\classes

echo Chemin de la DLL: %DLL_PATH%
echo.
echo Lecture des entrees de la manette...
echo Appuyez sur Ctrl+C pour arreter
echo.

REM Vérifier que les fichiers existent
if not exist "%DLL_PATH%\sim_input.dll" (
    echo ERREUR: DLL non trouvee: %DLL_PATH%\sim_input.dll
    echo Compilez d'abord le module windowsinputapicontroller avec CMake
    pause
    exit /b 1
)

if not exist "%DRIVER_JAR%" (
    echo ERREUR: JAR du driver non trouve: %DRIVER_JAR%
    echo Executez 'mvn clean install' dans windowsinputapicontroller
    pause
    exit /b 1
)

if not exist "%BUSINESS_CLASSES%" (
    echo ERREUR: Classes business non compilees
    echo Executez 'mvn clean compile' dans simucontrollerbusiness
    pause
    exit /b 1
)

REM Lancer le test
cd /d "%BASE_DIR%"
java -Djava.library.path="%DLL_PATH%" ^
     --enable-native-access=ALL-UNNAMED ^
     -cp "%BUSINESS_CLASSES%;%DRIVER_JAR%" ^
     fr.ensma.a3.ia.simucontrollerbusiness.TestSimuBusiness

pause
