/**
 * GUMSAPIService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC2 Nov 16, 2004 (12:19:44 EST) WSDL2Java emitter.
 */

package gov.bnl.gums.admin;

public interface GUMSAPIService extends javax.xml.rpc.Service {
    public java.lang.String getadminAddress();

    public gov.bnl.gums.admin.GUMSAPI getadmin() throws javax.xml.rpc.ServiceException;

    public gov.bnl.gums.admin.GUMSAPI getadmin(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
}
