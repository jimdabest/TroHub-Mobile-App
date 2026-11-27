package me.ihqqq.trohub.util;

import me.ihqqq.trohub.exception.AppException;
import me.ihqqq.trohub.exception.ErrorCode;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class PagingUtils {
    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 100;

    private PagingUtils() {}

    /** page từ 0, size từ 1 đến MAX_SIZE; sai thì trả INVALID_REQUEST (400). */
    public static void validate(int page, int size) {
        if (page < 0 || size < 1 || size > MAX_SIZE) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
    }

    public static Pageable pageable(int page, int size, Sort sort) {
        validate(page, size);
        return PageRequest.of(page, size, sort);
    }

    public static Pageable pageable(int page, int size) {
        validate(page, size);
        return PageRequest.of(page, size);
    }
}