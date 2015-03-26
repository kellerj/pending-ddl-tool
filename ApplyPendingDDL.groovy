import groovy.sql.Sql

import java.awt.datatransfer.DataFlavor;
import java.sql.Connection

import liquibase.CatalogAndSchema
import liquibase.Liquibase
import liquibase.database.DatabaseConnection
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.diff.DiffGeneratorFactory
import liquibase.diff.DiffResult
import liquibase.diff.compare.CompareControl
import liquibase.diff.output.report.DiffToReport
import liquibase.resource.ClassLoaderResourceAccessor
import liquibase.snapshot.DatabaseSnapshot
import liquibase.snapshot.SnapshotControl
import liquibase.snapshot.SnapshotGeneratorFactory



// TODO: CliBuilder - finish options
// test that one of known environments after parsing data
// set flag in defaults - unset in env-specific
// add record-only method - in case Babu needs to run manually for some reason
if (System.properties['os.name'].toLowerCase().contains('windows')) {
	scriptName = "ApplyPendingDDL.bat"
} else {
	scriptName = "ApplyPendingDDL.sh"
}

if ( System.getProperty("apply.pending.config") == null ) {
	println "The batch file wrapper must pass a configuration file in via the apply.pending.config -D property."
	return -1
}

File configFile = new File( System.getProperty("apply.pending.config") ).absoluteFile
if ( !configFile.exists() ) {
	println "Unable to locate specified configuration file: $configFile.canonicalPath"
	return -1
}

println "Extracting configuration from $configFile.canonicalPath"
baseDir = configFile.parentFile
println "Will use base path: $baseDir"


def cli = new CliBuilder(usage:"$scriptName  -e <environment code> [--do-it] [<options>]",header:"Options:")
cli.width = 120
cli.e ( longOpt : "environment", argName : "environment code", required : true, args : 1, "The environment code against which to run the scripts." )
cli._ ( longOpt : "do-it", "If you actually want to make changes to the database, you must add this flag.  Otherwise, it will just dump the SQL to standard out." )
cli._ ( longOpt : "test-it", "This will make all pending changes to the database and then roll them back.  Before and after it will take a snapshot for comparison.  This detects structural changes only." )
//cli.o ( longOpt : "offline", "" )
cli._ ( longOpt : "tag", args : 1, "When rolling back changes, use this tag to define the point to which to roll back." )
cli._ ( longOpt : "rollback", "If set, roll back the changes to the given tag." )
cli.t ( longOpt : "db-type", args : 1, "If specified, only run changelogs designated for one of the database types supported by this tool: tp, gl, ds, rice" )
cli.s ( longOpt : "schema-type", args : 1, "If specified, only run changelogs for the given schema type.  (E.g., kfs, kfs_user, fp, rice, dsload)" )
cli.r ( longOpt : "report", "Report differences between schemas by taking a snapshot before and after each one is run.  NOTE: This will take a *long* time on the larger schemas (KFS).")
cli._ ( longOpt : "password-file", args : 1, "Absolute path to the location of the file with the needed passwords.  See sample-passwords.groovy for the expected structure of the file.")
cli.v ( longOpt : "validate", "(NOT IMPLEMENTED) Just perform a quick validation on the script formats, this will not make any connection to a database." )
cli._ ( longOpt : "record-only", "(NOT IMPLEMENTED) If set, just record all pending changesets as if they have been run.  This is used if the DB scripts needed to be run manually for some reason." )
cli.o ( longOpt : "offline", "(NOT IMPLEMENTED) If set, Liquibase will check all the changesets without a connection to the database.  This will mean that *all* changesets will be output, regardless of the database state.  Can be used for quickly validating the changeset syntax." )

def opt = cli.parse(args);
if ( !opt ) {
	return -1
}

environment  = opt.e
rollbackMode = opt["rollback"]
liquibaseTag = opt["tag"]
dryRunOnly   = !opt["do-it"] && !opt["test-it"]
offlineMode  = opt.o
testMode     = opt["test-it"]
compareBeforeAndAfter = opt.r
schemaTypeFilter   = opt.s
dbTypeFilter       = opt.t
validateOnly     = opt.v

if ( rollbackMode && !liquibaseTag ) {
	println "If you are rolling back changes, you must specify a tag to which to roll back."
	return -1
}

println "Using Configuration File: $configFile.canonicalPath"
//
// We will keep the passwords in a separate file so it does not need to be checked into VCS
// (And will probably be dynamically generated once we automate this.)
//
def passwordFile = new File(baseDir,'passwords.groovy')
if ( opt["password-file"] ) {
	passwordFile = new File( opt["password-file"] )
}
println "Using Password File     : $passwordFile.canonicalPath"

config = new ConfigSlurper(environment).parse(configFile.toURL())
if ( !passwordFile.exists() ) {
	println "********** No password file - using developer defaults ***********"
} else {
	config.merge(new ConfigSlurper(environment).parse(passwordFile.toURL()))
}

//println "\n\n****$environment****\n\n"
//println config

def processDatabaseSchemaScripts( dbType, schemaName ) {
	println "\n\n"
	println '************************************************************************************'
	println "Running scripts for $dbType / $schemaName "
	println "Database URL      : ${config.database[dbType].url}"
	println "Schema            : ${config.database[dbType].schemas[schemaName]}"
	// obtain reference to master.xml script
	File dbTypeDir = new File( baseDir, dbType )
	File schemaDir = new File( dbTypeDir, schemaName )
	File masterChangelog = new File( schemaDir, "master.xml" )
	println "Master Changelog  : $masterChangelog.canonicalPath"
	println '************************************************************************************'
	if ( !masterChangelog.exists() ) {
		println "No master changelog exists for this combination - skipping"
		return
	}
	if ( dryRunOnly ) {
		println "****             DRY  RUN  ONLY - DATABASE WILL NOT BE UPDATED                  ****"
		println "****                     DUMPING SQL OUTPUT TO CONSOLE                          ****"
		println '************************************************************************************'
	}
	println "\n\n"
	def dbUrl = config.database[dbType].url
	// if in offline mode - then change DB URL
	if ( offlineMode || validateOnly ) {
		dbUrl = "offline:oracle"
	}

	// call liquibase
	DatabaseConnection dbConn = DatabaseFactory.getInstance().openConnection(
		dbUrl, config.database[dbType].schemas[schemaName], config.database[dbType].passwords[schemaName], null, new ClassLoaderResourceAccessor())
	dbConn.setAutoCommit(false)
	Liquibase liquibase = new Liquibase("$dbType/$schemaName/master.xml", new ClassLoaderResourceAccessor(),
			DatabaseFactory.getInstance().findCorrectDatabaseImplementation(dbConn))

	config.schemas.each { liquibase.setChangeLogParameter( it.key, it.value ) }
	config.dblinks.each { liquibase.setChangeLogParameter( it.key, it.value ) }

	if ( dbType == "rice" ) {
		liquibase.setChangeLogParameter( "import.workflow.database.password", config.database[dbType].passwords[schemaName] )
		liquibase.setChangeLogParameter( "import.workflow.classpath", this.class.classLoader.rootLoader.getURLs().collect().join(File.pathSeparator) )
		liquibase.setChangeLogParameter( "import.workflow.kfs.project.location", config.locations.kfs )
	}
	println "Validating..."
	liquibase.validate()

	if ( validateOnly ) {
		return
	}

	if ( compareBeforeAndAfter ) {
		println "Taking Initial Snapshot..."
		DatabaseSnapshot startingSnapshot = SnapshotGeneratorFactory.getInstance().createSnapshot( new CatalogAndSchema(schemaName.toUpperCase(), schemaName.toUpperCase()), liquibase.getDatabase(), new SnapshotControl(liquibase.getDatabase()));
	}

	// run using a context, so that scripts which should not be run in the current context are not executed
	// and - if we use the version of the method which takes a Writer, it dumps to that rather than updating the database
	if ( !rollbackMode ) {
		if ( dryRunOnly ) {
			liquibase.update(environment, new PrintWriter( new File( baseDir, "${dbType}-${schemaName}.sql"), "UTF-8" ) )
			liquibase.futureRollbackSQL(environment, new PrintWriter( new File( baseDir, "${dbType}-${schemaName}-rollback.sql"), "UTF-8" ) )
			println new File( baseDir, "${dbType}-${schemaName}.sql").text
		} else {
			liquibase.update(environment)
		}
	} else {
		println "Rolling back database to tag: $liquibaseTag"
		if ( dryRunOnly ) {
			liquibase.rollback( liquibaseTag, environment, new PrintWriter( new File( baseDir, "${dbType}-${schemaName}-rollback.sql"), "UTF-8" ) )
			println new File( baseDir, "${dbType}-${schemaName}-rollback.sql").text
		} else {
			liquibase.rollback( liquibaseTag, environment )
		}
	}

	if ( compareBeforeAndAfter ) {
		println "Taking After Snapshot..."
		DatabaseSnapshot endingSnapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(new CatalogAndSchema(schemaName.toUpperCase(), schemaName.toUpperCase()), liquibase.getDatabase(), new SnapshotControl(liquibase.getDatabase()));
		DiffResult diffResult = DiffGeneratorFactory.getInstance().compare( startingSnapshot, endingSnapshot, new CompareControl() )
		DiffToReport report = new DiffToReport( diffResult, System.out )
		report.print()
	}
}

def takeDatabaseSnapshot( dbType, schemaName, preTag ) {
	// test db connection
	Sql db = Sql.newInstance( config.database[dbType].url, config.database[dbType].schemas[schemaName], config.database[dbType].passwords[schemaName], "oracle.jdbc.OracleDriver" )
	// get connection from Groovy object
	Connection conn = db.connection
	// call liquibase
	DatabaseConnection dbConn = new JdbcConnection(conn);
	dbConn.setAutoCommit(false)
	Liquibase liquibase = new Liquibase("pre-test-tag.xml", new ClassLoaderResourceAccessor(),
			DatabaseFactory.getInstance().findCorrectDatabaseImplementation(dbConn))

	if ( preTag ) {
		liquibase.update(environment)
	}

	SnapshotGeneratorFactory.getInstance().createSnapshot( new CatalogAndSchema(schemaName.toUpperCase(), schemaName.toUpperCase()), liquibase.getDatabase(), new SnapshotControl(liquibase.getDatabase()))
}

def isMatchingSchemaFilter( dbType, schemaName ) {
	return (!dbTypeFilter || dbTypeFilter == dbType) && (!schemaTypeFilter || schemaTypeFilter == schemaName)
}

def testDatabaseConnection( dbType, schemaName ) {
	println "Testing Connection for $dbType / $schemaName : ${config.database[dbType].url} / ${config.database[dbType].schemas[schemaName]}"
	Sql db = Sql.newInstance( config.database[dbType].url, config.database[dbType].schemas[schemaName], config.database[dbType].passwords[schemaName], "oracle.jdbc.OracleDriver" )
	db.close()
}

initialSnapshots = []
finalSnapshots = []

if ( !validateOnly ) {
	// Test all database connections
	for ( pair in config.instanceSchemaPairs ) {
		if ( !isMatchingSchemaFilter(pair[0],pair[1]) ) continue
		testDatabaseConnection(pair[0], pair[1])
	}

	if ( testMode ) {
		for ( pair in config.instanceSchemaPairs ) {
			if ( !isMatchingSchemaFilter(pair[0],pair[1]) ) continue
			println "Taking Initial Snapshot: $pair"
			initialSnapshots += takeDatabaseSnapshot( pair[0], pair[1], true )
		}
	}
}

for ( pair in config.instanceSchemaPairs ) {
	if ( !isMatchingSchemaFilter(pair[0],pair[1]) ) continue
	processDatabaseSchemaScripts( pair[0], pair[1] )
}

if ( testMode && !validateOnly ) {
	rollbackMode = true
	liquibaseTag = "PRETEST"

	println "Rolling Back All Changes"
	for ( pair in config.instanceSchemaPairs ) {
		if ( !isMatchingSchemaFilter(pair[0],pair[1]) ) continue
		processDatabaseSchemaScripts( pair[0], pair[1] )
	}

	for ( pair in config.instanceSchemaPairs ) {
		if ( !isMatchingSchemaFilter(pair[0],pair[1]) ) continue
		println "Taking Final Snapshot: $pair"
		finalSnapshots += takeDatabaseSnapshot( pair[0], pair[1], false )
	}

	for ( i in 0..(initialSnapshots.size()-1) ) {
		println "comparing ${config.instanceSchemaPairs[i]}"
		DiffResult diffResult = DiffGeneratorFactory.getInstance().compare( initialSnapshots[i], finalSnapshots[i], new CompareControl() )
		if ( diffResult.areEqual() ) {
			println "NO DIFFERENCES"
		} else {
			DiffToReport report = new DiffToReport( diffResult, System.out )
			report.print()
		}
	}
}
