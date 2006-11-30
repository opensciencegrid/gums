/**
 * GUMSAPI.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC2 Nov 16, 2004 (12:19:44 EST) WSDL2Java emitter.
 */

package gov.bnl.gums.admin;

public interface GUMSAPI extends java.rmi.Remote {
    public void setConfiguration(gov.bnl.gums.configuration.Configuration configuration) throws java.rmi.RemoteException;
    public gov.bnl.gums.configuration.Configuration getConfiguration() throws java.rmi.RemoteException;
    public java.lang.String generateGrid3UserVoMap(java.lang.String hostname) throws java.rmi.RemoteException;
    public java.lang.String generateGridMapfile(java.lang.String hostname) throws java.rmi.RemoteException;
    public void manualGroupAdd(java.lang.String persistanceManager, java.lang.String group, java.lang.String userDN) throws java.rmi.RemoteException;
    public void manualGroupRemove(java.lang.String persistanceManager, java.lang.String group, java.lang.String userDN) throws java.rmi.RemoteException;
    public void manualMappingAdd(java.lang.String persistanceManager, java.lang.String group, java.lang.String userDN, java.lang.String account) throws java.rmi.RemoteException;
    public void manualMappingRemove(java.lang.String persistanceManager, java.lang.String group, java.lang.String userDN) throws java.rmi.RemoteException;
    public void poolAddAccount(java.lang.String persistanceManager, java.lang.String group, java.lang.String username) throws java.rmi.RemoteException;
    public java.lang.String mapUser(java.lang.String hostname, java.lang.String userDN, java.lang.String fqan) throws java.rmi.RemoteException;
    public void mapfileCacheRefresh() throws java.rmi.RemoteException;
    public void updateGroups() throws java.rmi.RemoteException;
}
