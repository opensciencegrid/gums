/**
 * GUMSAPI.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package gov.bnl.gums.admin;

public interface GUMSAPI extends java.rmi.Remote {
    public java.lang.String getVersion() throws java.rmi.RemoteException;
    public void setConfiguration(gov.bnl.gums.configuration.Configuration configuration) throws java.rmi.RemoteException;
    public gov.bnl.gums.configuration.Configuration getConfiguration() throws java.rmi.RemoteException;
    public void addAccountRange2(java.lang.String accountPoolMapperName, java.lang.String range) throws java.rmi.RemoteException;
    public void backupConfiguration(java.lang.String name) throws java.rmi.RemoteException;
    public void deleteBackupConfiguration(java.lang.String name) throws java.rmi.RemoteException;
    public java.lang.String generateEmailMapfile(java.lang.String hostname) throws java.rmi.RemoteException;
    public java.lang.String generateFqanMapfile(java.lang.String hostname) throws java.rmi.RemoteException;
    public java.lang.String generateGrid3UserVoMap(java.lang.String hostname) throws java.rmi.RemoteException;
    public java.lang.String generateGridMapfile(java.lang.String hostname) throws java.rmi.RemoteException;
    public java.lang.String generateOsgUserVoMap(java.lang.String hostname) throws java.rmi.RemoteException;
    public java.lang.String generateVoGridMapfile(java.lang.String hostname) throws java.rmi.RemoteException;
    public java.lang.Object[] getBackupNames() throws java.rmi.RemoteException;
    public java.lang.String getPoolAccountAssignments(java.lang.String accountPoolMapperName) throws java.rmi.RemoteException;
    public void manualGroupAdd2(java.lang.String manualUserGroupName, java.lang.String userDN) throws java.rmi.RemoteException;
    public void manualGroupAdd3(java.lang.String manualUserGroupName, java.lang.String userDN, java.lang.String fqan, java.lang.String email) throws java.rmi.RemoteException;
    public void manualGroupRemove2(java.lang.String manualUserGroupName, java.lang.String userDN) throws java.rmi.RemoteException;
    public void manualGroupRemove3(java.lang.String manualUserGroupName, java.lang.String userDN, java.lang.String fqan) throws java.rmi.RemoteException;
    public void manualMappingAdd2(java.lang.String manualAccountMapperName, java.lang.String userDN, java.lang.String account) throws java.rmi.RemoteException;
    public void manualMappingRemove2(java.lang.String manualAccountMapperName, java.lang.String userDN) throws java.rmi.RemoteException;
    public java.lang.String mapAccount(java.lang.String accountName) throws java.rmi.RemoteException;
    public java.lang.String mapUser(java.lang.String hostname, java.lang.String userDN, java.lang.String fqan) throws java.rmi.RemoteException;
    public void mergeConfiguration(gov.bnl.gums.configuration.Configuration conf, java.lang.String newConfUri, java.lang.String persistenceFactory, java.lang.String hostToGroupMapping) throws java.rmi.RemoteException;
    public void removeAccountRange(java.lang.String accountPoolMapperName, java.lang.String range) throws java.rmi.RemoteException;
    public void restoreConfiguration(java.lang.String name) throws java.rmi.RemoteException;
    public void unassignAccountRange(java.lang.String accountPoolMapperName, java.lang.String range) throws java.rmi.RemoteException;
    public void updateGroups() throws java.rmi.RemoteException;
    public java.lang.String getCurrentDn() throws java.rmi.RemoteException;
    public void manualGroupAdd(java.lang.String persistanceFactory, java.lang.String group, java.lang.String userDN) throws java.rmi.RemoteException;
    public void manualGroupRemove(java.lang.String persistanceFactory, java.lang.String group, java.lang.String userDN) throws java.rmi.RemoteException;
    public void manualMappingAdd(java.lang.String persistanceFactory, java.lang.String group, java.lang.String userDN, java.lang.String account) throws java.rmi.RemoteException;
    public void manualMappingRemove(java.lang.String persistanceFactory, java.lang.String group, java.lang.String userDN) throws java.rmi.RemoteException;
    public void poolAddAccount(java.lang.String persistanceFactory, java.lang.String group, java.lang.String accountName) throws java.rmi.RemoteException;
}
