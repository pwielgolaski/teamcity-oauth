package jetbrains.buildServer.auth.oauth;


import jetbrains.buildServer.users.AuthPropertyKey;

public interface PluginConstants {
    String OAUTH_AUTH_SCHEME_NAME = "OAUTH.v2";
    String OAUTH_AUTH_SCHEME_DESCRIPTION = "Authentication via OAuth 2";

    AuthPropertyKey ID_USER_PROPERTY_KEY = new AuthPropertyKey("HTTP", "teamcity-oauth-id", "OAuth ID");

    interface Web {
        String LOGIN_PATH = "/oauthLogin.html";
        String LOGIN_EXTENSION_PAGE = "loginViaOAuth.jsp";
        String EDIT_SCHEME_PAGE = "editOAuthSchemeProperties.jsp";
    }
}
