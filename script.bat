@echo off
set IMAGE=wigell-padel
set CONTAINER=wigell-padel
set NETWORK=wigell-network
set PORT=7575

echo Stopping %CONTAINER%
docker stop %CONTAINER%
echo Deleting container %CONTAINER%
docker rm %CONTAINER%
echo Deleting image %IMAGE%
docker rmi %IMAGE%
echo Running mvn package
call mvn package
echo Creating image %IMAGE%
docker build -t %IMAGE% .
echo Creating and running container %CONTAINER%
docker run -d -p %PORT%:%PORT% --name %CONTAINER% --network %NETWORK% %IMAGE%
echo Done!