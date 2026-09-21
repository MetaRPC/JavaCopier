package pro.mrpc.copier;

import java.util.ArrayList;
import java.util.List;

public class Models {
    public static class Account {
        public String type = "MT5";
        public long user;
        public String password = "";
        public String server = "";
        public String name = "";
        public String id = "";
    }

    public static class StartRequest {
        public String userKey = "";
        public String managerKey = "";
        public Account master = new Account();
        public Account slave = new Account();
        public String riskType = "LotMultiplier";
        public String riskValue = "1.0";
        public String fixedMasterBalance = "";
        public boolean copySl = true;
        public boolean copyTp = true;
        public boolean copyPendingOrders = false;
        public boolean reverseCopy = false;
    }

    public static class StartReply {
        public boolean ok = true;
        public String copierId = "";
        public String error = "";
    }

    public static class CopierSummary {
        public String id = "";
        public String masterType = "";
        public long masterUser;
        public String masterServer = "";
        public String slaveType = "";
        public long slaveUser;
        public String slaveServer = "";
        public String riskType = "";
        public String riskValue = "";
        public boolean paused;
        public String pauseReason = "";
    }

    public static class ListReply {
        public boolean ok = true;
        public List<CopierSummary> copiers = new ArrayList<>();
        public String error = "";
    }

    public static class SimpleReply {
        public boolean ok = true;
        public String error = "";
    }
}
