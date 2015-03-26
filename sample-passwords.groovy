
environments {
	cnv {
		database {
			tp {
				passwords = [ "kfs" : "NOT_KFS"
						  , "kfs_user" : "NOT_KFS_USER"
						  , "fp" : "NOT_FP"
						  , "dsload" : "NOT_DSLOAD"
						  ]
			}
			gl {
				passwords = [ "kfs" : "NOT_KFS"
						  , "kfs_user" : "NOT_KFS_USER"
						  , "fp" : "NOT_FP"
						  , "dsload" : "NOT_DSLOAD"
						  ]
			}
			rice {
				passwords = [ "rice" : "NOT_RICE"
						  ]
			}
		}
	}
	tst {
		database {
			tp {
				passwords = [ "kfs" : "NOT_KFS"
						  , "kfs_user" : "NOT_KFS_USER"
						  , "fp" : "NOT_FP"
						  , "dsload" : "NOT_DSLOAD"
						  ]
			}
			gl {
				passwords = [ "kfs" : "NOT_KFS"
						  , "kfs_user" : "NOT_KFS_USER"
						  , "fp" : "NOT_FP"
						  , "dsload" : "NOT_DSLOAD"
						  ]
			}
			rice {
				passwords = [ "rice" : "NOT_RICE"
						  ]
			}
		}
	}
	stg {
		database {
			tp {
				passwords = [ "kfs" : "NOT_KFS"
						  , "kfs_user" : "NOT_KFS_USER"
						  , "fp" : "NOT_FP"
						  , "dsload" : "NOT_DSLOAD"
						  ]
			}
			gl {
				passwords = [ "kfs" : "NOT_KFS"
						  , "kfs_user" : "NOT_KFS_USER"
						  , "fp" : "NOT_FP"
						  , "dsload" : "NOT_DSLOAD"
						  ]
			}
				passwords = [ "rice" : "NOT_RICE"
						  ]
		}
	}
	prd {
		database {
			tp {
				passwords = [ "kfs" : "NOT_KFS"
						  , "kfs_user" : "NOT_KFS_USER"
						  , "fp" : "NOT_FP"
						  , "dsload" : "NOT_DSLOAD"
						  ]
			}
			gl {
				passwords = [ "kfs" : "NOT_KFS"
						  , "kfs_user" : "NOT_KFS_USER"
						  , "fp" : "NOT_FP"
						  , "dsload" : "NOT_DSLOAD"
						  ]
			}
			rice {
				passwords = [ "rice" : "NOT_RICE"
						  ]
			}
		}
	}
}