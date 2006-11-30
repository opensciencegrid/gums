/**
 * Configuration.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC2 Nov 16, 2004 (12:19:44 EST) WSDL2Java emitter.
 */

package gov.bnl.gums.configuration;

public class Configuration  implements java.io.Serializable {
    private java.util.HashMap VOs;
    private java.util.HashMap accountMappers;
    private boolean errorOnMissedMapping;
    private java.util.HashMap groupToAccountMappings;
    private java.lang.Object[] hostToGroupMappings;
    private java.lang.Object keyStore;
    private java.util.HashMap persistenceFactories;
    private java.lang.Object[] readerUserGroups;
    private java.util.HashMap userGroups;
    private java.lang.Object[] writerUserGroups;

    public Configuration() {
    }

    public Configuration(
           java.util.HashMap VOs,
           java.util.HashMap accountMappers,
           boolean errorOnMissedMapping,
           java.util.HashMap groupToAccountMappings,
           java.lang.Object[] hostToGroupMappings,
           java.lang.Object keyStore,
           java.util.HashMap persistenceFactories,
           java.lang.Object[] readerUserGroups,
           java.util.HashMap userGroups,
           java.lang.Object[] writerUserGroups) {
           this.VOs = VOs;
           this.accountMappers = accountMappers;
           this.errorOnMissedMapping = errorOnMissedMapping;
           this.groupToAccountMappings = groupToAccountMappings;
           this.hostToGroupMappings = hostToGroupMappings;
           this.keyStore = keyStore;
           this.persistenceFactories = persistenceFactories;
           this.readerUserGroups = readerUserGroups;
           this.userGroups = userGroups;
           this.writerUserGroups = writerUserGroups;
    }


    /**
     * Gets the VOs value for this Configuration.
     * 
     * @return VOs
     */
    public java.util.HashMap getVOs() {
        return VOs;
    }


    /**
     * Sets the VOs value for this Configuration.
     * 
     * @param VOs
     */
    public void setVOs(java.util.HashMap VOs) {
        this.VOs = VOs;
    }


    /**
     * Gets the accountMappers value for this Configuration.
     * 
     * @return accountMappers
     */
    public java.util.HashMap getAccountMappers() {
        return accountMappers;
    }


    /**
     * Sets the accountMappers value for this Configuration.
     * 
     * @param accountMappers
     */
    public void setAccountMappers(java.util.HashMap accountMappers) {
        this.accountMappers = accountMappers;
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
     * Gets the groupToAccountMappings value for this Configuration.
     * 
     * @return groupToAccountMappings
     */
    public java.util.HashMap getGroupToAccountMappings() {
        return groupToAccountMappings;
    }


    /**
     * Sets the groupToAccountMappings value for this Configuration.
     * 
     * @param groupToAccountMappings
     */
    public void setGroupToAccountMappings(java.util.HashMap groupToAccountMappings) {
        this.groupToAccountMappings = groupToAccountMappings;
    }


    /**
     * Gets the hostToGroupMappings value for this Configuration.
     * 
     * @return hostToGroupMappings
     */
    public java.lang.Object[] getHostToGroupMappings() {
        return hostToGroupMappings;
    }


    /**
     * Sets the hostToGroupMappings value for this Configuration.
     * 
     * @param hostToGroupMappings
     */
    public void setHostToGroupMappings(java.lang.Object[] hostToGroupMappings) {
        this.hostToGroupMappings = hostToGroupMappings;
    }


    /**
     * Gets the keyStore value for this Configuration.
     * 
     * @return keyStore
     */
    public java.lang.Object getKeyStore() {
        return keyStore;
    }


    /**
     * Sets the keyStore value for this Configuration.
     * 
     * @param keyStore
     */
    public void setKeyStore(java.lang.Object keyStore) {
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
    public void setPersistenceFactories(java.util.HashMap persistenceFactories) {
        this.persistenceFactories = persistenceFactories;
    }


    /**
     * Gets the readerUserGroups value for this Configuration.
     * 
     * @return readerUserGroups
     */
    public java.lang.Object[] getReaderUserGroups() {
        return readerUserGroups;
    }


    /**
     * Sets the readerUserGroups value for this Configuration.
     * 
     * @param readerUserGroups
     */
    public void setReaderUserGroups(java.lang.Object[] readerUserGroups) {
        this.readerUserGroups = readerUserGroups;
    }


    /**
     * Gets the userGroups value for this Configuration.
     * 
     * @return userGroups
     */
    public java.util.HashMap getUserGroups() {
        return userGroups;
    }


    /**
     * Sets the userGroups value for this Configuration.
     * 
     * @param userGroups
     */
    public void setUserGroups(java.util.HashMap userGroups) {
        this.userGroups = userGroups;
    }


    /**
     * Gets the writerUserGroups value for this Configuration.
     * 
     * @return writerUserGroups
     */
    public java.lang.Object[] getWriterUserGroups() {
        return writerUserGroups;
    }


    /**
     * Sets the writerUserGroups value for this Configuration.
     * 
     * @param writerUserGroups
     */
    public void setWriterUserGroups(java.lang.Object[] writerUserGroups) {
        this.writerUserGroups = writerUserGroups;
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
            ((this.VOs==null && other.getVOs()==null) || 
             (this.VOs!=null &&
              this.VOs.equals(other.getVOs()))) &&
            ((this.accountMappers==null && other.getAccountMappers()==null) || 
             (this.accountMappers!=null &&
              this.accountMappers.equals(other.getAccountMappers()))) &&
            this.errorOnMissedMapping == other.isErrorOnMissedMapping() &&
            ((this.groupToAccountMappings==null && other.getGroupToAccountMappings()==null) || 
             (this.groupToAccountMappings!=null &&
              this.groupToAccountMappings.equals(other.getGroupToAccountMappings()))) &&
            ((this.hostToGroupMappings==null && other.getHostToGroupMappings()==null) || 
             (this.hostToGroupMappings!=null &&
              java.util.Arrays.equals(this.hostToGroupMappings, other.getHostToGroupMappings()))) &&
            ((this.keyStore==null && other.getKeyStore()==null) || 
             (this.keyStore!=null &&
              this.keyStore.equals(other.getKeyStore()))) &&
            ((this.persistenceFactories==null && other.getPersistenceFactories()==null) || 
             (this.persistenceFactories!=null &&
              this.persistenceFactories.equals(other.getPersistenceFactories()))) &&
            ((this.readerUserGroups==null && other.getReaderUserGroups()==null) || 
             (this.readerUserGroups!=null &&
              java.util.Arrays.equals(this.readerUserGroups, other.getReaderUserGroups()))) &&
            ((this.userGroups==null && other.getUserGroups()==null) || 
             (this.userGroups!=null &&
              this.userGroups.equals(other.getUserGroups()))) &&
            ((this.writerUserGroups==null && other.getWriterUserGroups()==null) || 
             (this.writerUserGroups!=null &&
              java.util.Arrays.equals(this.writerUserGroups, other.getWriterUserGroups())));
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
        if (getVOs() != null) {
            _hashCode += getVOs().hashCode();
        }
        if (getAccountMappers() != null) {
            _hashCode += getAccountMappers().hashCode();
        }
        _hashCode += (isErrorOnMissedMapping() ? Boolean.TRUE : Boolean.FALSE).hashCode();
        if (getGroupToAccountMappings() != null) {
            _hashCode += getGroupToAccountMappings().hashCode();
        }
        if (getHostToGroupMappings() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getHostToGroupMappings());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getHostToGroupMappings(), i);
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
        if (getReaderUserGroups() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getReaderUserGroups());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getReaderUserGroups(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getUserGroups() != null) {
            _hashCode += getUserGroups().hashCode();
        }
        if (getWriterUserGroups() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getWriterUserGroups());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getWriterUserGroups(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(Configuration.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://configuration.gums.bnl.gov", "Configuration"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("VOs");
        elemField.setXmlName(new javax.xml.namespace.QName("", "VOs"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://xml.apache.org/xml-soap", "Map"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("accountMappers");
        elemField.setXmlName(new javax.xml.namespace.QName("", "accountMappers"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://xml.apache.org/xml-soap", "Map"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("errorOnMissedMapping");
        elemField.setXmlName(new javax.xml.namespace.QName("", "errorOnMissedMapping"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("groupToAccountMappings");
        elemField.setXmlName(new javax.xml.namespace.QName("", "groupToAccountMappings"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://xml.apache.org/xml-soap", "Map"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("hostToGroupMappings");
        elemField.setXmlName(new javax.xml.namespace.QName("", "hostToGroupMappings"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "anyType"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("keyStore");
        elemField.setXmlName(new javax.xml.namespace.QName("", "keyStore"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "anyType"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("persistenceFactories");
        elemField.setXmlName(new javax.xml.namespace.QName("", "persistenceFactories"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://xml.apache.org/xml-soap", "Map"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("readerUserGroups");
        elemField.setXmlName(new javax.xml.namespace.QName("", "readerUserGroups"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "anyType"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("userGroups");
        elemField.setXmlName(new javax.xml.namespace.QName("", "userGroups"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://xml.apache.org/xml-soap", "Map"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("writerUserGroups");
        elemField.setXmlName(new javax.xml.namespace.QName("", "writerUserGroups"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "anyType"));
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
