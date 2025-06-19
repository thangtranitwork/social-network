@echo off
REM
FOR /F "tokens=1,2 delims==" %%A IN ('findstr /V "#" .env') DO (
    set "%%A=%%B"
)

REM Chạy ứng dụng Spring Boot
mvn spring-boot:run
