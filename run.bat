@echo off
setlocal enabledelayedexpansion

:: Create required directories for build output
if not exist bin mkdir bin
if not exist lib mkdir lib

:: Automatically download dependencies if they are missing
if not exist lib\mysql-connector-j-8.3.0.jar (
    echo Downloading MySQL Connector... (This only happens once^)
    powershell -Command "Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.3.0/mysql-connector-j-8.3.0.jar' -OutFile 'lib\mysql-connector-j-8.3.0.jar'"
)

if not exist lib\javafx-sdk-17.0.10 (
    echo Downloading JavaFX SDK... (This only happens once ^& may take a minute^)
    powershell -Command "Invoke-WebRequest -Uri 'https://download2.gluonhq.com/openjfx/17.0.10/openjfx-17.0.10_windows-x64_bin-sdk.zip' -OutFile 'lib\javafx-sdk.zip'; Expand-Archive -Path 'lib\javafx-sdk.zip' -DestinationPath 'lib' -Force; Remove-Item 'lib\javafx-sdk.zip'"
)

:: Compile the Project
echo.
echo Compiling Java code...
dir /s /b src\*.java > sources.txt
javac -encoding UTF-8 --module-path lib\javafx-sdk-17.0.10\lib --add-modules javafx.controls,javafx.fxml -cp "lib\mysql-connector-j-8.3.0.jar" -d bin @sources.txt

:: Check if Compilation hit any errors
if %ERRORLEVEL% EQU 0 (
    echo Build Succeeded! Starting Application...
    echo.
    java --module-path lib\javafx-sdk-17.0.10\lib --add-modules javafx.controls,javafx.fxml -cp "bin;lib\mysql-connector-j-8.3.0.jar" eventmanagement.Main
) else (
    echo.
    echo Compilation Failed. Look at the errors above.
)
