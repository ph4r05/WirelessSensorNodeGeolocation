Tos-install-jni

Install Java JDK first., then copy dll to jre/bin
Restart may be needed to correctly connect to the base station with JNI




Alternative:
Install java JDK
Install Cygwin with (gcc, file, python, nano, rpm, ...)
Install compilers
Install tos-tools, nesc, deputy
Install tinyos tree


the -32.dll problem
	•In C:\cygwin\lib\tinyos, make copies of getenv.dll and toscomm.dll and name them getenv-32.dll and toscomm-32.dllrespectively.
	•Copy these four DLL files into C:\Program Files\Java\jdk1.6.0_20\bin
