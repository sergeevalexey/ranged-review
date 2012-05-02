set CLASSPATH=target\dependency\groovy-all-1.8.3.jar;target\dependency\commons-codec-1.4.jar;target\dependency\commons-io-2.3.jar;target\dependency\commons-logging-1.1.1.jar;target\dependency\hamcrest-core-1.1.jar;target\dependency\hg4j-0.9.0.jar;target\dependency\httpclient-4.1.3.jar;target\dependency\httpcore-4.1.4.jar;target\ranged-review-1.0-SNAPSHOT.jar

set JAVA_EXE=%JAVA_HOME%\bin\java.exe

call %JAVA_EXE% -cp "%CLASSPATH%" org.beethoven.code.AnalysisTool %*
