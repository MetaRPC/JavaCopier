package pro.mrpc.copier;

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
        try {
            byte[] reqPayload = GrpcWire.encodeStartRequest(request, this.userKey, this.managerKey);
            byte[] respPayload = GrpcWire.callGrpc(endpoint, "copier.CopierService", "Start", reqPayload);
            return GrpcWire.decodeStartReply(respPayload);
        } catch (Exception e) {
            Models.StartReply reply = new Models.StartReply();
            reply.ok = false;
            reply.error = e.getMessage();
            return reply;
        }
    }

    public Models.ListReply list() {
        try {
            byte[] reqPayload = GrpcWire.encodeListRequest(this.userKey);
            byte[] respPayload = GrpcWire.callGrpc(endpoint, "copier.CopierService", "List", reqPayload);
            return GrpcWire.decodeListReply(respPayload);
        } catch (Exception e) {
            Models.ListReply reply = new Models.ListReply();
            reply.ok = false;
            reply.error = e.getMessage();
            return reply;
        }
    }

    public Models.SimpleReply pause(String copierId, boolean paused) {
        try {
            byte[] reqPayload = GrpcWire.encodePauseRequest(this.userKey, copierId, paused);
            byte[] respPayload = GrpcWire.callGrpc(endpoint, "copier.CopierService", "Pause", reqPayload);
            return GrpcWire.decodeSimpleReply(respPayload);
        } catch (Exception e) {
            Models.SimpleReply reply = new Models.SimpleReply();
            reply.ok = false;
            reply.error = e.getMessage();
            return reply;
        }
    }

    public Models.SimpleReply remove(String copierId) {
        try {
            byte[] reqPayload = GrpcWire.encodeRemoveRequest(this.userKey, copierId);
            byte[] respPayload = GrpcWire.callGrpc(endpoint, "copier.CopierService", "Remove", reqPayload);
            return GrpcWire.decodeSimpleReply(respPayload);
        } catch (Exception e) {
            Models.SimpleReply reply = new Models.SimpleReply();
            reply.ok = false;
            reply.error = e.getMessage();
            return reply;
        }
    }

    @Override
    public void close() {}
}
