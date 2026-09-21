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
        reply.ok = true;
        Models.CopierSummary s = new Models.CopierSummary();
        s.id = UUID.randomUUID().toString();
        s.masterType = "MT5";
        s.masterUser = 10001;
        s.masterServer = "MetaQuotes-Demo";
        s.slaveType = "MT5";
        s.slaveUser = 10002;
        s.slaveServer = "MetaQuotes-Demo";
        s.riskType = "LotMultiplier";
        s.riskValue = "1.5";
        reply.copiers.add(s);
        return reply;
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
