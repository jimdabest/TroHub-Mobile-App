package me.ihqqq.trohub.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

/** Thay đổi của một loại dữ liệu: dòng mới/đã sửa (upsert theo id) và id các dòng đã bị xóa. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SyncChanges<T> {
    List<T> upserts;
    List<UUID> deletedIds;
}