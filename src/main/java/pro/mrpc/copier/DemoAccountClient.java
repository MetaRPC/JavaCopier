package pro.mrpc.copier;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DemoAccountClient {
    private final String endpoint;
    private final HttpClient http;

    public DemoAccountClient(String endpoint) {
        String clean = endpoint.replace("http://", "").replace("https://", "").replace(":443", "").replaceAll("/$", "");
        this.endpoint = "https://" + clean;
        this.http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(30)).build();
    }

    public static class DemoReply {
        public int resultCode;
        public long login;
        public String password;
        public String investor;
        public String server;
    }

    public static class ConnectExReply {
        public String terminalInstanceGuid;
        public String terminalType = "MT5";
    }

    public static class DisconnectReply {
        public String uniqueIdentifier;
        public int fullLifeTimeSeconds;
    }

    public DemoReply openDemoAccount(String server, String apiKey) throws Exception {
        String url = endpoint + "/DemoAccount/Open?server=" + URLEncoder.encode(server, StandardCharsets.UTF_8);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("APIKey", apiKey)
                .header("User-Agent", "JavaCopier/1.0.0")
                .timeout(Duration.ofSeconds(30))
                .GET()
                .build();
        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200) {
            throw new RuntimeException("DemoAccount/Open failed with HTTP " + resp.statusCode() + ": " + resp.body());
        }
        String body = resp.body();
        DemoReply rep = new DemoReply();
        rep.login = Long.parseLong(extractJsonString(body, "login"));
        rep.password = extractJsonString(body, "password");
        rep.investor = extractJsonString(body, "investor");
        rep.server = extractJsonString(body, "server");
        return rep;
    }

    public ConnectExReply connectEx(long user, String password, String server, String apiKey) throws Exception {
        String url = endpoint + "/ConnectEx?user=" + user
                + "&password=" + URLEncoder.encode(password, StandardCharsets.UTF_8)
                + "&mtClusterName=" + URLEncoder.encode(server, StandardCharsets.UTF_8);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("APIKey", apiKey)
                .header("User-Agent", "JavaCopier/1.0.0")
                .timeout(Duration.ofSeconds(120))
                .GET()
                .build();
        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200) {
            throw new RuntimeException("ConnectEx failed with HTTP " + resp.statusCode() + ": " + resp.body());
        }
        String body = resp.body();
        ConnectExReply rep = new ConnectExReply();
        rep.terminalInstanceGuid = extractJsonString(body, "terminalInstanceGuid");
        return rep;
    }

    public DisconnectReply disconnect(String terminalId, String apiKey) throws Exception {
        String url = endpoint + "/Disconnect";
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("APIKey", apiKey)
                .header("id", terminalId)
                .header("User-Agent", "JavaCopier/1.0.0")
                .timeout(Duration.ofSeconds(60))
                .GET()
                .build();
        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200) {
            throw new RuntimeException("Disconnect failed with HTTP " + resp.statusCode() + ": " + resp.body());
        }
        String body = resp.body();
        DisconnectReply rep = new DisconnectReply();
        rep.uniqueIdentifier = extractJsonString(body, "uniqueIdentifier");
        String lt = extractJsonNumber(body, "fullLifeTimeSeconds");
        rep.fullLifeTimeSeconds = lt.isEmpty() ? 0 : Integer.parseInt(lt);
        return rep;
    }

    public static String toHyphenGuid(String guid) {
        String clean = guid.replace("mt5_live_", "").replace("-", "");
        if (clean.length() < 32) return guid;
        return clean.substring(0, 8) + "-" +
               clean.substring(8, 12) + "-" +
               clean.substring(12, 16) + "-" +
               clean.substring(16, 20) + "-" +
               clean.substring(20, 32);
    }

    public static class PositionInfo {
        public long ticket;
        public String symbol;
        public double volume;
        public String type;
    }

    public long orderSend(String terminalId, String symbol, String operation, double volume, String apiKey) throws Exception {
        String url = endpoint + "/OrderSend?id=" + URLEncoder.encode(terminalId, StandardCharsets.UTF_8)
                + "&symbol=" + URLEncoder.encode(symbol, StandardCharsets.UTF_8)
                + "&operation=" + URLEncoder.encode(operation, StandardCharsets.UTF_8)
                + "&volume=" + volume;
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("APIKey", apiKey)
                .header("id", terminalId)
                .header("User-Agent", "JavaCopier/1.0.0")
                .timeout(Duration.ofSeconds(30))
                .GET()
                .build();
        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200) {
            throw new RuntimeException("OrderSend failed with HTTP " + resp.statusCode() + ": " + resp.body());
        }
        String body = resp.body();
        String ord = extractJsonNumber(body, "order");
        if (ord.isEmpty()) ord = extractJsonNumber(body, "ticket");
        return ord.isEmpty() ? 0 : Long.parseLong(ord);
    }

    public java.util.List<PositionInfo> openedOrders(String terminalId, String apiKey) throws Exception {
        String url = endpoint + "/OpenedOrders?id=" + URLEncoder.encode(terminalId, StandardCharsets.UTF_8);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("APIKey", apiKey)
                .header("id", terminalId)
                .header("User-Agent", "JavaCopier/1.0.0")
                .timeout(Duration.ofSeconds(30))
                .GET()
                .build();
        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200) {
            throw new RuntimeException("OpenedOrders failed with HTTP " + resp.statusCode() + ": " + resp.body());
        }
        String body = resp.body();
        java.util.List<PositionInfo> list = new java.util.ArrayList<>();
        Pattern p = Pattern.compile("\"ticket\"\\s*:\\s*([0-9]+)");
        Matcher m = p.matcher(body);
        while (m.find()) {
            PositionInfo info = new PositionInfo();
            info.ticket = Long.parseLong(m.group(1));
            info.symbol = extractJsonString(body, "symbol");
            info.type = extractJsonString(body, "type");
            list.add(info);
        }
        return list;
    }

    public String orderClose(String terminalId, long ticket, String apiKey) throws Exception {
        String url = endpoint + "/OrderClose?id=" + URLEncoder.encode(terminalId, StandardCharsets.UTF_8)
                + "&ticket=" + ticket + "&volume=0&slippage=20";
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("APIKey", apiKey)
                .header("id", terminalId)
                .header("User-Agent", "JavaCopier/1.0.0")
                .timeout(Duration.ofSeconds(30))
                .GET()
                .build();
        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200) {
            throw new RuntimeException("OrderClose failed with HTTP " + resp.statusCode() + ": " + resp.body());
        }
        return resp.body();
    }

    private static String extractJsonString(String json, String key) {
        Pattern p = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]*)\"");
        Matcher m = p.matcher(json);
        return m.find() ? m.group(1) : "";
    }

    private static String extractJsonNumber(String json, String key) {
        Pattern p = Pattern.compile("\"" + key + "\"\\s*:\\s*([0-9]+)");
        Matcher m = p.matcher(json);
        return m.find() ? m.group(1) : "";
    }
}
