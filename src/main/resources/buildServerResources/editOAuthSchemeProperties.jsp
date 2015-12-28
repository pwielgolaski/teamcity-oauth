<%@ page import="jetbrains.buildServer.auth.oauth.ConfigKey" %>
<%@ include file="/include-internal.jsp"%>
<%@ taglib prefix="prop" tagdir="/WEB-INF/tags/props"%>
<div><jsp:include page="/admin/allowCreatingNewUsersByLogin.jsp"/></div>
<br/>
<div>
    <label width="100%" for="<%=ConfigKey.authorizeEndpoint%>">OAuth 2.0 authorization endpoint:</label><br/>
    <prop:textProperty style="width: 100%;" name="<%=ConfigKey.authorizeEndpoint.toString()%>"/><br/>
    <span class="grayNote">Endpoint at which TeamCity server can obtain an authorization code using OAuth 2.0.</span>
</div>
<div>
    <label width="100%" for="<%=ConfigKey.tokenEndpoint%>">OAuth 2.0 token endpoint:</label><br/>
    <prop:textProperty style="width: 100%;" name="<%=ConfigKey.tokenEndpoint.toString()%>"/><br/>
    <span class="grayNote">Endpoint at which TeamCity server can obtain an token using OAuth 2.0.</span>
</div>
<div>
    <label width="100%" for="<%=ConfigKey.userEndpoint%>">OAuth 2.0 user endpoint:</label><br/>
    <prop:textProperty style="width: 100%;" name="<%=ConfigKey.userEndpoint.toString()%>"/><br/>
    <span class="grayNote">Endpoint at which TeamCity server can obtain information about user</span>
</div>
<br/>
<div>
    <label width="100%" for="<%=ConfigKey.clientId%>">Client ID:</label><br/>
    <prop:textProperty style="width: 100%;" name="<%=ConfigKey.clientId.toString()%>"/><br/>
    <span class="grayNote">OAuth client identifier of this TeamCity server.</span>
</div>
<div>
    <label width="100%" for="<%=ConfigKey.clientSecret%>">Client Secret:</label><br/>
    <prop:textProperty style="width: 100%;" name="<%=ConfigKey.clientSecret.toString()%>"/><br/>
    <span class="grayNote">OAuth client secret of this TeamCity server.</span>
</div>
<div>
    <label width="100%" for="<%=ConfigKey.scope%>">Scope:</label><br/>
    <prop:textProperty style="width: 100%;" name="<%=ConfigKey.scope.toString()%>"/><br/>
    <span class="grayNote">OAuth scope of this TeamCity server.</span>
</div>