// these are the database instance schema pairs which will be imported by this tool
// and will be processed in the order shown
// These values are used as keys into the maps created by the configuration below
instanceSchemaPairs = [
	  [ "tp", "users" ]
	, [ "gl", "users" ]
	, [ "ds", "users" ]
	, [ "tp", "kfs" ]
	, [ "tp", "fp" ]
	, [ "gl", "kfs" ]
	, [ "gl", "fp" ]
	, [ "tp", "synonyms" ]
	, [ "gl", "synonyms" ]
	, [ "rice", "rice" ]
	, [ "ds", "finance" ]
	, [ "ds", "dsload" ]
	, [ "ds", "webuser" ]
	]

database {
	// These should be the "real" account names (but development passwords)
	// Where the account names need to be modified (when instances are co-hosted in a single DB)
	// those will be overridden in the "environments" variable below.
	tp {
		url = "jdbc:oracle:thin:@localhost:1521:ORCL"
		schemas = [ "kfs" : "KFS"
				  , "synonyms" : "LBQ_SYNONYM_ADMIN"
				  , "fp" : "FP"
				  , "dsload" : "DSLOAD"
				  , "users" : "LBQ_USER_ADMIN"
				  ]
		passwords = [ "kfs" : "KFS"
				  , "synonyms" : "LBQ_SYNONYM_ADMIN"
				  , "fp" : "FP"
				  , "dsload" : "DSLOAD"
				  , "users" : "LBQ_USER_ADMIN"
				  ]
	}
	gl {
		url = "jdbc:oracle:thin:@localhost:1521:ORCL"
		schemas = [ "kfs" : "KFS"
				  , "synonyms" : "LBQ_SYNONYM_ADMIN"
				  , "fp" : "FP"
				  , "dsload" : "DSLOAD"
				  , "users" : "LBQ_USER_ADMIN"
				  ]
		passwords = [ "kfs" : "KFS"
				  , "synonyms" : "LBQ_SYNONYM_ADMIN"
				  , "fp" : "FP"
				  , "dsload" : "DSLOAD"
				  , "users" : "LBQ_USER_ADMIN"
				  ]
	}
	ds {
		url = "jdbc:oracle:thin:@localhost:1521:ORCL"
		schemas = [ "webuser" : "WEBUSER"
				  , "finance" : "FINANCE"
				  , "dsload" : "DSLOAD"
				  , "users" : "LBQ_USER_ADMIN"
				  ]
		passwords = [ "webuser" : "WEBUSER"
				  , "finance" : "FINANCE"
				  , "dsload" : "DSLOAD"
				  , "users" : "LBQ_USER_ADMIN"
				  ]
	}
	rice {
		url = "jdbc:oracle:thin:@localhost:1521:ORCL"
		schemas = [ "rice" : "RICE"
				  ]
		passwords = [ "rice" : "RICE"
				  ]
	}
}

dblinks {
	tp_kfs_dblink_name = "@TP_KFS"
	gl_kfs_dblink_name = "@GL_KFS"
}

schemas {
	gl_kfs_schema_name = "KFS"
	tp_kfs_schema_name = "KFS"
}

locations {
	kfs = "../../uc-kfs-uc"
}
// This variable overrides those above when the environment passed in matches
environments {
	dev {
		dblinks {
			tp_kfs_dblink_name = ""
			gl_kfs_dblink_name = ""
		}
		schemas {
			gl_kfs_schema_name = "GL_KFS"
		}
		database {
			tp {
				schemas = [ "kfs" : "KFS"
						  , "synonyms" : "LBQ_SYNONYM_ADMIN"
						  , "fp" : "FP"
						  , "dsload" : "TP_DSLOAD"
						  , "users" : "LBQ_USER_ADMIN"
						  ]
				passwords = [ "kfs" : "KFS"
						  , "synonyms" : "LBQ_SYNONYM_ADMIN"
						  , "fp" : "FP"
						  , "dsload" : "TP_DSLOAD"
						  , "users" : "LBQ_USER_ADMIN"
						  ]
			}
			gl {
				schemas = [ "kfs" : "GL_KFS"
						  , "synonyms" : "LBQ_SYNONYM_ADMIN"
						  , "fp" : "GL_FP"
						  , "dsload" : "GL_DSLOAD"
						  , "users" : "LBQ_USER_ADMIN"
						  ]
				passwords = [ "kfs" : "GL_KFS"
						  , "synonyms" : "LBQ_SYNONYM_ADMIN"
						  , "fp" : "GL_FP"
						  , "dsload" : "GL_DSLOAD"
						  , "users" : "LBQ_USER_ADMIN"
						  ]
			}
		}
	}
	reg {
		database {
			tp {
				schemas = [ "kfs" : "KFS"
						  , "synonyms" : "LBQ_SYNONYM_ADMIN"
						  , "fp" : "FP"
						  , "dsload" : "TP_DSLOAD"
						  ]
				passwords = [ "kfs" : "KFS"
						  , "synonyms" : "LBQ_SYNONYM_ADMIN"
						  , "fp" : "FP"
						  , "dsload" : "TP_DSLOAD"
						  , "users" : "LBQ_USER_ADMIN"
						  ]
			}
			gl {
				schemas = [ "kfs" : "GL_KFS"
						  , "synonyms" : "LBQ_SYNONYM_ADMIN"
						  , "fp" : "GL_FP"
						  , "dsload" : "DSLOAD"
						  ]
				passwords = [ "kfs" : "GL_KFS"
						  , "synonyms" : "LBQ_SYNONYM_ADMIN"
						  , "fp" : "GL_FP"
						  , "dsload" : "DSLOAD"
						  ]
			}
		}
		locations {
			kfs = "../../kfs/"
		}
	}
	cnv {
		database {
			tp {
				url = "jdbc:oracle:thin:localhost:1521:FIS_TP_DEVEL"
			}
			gl {
				url = "jdbc:oracle:thin:localhost:1521:FIS_GL_DEVEL"
			}
			rice {
				url = "jdbc:oracle:thin:localhost:1521:FIS_TP_DEVEL"
			}
		}
	}
	tst {
		database {
			tp {
				url = "jdbc:oracle:thin:localhost:1521:FIS_TP_TEST"
			}
			gl {
				url = "jdbc:oracle:thin:localhost:1521:FIS_GL_TEST"
			}
			rice {
				url = "jdbc:oracle:thin:localhost:1521:RICE_TEST"
				schemas = [ "rice" : "KFS_KRTST"
						  ]
			}
		}
	}
	stg {
		database {
			tp {
				url = "jdbc:oracle:thin:localhost:1521:FIS_TP_STAGE"
			}
			gl {
				url = "jdbc:oracle:thin:localhost:1521:FIS_GL_STAGE"
			}
			rice {
				url = "jdbc:oracle:thin:localhost:1521:RICE_TEST"
				schemas = [ "rice" : "KFS_KRSTG"
				]
			}
		}
	}
	prd {
		database {
			tp {
				url = "jdbc:oracle:thin:localhost:1521:FIS_TP_PROD"
			}
			gl {
				url = "jdbc:oracle:thin:localhost:1521:FIS_GL_PROD"
			}
			rice {
				url = "jdbc:oracle:thin:localhost:1521:RICE"
				schemas = [ "rice" : "KFS_KRPRD"
				]
			}
		}
	}
}
