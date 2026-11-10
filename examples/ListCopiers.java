import pro.mrpc.copier.CopierService;
import pro.mrpc.copier.Models;
public class ListCopiers {
    public static void main(String[] args) {
        try (CopierService service = new CopierService("https://copy.mrpc.pro", System.getenv("MRPC_API_KEY"))) {
            Models.ListReply result = service.list();
            if (!result.ok) throw new IllegalStateException(result.error);
            for (Models.CopierSummary item : result.copiers) System.out.println(item.id + ": paused=" + item.paused);
        }
    }
}
