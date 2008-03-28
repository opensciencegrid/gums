# CVS: $Id: gums-client.spec,v 1.3 2005/04/07 15:03:08 carcassi Exp $

%define	name		gums-client
%define	version		@VERSION@
%define	release		@RELEASE@
%define pre             /opt
%define fix             gums
%define	prefix		%{pre}/%{fix}

Name: %name
Version: %version
Release: %release
Prefix: %{prefix}
Summary: Grid User Management System
URL: http://grid.racf.bnl.gov/GUMS/
Source: %{name}-%{version}-latest.tar.gz
License: GNU Public License (GPL)
Group: Grid/Security
Packager: Gabriele Carcassi <carcassi@bnl.gov>
BuildArchitectures: noarch
BuildRoot: %{_tmppath}/%{name}-root
Obsoletes: gums-host, gums-admin
#Requires: java >= 1.4.2

%description
The Grid User Management System is a series of tools that allow a centrilized,
site-wide, policy-based mapping of Grid identities to user accounts.

%prep
%setup -c

%build

%install
rm -rf $RPM_BUILD_ROOT

# Point the rpm build root to the directory where the setup macro unpacked the tarball:
mkdir -p $RPM_BUILD_ROOT/%{pre}
ln -s $RPM_BUILD_DIR/$RPM_PACKAGE_NAME-$RPM_PACKAGE_VERSION/%{fix} $RPM_BUILD_ROOT/%{prefix}

# Generate the gpt file list:
cd $RPM_BUILD_ROOT
find .%{prefix}/* -type d | sed 's,^\.,\%dir \%attr(-\,root\,root) ,' > $RPM_BUILD_DIR/file.list.%{name}
find .%{prefix}/bin -type f -o -type l | sed 's,^\.,\%attr(555\,root\,root) ,' >> $RPM_BUILD_DIR/file.list.%{name}
find .%{prefix}/lib -type f -o -type l | sed 's,^\.,\%attr(444\,root\,root) ,' >> $RPM_BUILD_DIR/file.list.%{name}
find .%{prefix}/log -type d -o -type l | sed 's,^\.,\%attr(1777\,root\,root) ,' >> $RPM_BUILD_DIR/file.list.%{name}
#XXX for now we just keep whatever is there. The example should be overwritten
find .%{prefix}/etc -type f -o -type l | sed 's,^\.,\%config(noreplace) \%attr(644\,root\,root) ,' >> $RPM_BUILD_DIR/file.list.%{name}

%clean
rm -rf $RPM_BUILD_ROOT
rm -f $RPM_BUILD_DIR/file.list.%{name}

%files -f ../file.list.%{name}

# NOTE: For the scripts below, $1 with this value generally corresponds to:
#   0  ->  rpm -e  (Removing the package)
#   1  ->  rpm -i  (Installing the package)
#  >1  ->  rpm -U  (Upgrading the package)

%post
# Add the GPT environment setup commands to the system's shell config files:
#  - NOTE: putting this in the postinstall script keeps installed files out of /etc which
#          makes this package relocatable.
#  - NOTE: $RPM_INSTALL_PREFIX only gets defined for relocatable packages like I made this!
#if [ $1 = 1 ]
#then
#  ln -s $RPM_INSTALL_PREFIX/sbin/gums-cron-updateGroups /etc/cron.hourly
#  perl -pi -e 's/^(GUMSDIR).*/$1=$ENV{RPM_INSTALL_PREFIX}/' $RPM_INSTALL_PREFIX/sbin/gums-cron-updateGroups
#fi

%postun
#if [ $1 = 0 ]
#then 
#  rm -f /etc/cron.hourly/gums-cron-updateGroups
#fi

# Changelog entries must start with:  * Day Mon DD YYYY blah blah blah....
%changelog
* Wed Aug 04 2004 Gabriele Carcassi <carcassi@bnl.gov>
- See the changelog in CVS
