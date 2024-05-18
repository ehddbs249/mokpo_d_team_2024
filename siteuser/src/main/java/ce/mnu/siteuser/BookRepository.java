package ce.mnu.siteuser;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {
    // 제목으로 책 검색
    List<Book> findByTitleContaining(String title);

    // 저자로 책 검색
    List<Book> findByAuthorContaining(String author);

    // 가격 범위로 책 검색
    List<Book> findByPriceBetween(double minPrice, double maxPrice);

    // 상태로 책 검색
    List<Book> findByStatus(String status);

    // 카테고리와 관련된 쿼리가 필요하다면 추가적인 메소드를 정의할 수 있습니다.
    // 예를 들어, 카테고리 필드가 있다면 다음과 같은 메소드를 추가할 수 있습니다.
    // List<Book> findByCategory(String category);

    List<Book> findAllByOrderByIdAsc();
}