/**
 * GUMSAPIServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package gov.bnl.gums.admin;

public class GUMSAPIServiceLocator extends org.apache.axis.client.Service implements gov.bnl.gums.admin.GUMSAPIService {

    public GUMSAPIServiceLocator() {
    }


    public GUMSAPIServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public GUMSAPIServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for admin
    private java.lang.String admin_address = "http://localhost:8080/gums/admin";

    public java.lang.String getadminAddress() {
        return admin_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String adminWSDDServiceName = "admin";

    public java.lang.String getadminWSDDServiceName() {
        return adminWSDDServiceName;
    }

    public void setadminWSDDServiceName(java.lang.String name) {
        adminWSDDServiceName = name;
    }

    public gov.bnl.gums.admin.GUMSAPI getadmin() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(admin_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getadmin(endpoint);
    }

    public gov.bnl.gums.admin.GUMSAPI getadmin(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            gov.bnl.gums.admin.AdminSoapBindingStub _stub = new gov.bnl.gums.admin.AdminSoapBindingStub(portAddress, this);
            _stub.setPortName(getadminWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setadminEndpointAddress(java.lang.String address) {
        admin_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (gov.bnl.gums.admin.GUMSAPI.class.isAssignableFrom(serviceEndpointInterface)) {
                gov.bnl.gums.admin.AdminSoapBindingStub _stub = new gov.bnl.gums.admin.AdminSoapBindingStub(new java.net.URL(admin_address), this);
                _stub.setPortName(getadminWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("admin".equals(inputPortName)) {
            return getadmin();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://www.bnl.gov/namespaces/2004/09/gums/GUMSService", "GUMSAPIService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://www.bnl.gov/namespaces/2004/09/gums/GUMSService", "admin"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("admin".equals(portName)) {
            setadminEndpointAddress(address);
        }
        else 
{ // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}
