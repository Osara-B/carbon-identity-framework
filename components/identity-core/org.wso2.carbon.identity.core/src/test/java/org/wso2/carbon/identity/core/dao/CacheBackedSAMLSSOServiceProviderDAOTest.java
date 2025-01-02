package org.wso2.carbon.identity.core.dao;

import org.apache.commons.lang.StringUtils;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.core.IdentityRegistryResources;
import org.wso2.carbon.identity.core.SAMLSSOServiceProviderManager;
import org.wso2.carbon.identity.core.model.SAMLSSOServiceProviderDO;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.core.util.TestUtils;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.testng.Assert.*;
import static org.wso2.carbon.identity.core.constant.TestConstants.ASSERTION_CONSUMER_URLS;
import static org.wso2.carbon.identity.core.constant.TestConstants.ASSERTION_ENCRYPTION_ALGORITHM_URI;
import static org.wso2.carbon.identity.core.constant.TestConstants.ATTRIBUTE_CONSUMING_SERVICE_INDEX;
import static org.wso2.carbon.identity.core.constant.TestConstants.CERT_ALIAS;
import static org.wso2.carbon.identity.core.constant.TestConstants.DEFAULT_ASSERTION_CONSUMER_URL;
import static org.wso2.carbon.identity.core.constant.TestConstants.DIGEST_ALGORITHM_URI;
import static org.wso2.carbon.identity.core.constant.TestConstants.DO_ENABLE_ENCRYPTED_ASSERTION;
import static org.wso2.carbon.identity.core.constant.TestConstants.DO_FRONT_CHANNEL_LOGOUT;
import static org.wso2.carbon.identity.core.constant.TestConstants.DO_SIGN_ASSERTIONS;
import static org.wso2.carbon.identity.core.constant.TestConstants.DO_SIGN_RESPONSE;
import static org.wso2.carbon.identity.core.constant.TestConstants.DO_SINGLE_LOGOUT;
import static org.wso2.carbon.identity.core.constant.TestConstants.DO_VALIDATE_SIGNATURE_IN_ARTIFACT_RESOLVE;
import static org.wso2.carbon.identity.core.constant.TestConstants.DO_VALIDATE_SIGNATURE_IN_REQUESTS;
import static org.wso2.carbon.identity.core.constant.TestConstants.ENABLE_ATTRIBUTES_BY_DEFAULT;
import static org.wso2.carbon.identity.core.constant.TestConstants.ENABLE_SAML2_ARTIFACT_BINDING;
import static org.wso2.carbon.identity.core.constant.TestConstants.FRONT_CHANNEL_LOGOUT_BINDING;
import static org.wso2.carbon.identity.core.constant.TestConstants.IDP_ENTITY_ID_ALIAS;
import static org.wso2.carbon.identity.core.constant.TestConstants.IDP_INIT_SLO_ENABLED;
import static org.wso2.carbon.identity.core.constant.TestConstants.IDP_INIT_SLO_RETURN_TO_URLS;
import static org.wso2.carbon.identity.core.constant.TestConstants.ISSUER1;
import static org.wso2.carbon.identity.core.constant.TestConstants.ISSUER2;
import static org.wso2.carbon.identity.core.constant.TestConstants.ISSUER_QUALIFIER;
import static org.wso2.carbon.identity.core.constant.TestConstants.IS_ASSERTION_QUERY_REQUEST_PROFILE_ENABLED;
import static org.wso2.carbon.identity.core.constant.TestConstants.IS_IDP_INIT_SSO_ENABLED;
import static org.wso2.carbon.identity.core.constant.TestConstants.KEY_ENCRYPTION_ALGORITHM_URI;
import static org.wso2.carbon.identity.core.constant.TestConstants.NAME_ID_FORMAT;
import static org.wso2.carbon.identity.core.constant.TestConstants.REQUESTED_AUDIENCES;
import static org.wso2.carbon.identity.core.constant.TestConstants.REQUESTED_RECIPIENTS;
import static org.wso2.carbon.identity.core.constant.TestConstants.SAML_ECP;
import static org.wso2.carbon.identity.core.constant.TestConstants.SIGNING_ALGORITHM_URI;
import static org.wso2.carbon.identity.core.constant.TestConstants.SLO_REQUEST_URL;
import static org.wso2.carbon.identity.core.constant.TestConstants.SLO_RESPONSE_URL;
import static org.wso2.carbon.identity.core.constant.TestConstants.SUPPORTED_ASSERTION_QUERY_REQUEST_TYPES;

public class CacheBackedSAMLSSOServiceProviderDAOTest {
    private MockedStatic<IdentityUtil> identityUtil;
    private MockedStatic<IdentityTenantUtil> identityTenantUtil;
    private MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil;
    @Mock
    private CacheBackedSAMLSSOServiceProviderDAO samlSSOServiceProviderDAO;

    public SAMLSSOServiceProviderManager samlSSOServiceProviderManager;

    public SAMLSSOServiceProviderDO sampleServiceProvider1;
    public SAMLSSOServiceProviderDO sampleServiceProvider2;
    public SAMLSSOServiceProviderDO invalidServiceProviderDO;

    @BeforeMethod
    public void setUp() throws Exception {
        samlSSOServiceProviderManager = new SAMLSSOServiceProviderManager();
        sampleServiceProvider1 = createServiceProviderDO(ISSUER1);
        sampleServiceProvider2 = createServiceProviderDO(ISSUER2);
        invalidServiceProviderDO = createServiceProviderDO(null);

        TestUtils.initiateH2Base();
        DataSource dataSource = mock(DataSource.class);
        identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class);
        identityUtil = mockStatic(IdentityUtil.class);
        identityTenantUtil = mockStatic(IdentityTenantUtil.class);

        identityUtil.when(() -> IdentityUtil.getProperty("SAMLStorage.Type")).thenReturn("database");
        identityDatabaseUtil.when(IdentityDatabaseUtil::getDataSource).thenReturn(dataSource);

        Connection connection = TestUtils.getConnection();
        Connection spyConnection = TestUtils.spyConnection(connection);

        lenient().when(dataSource.getConnection()).thenReturn(spyConnection);
        lenient().doNothing().when(spyConnection).close();

    }

    @AfterMethod
    public void tearDown() {

    }

    @Test
    public void testGetInstance() {

    }

    @Test
    public void testAddServiceProvider() {

    }

    @Test
    public void testGetServiceProvider() {

    }

    @Test
    public void testGetServiceProviders() {

    }

    @Test
    public void testRemoveServiceProvider() {

    }

    @Test
    public void testUploadServiceProvider() {

    }

    @Test
    public void testUpdateServiceProvider() {

    }

    private SAMLSSOServiceProviderDO createServiceProviderDO(String issuer) {

        SAMLSSOServiceProviderDO serviceProviderDO = new SAMLSSOServiceProviderDO();
        serviceProviderDO.setIssuer(issuer);
        serviceProviderDO.setIssuerQualifier(ISSUER_QUALIFIER);
        serviceProviderDO.setAssertionConsumerUrls(ASSERTION_CONSUMER_URLS);
        serviceProviderDO.setDefaultAssertionConsumerUrl(DEFAULT_ASSERTION_CONSUMER_URL);
        serviceProviderDO.setCertAlias(CERT_ALIAS);
        serviceProviderDO.setSloResponseURL(SLO_RESPONSE_URL);
        serviceProviderDO.setSloRequestURL(SLO_REQUEST_URL);
        serviceProviderDO.setDoSingleLogout(DO_SINGLE_LOGOUT);
        serviceProviderDO.setDoSignResponse(DO_SIGN_RESPONSE);
        serviceProviderDO.setDoSignAssertions(DO_SIGN_ASSERTIONS);
        serviceProviderDO.setAttributeConsumingServiceIndex(ATTRIBUTE_CONSUMING_SERVICE_INDEX);
        serviceProviderDO.setRequestedAudiences(REQUESTED_AUDIENCES);
        serviceProviderDO.setRequestedRecipients(REQUESTED_RECIPIENTS);
        serviceProviderDO.setEnableAttributesByDefault(ENABLE_ATTRIBUTES_BY_DEFAULT);
        serviceProviderDO.setNameIDFormat(NAME_ID_FORMAT);
        serviceProviderDO.setIdPInitSSOEnabled(IS_IDP_INIT_SSO_ENABLED);
        serviceProviderDO.setIdPInitSLOEnabled(IDP_INIT_SLO_ENABLED);
        serviceProviderDO.setIdpInitSLOReturnToURLs(IDP_INIT_SLO_RETURN_TO_URLS);
        serviceProviderDO.setDoEnableEncryptedAssertion(DO_ENABLE_ENCRYPTED_ASSERTION);
        serviceProviderDO.setDoValidateSignatureInRequests(DO_VALIDATE_SIGNATURE_IN_REQUESTS);
        serviceProviderDO.setDoValidateSignatureInArtifactResolve(DO_VALIDATE_SIGNATURE_IN_ARTIFACT_RESOLVE);
        serviceProviderDO.setSigningAlgorithmUri(SIGNING_ALGORITHM_URI);
        serviceProviderDO.setDigestAlgorithmUri(DIGEST_ALGORITHM_URI);
        serviceProviderDO.setAssertionEncryptionAlgorithmUri(ASSERTION_ENCRYPTION_ALGORITHM_URI);
        serviceProviderDO.setKeyEncryptionAlgorithmUri(KEY_ENCRYPTION_ALGORITHM_URI);
        serviceProviderDO.setAssertionQueryRequestProfileEnabled(IS_ASSERTION_QUERY_REQUEST_PROFILE_ENABLED);
        serviceProviderDO.setSupportedAssertionQueryRequestTypes(SUPPORTED_ASSERTION_QUERY_REQUEST_TYPES);
        serviceProviderDO.setEnableSAML2ArtifactBinding(ENABLE_SAML2_ARTIFACT_BINDING);
        serviceProviderDO.setSamlECP(SAML_ECP);
        serviceProviderDO.setIdpEntityIDAlias(IDP_ENTITY_ID_ALIAS);
        serviceProviderDO.setDoFrontChannelLogout(DO_FRONT_CHANNEL_LOGOUT);
        serviceProviderDO.setFrontChannelLogoutBinding(FRONT_CHANNEL_LOGOUT_BINDING);

        return serviceProviderDO;
    }

    public String getIssuerWithQualifier(String issuer) {

        return StringUtils.isNotBlank(ISSUER_QUALIFIER) ?
                issuer + IdentityRegistryResources.QUALIFIER_ID + ISSUER_QUALIFIER : issuer;
    }
}