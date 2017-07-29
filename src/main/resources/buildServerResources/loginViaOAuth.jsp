<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="jetbrains.buildServer.auth.oauth.PluginConstants" %>
<%@ include file="/include-internal.jsp" %>
<c:set var="oauthLoginUrl"><%=PluginConstants.Web.LOGIN_PATH%>
</c:set>
<c:if test="${oauth2_settings.schemeConfigured}">
    <c:if test="${oauth2_settings.hideLoginForm}">
        <style>
            .loginForm {
                display: none;
            }

            .oauth-guest-section {
                text-align: right;
                padding-top: 0.5em;
            }
        </style>
    </c:if>
    <div>
        <form action="<c:url value='${oauthLoginUrl}'/>" method="GET">
            <input class="btn loginButton" style="margin: auto; display: block" type="submit" name="submitLogin"
                   value="Log in via oAuth">
            <c:if test="${oauth2_settings.hideLoginForm and oauth2_settings.guestLoginAllowed}">
                <div class="oauth-guest-section">
                    <span class="greyNote">
                        <a href="<c:url value='/guestLogin.html?guest=1'/>">Log in as guest</a>&nbsp;
                        <bs:help file="User+Account"/>
                    </span>
                </div>
            </c:if>
        </form>
    </div>
</c:if>
