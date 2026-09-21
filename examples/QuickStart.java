import pro.mrpc.copier.*;
import java.util.List;

public class QuickStart {
    public static void main(String[] args) {
        System.out.println("=== MetaRPC JavaCopier Trade Replication Quick Start ===");
        String apiKey = "TRIAL";
        DemoAccountClient demo = new DemoAccountClient("https://mt5.mrpc.pro");
        String masterGuid = null;
        String slaveGuid = null;
        String copierId = null;

        try (CopierService copier = new CopierService("copy.mrpc.pro:443", apiKey)) {
            // 1. Provision live demo accounts
            System.out.println("\n[1] Provisioning live demo accounts on MetaQuotes-Demo...");
            DemoAccountClient.DemoReply master = demo.openDemoAccount("MetaQuotes-Demo", apiKey);
            System.out.println("    Master Account Provisioned: #" + master.login + " on " + master.server);
            Thread.sleep(1000);

            DemoAccountClient.DemoReply slave = demo.openDemoAccount("MetaQuotes-Demo", apiKey);
            System.out.println("    Slave Account Provisioned:  #" + slave.login + " on " + slave.server);
            Thread.sleep(1000);

            // 2. Connect terminals via ConnectEx with APIKey: TRIAL
            System.out.println("\n[2] Connecting terminals via ConnectEx (APIKey: " + apiKey + ")...");
            DemoAccountClient.ConnectExReply connM = demo.connectEx(master.login, master.password, master.server, apiKey);
            masterGuid = connM.terminalInstanceGuid;
            System.out.println("    Master Terminal Connected! GUID: " + masterGuid);

            DemoAccountClient.ConnectExReply connS = demo.connectEx(slave.login, slave.password, slave.server, apiKey);
            slaveGuid = connS.terminalInstanceGuid;
            System.out.println("    Slave Terminal Connected!  GUID: " + slaveGuid);

            String masterSessionId = DemoAccountClient.toHyphenGuid(masterGuid);
            String slaveSessionId = DemoAccountClient.toHyphenGuid(slaveGuid);

            // 3. Start Trade Copier via gRPC on copy.mrpc.pro:443
            System.out.println("\n[3] Starting Trade Copier via gRPC on copy.mrpc.pro:443...");
            Models.StartRequest req = new Models.StartRequest();
            req.userKey = apiKey;
            req.riskType = "LotMultiplier";
            req.riskValue = "1.0";

            req.master = new Models.Account();
            req.master.type = "MT5";
            req.master.user = master.login;
            req.master.password = master.password;
            req.master.server = master.server;
            req.master.id = masterSessionId;

            req.slave = new Models.Account();
            req.slave.type = "MT5";
            req.slave.user = slave.login;
            req.slave.password = slave.password;
            req.slave.server = slave.server;
            req.slave.id = slaveSessionId;

            Models.StartReply startReply = copier.start(req);
            System.out.println("    gRPC Start Reply: ok=" + startReply.ok + ", copierId=" + startReply.copierId + ", error=" + startReply.error);
            if (!startReply.ok) {
                throw new RuntimeException("Failed to start copier: " + startReply.error);
            }
            copierId = startReply.copierId;

            Thread.sleep(4000);

            // 4. Place Market Order on Master
            System.out.println("\n[4] Opening Market Order on Master (0.01 EURUSD BUY)...");
            long masterTicket = demo.orderSend(masterGuid, "EURUSD", "TMT5_ORDER_TYPE_BUY", 0.01, apiKey);
            System.out.println("    Master Order Placed! Ticket: " + masterTicket);

            // 5. Verify Trade Copied to Slave
            System.out.println("\n[5] Verifying replicated trade on Slave account...");
            boolean replicated = false;
            for (int attempt = 1; attempt <= 15; attempt++) {
                Thread.sleep(2000);
                List<DemoAccountClient.PositionInfo> positions = demo.openedOrders(slaveGuid, apiKey);
                System.out.println("    Attempt " + attempt + ": Slave active positions count = " + positions.size());
                if (!positions.isEmpty()) {
                    DemoAccountClient.PositionInfo first = positions.get(0);
                    System.out.println("    --> CONFIRMED ON SLAVE: Ticket=" + first.ticket + ", Symbol=" + first.symbol);
                    replicated = true;
                    break;
                }
            }

            if (!replicated) {
                System.out.println("    WARNING: Slave trade replication timed out.");
            } else {
                System.out.println("    SUCCESS: Trade successfully replicated to slave account!");
            }

            // 6. Close Position on Master
            if (masterTicket != 0) {
                System.out.println("\n[6] Closing Master trade ticket #" + masterTicket + "...");
                String closeResp = demo.orderClose(masterGuid, masterTicket, apiKey);
                System.out.println("    Master OrderClose result: " + closeResp);

                // 7. Verify Trade Closed on Slave
                System.out.println("\n[7] Verifying trade closed on Slave...");
                for (int attempt = 1; attempt <= 15; attempt++) {
                    Thread.sleep(2000);
                    List<DemoAccountClient.PositionInfo> positions = demo.openedOrders(slaveGuid, apiKey);
                    if (positions.isEmpty()) {
                        System.out.println("    SUCCESS: Slave position closed by trade copier!");
                        break;
                    }
                    System.out.println("    Attempt " + attempt + ": Slave positions still open: " + positions.size());
                }
            }

            // 8. Remove Copier via gRPC
            if (copierId != null && !copierId.isEmpty()) {
                System.out.println("\n[8] Removing Copier " + copierId + " via gRPC...");
                Models.SimpleReply remReply = copier.remove(copierId);
                System.out.println("    Copier Remove Reply: ok=" + remReply.ok);
            }
        } catch (Exception e) {
            System.err.println("Execution error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 9. Cleanly Disconnect Terminal Sessions
            System.out.println("\n[9] Disconnecting terminal sessions cleanly via /Disconnect...");
            if (masterGuid != null) {
                try {
                    DemoAccountClient.DisconnectReply discM = demo.disconnect(masterGuid, apiKey);
                    System.out.println("    Master Terminal Cleanly Disconnected: " + discM.uniqueIdentifier + " (Lifetime: " + discM.fullLifeTimeSeconds + "s)");
                } catch (Exception ex) {
                    System.out.println("    Master disconnect error: " + ex.getMessage());
                }
            }
            if (slaveGuid != null) {
                try {
                    DemoAccountClient.DisconnectReply discS = demo.disconnect(slaveGuid, apiKey);
                    System.out.println("    Slave Terminal Cleanly Disconnected:  " + discS.uniqueIdentifier + " (Lifetime: " + discS.fullLifeTimeSeconds + "s)");
                } catch (Exception ex) {
                    System.out.println("    Slave disconnect error: " + ex.getMessage());
                }
            }
            System.out.println("\n=== JavaCopier Trade Replication Completed Successfully ===");
        }
    }
}
