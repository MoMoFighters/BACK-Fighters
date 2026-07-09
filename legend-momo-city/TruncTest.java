import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.util.*;

public class TruncTest {
    static void check(String label, int requestedSize) {
        int unpinnedSize = Math.max(requestedSize - 1, 1);
        Pageable unpinnedPageable = PageRequest.of(0, unpinnedSize);
        // 실제 DB에서 일반 공지가 unpinnedSize개 반환된다고 가정(11개 있으니 충분)
        List<String> unpinnedContent = new ArrayList<>();
        for (int i = 0; i < unpinnedSize; i++) unpinnedContent.add("일반" + i);

        List<String> merged = new ArrayList<>();
        merged.add("고정공지");
        merged.addAll(unpinnedContent);

        if (merged.size() > requestedSize) {
            merged = merged.subList(0, requestedSize);
        }
        System.out.println(label + " -> requestedSize=" + requestedSize + ", merged=" + merged);
    }
    public static void main(String[] args) {
        check("size=1 극단케이스", 1);
        check("size=2", 2);
        check("size=10 정상케이스", 10);
    }
}
