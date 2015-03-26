PROJECT_BASE=../..

if [[ "$1" == "-o" ]]; then

	if [[ -e $PROJECT_BASE/liquibase-oracle ]]; then
		pushd $PROJECT_BASE/liquibase-oracle
		mvn package -DskipTests
		popd
		cp -v $PROJECT_BASE/liquibase-oracle/target/liquibase*.jar ./lib
		# get the current versions of the XSD files and place them here for use
		# and reference by the scripts (for code completion within Eclipse)
		find $PROJECT_BASE/liquibase-oracle/target -name '*.xsd' -exec cp -v {} . \;
	else
		echo liquibase-oracle project not checked out, skipping
	fi
fi

if [[ -e $PROJECT_BASE/liquibase-kuali ]]; then

	pushd $PROJECT_BASE/liquibase-kuali
	mvn package -DskipTests
	popd
	
	cp -v $PROJECT_BASE/liquibase-kuali/target/liquibase*.jar ./lib
	
	# get the current versions of the XSD files and place them here for use
	# and reference by the scripts (for code completion within Eclipse)
	find $PROJECT_BASE/liquibase-kuali/target -name '*.xsd' -exec cp -v {} . \;
else
	echo liquibase-kuali project not checked out, skipping
fi
rm -f dbchangelog-2.0.xsd
