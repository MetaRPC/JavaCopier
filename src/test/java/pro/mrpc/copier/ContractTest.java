package pro.mrpc.copier;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.io.*;
public class ContractTest {
 @Test void falseRepliesStayFalse() {
   assertFalse(GrpcWire.decodeStartReply(new byte[0]).ok);
   assertFalse(GrpcWire.decodeListReply(new byte[0]).ok);
   assertFalse(GrpcWire.decodeSimpleReply(new byte[0]).ok);
   assertTrue(GrpcWire.decodeSimpleReply(new byte[]{8,1}).ok);
 }
 @Test void malformedFramesFail() {
   for(byte[] b:new byte[][]{ {}, {0,0,0,0,9,8}, {1,0,0,0,1,8} }) assertThrows(IllegalArgumentException.class,()->GrpcWire.unframeGrpc(b));
   byte[] bytes={8,1};assertArrayEquals(bytes,GrpcWire.unframeGrpc(GrpcWire.frameGrpc(bytes)));
 }
 @Test void exactIdentifiers() throws Exception {
   for(long n:new long[]{0,127,128,9007199254740993L,Long.MAX_VALUE}) {
     ByteArrayOutputStream out=new ByteArrayOutputStream();GrpcWire.writeVarint(out,n);
     assertEquals(n,GrpcWire.readVarint(new ByteArrayInputStream(out.toByteArray())));
   }
 }
 @Test void invalidKeysFailBeforeNetwork() {
   for(String key:new String[]{"","TRIAL","trial","a b","x\r\nInjected: yes"}) assertThrows(IllegalArgumentException.class,()->new CopierService("copy.mrpc.pro",key));
 }
 @Test void startCarriesIdentityAndSizing() {
   Models.StartRequest req=new Models.StartRequest();req.riskType="FixedLot";req.riskValue="0.01";req.copySl=false;req.copyTp=true;
   String bytes=new String(GrpcWire.encodeStartRequest(req,"owner-test","manager-test"),java.nio.charset.StandardCharsets.UTF_8);
   assertTrue(bytes.contains("owner-test"));assertTrue(bytes.contains("manager-test"));assertTrue(bytes.contains("FixedLot"));assertTrue(bytes.contains("0.01"));
 }
}
