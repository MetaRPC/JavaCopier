package pro.mrpc.copier;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;

public class GrpcWire {

    public static void writeVarint(ByteArrayOutputStream out, long value) {
        while ((value & ~0x7FL) != 0) {
            out.write((int) ((value & 0x7F) | 0x80));
            value >>>= 7;
        }
        out.write((int) (value & 0x7F));
    }

    public static void writeTag(ByteArrayOutputStream out, int fieldNumber, int wireType) {
        writeVarint(out, ((long) fieldNumber << 3) | wireType);
    }

    public static void writeString(ByteArrayOutputStream out, int fieldNumber, String value) {
        if (value == null || value.isEmpty()) return;
        byte[] b = value.getBytes(StandardCharsets.UTF_8);
        writeTag(out, fieldNumber, 2);
        writeVarint(out, b.length);
        out.write(b, 0, b.length);
    }

    public static void writeUInt64(ByteArrayOutputStream out, int fieldNumber, long value) {
        if (value == 0) return;
        writeTag(out, fieldNumber, 0);
        writeVarint(out, value);
    }

    public static void writeBool(ByteArrayOutputStream out, int fieldNumber, boolean value) {
        writeTag(out, fieldNumber, 0);
        writeVarint(out, value ? 1 : 0);
    }

    public static void writeMessage(ByteArrayOutputStream out, int fieldNumber, byte[] messageBytes) {
        if (messageBytes == null || messageBytes.length == 0) return;
        writeTag(out, fieldNumber, 2);
        writeVarint(out, messageBytes.length);
        out.write(messageBytes, 0, messageBytes.length);
    }

    public static byte[] encodeAccount(Models.Account a) {
        if (a == null) return new byte[0];
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        writeString(out, 1, a.type != null ? a.type : "MT5");
        writeUInt64(out, 2, a.user);
        writeString(out, 3, a.password);
        writeString(out, 4, a.server);
        writeString(out, 5, a.name);
        writeString(out, 6, a.id);
        return out.toByteArray();
    }

    public static byte[] encodeStartRequest(Models.StartRequest req, String fallbackUserKey, String fallbackManagerKey) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        String userKey = req.userKey != null && !req.userKey.isEmpty() ? req.userKey : fallbackUserKey;
        String managerKey = req.managerKey != null && !req.managerKey.isEmpty() ? req.managerKey : (fallbackManagerKey != null && !fallbackManagerKey.isEmpty() ? fallbackManagerKey : userKey);
        writeString(out, 1, userKey);
        writeString(out, 2, managerKey);
        writeMessage(out, 3, encodeAccount(req.master));
        writeMessage(out, 4, encodeAccount(req.slave));
        writeString(out, 5, req.riskType != null ? req.riskType : "LotMultiplier");
        writeString(out, 6, req.riskValue != null ? req.riskValue : "1.0");
        writeString(out, 7, req.fixedMasterBalance);
        if (req.copySl) writeBool(out, 8, true);
        if (req.copyTp) writeBool(out, 9, true);
        if (req.copyPendingOrders) writeBool(out, 10, true);
        if (req.reverseCopy) writeBool(out, 11, true);
        return out.toByteArray();
    }

    public static byte[] encodeListRequest(String userKey) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        writeString(out, 1, userKey);
        return out.toByteArray();
    }

    public static byte[] encodePauseRequest(String userKey, String copierId, boolean paused) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        writeString(out, 1, userKey);
        writeString(out, 2, copierId);
        writeBool(out, 3, paused);
        return out.toByteArray();
    }

    public static byte[] encodeRemoveRequest(String userKey, String copierId) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        writeString(out, 1, userKey);
        writeString(out, 2, copierId);
        return out.toByteArray();
    }

    public static byte[] frameGrpc(byte[] protoPayload) {
        byte[] frame = new byte[5 + protoPayload.length];
        frame[0] = 0; // Uncompressed
        int len = protoPayload.length;
        frame[1] = (byte) ((len >>> 24) & 0xFF);
        frame[2] = (byte) ((len >>> 16) & 0xFF);
        frame[3] = (byte) ((len >>> 8) & 0xFF);
        frame[4] = (byte) (len & 0xFF);
        System.arraycopy(protoPayload, 0, frame, 5, len);
        return frame;
    }

    public static byte[] unframeGrpc(byte[] grpcResponse) {
        if (grpcResponse == null || grpcResponse.length < 5) {
            throw new IllegalArgumentException("Missing gRPC response frame");
        }
        int len = ((grpcResponse[1] & 0xFF) << 24) |
                  ((grpcResponse[2] & 0xFF) << 16) |
                  ((grpcResponse[3] & 0xFF) << 8) |
                  (grpcResponse[4] & 0xFF);
        if (grpcResponse[0] != 0 || len < 0 || len != grpcResponse.length - 5) throw new IllegalArgumentException("Invalid or truncated gRPC frame");
        int actual = len;
        byte[] payload = new byte[actual];
        System.arraycopy(grpcResponse, 5, payload, 0, actual);
        return payload;
    }

    public static long readVarint(InputStream in) throws IOException {
        long result = 0;
        int shift = 0;
        while (shift < 64) {
            int b = in.read();
            if (b == -1) throw new IOException("Unexpected EOF while reading varint");
            result |= (long) (b & 0x7F) << shift;
            if ((b & 0x80) == 0) return result;
            shift += 7;
        }
        throw new IOException("Malformed varint");
    }

    public static void skipField(InputStream in, int wireType) throws IOException {
        if (wireType == 0) {
            readVarint(in);
        } else if (wireType == 1) {
            in.readNBytes(8);
        } else if (wireType == 2) {
            int len = (int) readVarint(in);
            in.readNBytes(len);
        } else if (wireType == 5) {
            in.readNBytes(4);
        } else {
            throw new IOException("Unsupported wire type: " + wireType);
        }
    }

    public static Models.StartReply decodeStartReply(byte[] data) {
        Models.StartReply reply = new Models.StartReply();
        try {
            ByteArrayInputStream in = new ByteArrayInputStream(data);
            while (in.available() > 0) {
                long tag = readVarint(in);
                int field = (int) (tag >>> 3);
                int wire = (int) (tag & 0x07);
                if (wire == 0) {
                    long val = readVarint(in);
                    if (field == 1) reply.ok = (val != 0);
                } else if (wire == 2) {
                    int len = (int) readVarint(in);
                    String s = new String(in.readNBytes(len), StandardCharsets.UTF_8);
                    if (field == 2) reply.copierId = s;
                    else if (field == 3) reply.error = s;
                } else {
                    skipField(in, wire);
                }
            }
        } catch (Exception e) {
            reply.ok = false;
            reply.error = "Failed to parse StartReply: " + e.getMessage();
        }
        return reply;
    }

    public static Models.SimpleReply decodeSimpleReply(byte[] data) {
        Models.SimpleReply reply = new Models.SimpleReply();
        try {
            ByteArrayInputStream in = new ByteArrayInputStream(data);
            while (in.available() > 0) {
                long tag = readVarint(in);
                int field = (int) (tag >>> 3);
                int wire = (int) (tag & 0x07);
                if (wire == 0) {
                    long val = readVarint(in);
                    if (field == 1) reply.ok = (val != 0);
                } else if (wire == 2) {
                    int len = (int) readVarint(in);
                    String s = new String(in.readNBytes(len), StandardCharsets.UTF_8);
                    if (field == 2) reply.error = s;
                } else {
                    skipField(in, wire);
                }
            }
        } catch (Exception e) {
            reply.ok = false;
            reply.error = "Failed to parse SimpleReply: " + e.getMessage();
        }
        return reply;
    }

    public static Models.ListReply decodeListReply(byte[] data) {
        Models.ListReply reply = new Models.ListReply();
        try {
            ByteArrayInputStream in = new ByteArrayInputStream(data);
            while (in.available() > 0) {
                long tag = readVarint(in);
                int field = (int) (tag >>> 3);
                int wire = (int) (tag & 0x07);
                if (wire == 0) {
                    long val = readVarint(in);
                    if (field == 1) reply.ok = (val != 0);
                } else if (wire == 2) {
                    int len = (int) readVarint(in);
                    byte[] bytes = in.readNBytes(len);
                    if (field == 2) {
                        reply.copiers.add(decodeCopierSummary(bytes));
                    } else if (field == 3) {
                        reply.error = new String(bytes, StandardCharsets.UTF_8);
                    }
                } else {
                    skipField(in, wire);
                }
            }
        } catch (Exception e) {
            reply.ok = false;
            reply.error = "Failed to parse ListReply: " + e.getMessage();
        }
        return reply;
    }

    public static Models.CopierSummary decodeCopierSummary(byte[] data) {
        Models.CopierSummary s = new Models.CopierSummary();
        try {
            ByteArrayInputStream in = new ByteArrayInputStream(data);
            while (in.available() > 0) {
                long tag = readVarint(in);
                int field = (int) (tag >>> 3);
                int wire = (int) (tag & 0x07);
                if (wire == 0) {
                    long val = readVarint(in);
                    if (field == 3) s.masterUser = val;
                    else if (field == 6) s.slaveUser = val;
                    else if (field == 10) s.paused = (val != 0);
                } else if (wire == 2) {
                    int len = (int) readVarint(in);
                    String str = new String(in.readNBytes(len), StandardCharsets.UTF_8);
                    if (field == 1) s.id = str;
                    else if (field == 2) s.masterType = str;
                    else if (field == 4) s.masterServer = str;
                    else if (field == 5) s.slaveType = str;
                    else if (field == 7) s.slaveServer = str;
                    else if (field == 8) s.riskType = str;
                    else if (field == 9) s.riskValue = str;
                    else if (field == 11) s.pauseReason = str;
                } else {
                    skipField(in, wire);
                }
            }
        } catch (Exception ignored) {}
        return s;
    }

    // grpc-java handles HTTP/2 framing, trailers, TLS verification and deadlines.
    public static byte[] callGrpc(String endpoint, String service, String method, byte[] payload, String apiKey) throws Exception {
        URI uri = URI.create(endpoint.contains("://") ? endpoint : "https://" + endpoint);
        if (!"https".equals(uri.getScheme()) || uri.getHost() == null)
            throw new IllegalArgumentException("Use a TLS (https) copier endpoint.");
        io.grpc.ManagedChannel channel = io.grpc.ManagedChannelBuilder.forAddress(uri.getHost(), uri.getPort() < 0 ? 443 : uri.getPort()).useTransportSecurity().build();
        try {
            io.grpc.Metadata metadata = new io.grpc.Metadata();
            metadata.put(io.grpc.Metadata.Key.of("apikey", io.grpc.Metadata.ASCII_STRING_MARSHALLER), apiKey);
            io.grpc.Channel authenticated = io.grpc.ClientInterceptors.intercept(channel, io.grpc.stub.MetadataUtils.newAttachHeadersInterceptor(metadata));
            io.grpc.MethodDescriptor.Marshaller<byte[]> bytes = new io.grpc.MethodDescriptor.Marshaller<byte[]>() {
                public InputStream stream(byte[] value) { return new ByteArrayInputStream(value); }
                public byte[] parse(InputStream input) {
                    try { return input.readAllBytes(); }
                    catch (IOException e) { throw new java.io.UncheckedIOException(e); }
                }
            };
            io.grpc.MethodDescriptor<byte[], byte[]> descriptor = io.grpc.MethodDescriptor.<byte[], byte[]>newBuilder()
                .setType(io.grpc.MethodDescriptor.MethodType.UNARY).setFullMethodName(service + "/" + method)
                .setRequestMarshaller(bytes).setResponseMarshaller(bytes).build();
            return io.grpc.stub.ClientCalls.blockingUnaryCall(authenticated, descriptor,
                io.grpc.CallOptions.DEFAULT.withDeadlineAfter(180, java.util.concurrent.TimeUnit.SECONDS), payload);
        } finally { channel.shutdownNow(); }
    }
}
