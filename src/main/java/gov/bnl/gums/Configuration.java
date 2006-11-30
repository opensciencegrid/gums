/**
 * Configuration.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC2 Nov 16, 2004 (12:19:44 EST) WSDL2Java emitter.
 */

package gov.bnl.gums;

public class Configuration  implements java.io.Serializable {
    private java.lang.Object adminGroup;
    private java.lang.Object[] allGroups;
    private boolean errorOnMissedMapping;
    private java.util.HashMap groupMapping;
    private java.lang.Object[] hostGroup;
    private gov.bnl.gums.KeyStore keyStore;
    private java.util.HashMap persistenceFactories;

    public Configuration() {
    }

    public Configuration(
           java.lang.Object adminGroup,
           java.lang.Object[] allGroups,
           boolean errorOnMissedMapping,
           java.util.HashMap groupMapping,
           java.lang.Object[] hostGroup,
           gov.bnl.gums.KeyStore keyStore,
           java.util.HashMap persistenceFactories) {
           this.adminGroup = adminGroup;
           this.allGroups = allGroups;
           this.errorOnMissedMapping = errorOnMissedMapping;
           this.groupMapping = groupMapping;
           this.hostGroup = hostGroup;
           this.keyStore = keyStore;
           this.persistenceFactories = persistenceFactories;
    }


    /**
     * Gets the adminGroup value for this Configuration.
     * 
     * @return adminGroup
     */
    public java.lang.Object getAdminGroup() {
        return adminGroup;
    }


    /**
     * Sets the adminGroup value for this Configuration.
     * 
     * @param adminGroup
     */
    public void setAdminGroup(java.lang.Object adminGroup) {
        this.adminGroup = adminGroup;
    }


    /**
     * Gets the allGroups value for this Configuration.
     * 
     * @return allGroups
     */
    public java.lang.Object[] getAllGroups() {
        return allGroups;
    }


    /**
     * Sets the allGroups value for this Configuration.
     * 
     * @param allGroups
     */
    public void setAllGroups(java.lang.Object[] allGroups) {
        this.allGroups = allGroups;
    }


    /**
     * Gets the errorOnMissedMapping value for this Configuration.
     * 
     * @return errorOnMissedMapping
     */
    public boolean isErrorOnMissedMapping() {
        return errorOnMissedMapping;
    }


    /**
     * Sets the errorOnMissedMapping value for this Configuration.
     * 
     * @param errorOnMissedMapping
     */
    public void setErrorOnMissedMapping(boolean errorOnMissedMapping) {
        this.errorOnMissedMapping = errorOnMissedMapping;
    }


    /**
     * Gets the groupMapping value for this Configuration.
     * 
     * @return groupMapping
     */
    public java.util.HashMap getGroupMapping() {
        return groupMapping;
    }


    /**
     * Sets the groupMapping value for this Configuration.
     * 
     * @param groupMapping
     */
    public void setGroupMapping(java.util.HashMap groupMapping) {
        this.groupMapping = groupMapping;
    }


    /**
     * Gets the hostGroup value for this Configuration.
     * 
     * @return hostGroup
     */
    public java.lang.Object[] getHostGroup() {
        return hostGroup;
    }


    /**
     * Sets the hostGroup value for this Configuration.
     * 
     * @param hostGroup
     */
    public void setHostGroup(java.lang.Object[] hostGroup) {
        this.hostGroup = hostGroup;
    }


    /**
     * Gets the keyStore value for this Configuration.
     * 
     * @return keyStore
     */
    public gov.bnl.gums.KeyStore getKeyStore() {
        return keyStore;
    }


    /**
     * Sets the keyStore value for this Configuration.
     * 
     * @param keyStore
     */
    public void setKeyStore(gov.bnl.gums.KeyStore keyStore) {
        this.keyStore = keyStore;
    }


    /**
     * Gets the persistenceFactories value for this Configuration.
     * 
     * @return persistenceFactories
     */
    public java.util.HashMap getPersistenceFactories() {
        return persistenceFactories;
    }


    /**
     * Sets the persistenceFactories value for this Configuration.
     * 
     * @param persistenceFactories
     */
    public void setPersistenceManagers(java.util.HashMap persistenceFactories) {
        this.persistenceFactories = persistenceFactories;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof Configuration)) return false;
        Configuration other = (Configuration) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.adminGroup==null && other.getAdminGroup()==null) || 
             (this.adminGroup!=null &&
              this.adminGroup.equals(other.getAdminGroup()))) &&
            ((this.allGroups==null && other.getAllGroups()==null) || 
             (this.allGroups!=null &&
              java.util.Arrays.equals(this.allGroups, other.getAllGroups()))) &&
            this.errorOnMissedMapping == other.isErrorOnMissedMapping() &&
            ((this.groupMapping==null && other.getGroupMapping()==null) || 
             (this.groupMapping!=null &&
              this.groupMapping.equals(other.getGroupMapping()))) &&
            ((this.hostGroup==null && other.getHostGroup()==null) || 
             (this.hostGroup!=null &&
              java.util.Arrays.equals(this.hostGroup, other.getHostGroup()))) &&
            ((this.keyStore==null && other.getKeyStore()==null) || 
             (this.keyStore!=null &&
              this.keyStore.equals(other.getKeyStore()))) &&
            ((this.persistenceFactories==null && other.getPersistenceFactories()==null) || 
             (this.persistenceFactories!=null &&
              this.persistenceFactories.equals(other.getPersistenceFactories())));
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getAdminGroup() != null) {
            _hashCode += getAdminGroup().hashCode();
        }
        if (getAllGroups() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getAllGroups());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getAllGroups(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        _hashCode += (isErrorOnMissedMapping() ? Boolean.TRUE : Boolean.FALSE).hashCode();
        if (getGroupMapping() != null) {
            _hashCode += getGroupMapping().hashCode();
        }
        if (getHostGroup() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getHostGroup());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getHostGroup(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getKeyStore() != null) {
            _hashCode += getKeyStore().hashCode();
        }
        if (getPersistenceFactories() != null) {
            _hashCode += getPersistenceFactories().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(Configuration.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://gums.bnl.gov", "Configuration"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("adminGroup");
        elemField.setXmlName(new javax.xml.namespace.QName("", "adminGroup"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "anyType"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("allGroups");
        elemField.setXmlName(new javax.xml.namespace.QName("", "allGroups"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "anyType"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("errorOnMissedMapping");
        elemField.setXmlName(new javax.xml.namespace.QName("", "errorOnMissedMapping"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("groupMapping");
        elemField.setXmlName(new javax.xml.namespace.QName("", "groupMapping"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://xml.apache.org/xml-soap", "Map"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("hostGroup");
        elemField.setXmlName(new javax.xml.namespace.QName("", "hostGroup"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "anyType"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("keyStore");
        elemField.setXmlName(new javax.xml.namespace.QName("", "keyStore"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://gums.bnl.gov", "KeyStore"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("persistenceFactories");
        elemField.setXmlName(new javax.xml.namespace.QName("", "persistenceFactories"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://xml.apache.org/xml-soap", "Map"));
        typeDesc.addFieldDesc(elemField);
    }

    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

    /**
     * Get Custom Serializer
     */
    public static org.apache.axis.encoding.Serializer getSerializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanSerializer(
            _javaType, _xmlType, typeDesc);
    }

    /**
     * Get Custom Deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanDeserializer(
            _javaType, _xmlType, typeDesc);
    }

}
