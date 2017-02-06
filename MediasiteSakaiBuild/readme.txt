This project is meant to build the Mediasite integration for Sakai. The
tool is written in Java and should be compiled with the same JDK used to build
Sakai (Java 6 for Sakai 2.9, Java 7 for Sakai 10, Java 8 for Sakai 11).

Build.xml can be customized to control how the output WAR file is named if 
necessary. Ant will automatically increment the build number in 
..\MediasiteBlackboardPlugin\build.number with each build. Build Manager may
overwrite the value as necessary.
