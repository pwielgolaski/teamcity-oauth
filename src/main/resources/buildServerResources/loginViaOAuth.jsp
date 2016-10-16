<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="jetbrains.buildServer.auth.oauth.PluginConstants" %>
<%@ include file="/include-internal.jsp" %>
<c:set var="oauthLoginUrl"><%=PluginConstants.Web.LOGIN_PATH%>
</c:set>
<c:if test="${oauth2_settings.schemeConfigured}">
    <style>
        .loginForm {
            display: none;
        }
    </style>
    <div>
        <form action="<c:url value='${oauthLoginUrl}'/>" method="GET">
            <input class="btn loginButton" style="margin: auto; display: block" type="submit" name="submitLogin"
                   value="Log in via oAuth">
        </form>
    </div>
</c:if>
