/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.core;

import org.apache.commons.lang.StringUtils;
import org.mockito.MockedStatic;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.model.SAMLSSOServiceProviderDO;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.registry.api.RegistryService;

import java.sql.Connection;
import java.util.Arrays;

import javax.sql.DataSource;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.testng.Assert.assertEquals;

import static org.testng.Assert.assertTrue;
import static org.wso2.carbon.identity.core.constant.TestConstants.*;

@Listeners(MockitoTestNGListener.class)
public class SAMLSSOServiceProviderManagerTest {

    private MockedStatic<IdentityUtil> identityUtil;
    private MockedStatic<IdentityTenantUtil> identityTenantUtil;
    private MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil;
    private RegistryService mockRegistry = mock(RegistryService.class);
    private DataSource dataSource;
    private Connection connection;
    private Connection spyConnection;

    public SAMLSSOServiceProviderManager samlSSOServiceProviderManager;

    public SAMLSSOServiceProviderDO sampleServiceProvider1;
    public SAMLSSOServiceProviderDO sampleServiceProvider2;
    public SAMLSSOServiceProviderDO invalidServiceProviderDO;

    @BeforeMethod
    public void setUp() throws Exception {

        samlSSOServiceProviderManager = new SAMLSSOServiceProviderManager();
        sampleServiceProvider1 = createServiceProviderDO(ISSUER1);
        invalidServiceProviderDO = createServiceProviderDO(null);

        TestUtils.initiateH2Base();
        dataSource = mock(DataSource.class);
        identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class);
        identityUtil = mockStatic(IdentityUtil.class);
        identityTenantUtil = mockStatic(IdentityTenantUtil.class);

        identityUtil.when(() -> IdentityUtil.getProperty("SAMLStorage.Type")).thenReturn("database");
        identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDataSource()).thenReturn(dataSource);

        connection = TestUtils.getConnection();
        spyConnection = TestUtils.spyConnection(connection);

        lenient().when(dataSource.getConnection()).thenReturn(spyConnection);
        lenient().doNothing().when(spyConnection).close();

    }

    @AfterMethod
    public void tearDown() throws Exception {

        identityUtil.close();
        identityTenantUtil.close();
        identityDatabaseUtil.close();
        TestUtils.closeH2Base();
    }

    @Test
    public void testAddServiceProvider() throws Exception {

        samlSSOServiceProviderManager.addServiceProvider(sampleServiceProvider1, TENANT_ID);
        SAMLSSOServiceProviderDO serviceProviderFromStorage =
                samlSSOServiceProviderManager.getServiceProvider(getIssuerWithQualifier(ISSUER1), TENANT_ID);
        assertEquals(serviceProviderFromStorage, sampleServiceProvider1);
    }

    @Test
    public void testAddInvalidServiceProvider() {

        assertThrows(IdentityException.class,
                () -> samlSSOServiceProviderManager.addServiceProvider(invalidServiceProviderDO, TENANT_ID));
    }

    @Test
    public void addServiceProviderWithDuplicateIssuer() throws Exception {

        samlSSOServiceProviderManager.addServiceProvider(sampleServiceProvider1, TENANT_ID);
        assertThrows(IdentityException.class,
                () -> samlSSOServiceProviderManager.addServiceProvider(invalidServiceProviderDO, TENANT_ID));
    }

    @Test
    public void testUpdateServiceProvider() throws Exception {

        samlSSOServiceProviderManager.addServiceProvider(sampleServiceProvider1, TENANT_ID);
        sampleServiceProvider1.setDoSingleLogout(UPDATED_DO_SINGLE_LOGOUT);
        sampleServiceProvider1.setRequestedRecipients(UPDATED_REQUESTED_RECIPIENTS);

        samlSSOServiceProviderManager.updateServiceProvider(sampleServiceProvider1, getIssuerWithQualifier(ISSUER1),
                TENANT_ID);

        SAMLSSOServiceProviderDO updatedServiceProvider =
                samlSSOServiceProviderManager.getServiceProvider("issuer1:urn:sp:qualifier:issuerQualifier", TENANT_ID);
        assertEquals(sampleServiceProvider1, updatedServiceProvider);
    }

    @Test
    public void testUpdateInvalidServiceProvider() {

        assertThrows(IdentityException.class,
                () -> samlSSOServiceProviderManager.updateServiceProvider(invalidServiceProviderDO, ISSUER1,
                        TENANT_ID));
    }

    @Test
    public void testGetServiceProviders() throws Exception {

        samlSSOServiceProviderManager.addServiceProvider(sampleServiceProvider1, TENANT_ID);
        sampleServiceProvider2 = createServiceProviderDO(ISSUER2);
        samlSSOServiceProviderManager.addServiceProvider(sampleServiceProvider2, TENANT_ID);

        SAMLSSOServiceProviderDO[] serviceProviders = samlSSOServiceProviderManager.getServiceProviders(TENANT_ID);

        assertEquals(serviceProviders, new SAMLSSOServiceProviderDO[]{sampleServiceProvider1, sampleServiceProvider2});
    }

    @Test
    public void testGetServiceProvider() throws Exception {

        samlSSOServiceProviderManager.addServiceProvider(sampleServiceProvider1, TENANT_ID);

        SAMLSSOServiceProviderDO serviceProviderFromStorage =
                samlSSOServiceProviderManager.getServiceProvider(getIssuerWithQualifier(ISSUER1), TENANT_ID);

        assertEquals(serviceProviderFromStorage, sampleServiceProvider1);
    }

    @Test
    public void testIsServiceProviderExists() throws Exception {

        samlSSOServiceProviderManager.addServiceProvider(sampleServiceProvider1, TENANT_ID);
        boolean exists =
                samlSSOServiceProviderManager.isServiceProviderExists(getIssuerWithQualifier(ISSUER1), TENANT_ID);

        assertTrue(exists);
    }

    @Test
    public void testRemoveServiceProvider() throws Exception {

        samlSSOServiceProviderManager.addServiceProvider(sampleServiceProvider1, TENANT_ID);
        samlSSOServiceProviderManager.removeServiceProvider(getIssuerWithQualifier(ISSUER1), TENANT_ID);
        assertNull(samlSSOServiceProviderManager.getServiceProvider(getIssuerWithQualifier(ISSUER1), TENANT_ID));

    }

    @Test
    public void testRemoveInvalidServiceProvider() throws Exception {

        assertFalse(samlSSOServiceProviderManager.removeServiceProvider(getIssuerWithQualifier(ISSUER1), TENANT_ID));
//       TODO: check whether same exception is thrown in registry implementation
        assertThrows(IllegalArgumentException.class,
                () -> samlSSOServiceProviderManager.removeServiceProvider(null, TENANT_ID));

    }

    @Test
    public void testUploadServiceProvider() throws Exception {

        samlSSOServiceProviderManager.uploadServiceProvider(sampleServiceProvider1, TENANT_ID);

        SAMLSSOServiceProviderDO serviceProviderFromStorage =
                samlSSOServiceProviderManager.getServiceProvider(getIssuerWithQualifier(ISSUER1), TENANT_ID);

        assertEquals(serviceProviderFromStorage, sampleServiceProvider1);
    }

    private SAMLSSOServiceProviderDO createServiceProviderDO(String issuer) {

        SAMLSSOServiceProviderDO serviceProviderDO = new SAMLSSOServiceProviderDO();
        serviceProviderDO.setIssuer(issuer);
        serviceProviderDO.setIssuerQualifier(ISSUER_QUALIFIER);
//        serviceProviderDO.setAssertionConsumerUrl("ASSERTION_CONSUMER_URL");
        serviceProviderDO.setAssertionConsumerUrls(ASSERTION_CONSUMER_URLS);
        serviceProviderDO.setDefaultAssertionConsumerUrl(DEFAULT_ASSERTION_CONSUMER_URL);
        serviceProviderDO.setCertAlias(CERT_ALIAS);
        serviceProviderDO.setSloResponseURL(SLO_RESPONSE_URL);
        serviceProviderDO.setSloRequestURL(SLO_REQUEST_URL);
        serviceProviderDO.setDoSingleLogout(DO_SINGLE_LOGOUT);
//        serviceProviderDO.setLoginPageURL(LOGIN_PAGE_URL);
        serviceProviderDO.setDoSignResponse(DO_SIGN_RESPONSE);
        serviceProviderDO.setDoSignAssertions(DO_SIGN_ASSERTIONS);
        serviceProviderDO.setAttributeConsumingServiceIndex(ATTRIBUTE_CONSUMING_SERVICE_INDEX);
//        serviceProviderDO.setRequestedClaims(REQUESTED_CLAIMS);
        serviceProviderDO.setRequestedAudiences(REQUESTED_AUDIENCES);
        serviceProviderDO.setRequestedRecipients(REQUESTED_RECIPIENTS);
        serviceProviderDO.setEnableAttributesByDefault(ENABLE_ATTRIBUTES_BY_DEFAULT);
//        serviceProviderDO.setNameIdClaimUri(NAME_ID_CLAIM_URI);
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
//        serviceProviderDO.setSigningCertificate(SIGNING_CERTIFICATE);
//        serviceProviderDO.setEncryptionCertificate(ENCRYPTION_CERTIFICATE);
        serviceProviderDO.setAssertionQueryRequestProfileEnabled(IS_ASSERTION_QUERY_REQUEST_PROFILE_ENABLED);
        serviceProviderDO.setSupportedAssertionQueryRequestTypes(SUPPORTED_ASSERTION_QUERY_REQUEST_TYPES);
        serviceProviderDO.setEnableSAML2ArtifactBinding(ENABLE_SAML2_ARTIFACT_BINDING);
        serviceProviderDO.setSamlECP(SAML_ECP);
        serviceProviderDO.setIdpEntityIDAlias(IDP_ENTITY_ID_ALIAS);
        serviceProviderDO.setDoFrontChannelLogout(DO_FRONT_CHANNEL_LOGOUT);
        serviceProviderDO.setFrontChannelLogoutBinding(FRONT_CHANNEL_LOGOUT_BINDING);

        return serviceProviderDO;
    }

    public void isEqual(SAMLSSOServiceProviderDO sample, SAMLSSOServiceProviderDO serviceProviderFromStorage) {

        System.out.println("sampleServiceProvider1: " + sample);
        System.out.println("serviceProviderFromStorage: " + serviceProviderFromStorage);
        System.out.println("equality " + sample.equals(serviceProviderFromStorage));
        System.out.println("sampleServiceProvider1: " + sampleServiceProvider1);
        System.out.println("serviceProviderFromStorage: " + serviceProviderFromStorage);
        System.out.println("Are they equal? " + sampleServiceProvider1.equals(serviceProviderFromStorage));
        System.out.println("issuer: " + sampleServiceProvider1.getIssuer() + " == " + serviceProviderFromStorage.getIssuer() + " -> " + sampleServiceProvider1.getIssuer().equals(serviceProviderFromStorage.getIssuer()));
        System.out.println("issuerQualifier: " + sampleServiceProvider1.getIssuerQualifier() + " == " + serviceProviderFromStorage.getIssuerQualifier() + " -> " + sampleServiceProvider1.getIssuerQualifier().equals(serviceProviderFromStorage.getIssuerQualifier()));
//                System.out.println("assertionConsumerUrl: " + sampleServiceProvider1.getAssertionConsumerUrl() + " == " + serviceProviderFromStorage.getAssertionConsumerUrl() + " -> " + sampleServiceProvider1.getAssertionConsumerUrl().equals(serviceProviderFromStorage.getAssertionConsumerUrl()));
        System.out.println("assertionConsumerUrls: " + Arrays.toString(sampleServiceProvider1.getAssertionConsumerUrls()) + " == " + Arrays.toString(serviceProviderFromStorage.getAssertionConsumerUrls()) + " -> " + Arrays.equals(sampleServiceProvider1.getAssertionConsumerUrls(), serviceProviderFromStorage.getAssertionConsumerUrls()));
        System.out.println("defaultAssertionConsumerUrl: " + sampleServiceProvider1.getDefaultAssertionConsumerUrl() + " == " + serviceProviderFromStorage.getDefaultAssertionConsumerUrl() + " -> " + sampleServiceProvider1.getDefaultAssertionConsumerUrl().equals(serviceProviderFromStorage.getDefaultAssertionConsumerUrl()));
        System.out.println("certAlias: " + sampleServiceProvider1.getCertAlias() + " == " + serviceProviderFromStorage.getCertAlias() + " -> " + sampleServiceProvider1.getCertAlias().equals(serviceProviderFromStorage.getCertAlias()));
        System.out.println("sloResponseURL: " + sampleServiceProvider1.getSloResponseURL() + " == " + serviceProviderFromStorage.getSloResponseURL() + " -> " + sampleServiceProvider1.getSloResponseURL().equals(serviceProviderFromStorage.getSloResponseURL()));
        System.out.println("sloRequestURL: " + sampleServiceProvider1.getSloRequestURL() + " == " + serviceProviderFromStorage.getSloRequestURL() + " -> " + sampleServiceProvider1.getSloRequestURL().equals(serviceProviderFromStorage.getSloRequestURL()));
        System.out.println("doSingleLogout: " + sampleServiceProvider1.isDoSingleLogout() + " == " + serviceProviderFromStorage.isDoSingleLogout() + " -> " + (sampleServiceProvider1.isDoSingleLogout() == serviceProviderFromStorage.isDoSingleLogout()));
        System.out.println("doSignResponse: " + sampleServiceProvider1.isDoSignResponse() + " == " + serviceProviderFromStorage.isDoSignResponse() + " -> " + (sampleServiceProvider1.isDoSignResponse() == serviceProviderFromStorage.isDoSignResponse()));
        System.out.println("doSignAssertions: " + sampleServiceProvider1.isDoSignAssertions() + " == " + serviceProviderFromStorage.isDoSignAssertions() + " -> " + (sampleServiceProvider1.isDoSignAssertions() == serviceProviderFromStorage.isDoSignAssertions()));
        System.out.println("attributeConsumingServiceIndex: " + sampleServiceProvider1.getAttributeConsumingServiceIndex() + " == " + serviceProviderFromStorage.getAttributeConsumingServiceIndex() + " -> " + sampleServiceProvider1.getAttributeConsumingServiceIndex().equals(serviceProviderFromStorage.getAttributeConsumingServiceIndex()));
        System.out.println("requestedClaims: " + Arrays.toString(sampleServiceProvider1.getRequestedClaims()) + " == " + Arrays.toString(serviceProviderFromStorage.getRequestedClaims()) + " -> " + Arrays.equals(sampleServiceProvider1.getRequestedClaims(), serviceProviderFromStorage.getRequestedClaims()));
        System.out.println("requestedAudiences: " + Arrays.toString(sampleServiceProvider1.getRequestedAudiences()) + " == " + Arrays.toString(serviceProviderFromStorage.getRequestedAudiences()) + " -> " + Arrays.equals(sampleServiceProvider1.getRequestedAudiences(), serviceProviderFromStorage.getRequestedAudiences()));
        System.out.println("requestedRecipients: " + Arrays.toString(sampleServiceProvider1.getRequestedRecipients()) + " == " + Arrays.toString(serviceProviderFromStorage.getRequestedRecipients()) + " -> " + Arrays.equals(sampleServiceProvider1.getRequestedRecipients(), serviceProviderFromStorage.getRequestedRecipients()));
        System.out.println("enableAttributesByDefault: " + sampleServiceProvider1.isEnableAttributesByDefault() + " == " + serviceProviderFromStorage.isEnableAttributesByDefault() + " -> " + (sampleServiceProvider1.isEnableAttributesByDefault() == serviceProviderFromStorage.isEnableAttributesByDefault()));
//        System.out.println("nameIdClaimUri: " + sampleServiceProvider1.getNameIdClaimUri() + " == " + serviceProviderFromStorage.getNameIdClaimUri() + " -> " + sampleServiceProvider1.getNameIdClaimUri().equals(serviceProviderFromStorage.getNameIdClaimUri()));
        System.out.println("nameIDFormat: " + sampleServiceProvider1.getNameIDFormat() + " == " + serviceProviderFromStorage.getNameIDFormat() + " -> " + sampleServiceProvider1.getNameIDFormat().equals(serviceProviderFromStorage.getNameIDFormat()));
        System.out.println("isIdPInitSSOEnabled: " + sampleServiceProvider1.isIdPInitSSOEnabled() + " == " + serviceProviderFromStorage.isIdPInitSSOEnabled() + " -> " + (sampleServiceProvider1.isIdPInitSSOEnabled() == serviceProviderFromStorage.isIdPInitSSOEnabled()));
        System.out.println("idPInitSLOEnabled: " + sampleServiceProvider1.isIdPInitSLOEnabled() + " == " + serviceProviderFromStorage.isIdPInitSLOEnabled() + " -> " + (sampleServiceProvider1.isIdPInitSLOEnabled() == serviceProviderFromStorage.isIdPInitSLOEnabled()));
        System.out.println("idpInitSLOReturnToURLs: " + Arrays.toString(sampleServiceProvider1.getIdpInitSLOReturnToURLs()) + " == " + Arrays.toString(serviceProviderFromStorage.getIdpInitSLOReturnToURLs()) + " -> " + Arrays.equals(sampleServiceProvider1.getIdpInitSLOReturnToURLs(), serviceProviderFromStorage.getIdpInitSLOReturnToURLs()));
        System.out.println("doEnableEncryptedAssertion: " + sampleServiceProvider1.isDoEnableEncryptedAssertion() + " == " + serviceProviderFromStorage.isDoEnableEncryptedAssertion() + " -> " + (sampleServiceProvider1.isDoEnableEncryptedAssertion() == serviceProviderFromStorage.isDoEnableEncryptedAssertion()));
        System.out.println("doValidateSignatureInRequests: " + sampleServiceProvider1.isDoValidateSignatureInRequests() + " == " + serviceProviderFromStorage.isDoValidateSignatureInRequests() + " -> " + (sampleServiceProvider1.isDoValidateSignatureInRequests() == serviceProviderFromStorage.isDoValidateSignatureInRequests()));
        System.out.println("doValidateSignatureInArtifactResolve: " + sampleServiceProvider1.isDoValidateSignatureInArtifactResolve() + " == " + serviceProviderFromStorage.isDoValidateSignatureInArtifactResolve() + " -> " + (sampleServiceProvider1.isDoValidateSignatureInArtifactResolve() == serviceProviderFromStorage.isDoValidateSignatureInArtifactResolve()));
        System.out.println("signingAlgorithmUri: " + sampleServiceProvider1.getSigningAlgorithmUri() + " == " + serviceProviderFromStorage.getSigningAlgorithmUri() + " -> " + sampleServiceProvider1.getSigningAlgorithmUri().equals(serviceProviderFromStorage.getSigningAlgorithmUri()));
        System.out.println("digestAlgorithmUri: " + sampleServiceProvider1.getDigestAlgorithmUri() + " == " + serviceProviderFromStorage.getDigestAlgorithmUri() + " -> " + sampleServiceProvider1.getDigestAlgorithmUri().equals(serviceProviderFromStorage.getDigestAlgorithmUri()));
        System.out.println("assertionEncryptionAlgorithmUri: " + sampleServiceProvider1.getAssertionEncryptionAlgorithmUri() + " == " + serviceProviderFromStorage.getAssertionEncryptionAlgorithmUri() + " -> " + sampleServiceProvider1.getAssertionEncryptionAlgorithmUri().equals(serviceProviderFromStorage.getAssertionEncryptionAlgorithmUri()));
        System.out.println("keyEncryptionAlgorithmUri: " + sampleServiceProvider1.getKeyEncryptionAlgorithmUri() + " == " + serviceProviderFromStorage.getKeyEncryptionAlgorithmUri() + " -> " + sampleServiceProvider1.getKeyEncryptionAlgorithmUri().equals(serviceProviderFromStorage.getKeyEncryptionAlgorithmUri()));
//        System.out.println("signingCertificate: " + sampleServiceProvider1.getSigningCertificate() + " == " + serviceProviderFromStorage.getSigningCertificate() + " -> " + sampleServiceProvider1.getSigningCertificate().equals(serviceProviderFromStorage.getSigningCertificate()));
        System.out.println("isAssertionQueryRequestProfileEnabled: " + sampleServiceProvider1.isAssertionQueryRequestProfileEnabled() + " == " + serviceProviderFromStorage.isAssertionQueryRequestProfileEnabled() + " -> " + (sampleServiceProvider1.isAssertionQueryRequestProfileEnabled() == serviceProviderFromStorage.isAssertionQueryRequestProfileEnabled()));
        System.out.println("supportedAssertionQueryRequestTypes: " + sampleServiceProvider1.getSupportedAssertionQueryRequestTypes() + " == " + serviceProviderFromStorage.getSupportedAssertionQueryRequestTypes() + " -> " + sampleServiceProvider1.getSupportedAssertionQueryRequestTypes().equals(serviceProviderFromStorage.getSupportedAssertionQueryRequestTypes()));
        System.out.println("enableSAML2ArtifactBinding: " + sampleServiceProvider1.isEnableSAML2ArtifactBinding() + " == " + serviceProviderFromStorage.isEnableSAML2ArtifactBinding() + " -> " + (sampleServiceProvider1.isEnableSAML2ArtifactBinding() == serviceProviderFromStorage.isEnableSAML2ArtifactBinding()));
        System.out.println("samlECP: " + sampleServiceProvider1.isSamlECP() + " == " + serviceProviderFromStorage.isSamlECP() + " -> " + (sampleServiceProvider1.isSamlECP() == serviceProviderFromStorage.isSamlECP()));
        System.out.println("idpEntityIDAlias: " + sampleServiceProvider1.getIdpEntityIDAlias() + " == " + serviceProviderFromStorage.getIdpEntityIDAlias() + " -> " + sampleServiceProvider1.getIdpEntityIDAlias().equals(serviceProviderFromStorage.getIdpEntityIDAlias()));
        System.out.println("doFrontChannelLogout: " + sampleServiceProvider1.isDoFrontChannelLogout() + " == " + serviceProviderFromStorage.isDoFrontChannelLogout() + " -> " + (sampleServiceProvider1.isDoFrontChannelLogout() == serviceProviderFromStorage.isDoFrontChannelLogout()));
        System.out.println("frontChannelLogoutBinding: " + sampleServiceProvider1.getFrontChannelLogoutBinding() + " == " + serviceProviderFromStorage.getFrontChannelLogoutBinding() + " -> " + sampleServiceProvider1.getFrontChannelLogoutBinding().equals(serviceProviderFromStorage.getFrontChannelLogoutBinding()));

    }

    public String getIssuerWithQualifier(String issuer) {
        return StringUtils.isNotBlank(ISSUER_QUALIFIER) ? issuer + IdentityRegistryResources.QUALIFIER_ID + ISSUER_QUALIFIER : issuer;
    }
}
