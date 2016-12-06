This project is meant to build the Mediasite Building Block for Blackboard. The
building block is written in Java and should be compiled against JDK 1.6 (a.k.a.
Java 6) for compatibility with more Blackboard Service Packs. The more recent 
Blackboard Service Packs require JDK 1.7 -- Java 7.

The build.bat file calls Ant to build the a WAR file that will be deposited in
..\MediasiteBlackboardPlugin\target\lib\*.war

Build.xml can be customized to control how the output WAR file is named if 
necessary. Ant will automatically increment the build number in 
..\MediasiteBlackboardPlugin\build.number with each build. Build Manager may
overwrite the value as necessary.