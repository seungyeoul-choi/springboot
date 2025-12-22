package org.zerock.ex2.repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Commit;
import org.zerock.ex2.entity.Memo;

import jakarta.transaction.Transactional;

@SpringBootTest
@SuppressWarnings("null")
public class MemoRepositoryTests {

    @Autowired
    MemoRepository memoRepository;

    @Test
    public void testInsertDummies() {
        IntStream.rangeClosed(1, 100).forEach(i -> {
            Memo memo = Memo.builder().memoText("Sample..." + i).build();
            memoRepository.save(memo);
        });
    }

    @Test
    public void testSelect() {
        // 데이터베이스에 존재하는 mno
        Long mno = 100L;

        // 있을 수도 있고 없을수도 있는 객체를 담을 수 있는 객체
        Optional<Memo> result = memoRepository.findById(mno);

        System.out.println("================================================");

        if (result.isPresent()) {
            Memo memo = result.get();
            System.out.println(memo);
        }
    }

    @Transactional
    @Test
    public void testSelect2() {
        Long mno = 100L;
        Memo memo = memoRepository.getReferenceById(mno);
        System.out.println("================================================");
        System.out.println(memo);
    }

    @Test
    public void testUpdate() {
        Memo memo = Memo.builder().mno(100L).memoText("Update Text").build();
        System.out.println(memoRepository.save(memo));
    }

    // @Commit
    @Test
    public void testDelete() {
        Long mno = 100L;
        memoRepository.deleteById(mno);
    }

    @Test
    public void testPageDefault() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Memo> result = memoRepository.findAll(pageable);
        System.out.println(result);

        System.out.println("================================================");
        System.out.println("Total Pages: " + result.getTotalPages()); // 총 몇 페이지
        System.out.println("Total Elements: " + result.getTotalElements()); // 전체 개수
        System.out.println("Page Number: " + result.getNumber()); // 현재 페이지 번호
        System.out.println("Page Size: " + result.getSize()); // 페이지 크기
        System.out.println("has next page: " + result.hasNext()); // 다음 페이지 존재 여부
        System.out.println("has previous page: " + result.hasPrevious()); // 이전 페이지 존재 여부
        System.out.println("first page? :" + result.isFirst());
        System.out.println("================================================");
    }

    @Test
    public void testSort() {
        Sort sort1 = Sort.by("mno").descending();
        Pageable pageable = PageRequest.of(0, 10, sort1);
        Page<Memo> result = memoRepository.findAll(pageable);

        result.get().forEach(memo -> System.out.println(memo));
    }

    @Test
    public void testQueryMethods() {
        List<Memo> result = memoRepository.findByMnoBetweenOrderByMnoDesc(70L, 80L);

        for (Memo memo : result) {
            System.out.println(memo);
        }
    }

    @Test
    public void testQueryMethodWithPageable() {
        System.out.println("================================================");
        System.out.println("=== SQL 쿼리 실행 시작 ===");
        System.out.println("================================================");

        Pageable pageable = PageRequest.of(0, 10, Sort.by("mno").descending());
        Page<Memo> result = memoRepository.findByMnoBetween(10L, 50L, pageable);

        System.out.println("================================================");
        System.out.println("=== 조회 결과 ===");
        System.out.println("Total Elements: " + result.getTotalElements());
        System.out.println("Total Pages: " + result.getTotalPages());
        System.out.println("================================================");

        result.get().forEach(memo -> System.out.println(memo));

        System.out.println("================================================");
    }

    @Commit
    @Transactional
    @Test
    public void testDeleteQueryMethods() {
        memoRepository.deleteMemoByMnoLessThan(10L);
    }
}
