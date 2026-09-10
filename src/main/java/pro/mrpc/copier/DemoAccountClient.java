package pro.mrpc.copier;

import java.util.Random;

public class DemoAccountClient {
    private final String endpoint;
    public DemoAccountClient(String endpoint) { this.endpoint = endpoint; }

    public static class DemoReply {
        public int resultCode = 0;
        public long login;
        public String password;
        public String investor;
        public String server;
    }

    public DemoReply openDemoAccount(String company, String firstName, String lastName, String email, String server) {
        Random r = new Random();
        DemoReply rep = new DemoReply();
        rep.login = 100000 + r.nextInt(900000);
        rep.password = "Demo" + (1000 + r.nextInt(9000)) + "!";
        rep.investor = "Inv" + (1000 + r.nextInt(9000)) + "!";
        rep.server = server;
        return rep;
    }
}
