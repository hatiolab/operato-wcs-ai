# Oauth2 Grant Type

The OAuth framework specifies several grant types for different use cases, as well as a framework for creating new grant types.

The most common OAuth grant types are listed below.

- Authorization Code
- Implicit Grant
- Resource Owner Password Credentials Grant
- Client Credentials
- JWT as Authorization Grant

[from oauth.net](https://oauth.net/2/grant-types/)

## Authorization Code Grant

The authorization code grant type is used to obtain both access tokens and refresh tokens and is optimized for confidential clients. Since this is a redirection-based flow, the client must be capable of interacting with the resource owner's user-agent (typically a web browser) and capable of receiving incoming requests (via redirection) from the authorization server.

## Implicit Grant

The implicit grant type is used to obtain access tokens (it does not support the issuance of refresh tokens) and is optimized for public clients known to operate a particular redirection URI. These clients are typically implemented in a browser using a scripting language such as JavaScript.

## Resource Owner Password Credentials Grant

The resource owner password credentials grant type is suitable in cases where the resource owner has a trust relationship with the client, such as the device operating system or a highly privileged application. The authorization server should take special care when enabling this grant type and only allow it when other flows are not viable.

## Client Credentials Grant

The client can request an access token using only its client credentials (or other supported means of authentication) when the client is requesting access to the protected resources under its control, or those of another resource owner that have been previously arranged with the authorization server (the method of which is beyond the scope of this specification).

## JWT as Authorization Grant

A JSON Web Token (JWT) Bearer Token can be used to request an access token when a client wishes to utilize an existing trust relationship, expressed through the semantics of (and digital signature or Message Authentication Code calculated over) the JWT, without a direct user approval step at the authorization server.
