all: compile run

compile:
	mvn compile

run:
	mvn exec:java -Dexec.mainClass="com.nexmo.xwithy.App"