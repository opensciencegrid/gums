#!/bin/bash
# Script to run GUMS Java CLI application. Works with both
# standard VDT layout and standard RPM layout. Made to be wrapped by 
# simple gums-* wrapper scripts that call this script with following args:
#
# --client (or default with no argument)
#    AdminCommandLine, user proxy 
#
# --service
#    AdminCommandLine, hostcert + key
#
# --host
#    HostCommandLine, hostcert + key
#
# --nagios
#    HostCommandLine, hostcert + key, + nagios probe functionality
#
# Author: John R. Hover <jhover@bnl.gov> 
# based on initial gums script by Gabriele Carcassi. 
#

#
# Defaults
#
DEBUG=1
MODE="CLIENT"

#
# Differentiate script args, collect other arguments to pass to Java CLI...
#
ARGS=""
for opt in $@ ; do
	if [ $DEBUG -eq 1 ] ; then 
		echo "[DEBUG]: Opt is $opt"
	fi
	case $opt in
		--host)
		MODE="HOST"
		;;

		--service)
		MODE="SERVICE"
		;;

		--nagios)
		MODE="NAGIOS"
		;;

		--client)
		MODE="CLIENT"
		;;

		*)
		ARGS="$ARGS $opt"
	esac
done

#
# Global Defaults
#
BIN=/usr/bin
ETC=/etc/gums
LIB=/usr/lib/gums
ENDORSED=$LIB/endorsed
PROG=$(basename $0)
BINDIR=$(dirname $0)
# defaults for the CA Certs
CERTDIR=${X509_CERT_DIR-/etc/grid-security/certificates}
# Calculate user proxy filename
CURRENT_UID=`id -u`
PROXY=${X509_PROXY_FILE-/tmp/x509up_u$CURRENT_UID}
TRUST_OPTS="-Daxis.socketSecureFactory=org.glite.security.trustmanager.axis.AXISSocketFactory" 
HOST_CERT=${X509_HOST_CERT-/etc/grid-security/hostcert.pem}
HOST_KEY=${X509_HOST_KEY-/etc/grid-security/hostkey.pem}
USER_CERT=${X509_USER_CERT-$HOME/.globus/usercert.pem}
USER_KEY=${X509_USER_CERT-$HOME/.globus/userkey.pem}

#
# Check for tarball install, sanity check, and adjust paths as necessary
#
INVOKE=$0
BASE=`basename $INVOKE`
cd $BASE
INVOKE_BIN=$PWD
cd -
GUMSDIR=`basename $INVOKE_BIN`

if [ -d $GUMSDIR/scripts ]; then
	BIN=$GUMSDIR/scripts
fi

if [ -d $GUMSDIR/config ]; then
	ETC=$GUMSDIR/config
fi

if [ -d $GUMSDIR/lib ]; then
	LIB=$GUMSDIR/lib ]
fi

if [ -d $LIB/endorsed ]; then
	ENDORSED=$LIB/endorsed
fi







#
# Check for VDT, adjust base paths as necessary
#
if [ -n "$VDT_LOCATION" ]; then
	if [ $DEBUG -eq 1 ] ; then
		echo "Setting paths using VDT_LOCATION..."
	fi
	BIN=$VDT_LOCATION/gums/bin/
	ETC=$VDT_LOCATION/gums/etc/
	LIB=$VDT_LOCATION/gums/lib/
	ENDORSED=$LIB/endorsed
	
fi

#
# Construct GUMS classpath
#
LIBCLASSPATH=`ls -1 $LIB/*.jar | tr "\n" ":"`
GUMSCP=$LIBCLASSPATH$ETC

#
# Set command line class and cert handling by mode
#
if [ "$MODE" = "CLIENT" ]; then
	MAINCLASS=gov.bnl.gums.admin.AdminCommandLine
	CERT_OPTS="-DsslCAFiles=$CERTDIR/*.0 -DgridProxyFile=$PROXY" 	

elif [ "$MODE" = "HOST" ]; then
	MAINCLASS=gov.bnl.gums.admin.HostCommandLine
	CERT_OPTS="-DsslCertfile=$HOST_CERT -DsslKey=$HOST_KEY" 	

elif [ "$MODE" = "NAGIOS" ]; then
	MAINCLASS=gov.bnl.gums.admin.HostCommandLine
	CERT_OPTS="-DsslCertfile=$HOST_CERT -DsslKey=$HOST_KEY"

elif [ "$MODE" = "SERVICE" ]; then
	MAINCLASS=gov.bnl.gums.admin.AdminCommandLine
	CERT_OPTS="-DsslCertfile=$USER_CERT -DsslKey=$USER_KEY"
else
	echo "ERROR: Unknown mode $MODE. Exitting."
	exit 1
fi	

SECURITY_OPTS="$TRUST_OPTS $CERT_OPTS"

if [ $DEBUG -eq 1 ] ; then 
	echo "[DEBUG]: Program is $PROG"
	echo "[DEBUG]: Program dir is $BINDIR"
	echo "[DEBUG]: Binary dir is $BIN"
	echo "[DEBUG]: Config dir is $ETC"
	echo "[DEBUG]: Library dir is $LIB"
	echo "[DEBUG]: Endorsed lib dir is $ENDORSED"
	echo "[DEBUG]: Cert dir is $CERTDIR"
	echo "[DEBUG]: Full classpath is: $GUMSCP"
	echo "[DEBUG]: Security opts are: $SECURITY_OPTS"
	echo "[DEBUG]: Pass-through args are $ARGS"
	echo "[DEBUG]: Mode is $MODE"
fi

#
# Do  basic validation of variables
#


#
# Execute correct main class with proper arguments depending on mode.
#
if [ "$MODE" != "NAGIOS" ] ; then
	java -Djava.endorsed.dirs=$ENDORSED $SECURITY_OPTS -cp $GUMSCP $MAINCLASS $ARGS
else
#
# Perform nagios probe...
#
	if [ -f $ETC/gums-nagios.conf ]; then
		exec 0< $ETC/gums-nagios.conf
		STATE=0
		SUCCESS_COUNT=0
		TOTAL_COUNT=0
		while read LINE
			do
				LINE=${LINE## }
				if [ "${LINE:0:1}" != "#" ] && [ "$LINE" != "" ]; then
					if [ $STATE -eq 0 ] ;	then
						USERDN=$LINE
					fi
					if [ $STATE -eq 1 ] ; then
						ACCOUNT=$LINE
					fi
					let STATE=$STATE+1
					if [ $STATE -eq 2 ] ; then
						ANSWER=`"$BINDIR/gums-host.sh mapUser -b "` 
						if [ $ANSWER == "$ACCOUNT" ] ; then
							let SUCCESS_COUNT=$SUCCESS_COUNT+1 
						fi
						let TOTAL_COUNT=$TOTAL_COUNT+1;
						STATE=0
					fi
				fi
			done
	
		if [ $SUCCESS_COUNT -eq $TOTAL_COUNT ] ; then
			echo All GUMS mappings succeeded
			exit 0
		fi
		if [ $SUCCESS_COUNT -gt 0 ] ; then
			echo Some GUMS mappings did not succeed 
			exit 1
		fi
		echo No GUMS mappings succeeded 
		exit 2	
	else
		echo "Nagios config file $ETC/gums-nagios.conf doesn't exist."
		exit 3
	fi
fi
