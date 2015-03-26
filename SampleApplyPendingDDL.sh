
PARAMS=

# Accept -D parameters as the first ones.  Peel them off and insert them before the call to the script
while [[ $1 == -D* ]]; do
	PARAMS="$PARAMS $1"
	shift
done

CLASSPATH=
for i in `ls pending-ddl-tool/lib/*.jar`; do
	CLASSPATH="${CLASSPATH}:${i}"
done

groovy -cp "$CLASSPATH" -Dapply.pending.config=sample-config.groovy $PARAMS pending-ddl-tool/ApplyPendingDDL.groovy "$@"
