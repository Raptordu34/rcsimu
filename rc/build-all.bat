@echo off
REM Script de build pour tous les modules RC
REM Usage: build-all.bat

setlocal enabledelayedexpansion

echo ========================================
echo   Build RC Modules
echo ========================================
echo.

set "BASE_DIR=%~dp0"
set "FAILED=0"

REM Ordre de build: drivers d'abord, puis business, puis aggregator

set MODULES=mpudriver mpubusiness urmdriver urmbusiness sensorsbusiness

for %%M in (%MODULES%) do (
    echo.
    echo [BUILD] %%M
    echo ----------------------------------------
    cd /d "%BASE_DIR%\%%M"
    if exist pom.xml (
        call mvn clean install -q
        if !ERRORLEVEL! NEQ 0 (
            echo [ERREUR] %%M a echoue!
            set "FAILED=1"
            goto :end
        ) else (
            echo [OK] %%M
        )
    ) else (
        echo [SKIP] %%M - pas de pom.xml
    )
)

:end
echo.
echo ========================================
if %FAILED%==0 (
    echo   BUILD REUSSI
) else (
    echo   BUILD ECHOUE
)
echo ========================================

cd /d "%BASE_DIR%"
endlocal
exit /b %FAILED%
