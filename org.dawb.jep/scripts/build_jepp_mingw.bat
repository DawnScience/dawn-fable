set JEPP_HOME=C:\Fable\jep-2.3\jep-2.3
set JAVA_HOME="c:\Program Files\Java\jdk1.6.0_10"
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
cd %JEPP_HOME%\src\jep
set PYHOME=c:\python25
set CSOURCE=python/jep_object.c jep.c system.h pyembed.c util.c pyjmethod.c pyjobject.c pyjclass.c pyjfield.c pyjarray.c invocationhandler.c
set COBJS=jep_object.o jep.o pyembed.o util.o pyjmethod.o pyjobject.o pyjclass.o pyjfield.o pyjarray.o invocationhandler.o
set CINC=-I%JAVA_HOME%/include -I%JAVA_HOME%/include/win32 -I%PYHOME%/include -I%JEPP_HOME%/src/jep
set LIBS=-L%PYHOME%/libs -lpython25
set COPT=-Wall -D_JNI_IMPLEMENTATION_ -Wl,--kill-at -mno-cygwin
set CLIB=-shared

REM Use gcc to compile c
gcc %COPT% %CINC% %LIBS% %CLIB% -Wl,--kill-at -c %CSOURCE% 2> gcc_errors.log
REM But g++ to link symbols as __imp__ rather than _imp__ ... ahem.
REM g++ %COPT% %COBJS% %CINC% %LIBS% %CLIB% -Wl,--kill-at %JAVA_HOME%/lib/jvm.lib -o jep.dll 2> gpp_errors2.log
g++ %COPT% %COBJS% %CINC% %LIBS% %CLIB% -Wl,--kill-at %JAVA_HOME%/lib/jvm.lib -o jep.dll

REM gvim errors.log
REM gvim errors2.log
more errors2.log

:TEST

:END 