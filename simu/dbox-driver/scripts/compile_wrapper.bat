@echo off
REM ============================================================================
REM Script pour compiler SimpleDboxAPI.dll et dbox_controller_wrapper.dll
REM
REM PREREQUIS:
REM   - Visual Studio avec outils C++ installes
REM   - Executer depuis "Developer Command Prompt for VS"
REM
REM USAGE:
REM   cd dbox-driver\scripts
REM   compile_wrapper.bat
REM
REM ============================================================================

echo.
echo ========================================================
echo    Compilation de SimpleDboxAPI.dll et
echo    dbox_controller_wrapper.dll pour Java Panama FFM
echo ========================================================
echo.

REM ---------------------------------------------------------------------------
REM Configuration des chemins
REM ---------------------------------------------------------------------------

set SCRIPT_DIR=%~dp0
set DRIVER_ROOT=%SCRIPT_DIR%..
set SDK_ROOT=%DRIVER_ROOT%\LPSIMUDevSim\DBOX SDK\CppSolution

REM Chemins des headers
set DRIVER_INCLUDE=%DRIVER_ROOT%\include
set SDK_INCLUDE=%SDK_ROOT%\include

REM Chemins des libs
set DRIVER_LIB=%DRIVER_ROOT%\lib
set SDK_LIB=%SDK_ROOT%\lib\windows

echo [Configuration]
echo   Driver root:    %DRIVER_ROOT%
echo   SDK root:       %SDK_ROOT%
echo   Driver include: %DRIVER_INCLUDE%
echo   SDK include:    %SDK_INCLUDE%
echo   Driver lib:     %DRIVER_LIB%
echo   SDK lib:        %SDK_LIB%
echo.

REM ---------------------------------------------------------------------------
REM Verification des prerequis
REM ---------------------------------------------------------------------------

echo [1/6] Verification des prerequis...

where cl.exe >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERREUR: cl.exe non trouve!
    echo.
    echo Ouvrez "Developer Command Prompt for VS" et relancez ce script
    echo.
    pause
    exit /b 1
)
echo   [OK] cl.exe trouve

if not exist "%DRIVER_INCLUDE%\SimpleDboxAPI.h" (
    echo ERREUR: SimpleDboxAPI.h non trouve dans %DRIVER_INCLUDE%
    pause
    exit /b 1
)
echo   [OK] SimpleDboxAPI.h trouve

if not exist "%DRIVER_ROOT%\src\SimpleDboxAPI.cpp" (
    echo ERREUR: SimpleDboxAPI.cpp non trouve dans %DRIVER_ROOT%\src
    pause
    exit /b 1
)
echo   [OK] SimpleDboxAPI.cpp trouve

@REM if not exist "%SDK_INCLUDE%\LiveMotion\dboxLiveMotion.h" (
@REM     echo ERREUR: dboxLiveMotion.h non trouve dans %SDK_INCLUDE%\LiveMotion
@REM     echo Verifiez que SDK_ROOT pointe vers le bon repertoire
@REM     pause
@REM     exit /b 1
@REM )
@REM echo   [OK] SDK DBOX trouve

@REM if not exist "%SDK_LIB%\dbxLive64MD-vc142.lib" (
@REM     echo ERREUR: dbxLive64MD-vc142.lib non trouve dans %SDK_LIB%
@REM     pause
@REM     exit /b 1
@REM )
@REM echo   [OK] dbxLive64MD-vc142.lib trouve

echo.

REM ---------------------------------------------------------------------------
REM Compilation de SimpleDboxAPI.dll
REM ---------------------------------------------------------------------------

echo [2/6] Compilation de SimpleDboxAPI.dll...
echo.

cd /d "%DRIVER_ROOT%\src"

REM SIMPLEDBOXAPI_EXPORTS active __declspec(dllexport) dans SimpleDboxAPI.h
cl.exe /LD /EHsc /O2 /MD ^
    /DSIMPLEDBOXAPI_EXPORTS ^
    SimpleDboxAPI.cpp ^
    /I"%DRIVER_INCLUDE%" ^
    /I"%SDK_INCLUDE%" ^
    /link ^
    "%SDK_LIB%\dbxLive64MD-vc142.lib" ^
    Shell32.lib ^
    /OUT:"%DRIVER_LIB%\SimpleDboxAPI.dll"

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERREUR: Compilation de SimpleDboxAPI.dll echouee!
    pause
    exit /b 1
)

echo   [OK] SimpleDboxAPI.dll compile!
echo.

REM Nettoyage des fichiers temporaires de SimpleDboxAPI
if exist SimpleDboxAPI.obj del SimpleDboxAPI.obj
if exist SimpleDboxAPI.exp del SimpleDboxAPI.exp
if exist SimpleDboxAPI.lib move SimpleDboxAPI.lib "%DRIVER_LIB%\" >nul

REM ---------------------------------------------------------------------------
REM Compilation de dbox_controller_wrapper.dll
REM ---------------------------------------------------------------------------

echo [3/6] Compilation de dbox_controller_wrapper.dll...
echo.

cl.exe /LD /EHsc /O2 /MD ^
    /DDBOX_CONTROLLER_WRAPPER_EXPORTS ^
    dbox_controller_wrapper.cpp ^
    /I"%DRIVER_INCLUDE%" ^
    /I"%SDK_INCLUDE%" ^
    /link ^
    "%DRIVER_LIB%\SimpleDboxAPI.lib" ^
    "%SDK_LIB%\dbxLive64MD-vc142.lib" ^
    Shell32.lib ^
    /OUT:"%DRIVER_LIB%\dbox_controller_wrapper.dll"

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERREUR: Compilation de dbox_controller_wrapper.dll echouee!
    pause
    exit /b 1
)

echo   [OK] dbox_controller_wrapper.dll compile!
echo.

REM ---------------------------------------------------------------------------
REM Nettoyage des fichiers temporaires
REM ---------------------------------------------------------------------------

echo [4/6] Nettoyage des fichiers temporaires...

if exist dbox_controller_wrapper.obj del dbox_controller_wrapper.obj
if exist dbox_controller_wrapper.exp del dbox_controller_wrapper.exp
if exist dbox_controller_wrapper.lib move dbox_controller_wrapper.lib "%DRIVER_LIB%\" >nul

echo   [OK] Fichiers temporaires nettoyes
echo.

REM ---------------------------------------------------------------------------
REM Copie vers dbox-business pour les tests
REM ---------------------------------------------------------------------------

echo [5/6] Copie des DLLs vers dbox-business...

set BUSINESS_TEST=%PROJECT_ROOT%\dbox-business\src\main\java\fr\ensma\a3\ia\test

if exist "%BUSINESS_TEST%" (
    copy /Y "%DRIVER_LIB%\dbox_controller_wrapper.dll" "%BUSINESS_TEST%\" >nul
    copy /Y "%DRIVER_LIB%\SimpleDboxAPI.dll" "%BUSINESS_TEST%\" >nul
    copy /Y "%DRIVER_LIB%\dbxLive64.dll" "%BUSINESS_TEST%\" >nul
    echo   [OK] DLLs copiees vers dbox-business/test
) else (
    echo   [SKIP] Dossier test non trouve
)

echo.

REM ---------------------------------------------------------------------------
REM Resume
REM ---------------------------------------------------------------------------

echo [6/6] Resume
echo.
echo ========================================================
echo    COMPILATION REUSSIE!
echo ========================================================
echo.
echo Fichiers generes dans %DRIVER_LIB%:
echo   - SimpleDboxAPI.dll        (API simplifiee DBOX)
echo   - SimpleDboxAPI.lib        (import library)
echo   - dbox_controller_wrapper.dll  (wrapper Java FFM)
echo   - dbox_controller_wrapper.lib
echo.
echo DLLs requises au runtime:
echo   - dbox_controller_wrapper.dll
echo   - SimpleDboxAPI.dll
echo   - dbxLive64.dll
echo.
echo ========================================================

pause
