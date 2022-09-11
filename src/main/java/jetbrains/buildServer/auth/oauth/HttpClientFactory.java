package jetbrains.buildServer.auth.oauth;


import okhttp3.OkHttpClient;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

class HttpClientFactory {

    static OkHttpClient createClient(boolean allowInsecure) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        if (allowInsecure) {
            builder.hostnameVerifier((s, sslSession) -> true)
                    .sslSocketFactory(createInsecureSslSocketFactory(), new AcceptEverythingTrustManager());
        }
        return builder.build();
    }

    private static SSLSocketFactory createInsecureSslSocketFactory() {
        try {
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new TrustManager[]{new AcceptEverythingTrustManager()}, null);
            return context.getSocketFactory();
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    private static class AcceptEverythingTrustManager implements X509TrustManager {
        public void checkClientTrusted(X509Certificate[] certificates, String authType) throws CertificateException {
        }

        public void checkServerTrusted(X509Certificate[] certificates, String authType) throws CertificateException {
        }

        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }
}
