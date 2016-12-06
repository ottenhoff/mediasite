@echo off
echo * * * * * * * * * * * * * * * * * * * * * * * * * *
echo  CLEANING MEDIASITE FOR SAKAI
echo * * * * * * * * * * * * * * * * * * * * * * * * * *
REM Delete existing war files
IF EXIST ..\..\sakai-mediasite\target del ..\..\sakai-mediasite\target\sakai-mediasite*.war