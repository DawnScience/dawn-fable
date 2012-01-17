REM Script to build jepp for win64 using MSVC, python 2.6


set JEPP_HOME=C:\Users\jon\Work\fable\jep-2.3
set JAVA_HOME="c:\Program Files\Java\jdk1.6.0_14"
set JAVAC=%JAVA_HOME%\bin\javac
set JAVAH=%JAVA_HOME%\bin\javah
set JAVACOPT=-deprecation -classpath src/
set JAR=%JAVA_HOME%\bin\jar
set JAROPTS=-u0f



:JAVAC

REM Make the Java classes

cd %JEPP_HOME%

%JAVAC% %JAVACOPT% src/jep/python/PyObject.java
%JAVAC% %JAVACOPT% src/jep/python/PyModule.java
%JAVAC% %JAVACOPT% src/jep/python/PyClass.java
%JAVAC% %JAVACOPT% src/jep/Jep.java
%JAVAC% %JAVACOPT% src/jep/Run.java
%JAVAC% %JAVACOPT%;./ext/bsf.jar src/jep/BSFJepEngine.java
%JAVAC% %JAVACOPT% src/jep/Test.java
%JAVAC% %JAVACOPT% src/jep/JepException.java
%JAVAC% %JAVACOPT% src/jep/ClassList.java
%JAVAC% %JAVACOPT% src/jep/JepScriptEngine.java
%JAVAC% %JAVACOPT% src/jep/JepScriptEngineFactory.java
%JAVAC% %JAVACOPT% src/jep/InvocationHandler.java
%JAVAC% %JAVACOPT% src/jep/Proxy.java
%JAVAC% %JAVACOPT% src/jep/Util.java


:JAVAH
REM Make the C header files (depend on classes)

cd %JEPP_HOME%
%JAVAH% -o src/jep/python/jep_object.h -classpath src/ jep.python.PyObject
%JAVAH% -o src/jep/jep.h -classpath src/ jep.Jep
%JAVAH% -o src/jep/invocationhandler.h -classpath src/ jep.InvocationHandler

REM Make the jar file
:JAR

cd %JEPP_HOME%\src
set JARFILE=%JEPP_HOME%\jep.jar
%JAR% -cfm %JARFILE% manifest META-INF/services/javax.script.ScriptEngineFactory

set CLASSES=jep/python/PyObject.class jep/python/PyModule.class jep/python/PyClass.class jep/Jep.class jep/JepException.class jep/Test.class jep/Run.class jep/InvocationHandler.class jep/Proxy.class  jep/Util.class jep/ClassList.class

%JAR% %JAROPTS% %JARFILE% %CLASSES%

cd %JEPP_HOME%


:C

REM First get VS Express Edition to have a 32 bit compiler
REM The add the platform SDK which provides a 64 bit compiler

call "C:\Program Files\Microsoft Visual Studio 9.0\VC\bin\vcvars64.bat"
cd %JEPP_HOME%\src\jep
REM This is 64 bit on Jons home PC
set PYHOME=c:\software\Python26
set CSOURCE=python/jep_object.c jep.c  pyembed.c util.c pyjmethod.c pyjobject.c pyjclass.c pyjfield.c pyjarray.c invocationhandler.c
set COBJS=jep_object.obj jep.obj pyembed.obj util.obj pyjmethod.obj pyjobject.obj pyjclass.obj pyjfield.obj pyjarray.obj invocationhandler.obj
set CINC=-I%JAVA_HOME%/include -I%JAVA_HOME%/include/win32 -I%PYHOME%/include -I%JEPP_HOME%/src/jep
set LIBS=c:\software\Python26\libs\python26.lib
set COPT=-MD 
set CLIB=-LD

REM Use cl to compile c
cl %COPT% %CINC% -c %CSOURCE%
cl %CLIB% /o jep.dll %COPT% %COBJS% %CINC% %LIBS% %JAVA_HOME%/lib/jvm.lib 
mt -manifest jep.dll.manifest -outputresource:jep.dll;2
copy jep.dll ..\..
cd %JEPP_HOME%


REM gvim errors.log
more errors.log
REM gvim errors2.log
more errors2.log

:TEST

echo import sys;print sys.version | %JAVA_HOME%\bin\java -classpath jep.jar jep.Run console.py

:END 

