package me.ihqqq.trohub.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/** Một trang kết quả. page tính từ 0. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PageResponse<T> {
    List<T> items;
    int page;
    int size;
    long totalItems;
    int totalPages;
    boolean hasNext;

    /** Từ trang phân trang ở mức database. */
    public static <S, T> PageResponse<T> from(Page<S> page, Function<S, T> mapper) {
        return PageResponse.<T>builder()
                .items(page.getContent().stream().map(mapper).toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalItems(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .hasNext(page.hasNext())
                .build();
    }

    /** Cắt trang trong bộ nhớ từ danh sách đã lọc; chỉ ánh xạ các phần tử của trang được trả. */
    public static <S, T> PageResponse<T> of(List<S> all, int page, int size, Function<S, T> mapper) {
        long total = all.size();
        int from = (int) Math.min((long) page * size, total);
        int to = (int) Math.min(from + (long) size, total);
        List<T> items = new ArrayList<>();
        for (S s : all.subList(from, to)) {
            items.add(mapper.apply(s));
        }
        return PageResponse.<T>builder()
                .items(items)
                .page(page)
                .size(size)
                .totalItems(total)
                .totalPages((int) Math.ceil(total / (double) size))
                .hasNext(to < total)
                .build();
    }
}