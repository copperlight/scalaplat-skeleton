# send /dev/null to stdin to avoid interactive prompts, in case there is a script failure
SBT := cat /dev/null | project/sbt

.PHONY: build clean coverage format

build:
	$(SBT) clean test checkLicenseHeaders scalafmtCheckAll

clean:
	$(SBT) clean

coverage:
	$(SBT) clean coverage test coverageReport
	$(SBT) coverageAggregate

format:
	$(SBT) formatLicenseHeaders scalafmtAll
