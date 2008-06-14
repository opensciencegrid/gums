/*
 * GUMSAuthZServiceImpl.java
 *
 * Created on January 5, 2005, 6:04 PM
 */

package gov.bnl.gums.service;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import org.joda.time.DateTime;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

import gov.bnl.gums.admin.GUMSAPI;
import gov.bnl.gums.admin.GUMSAPIImpl;

import org.opensaml.common.impl.SecureRandomIdentifierGenerator;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Statement;
import org.opensaml.saml2.core.Subject;
import org.opensaml.saml2.core.impl.AssertionBuilder;
import org.opensaml.saml2.core.impl.IssuerBuilder;
import org.opensaml.saml2.core.impl.SubjectBuilder;
import org.opensaml.saml2.core.impl.AssertionImpl;
import org.opensaml.xml.XMLObjectBuilderFactory;
import org.opensaml.xacml.ctx.RequestType;
import org.opensaml.xacml.ctx.DecisionType;
import org.opensaml.xacml.ctx.DecisionType;
import org.opensaml.xacml.ctx.ResponseType;
import org.opensaml.xacml.ctx.StatusType;
import org.opensaml.xacml.ctx.StatusCodeType;
import org.opensaml.xacml.ctx.SubjectType;
import org.opensaml.xacml.ctx.ResultType;
import org.opensaml.xacml.ctx.ResourceType;
import org.opensaml.xacml.ctx.AttributeType;
import org.opensaml.xacml.ctx.AttributeValueType;
import org.opensaml.xacml.policy.AttributeAssignmentType;
import org.opensaml.xacml.policy.EffectType;
import org.opensaml.xacml.policy.ObligationsType;
import org.opensaml.xacml.policy.ObligationType;
import org.opensaml.xacml.profile.saml.XACMLAuthzDecisionQueryType;
import org.opensaml.xacml.profile.saml.XACMLAuthzDecisionStatementType;
import org.opensaml.xacml.profile.saml.impl.XACMLAuthzDecisionStatementTypeImplBuilder;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallerFactory;
import org.opensaml.xml.parse.BasicParserPool;

public class GUMSXACMLMappingServiceImpl implements XACMLMappingService {
    private static String RESOURCE_ID = "http://authz-interop.org/xacml/2.0/subject/resource-x509-id";
    private static String SUBJECT_ID = "http://authz-interop.org/xacml/2.0/subject/subject-x509-id";
    private static String VOMS_FQAN = "http://authz-interop.org/xacml/2.0/subject/voms-fqan";
    private static String USERNAME = "http://authz-interop.org/xacml/2.0/obligation/Username";
    private static String ERROR = "http://oasis/names/tc/xacml/1.0/status/error";
    private static String OK = "http://oasis/names/tc/xacml/1.0/status/ok";
   
    public XACMLAuthzDecisionStatementType mapCredentials(XACMLAuthzDecisionQueryType xacmlQuery) throws Exception {
		XMLObjectBuilderFactory builderFactory = org.opensaml.xml.Configuration.getBuilderFactory();

    	// Get information from request
    	RequestType request = xacmlQuery.getRequest();
    	String hostDn = getResourceAttributeValue(request, RESOURCE_ID);
    	String userDn = getSubjectAttributeValue(request, SUBJECT_ID);
    	String userFqan = getSubjectAttributeValue(request, VOMS_FQAN);
		
        // Attribute Assignment, decision, and status code
		AttributeAssignmentType attributeAssignment = null;
        SubjectBuilder decisionBuilder = (SubjectBuilder)builderFactory.getBuilder(DecisionType.DEFAULT_ELEMENT_NAME);
		DecisionType decision = (DecisionType)decisionBuilder.buildObject();
        SubjectBuilder statusCodeBuilder = (SubjectBuilder)builderFactory.getBuilder(StatusCodeType.DEFAULT_ELEMENT_NAME);
        StatusCodeType statusCode = (StatusCodeType)statusCodeBuilder.buildObject();
        statusCode.setValue(OK);
		try {
			String account = gums.mapUser(hostDn, userDn, userFqan);
			if (account == null) {
			    decision.setDecision(DecisionType.DECISION.Deny);
			    log.debug("Denied access on '" + hostDn + "' for '" + userDn + "' with fqan '" + userFqan + "'");
			}
			else {
			    SubjectBuilder attributeAssignmentBuilder = (SubjectBuilder)builderFactory.getBuilder(AttributeAssignmentType.DEFAULT_ELEMENT_NAME);
				attributeAssignment = (AttributeAssignmentType)attributeAssignmentBuilder.buildObject();
				attributeAssignment.setAttributeId(USERNAME);
				attributeAssignment.setDataType("http://www.w3.org/2001/XMLSchema#string");
				attributeAssignment.setValue(account);
			    decision.setDecision(DecisionType.DECISION.Permit);
				log.debug("Credentials mapped on '" + hostDn + "' for '" + userDn + "' with fqan '" + userFqan + "' to '" + account + "'");
			}
		} catch (RuntimeException e1) {
	        statusCode.setValue(ERROR);
	        log.debug(e1.getMessage());
		throw e1;
		}
        
        // Status
        SubjectBuilder statusBuilder = (SubjectBuilder)builderFactory.getBuilder(StatusType.DEFAULT_ELEMENT_NAME);
        StatusType status = (StatusType)statusBuilder.buildObject();
        status.setStatusCode(statusCode);
        
        // Obligation
        SubjectBuilder obligationBuilder = (SubjectBuilder)builderFactory.getBuilder(ObligationType.DEFAULT_ELEMENT_QNAME);
		ObligationType obligation = (ObligationType)obligationBuilder.buildObject();
		obligation.setFulfillOn(EffectType.Permit);
		obligation.setObligationId(USERNAME);
		if (attributeAssignment != null)
			obligation.getAttributeAssignments().add(attributeAssignment);
        
        // Obligations
        SubjectBuilder obligationsBuilder = (SubjectBuilder)builderFactory.getBuilder(ObligationsType.DEFAULT_ELEMENT_QNAME);
		ObligationsType obligations = (ObligationsType)obligationsBuilder.buildObject();
		obligations.getObligations().add(obligation);
        
        // Result
        SubjectBuilder resultBuilder = (SubjectBuilder)builderFactory.getBuilder(ResultType.DEFAULT_ELEMENT_NAME);
		ResultType result = (ResultType)resultBuilder.buildObject();
		result.setStatus(status);
		result.setDecision(decision);
		result.setObligations(obligations);
        
        // Response      
        SubjectBuilder responseBuilder = (SubjectBuilder)builderFactory.getBuilder(ResponseType.DEFAULT_ELEMENT_NAME);
		ResponseType response = (ResponseType)responseBuilder.buildObject();
		response.setResult(result);
		
		// Statement
		XACMLAuthzDecisionStatementTypeImplBuilder xacmlauthzBuilder =  
			(XACMLAuthzDecisionStatementTypeImplBuilder)builderFactory.getBuilder(XACMLAuthzDecisionStatementType.TYPE_NAME_XACML20);
 	   	XACMLAuthzDecisionStatementType xacmlAuthzStatement = 
 	   		xacmlauthzBuilder.buildObject( Statement.DEFAULT_ELEMENT_NAME, XACMLAuthzDecisionStatementType.TYPE_NAME_XACML20);	
 	   	xacmlAuthzStatement.setRequest(request);
		xacmlAuthzStatement.setResponse(response);
 	   	
    	return xacmlAuthzStatement;
    }
    
    private String getSubjectAttributeValue(RequestType request, String attributeId) {
    	List<SubjectType> subjectList = request.getSubjects();
	    for(SubjectType subject : subjectList) {
	    	List<AttributeType> attributeList = subject.getAttributes();
	        for(AttributeType attribute : attributeList) {
	            String curAttributeId = attribute.getAttributeID();
	            if (attributeId.equals(curAttributeId)) {
		            List<XMLObject> attributeValueList = attribute.getAttributeValues();
		            for(XMLObject attributeValue : attributeValueList) {
		                return attributeValue.toString();
		            }
	            }
	        }
	    }  
	    return null;
	}
    
    private String getResourceAttributeValue(RequestType request, String attributeId) {
    	List<ResourceType> resourceList = request.getResources();
	    for(ResourceType resource : resourceList) {
	    	List<AttributeType> attributeList = resource.getAttributes();
	        for(AttributeType attribute : attributeList) {
	            String curAttributeId = attribute.getAttributeID();
	            if (attributeId.equals(curAttributeId)) {
		            List<XMLObject> attributeValueList = attribute.getAttributeValues();
		            for(XMLObject attributeValue : attributeValueList) {
		                return attributeValue.toString();
		            }
	            }
	        }
	    }  
	    return null;
	}
}
