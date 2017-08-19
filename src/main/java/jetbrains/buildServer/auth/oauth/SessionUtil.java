package jetbrains.buildServer.auth.oauth;

import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;

class SessionUtil {

    @NotNull
    static String getSessionId(@NotNull final HttpServletRequest request) {
        // we must use requested session id, if it is presented, and only if not, then we can use current session id, see TW-23821
        final String requestedSessionId = request.getRequestedSessionId();
        if (requestedSessionId != null) {
            return requestedSessionId;
        }
        return request.getSession().getId();
    }
}
