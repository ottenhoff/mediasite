@echo off
echo * * * * * * * * * * * * * * * * * * * * * * * * * *
echo  BUILDING MEDIASITE FOR SAKAI
echo * * * * * * * * * * * * * * * * * * * * * * * * * *
REM Let Ant pretend to increment the build number, but let the build
REM manager actually manage the build numbers. 
if "%~1" == "Debug" attrib ..\sakai-mediasite\build.number -r		
copy ..\..\sakai-mediasite\build.number ..\..\sakai-mediasite\build.number.tmp /y	
call ..\..\..\..\..\3rdParty\sdk\Java\Ant\org.apache.ant_1.8.3.v201301120609\bin\ant -buildfile ..\..\sakai-mediasite\build.xml
copy ..\..\sakai-mediasite\build.number.tmp ..\..\sakai-mediasite\build.number /y
del ..\..\sakai-mediasite\build.number.tmp	
if "%~1" == "Debug" attrib ..\sakai-mediasite\build.number +r