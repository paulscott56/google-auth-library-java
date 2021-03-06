package com.google.auth;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Represents an abstract authorized identity instance.
 */
public abstract class Credentials {

  /**
   * A constant string name describing the authentication technology.
   *
   * <p>E.g. “OAuth2”, “SSL”. For use by the transport layer to determine whether it supports the
   * type of authentication in the case where {@link Credentials#hasRequestMetadataOnly} is false.
   * Also serves as a debugging helper.
   */
  public abstract String getAuthenticationType();

  /**
   * Get the current request metadata, refreshing tokens if required.
   *
   * <p>This should be called by the transport layer on each request, and the data should be
   * populated in headers or other context. The operation can block and fail to complete and may do
   * things such as refreshing access tokens.
   *
   * @throws IOException if there was an error getting up-to-date access.
   */
  public abstract Map<String, List<String>> getRequestMetadata() throws IOException;

  /**
   * Whether the credentials have metadata entries that should be added to each request.
   *
   * <p>This should be called by the transport layer to see if
   * {@link Credentials#getRequestMetadata} should be used for each request.
   */
  public abstract boolean hasRequestMetadata();

  /**
   * Indicates whether or not the Auth mechanism works purely by including request metadata.
   *
   * <p>This is meant for the transport layer. If this is true a transport does not need to take
   * actions other than including the request metadata. If this is false, a transport must
   * specifically know about the authentication technology to support it, and should fail to
   * accept the credentials otherwise.
   */
  public abstract boolean hasRequestMetadataOnly();

  /**
   * Refresh the authorization data, discarding any cached state.
   *
   * <p> For use by the transport to allow retry after getting an error indicating there may be
   * invalid tokens or other cached state.
   *
   * @throws IOException if there was an error getting up-to-date access.
   */
  public abstract void refresh() throws IOException;
}
