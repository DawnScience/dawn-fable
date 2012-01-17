#include <stdlib.h>
#include <string.h>
#include "fable_jputenv_Putenv.h"

/* putenv */
JNIEXPORT jint JNICALL Java_fable_jputenv_Putenv_putenv

(JNIEnv *env, jclass cls, jstring name, jstring value){
  jboolean icname;
  const char *tname = (*env)->GetStringUTFChars(env, name, &icname);
  if(!tname){
    jclass cls;
    cls = (*env)->FindClass(env, "java/lang/OutOfMemoryError");
    (*env)->ThrowNew(env, cls,  "out of memory");
    return 0;
  }
  jboolean icvalue;
  const char *tvalue = (*env)->GetStringUTFChars(env, value, &icvalue);
  if(!tvalue){
    jclass cls;
    cls = (*env)->FindClass(env, "java/lang/OutOfMemoryError");
    (*env)->ThrowNew(env, cls,  "out of memory");
    return 0;
  }
  int len = strlen(tname) + strlen(tvalue) + 1;
  char expr[len+1];
  sprintf(expr, "%s=%s", tname, tvalue);
  printf("%s=%s", tname, tvalue);
  fflush(NULL);
  int rv = putenv(expr);
  if(icvalue) (*env)->ReleaseStringUTFChars(env, value, tvalue);
  if(icname) (*env)->ReleaseStringUTFChars(env, name, tname);
  return rv;
}

