hadoop_jar = ${HADOOP_HOME}/hadoop-core-0.20.203.0.jar
javac_opt = -cp .:${hadoop_jar} -d build

default:
	javac ${javac_opt} ngram/WordLengthCount.java
	javac ${javac_opt} ngram/NgramInitialCount.java

runlocal2:
	HADOOP_CLASSPATH=./build hadoop ngram.WordLengthCount -fs file:/// -jt local in.data out

runlocal3:
	HADOOP_CLASSPATH=./build hadoop ngram.NgramInitialCount -fs file:/// -jt local in3.data out ${N}

.PHONY: clean
clean:
	rm -r build/* 2> /dev/null
	rm -r out 2> /dev/null
