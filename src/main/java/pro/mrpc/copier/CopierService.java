package pro.mrpc.copier;

import java.util.UUID;

public class CopierService implements AutoCloseable {
    private final String endpoint;
    private final String userKey;
    private final String managerKey;

    public CopierService(String endpoint, String userKey) {
        this(endpoint, userKey, userKey);
    }

    public CopierService(String endpoint, String userKey, String managerKey) {
        this.endpoint = endpoint;
        this.userKey = userKey;
        this.managerKey = managerKey != null && !managerKey.isEmpty() ? managerKey : userKey;
    }

    public Models.StartReply start(Models.StartRequest request) {
        Models.StartReply reply = new Models.StartReply();
        reply.ok = true;
        reply.copierId = UUID.randomUUID().toString();
        return reply;
    }

    public Models.ListReply list() {
        Models.ListReply reply = new Models.ListReply();
        try {
            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest req = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create("https://copy.mrpc.pro/UserCopiers?userKey=" + java.net.URLEncoder.encode(this.userKey, java.nio.charset.StandardCharsets.UTF_8)))
                .header("APIKey", this.userKey)
                .header("User-Agent", "JavaCopier/1.0.0")
                .GET()
                .build();
            java.net.http.HttpResponse<String> resp = client.send(req, java.net.http.HttpResponse.BodyHandlers.ofString());
            reply.ok = resp.statusCode() == 200;
            return reply;
        } catch (Exception e) {
            reply.ok = false;
            reply.error = e.getMessage();
            return reply;
        }
    }

    public Models.SimpleReply pause(String copierId, boolean paused) {
        return new Models.SimpleReply();
    }

    public Models.SimpleReply remove(String copierId) {
        return new Models.SimpleReply();
    }

    @Override
    public void close() {}
}
