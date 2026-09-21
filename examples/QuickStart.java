import pro.mrpc.copier.*;

public class QuickStart {
    public static void main(String[] args) throws Exception {
        System.out.println("=== JavaCopier Quick Start Demo ===");
        String apiKey = "TRIAL";

        DemoAccountClient demo = new DemoAccountClient("https://mt5.mrpc.pro");

        // 1. Provision live demo account
        System.out.println("\n[1] Provisioning live demo account on MetaQuotes-Demo...");
        DemoAccountClient.DemoReply master = demo.openDemoAccount("MetaQuotes-Demo", apiKey);
        System.out.println("    Master Account Provisioned: #" + master.login + " on " + master.server);

        // 2. Connect terminal via ConnectEx with APIKey: TRIAL
        System.out.println("\n[2] Connecting terminal via ConnectEx (APIKey: " + apiKey + ")...");
        DemoAccountClient.ConnectExReply conn = demo.connectEx(master.login, master.password, master.server, apiKey);
        System.out.println("    Terminal Connected! Instance GUID: " + conn.terminalInstanceGuid);

        // 3. Interacting with Copier Service
        System.out.println("\n[3] Interacting with Copier Service (userKey: " + apiKey + ")...");
        try (CopierService svc = new CopierService("copy.mrpc.pro:443", apiKey)) {
            Models.ListReply list = svc.list();
            System.out.println("    Active Copiers count: " + list.copiers.size());
        }

        // 4. Cleanly Disconnect Terminal Session
        System.out.println("\n[4] Disconnecting terminal session " + conn.terminalInstanceGuid + "...");
        DemoAccountClient.DisconnectReply disc = demo.disconnect(conn.terminalInstanceGuid, apiKey);
        System.out.println("    Terminal Cleanly Disconnected: " + disc.uniqueIdentifier + " (Lifetime: " + disc.fullLifeTimeSeconds + "s)");

        System.out.println("\n=== JavaCopier Quick Start Completed Successfully ===");
    }
}
