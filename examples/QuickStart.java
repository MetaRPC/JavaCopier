package examples;

import pro.mrpc.copier.*;

public class QuickStart {
    public static void main(String[] args) {
        System.out.println("=== JavaCopier Quick Start ===");
        DemoAccountClient demo = new DemoAccountClient("mt5.mrpc.pro:443");
        var master = demo.openDemoAccount("MetaQuotes-Demo", "Master", "Trader", "master@example.com", "MetaQuotes-Demo");
        var slave = demo.openDemoAccount("MetaQuotes-Demo", "Slave", "Follower", "slave@example.com", "MetaQuotes-Demo");
        System.out.println("Created accounts: Master=" + master.login + ", Slave=" + slave.login);

        try (CopierService svc = new CopierService("copy.mrpc.pro:443", "YOUR_USER_KEY", "YOUR_MANAGER_KEY")) {
            Models.StartRequest req = new Models.StartRequest();
            req.master.user = master.login;
            req.master.password = master.password;
            req.master.server = master.server;
            req.slave.user = slave.login;
            req.slave.password = slave.password;
            req.slave.server = slave.server;
            req.riskType = "LotMultiplier";
            req.riskValue = "1.5";

            var reply = svc.start(req);
            System.out.println("Started Copier ID: " + reply.copierId);
        }
    }
}
