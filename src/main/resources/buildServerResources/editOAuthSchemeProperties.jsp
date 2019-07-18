<%@ page import="jetbrains.buildServer.auth.oauth.ConfigKey" %>
<%@ include file="/include-internal.jsp" %>
<%@ taglib prefix="prop" tagdir="/WEB-INF/tags/props" %>
<div>
    <jsp:include page="/admin/allowCreatingNewUsersByLogin.jsp"/>
</div>
<br/>
<script type="text/javascript">
    (function () {
        BS.TeamCityOAuth = {
            selectedPreset: undefined,
            init: function (select) {
                this.selector = $j(select);
                this.selectPresetType();
            },
            selectPresetType: function () {
                this.selectedPreset = this.selector.val();
                this.onTypeChanged();
                this.displayOrganizations();
            },
            onTypeChanged: function () {
                var isCustom = (this.selectedPreset === 'custom'),
                        settings = $j('#oauth_server_settings');
                if (isCustom) {
                    settings.show();
                } else {
                    settings.hide();
                }
            },
            displayOrganizations: function() {
              var isGithub = (this.selectedPreset === 'github'),
                orgs = $j('#github_organizations');
              if (isGithub) {
                orgs.show();
              } else {
                orgs.hide();
              }
            }
        };
    })();
</script>
<div>
    <label for="${config.pre}">Preset:</label>
    <prop:selectProperty name="<%=ConfigKey.preset.toString()%>"
                         id="<%=ConfigKey.preset.toString()%>"
                         multiple="false"
                         onchange="BS.TeamCityOAuth.selectPresetType()">
        <prop:option value="github">GitHub</prop:option>
        <prop:option value="bitbucket">Bitbucket</prop:option>
        <prop:option value="google">Google</prop:option>
        <prop:option value="azure">Azure Active Directory</prop:option>
        <prop:option value="custom">Custom</prop:option>
    </prop:selectProperty><br/>
    <span class="grayNote">Preset settings</span>
</div>
<div id="oauth_server_settings">
    <div>
        <label for="<%=ConfigKey.authorizeEndpoint%>">OAuth 2.0 authorization endpoint:</label><br/>
        <prop:textProperty style="width: 100%;" name="<%=ConfigKey.authorizeEndpoint.toString()%>"/><br/>
        <span class="grayNote">Endpoint at which TeamCity server can obtain an authorization code using OAuth 2.0.</span>
    </div>
    <div>
        <label for="<%=ConfigKey.tokenEndpoint%>">OAuth 2.0 token endpoint:</label><br/>
        <prop:textProperty style="width: 100%;" name="<%=ConfigKey.tokenEndpoint.toString()%>"/><br/>
        <span class="grayNote">Endpoint at which TeamCity server can obtain an token using OAuth 2.0.</span>
    </div>
    <div>
        <label for="<%=ConfigKey.userEndpoint%>">OAuth 2.0 user endpoint:</label><br/>
        <prop:textProperty style="width: 100%;" name="<%=ConfigKey.userEndpoint.toString()%>"/><br/>
        <span class="grayNote">Endpoint at which TeamCity server can obtain information about user</span>
    </div>
</div>
<div>
    <label for="<%=ConfigKey.clientId%>">Client ID:</label><br/>
    <prop:textProperty style="width: 100%;" name="<%=ConfigKey.clientId.toString()%>"/><br/>
    <span class="grayNote">OAuth client identifier of this TeamCity server.</span>
</div>
<div>
    <label for="<%=ConfigKey.clientSecret%>">Client Secret:</label><br/>
    <prop:textProperty style="width: 100%;" name="<%=ConfigKey.clientSecret.toString()%>"/><br/>
    <span class="grayNote">OAuth client secret of this TeamCity server.</span>
</div>
<div>
    <label for="<%=ConfigKey.scope%>">Scope:</label><br/>
    <prop:textProperty style="width: 100%;" name="<%=ConfigKey.scope.toString()%>"/><br/>
    <span class="grayNote">OAuth scope of this TeamCity server.</span>
</div>
<div>
    <label for="<%=ConfigKey.emailDomain%>">Email Domain:</label><br/>
    <prop:textProperty style="width: 100%;" name="<%=ConfigKey.emailDomain.toString()%>"/><br/>
    <span class="grayNote">Authorize only users with emails in these domains (optional). Please separate the domains by space.</span>
</div>
<div id="github_organizations">
    <label for="<%=ConfigKey.organizations%>">Organizations:</label><br/>
    <prop:textProperty style="width: 100%;" name="<%=ConfigKey.organizations.toString()%>"/><br/>
    <span class="grayNote">Authorize only users who belongs to these organizations (optional). Please separate the organizations by comma. </span>
</div>
<div>
    <prop:checkboxProperty uncheckedValue="false" name="<%=ConfigKey.hideLoginForm.toString()%>"/>
    <label for="<%=ConfigKey.hideLoginForm%>">Hide login form</label><br/>
    <span class="grayNote">Hide user/password login form on Teamcity login page.</span>
</div>
<div>
    <prop:checkboxProperty uncheckedValue="false" name="<%=ConfigKey.allowInsecureHttps.toString()%>"/>
    <label for="<%=ConfigKey.allowInsecureHttps%>">Insecure https</label><br/>
    <span class="grayNote">Allow insecure https access like invalid certificate</span>
</div>
<script type="text/javascript">
    BS.TeamCityOAuth.init('#<%=ConfigKey.preset.toString()%>');
</script>