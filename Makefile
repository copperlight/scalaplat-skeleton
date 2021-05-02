JAR  := $(shell ls -t target/scala-2.13/*.jar 2> /dev/null |head -1)
JAVA := ${JAVA_HOME}/bin/java
SBT  := project/sbt

.PHONY: build clean coverage fatjar format run

build:
	$(SBT) clean test checkLicenseHeaders scalafmtCheckAll

check:
	$(SBT) checkLicenseHeaders scalafmtCheckAll

clean:
	$(SBT) clean

coverage:
	$(SBT) clean coverage test coverageReport
	$(SBT) coverageAggregate

fatjar:
	$(SBT) assembly

format:
	$(SBT) formatLicenseHeaders scalafmtAll

run:
ifeq ($(JAR),)
	$(error cannot find jar file, run the fatjar task to produce one)
endif
	$(JAVA) -jar $(JAR)
