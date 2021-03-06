package com.google.auth.oauth2;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.util.Beta;
import com.google.api.client.util.PemReader;
import com.google.api.client.util.Preconditions;
import com.google.api.client.util.SecurityUtils;
import com.google.api.client.util.PemReader.Section;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Collection;
import java.util.Collections;

/**
 * Base type for credentials for authorizing calls to Google APIs using OAuth2.
 */
public abstract class GoogleCredentials extends OAuth2Credentials {

  static final String USER_FILE_TYPE = "authorized_user";
  static final String SERVICE_ACCOUNT_FILE_TYPE = "service_account";

  private static final DefaultCredentialsProvider defaultCredentialsProvider =
      new DefaultCredentialsProvider();

  /**
   * Returns the Application Default Credentials.
   *
   * <p>Returns the Application Default Credentials which are credentials that identify and
   * authorize the whole application. This is the built-in service account if running on Google
   * Compute Engine or the credentials file from the path in the environment variable
   * GOOGLE_APPLICATION_CREDENTIALS.</p>
   *
   * @return the credentials instance.
   * @throws IOException if the credentials cannot be created in the current environment.
   */
  public static GoogleCredentials getApplicationDefault() throws IOException {
    return getApplicationDefault(OAuth2Utils.HTTP_TRANSPORT);
  }

  /**
   * Returns the Application Default Credentials.
   *
   * <p>Returns the Application Default Credentials which are credentials that identify and
   * authorize the whole application. This is the built-in service account if running on Google
   * Compute Engine or the credentials file from the path in the environment variable
   * GOOGLE_APPLICATION_CREDENTIALS.</p>
   *
   * @param transport the transport for Http calls.
   * @return the credentials instance.
   * @throws IOException if the credentials cannot be created in the current environment.
   **/
  public static GoogleCredentials getApplicationDefault(
      HttpTransport transport) throws IOException {
    Preconditions.checkNotNull(transport);
    return defaultCredentialsProvider.getDefaultCredentials(transport);
  }

  /**
   * Returns credentials defined by a JSON file stream.
   *
   * <p>The stream can contain a Service Account key file in JSON format from the Google Developers
   * Console or a stored user credential using the format supported by the Cloud SDK.</p>
   *
   * @param credentialsStream the stream with the credential definition.
   * @return the credential defined by the credentialsStream.
   * @throws IOException if the credential cannot be created from the stream.
   **/
  public static GoogleCredentials fromStream(InputStream credentialsStream) throws IOException {
    return fromStream(credentialsStream, OAuth2Utils.HTTP_TRANSPORT);
  }

  /**
   * Returns credentials defined by a JSON file stream.
   *
   * <p>The stream can contain a Service Account key file in JSON format from the Google Developers
   * Console or a stored user credential using the format supported by the Cloud SDK.</p>
   *
   * @param credentialsStream the stream with the credential definition.
   * @param transport the transport for Http calls.
   * @return the credential defined by the credentialsStream.
   * @throws IOException if the credential cannot be created from the stream.
   **/
  public static GoogleCredentials fromStream(InputStream credentialsStream, HttpTransport transport)
      throws IOException {
    Preconditions.checkNotNull(credentialsStream);
    Preconditions.checkNotNull(transport);

    JsonFactory jsonFactory = OAuth2Utils.JSON_FACTORY;
    JsonObjectParser parser = new JsonObjectParser(jsonFactory);
    GenericJson fileContents = parser.parseAndClose(
        credentialsStream, OAuth2Utils.UTF_8, GenericJson.class);

    String fileType = (String) fileContents.get("type");
    if (fileType == null) {
      throw new IOException("Error reading credentials from stream, 'type' field not specified.");
    }
    if (USER_FILE_TYPE.equals(fileType)) {
      return UserCredentials.fromJson(fileContents, transport);
    }
    if (SERVICE_ACCOUNT_FILE_TYPE.equals(fileType)) {
      return ServiceAccountCredentials.fromJson(fileContents, transport);
    }
    throw new IOException(String.format(
        "Error reading credentials from stream, 'type' value '%s' not recognized."
            + " Expecting '%s' or '%s'.",
        fileType, USER_FILE_TYPE, SERVICE_ACCOUNT_FILE_TYPE));
  }

  /**
   * Indicates whether the credentials require scopes to be specified via a call to
   * {link GoogleCredentials#createScoped} before use.
   */
  public boolean createScopedRequired() {
    return false;
  }

  /**
   * If the credentials support scopes, create a copy of the the idenitity with the specified
   * scopes, otherwise returns the same instance.
   */
  @SuppressWarnings("unused")
  public GoogleCredentials createScoped(Collection<String> scopes) {
    return this;
  }
}
