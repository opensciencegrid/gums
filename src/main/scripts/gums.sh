#!/bin/bash
# Wrapper script to run GUMS Java CLI application. Works with both
# standard VDT layout and standard RPM layout. 
#
# for RPM:
# BIN=/usr/bin
# ETC=/etc/gums 
# LIB=/usr/lib/gums
# ENDORSED=$LIB/endorsed
# 
# for Tarball/VDT:
# GUMSDIR=$VDT_LOCATION/gums
# BIN=$GUMSDIR/bin
# ETC=$GUMSDIR/etc 
# LIB=$GUMSDIR/lib
# ENDORSED=$LIB/endorsed
# 
#
DEBUG=1

#
# RPM Defaults
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

#
# Check for VDT, adjust bases as necessary
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

LIBCLASSPATH=`ls -1 $LIB/*.jar | tr "\n" ":"`
GUMSCP=$LIBCLASSPATH$ETC


if [ $DEBUG -eq 1 ] ; then 
	echo "Program is $PROG"
	echo "Bin dir is $BINDIR"
	echo "Binary directory is $BIN"
	echo "Config dir is $ETC"
	echo "Library dir is $LIB"
	echo "Cert dir is $CERTDIR"
	echo "Full classpath is $GUMSCP"
fi

SECURITY_OPTS="-Daxis.socketSecureFactory=org.glite.security.trustmanager.axis.AXISSocketFactory \
-DsslCAFiles=$CERTDIR/*.0 \
-DgridProxyFile=$PROXY" 


# For gums-service command
#-DsslCertfile=$USER_CERT \
#-DsslKey=$USER_KEY \


java -Djava.endorsed.dirs=$ENDORSED $SECURITY_OPTS -cp $GUMSCP gov.bnl.gums.admin.AdminCommandLine "$@"

