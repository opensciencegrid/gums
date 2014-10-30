/*
 * GUMSAuthZServiceImpl.java
 *
 * Created on January 5, 2005, 6:04 PM
 */

package gov.bnl.gums.service;

import java.util.List;

import org.apache.log4j.Logger;

import gov.bnl.gums.AccountInfo;
import gov.bnl.gums.admin.GUMSAPI;
import gov.bnl.gums.admin.GUMSAPIImpl;

import org.opensaml.saml2.core.Statement;
import org.opensaml.saml2.core.impl.SubjectBuilder;
import org.opensaml.xml.XMLObjectBuilderFactory;
import org.opensaml.xacml.ctx.RequestType;
import org.opensaml.xacml.ctx.DecisionType;
import org.opensaml.xacml.ctx.ResponseType;
import org.opensaml.xacml.ctx.StatusType;
import org.opensaml.xacml.ctx.StatusCodeType;
import org.opensaml.xacml.ctx.SubjectType;
import org.opensaml.xacml.ctx.ResultType;
import org.opensaml.xacml.ctx.ResourceType;
import org.opensaml.xacml.ctx.AttributeType;
import org.opensaml.xacml.ctx.AttributeValueType;
import org.opensaml.xacml.ctx.impl.DecisionTypeImplBuilder;
import org.opensaml.xacml.ctx.impl.StatusCodeTypeImplBuilder;
import org.opensaml.xacml.ctx.impl.StatusTypeImplBuilder;
import org.opensaml.xacml.ctx.impl.ResultTypeImplBuilder;
import org.opensaml.xacml.ctx.impl.ResponseTypeImplBuilder;
import org.opensaml.xacml.ctx.impl.AttributeValueTypeImpl;
import org.opensaml.xacml.policy.AttributeAssignmentType;
import org.opensaml.xacml.policy.EffectType;
import org.opensaml.xacml.policy.ObligationsType;
import org.opensaml.xacml.policy.ObligationType;
import org.opensaml.xacml.policy.impl.AttributeAssignmentTypeImplBuilder;
import org.opensaml.xacml.policy.impl.ObligationTypeImplBuilder;
import org.opensaml.xacml.policy.impl.ObligationsTypeImplBuilder;
import org.opensaml.xacml.profile.saml.XACMLAuthzDecisionQueryType;
import org.opensaml.xacml.profile.saml.XACMLAuthzDecisionStatementType;
import org.opensaml.xacml.profile.saml.impl.XACMLAuthzDecisionStatementTypeImplBuilder;
import org.opensaml.xml.XMLObject;

import org.opensciencegrid.authz.xacml.service.XACMLMappingService;
import org.opensciencegrid.authz.xacml.common.XACMLConstants;
import org.opensaml.xacml.ctx.AttributeValueType;

public class GUMSXACMLMappingServiceImpl implements XACMLMappingService {
	private static String ERROR = "http://oasis/names/tc/xacml/1.0/status/error";
	private static String OK = "http://oasis/names/tc/xacml/1.0/status/ok";
	private Logger log = Logger.getLogger(GUMSXACMLMappingServiceImpl.class);
	private static GUMSAPI gums = new GUMSAPIImpl();

	public XACMLAuthzDecisionStatementType mapCredentials(XACMLAuthzDecisionQueryType xacmlQuery) throws Exception {
		XMLObjectBuilderFactory builderFactory = org.opensaml.xml.Configuration.getBuilderFactory();

		// Get information from request
		RequestType request = xacmlQuery.getRequest();
		boolean supportsAccount = getEnvironmentSupportsObligation(request, XACMLConstants.OBLIGATION_ACCOUNT);
		String userDn = getSubjectAttributeValue(request, XACMLConstants.SUBJECT_X509_ID);
		String userFqan = getSubjectAttributeValue(request, XACMLConstants.SUBJECT_VOMS_PRIMARY_FQAN_ID);
		
		String hostDn = getResourceAttributeValue(request, XACMLConstants.RESOURCE_X509_ID);
		if (hostDn==null || hostDn.length()==0) {
			hostDn = getResourceAttributeValue(request, XACMLConstants.RESOURCE_DNS_HOST_NAME_ID);
			if (hostDn==null || hostDn.length()==0) {
				log.debug("missing attributes: "+XACMLConstants.RESOURCE_X509_ID + " or " + XACMLConstants.RESOURCE_DNS_HOST_NAME_ID);
				throw new Exception("missing attribute: "+XACMLConstants.RESOURCE_X509_ID + " or " + XACMLConstants.RESOURCE_DNS_HOST_NAME_ID);
			}
		}
		if (userDn==null || userDn.length()==0) {
			log.debug("missing attribute: "+XACMLConstants.SUBJECT_X509_ID);
			throw new Exception("missing attribute: "+XACMLConstants.SUBJECT_X509_ID);
		}
		/*if (userFqan==null || userFqan.length()==0) {
			log.debug("missing attribute: "+VOMS_FQAN);
			throw new Exception("missing attribute: "+VOMS_FQAN);
		}*/

		// Attribute Assignment, decision, and status code
		AttributeAssignmentType attributeAssignment = null;
		AttributeAssignmentType attributeAssignmentGid = null;
		AttributeAssignmentType attributeAssignmentGroup = null;
		boolean hasGid = false;
		boolean hasGroup = false;
		DecisionTypeImplBuilder decisionBuilder = (DecisionTypeImplBuilder)builderFactory.getBuilder(DecisionType.DEFAULT_ELEMENT_NAME);
		DecisionType decision = decisionBuilder.buildObject();
		StatusCodeTypeImplBuilder statusCodeBuilder = (StatusCodeTypeImplBuilder)builderFactory.getBuilder(StatusCodeType.DEFAULT_ELEMENT_NAME);
		StatusCodeType statusCode = statusCodeBuilder.buildObject();
		statusCode.setValue(OK);
		try {
			log.debug("Checking access on '" + hostDn + "' for '" + userDn + "' with fqan '" + userFqan + "'");
			AccountInfo account = gums.mapUser(hostDn, userDn, userFqan);
			if (account == null || account.getUser() == null) {
				decision.setDecision(DecisionType.DECISION.Deny);
				
				log.debug("Denied access on '" + hostDn + "' for '" + userDn + "' with fqan '" + userFqan + "'");
			}
			else {
				AttributeAssignmentTypeImplBuilder attributeAssignmentBuilder = (AttributeAssignmentTypeImplBuilder)builderFactory.getBuilder(AttributeAssignmentType.DEFAULT_ELEMENT_NAME);
				attributeAssignment = attributeAssignmentBuilder.buildObject();
				attributeAssignment.setAttributeId(XACMLConstants.ATTRIBUTE_USERNAME_ID);
				attributeAssignment.setDataType(XACMLConstants.STRING_DATATYPE);
				attributeAssignment.setValue(account.getUser());

				if (account.getGroup() != null && !account.getGroup().equals("")) {
					try {
						Integer.parseInt(account.getGroup());
						hasGid = true;
						attributeAssignmentGid = attributeAssignmentBuilder.buildObject();
						attributeAssignmentGid.setAttributeId(XACMLConstants.ATTRIBUTE_POSIX_GID_ID);
						attributeAssignmentGid.setDataType(XACMLConstants.STRING_DATATYPE);
						attributeAssignmentGid.setValue(account.getGroup());
					} catch (NumberFormatException e1) {
						hasGroup = true;
						attributeAssignmentGroup = attributeAssignmentBuilder.buildObject();
						attributeAssignmentGroup.setAttributeId(XACMLConstants.ATTRIBUTE_PRIMARY_GROUPNAME_ID);
						attributeAssignmentGroup.setDataType(XACMLConstants.STRING_DATATYPE);
						attributeAssignmentGroup.setValue(account.getGroup());
					}
				}
				log.debug("Has group " + hasGroup + ", supports account " + supportsAccount);
				if (hasGroup && !supportsAccount)
				{
					decision.setDecision(DecisionType.DECISION.Indeterminate);
					statusCode.setValue(ERROR);
					log.warn("Credentials mapped on '" + hostDn + "' for '" + userDn + "' with fqan '" + userFqan + "' to '" + account + "'.  However, the client cannot understand our response; will return an indeterminate response.");
				}
				else
				{
					decision.setDecision(DecisionType.DECISION.Permit);
					log.debug("Credentials mapped on '" + hostDn + "' for '" + userDn + "' with fqan '" + userFqan + "' to '" + account + "'");
				}
			}
		} catch (Exception e1) {
			statusCode.setValue(ERROR);
			log.debug(e1.getMessage());
			throw e1;
		}

		try {
			// Status
			StatusTypeImplBuilder statusBuilder = (StatusTypeImplBuilder)builderFactory.getBuilder(StatusType.DEFAULT_ELEMENT_NAME);
			StatusType status = statusBuilder.buildObject();
			status.setStatusCode(statusCode);
	
			// Obligation
			ObligationTypeImplBuilder obligationBuilder = (ObligationTypeImplBuilder)builderFactory.getBuilder(ObligationType.DEFAULT_ELEMENT_QNAME);
			ObligationType obligation = obligationBuilder.buildObject();
			obligation.setFulfillOn(EffectType.Permit);
			obligation.setObligationId(XACMLConstants.OBLIGATION_USERNAME);

			// Obligations
			ObligationsTypeImplBuilder obligationsBuilder = (ObligationsTypeImplBuilder)builderFactory.getBuilder(ObligationsType.DEFAULT_ELEMENT_QNAME);
			ObligationsType obligations = obligationsBuilder.buildObject();

			if (hasGid) {
				ObligationType obligationgid = obligationBuilder.buildObject();
				obligationgid.setFulfillOn(EffectType.Permit);
				obligationgid.setObligationId(XACMLConstants.OBLIGATION_UIDGID);
				obligationgid.getAttributeAssignments().add(attributeAssignmentGid);
				obligations.getObligations().add(obligationgid);
			}
			if (supportsAccount)
			{
				ObligationType obligationAccount = obligationBuilder.buildObject();
				obligationAccount.setFulfillOn(EffectType.Permit);
				obligationAccount.setObligationId(XACMLConstants.OBLIGATION_ACCOUNT);
				if (attributeAssignment != null) {obligationAccount.getAttributeAssignments().add(attributeAssignment);}
				if (hasGroup) {obligationAccount.getAttributeAssignments().add(attributeAssignmentGroup);}
				obligations.getObligations().add(obligationAccount);
			}
			else
			{
				if (attributeAssignment != null) {obligation.getAttributeAssignments().add(attributeAssignment);}
				obligations.getObligations().add(obligation);
			}
	
			// Result
			ResultTypeImplBuilder resultBuilder = (ResultTypeImplBuilder)builderFactory.getBuilder(ResultType.DEFAULT_ELEMENT_NAME);
			ResultType result = resultBuilder.buildObject();
			result.setStatus(status);
			result.setDecision(decision);
			result.setObligations(obligations);
	
			// Response      
			ResponseTypeImplBuilder responseBuilder = (ResponseTypeImplBuilder)builderFactory.getBuilder(ResponseType.DEFAULT_ELEMENT_NAME);
			ResponseType response = responseBuilder.buildObject();
			response.setResult(result);
	
			// Statement
			XACMLAuthzDecisionStatementTypeImplBuilder xacmlauthzBuilder = (XACMLAuthzDecisionStatementTypeImplBuilder)builderFactory.getBuilder(XACMLAuthzDecisionStatementType.TYPE_NAME_XACML20);
			XACMLAuthzDecisionStatementType xacmlAuthzStatement = xacmlauthzBuilder.buildObject( Statement.DEFAULT_ELEMENT_NAME, XACMLAuthzDecisionStatementType.TYPE_NAME_XACML20);	
			if (xacmlQuery.getReturnContextXSBooleanValue() != null && xacmlQuery.getReturnContextXSBooleanValue().getValue()) 
			{
				request.detach();
				xacmlAuthzStatement.setRequest(request);
			}
			xacmlAuthzStatement.setResponse(response);

			return xacmlAuthzStatement;
		} catch (Exception e1) {
			statusCode.setValue(ERROR);
			log.debug(e1.getMessage());
			throw e1;
		}
	}

	private String getSubjectAttributeValue(RequestType request, String attributeId) {
		List<SubjectType> subjectList = request.getSubjects();
		for(SubjectType subject : subjectList) {
			List<AttributeType> attributeList = subject.getAttributes();
			for(AttributeType attribute : attributeList) {
				String curAttributeId = attribute.getAttributeID();
				if (attributeId.equals(curAttributeId)) {
					List<AttributeValueType> attributeValueList = attribute.getAttributeValues();
					for(AttributeValueType attributeValue : attributeValueList) {
						return ((AttributeValueTypeImpl)attributeValue).getValue();
					}
				}
			}
		}  
		return null;
	}

	private boolean getEnvironmentSupportsObligation(RequestType request, String obligation) {
		List<AttributeType> attributeList = request.getEnvironment().getAttributes();
		for (AttributeType attribute : attributeList) {
			String curAttributeId = attribute.getAttributeID();
			if (curAttributeId.equals(XACMLConstants.SUPPORTED_OBLIGATIONS)) {
				List<AttributeValueType> attributeValueList = attribute.getAttributeValues();
				for (AttributeValueType attributeValue : attributeValueList) {
					if (((AttributeValueTypeImpl)attributeValue).getValue().equals(obligation))
					{
						return true;
					}
				}
			}
		}
		return false;
	}

	private String getResourceAttributeValue(RequestType request, String attributeId) {
		List<ResourceType> resourceList = request.getResources();
		for(ResourceType resource : resourceList) {
			List<AttributeType> attributeList = resource.getAttributes();
			for(AttributeType attribute : attributeList) {
				String curAttributeId = attribute.getAttributeID();
				if (attributeId.equals(curAttributeId)) {
					List<AttributeValueType> attributeValueList = attribute.getAttributeValues();
					for(AttributeValueType attributeValue : attributeValueList) {
						return ((AttributeValueTypeImpl)attributeValue).getValue();
					}
				}
			}
		}  
		return null;
	}
}
