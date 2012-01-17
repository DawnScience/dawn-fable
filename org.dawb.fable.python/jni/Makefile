#
# Makefile for putenv for Java
#
SHELL = /bin/bash
# Package information
PACKAGE = fable.jputenv
VERSION = 1.0
PACKAGEDIR = $(PACKAGE)-$(VERSION)
PACKAGETGZ = $(PACKAGE)-$(VERSION).tar.gz
LIBVER = 1
LIBREV = 0
# Targets
JARFILES = jputenv.jar
JAVAFILES = Putenv.java
LIBRARYFILES =  libjputenv.so.1.0.0 libjputenv.so.1 libjputenv.so
LIBOBJFILES = jputenv.o 

# Building configuration
JAVAC = /usr/bin/javac
JAVACFLAGS = -source 1.6 -d .
JAR = /usr/bin/jar
JAVAH = /usr/bin/javah
JAVADOC = /usr/bin/javadoc
JAVARUN = /usr/bin/java
CC = gcc
CPPFLAGS = -I/usr/lib/jvm/java-6-sun/include -I/usr/lib/jvm/java-6-sun/include/linux -I. 
CFLAGS = -std=c99 -Wall -fPIC -fsigned-char -O2  -D_GNU_SOURCE=1
LDFLAGS = -L. -L$(LIBDIR) -L/home/goetz/lib -L/usr/local/lib
LIBS = -lz -lpthread -lm -lc 
LDENV = LD_RUN_PATH=/lib:/usr/lib:$(LIBDIR):$(HOME)/lib:/usr/local/lib:$(LIBDIR):.
RUNENV = LD_LIBRARY_PATH=.:/lib:/usr/lib:$(LIBDIR):$(HOME)/lib:/usr/local/lib:$(LIBDIR)

#================================================================
# Suffix rules
#================================================================


.SUFFIXES :
.SUFFIXES : .c .o

.c.o :
	$(CC) -c $(CPPFLAGS) $(CFLAGS) $<



#================================================================
# Actions
#================================================================


all : $(JARFILES) $(LIBRARYFILES)
	@printf '\n'
	@printf '#================================================================\n'
	@printf '# Ready to install.\n'
	@printf '#================================================================\n'


clean :
	rm -rf $(JARFILES) $(LIBRARYFILES) $(LIBOBJFILES) \
	  *.o a.out *.class check.in check.out gmon.out leak.log \
	  casket casket-* casket.* *.wal *~ hoge moge


head : jputenv.jar
	CLASSPATH=jputenv.jar $(JAVAH) -jni fable/jputenv

#================================================================
# Building binaries
#================================================================


jputenv.jar : $(JAVAFILES)
	$(JAVAC) $(JAVACFLAGS) $(JAVAFILES)
	$(JAR) cvf $@ fable/jputenv/*.class
	CLASSPATH=jputenv.jar $(JAVAH) -jni fable.jputenv.Putenv
	rm -rf fable


libjputenv.so.$(LIBVER).$(LIBREV).0 : $(LIBOBJFILES)
	$(LDENV) $(CC) $(CFLAGS) -shared -Wl,-soname,libjputenv.so.$(LIBVER) -o $@ \
	  $(LIBOBJFILES) $(LDFLAGS) $(LIBS)


libjputenv.so.$(LIBVER) : libjputenv.so.$(LIBVER).$(LIBREV).0
	ln -f -s libjputenv.so.$(LIBVER).$(LIBREV).0 $@


libjputenv.so : libjputenv.so.$(LIBVER).$(LIBREV).0
	ln -f -s libjputenv.so.$(LIBVER).$(LIBREV).0 $@


libjputenv.$(LIBVER).$(LIBREV).0.dylib : $(LIBOBJFILES)
	$(CC) -dynamiclib -o $@ \
	  -install_name $(LIBDIR)/libjputenv.$(LIBVER).dylib \
	  -current_version $(LIBVER).$(LIBREV).0 -compatibility_version $(LIBVER) \
	  $(LIBOBJFILES) $(LDFLAGS) $(LIBS)


libjputenv.$(LIBVER).dylib : libjputenv.$(LIBVER).$(LIBREV).0.dylib
	ln -f -s libjputenv.$(LIBVER).$(LIBREV).0.dylib $@


libjputenv.dylib : libjputenv.$(LIBVER).$(LIBREV).0.dylib
	ln -f -s libjputenv.$(LIBVER).$(LIBREV).0.dylib $@


libjputenv.jnilib : libjputenv.dylib
	ln -f -s libjputenv.dylib $@


jputenv.o : jputenv.c

# END OF FILE
