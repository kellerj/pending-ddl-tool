Running Pending DDL
===================

The pending DDL is structured in the nested directories as Liquibase scripts.  To run the tool which will apply these scripts, run the groovy script as below.

### Base Command

This is the base command.  Running it will display more usage instructions:

```
./ApplyPendingDDL.sh
```

results in

```
error: Missing required option: e
usage: ApplyPendingDDL.sh  -e <environment code> [<options>]
Options:
    --do-it                            If you actually want to make
                                       changes to the database, you must
                                       add this flag.  Otherwise, it will
                                       just dump the SQL to standard out.
 -e,--environment <environment code>   The environment code against which
                                       to run the scripts.
 -o,--offline                          (NOT IMPLEMENTED) If set, Liquibase
                                       will check all the changesets
                                       without a connection to the
                                       database.  This will mean that
                                       *all* changesets will be output,
                                       regardless of the database state.
                                       Can be used for quickly validating
                                       the changeset syntax.
 -r,--report                           Report differences between schemas
                                       by taking a snapshot before and
                                       after each one is run.  NOTE: This
                                       will take a *long* time on the
                                       larger schemas (KFS).
    --record-only                      (NOT IMPLEMENTED) If set, just
                                       record all pending changesets as if
                                       they have been run.  This is used
                                       if the DB scripts needed to be run
                                       manually for some reason.
    --rollback                         If set, roll back the changes to
                                       the given tag.
 -s,--schema-type <arg>                If specified, only run changelogs
                                       for the given schema type.  (kfs,
                                       kfs_user, fp, rice, dsload)
 -t,--db-type <arg>                    If specified, only run changelogs
                                       designated for one of the database
                                       types supported by this tool: tp,
                                       gl, ds, rice
    --tag <arg>                        When rolling back changes, use this
                                       tag to define the point to which to
                                       roll back.
    --test-it                          This will make all pending changes
                                       to the database and then roll them
                                       back.  Before and after it will
                                       take a snapshot for comparison.
                                       This detects structural changes
                                       only.
```


### Running in Test Mode

By default, if you specify the environment only, it will generate any needed SQL statements to update the database and dump them to the console and to files with the name type of `<db type>-<schema>.sql`.

This *will* connect to your local development database to query the liquibase control tables (to determine what still needs to be run), so it must be up even when testing your scripts.

```
./ApplyPendingDDL.sh -e dev
```

This will also test all your rollbacks.  If any changeset in your files does not contain a rollback, this proces will fail.  Any rollback SQL will be placed in files named: `<db type>-<schema>-rollback.sql`.

### Testing Your Rollback Commands

If you provide the `--test-it` parameter, the script will run all pending database changes **and then attempt to roll them back**.  Before and after, it will take a structural snapshot of the database and compare them to ensure that your scripts properly roll back changes.

**It is important** that you take a VirtualBox snapshot of your database before running this as, if the process fails at any point, you will want to restore to it before trying again.


### Applying Changes

When you are ready to run the scripts against the database, add the `--do-it` parameter to the command and it will execute the changes.

```
./ApplyPendingDDL.sh -e dev --do-it
```

### Rolling Back Changes

* tags
* --rollback

### Tagging

In case an SR needs to be rolled back, every script must have a `<tagDatabase>` command in its own changeset as the first item which will be run.  The changesets should be of the form:

```
    <changeSet id="PRE-KFS-18130" author="kellerj">
    	<tagDatabase tag="PRE-KFS-18130"/>
    </changeSet>
```

Additionally, this tagging needs to be done in *each* schema to which you are migrating.  Every schema we use liquibase against has its own `DATABASECHANGELOG` table.

The convention with these changesets is to name them like the above, using the JIRA number as part of the changeset and tag names.

### passwords.groovy

Not needed for dev environments.