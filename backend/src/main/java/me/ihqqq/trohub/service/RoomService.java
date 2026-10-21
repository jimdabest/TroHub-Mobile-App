package me.ihqqq.trohub.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import me.ihqqq.trohub.dto.request.RoomRequest;
import me.ihqqq.trohub.dto.request.RoomStatusRequest;
import me.ihqqq.trohub.dto.response.RoomResponse;
import me.ihqqq.trohub.entity.Room;
import me.ihqqq.trohub.enums.ContractStatus;
import me.ihqqq.trohub.enums.RoomStatus;
import me.ihqqq.trohub.exception.AppException;
import me.ihqqq.trohub.exception.ErrorCode;
import me.ihqqq.trohub.mapper.RoomMapper;
import me.ihqqq.trohub.repository.ContractRepository;
import me.ihqqq.trohub.repository.RoomRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class RoomService {

    RoomRepository roomRepository;
    ContractRepository contractRepository;
    RoomMapper roomMapper;
    PropertyAccessService accessService;

    @Transactional(readOnly = true)
    public List<RoomResponse> list(UUID propertyId, RoomStatus status) {
        accessService.requireMember(propertyId);
        List<Room> rooms = status == null
                ? roomRepository.findAllByPropertyIdAndDeletedFalseOrderByCodeAsc(propertyId)
                : roomRepository.findAllByPropertyIdAndStatusAndDeletedFalseOrderByCodeAsc(propertyId, status);
        return rooms.stream().map(roomMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public RoomResponse get(UUID propertyId, UUID roomId) {
        accessService.requireMember(propertyId);
        return roomMapper.toResponse(find(propertyId, roomId));
    }

    public RoomResponse create(UUID propertyId, RoomRequest request) {
        var access = accessService.requireMember(propertyId);
        String code = request.getCode().trim();
        if (roomRepository.existsByPropertyIdAndCodeAndDeletedFalse(propertyId, code)) {
            throw new AppException(ErrorCode.ROOM_CODE_EXISTED);
        }

        Room room = roomMapper.toRoom(request);
        room.setCode(code);
        room.setProperty(access.property());
        room.setStatus(RoomStatus.AVAILABLE);
        return roomMapper.toResponse(saveUnique(room));
    }

    public RoomResponse update(UUID propertyId, UUID roomId, RoomRequest request) {
        accessService.requireMember(propertyId);
        Room room = find(propertyId, roomId);
        String code = request.getCode().trim();
        if (roomRepository.existsByPropertyIdAndCodeAndDeletedFalseAndIdNot(propertyId, code, roomId)) {
            throw new AppException(ErrorCode.ROOM_CODE_EXISTED);
        }

        roomMapper.update(room, request);
        room.setCode(code);
        return roomMapper.toResponse(saveUnique(room));
    }

    public RoomResponse changeStatus(UUID propertyId, UUID roomId, RoomStatusRequest request) {
        accessService.requireMember(propertyId);
        Room room = find(propertyId, roomId);
        RoomStatus target = request.getStatus();

        if (target == RoomStatus.OCCUPIED || room.getStatus() == RoomStatus.OCCUPIED) {
            throw new AppException(ErrorCode.ROOM_STATUS_INVALID);
        }
        room.setStatus(target);
        return roomMapper.toResponse(roomRepository.save(room));
    }

    public void delete(UUID propertyId, UUID roomId) {
        accessService.requireMember(propertyId);
        Room room = find(propertyId, roomId);
        if (room.getStatus() == RoomStatus.OCCUPIED
                || contractRepository.existsByRoomIdAndStatusAndDeletedFalse(roomId, ContractStatus.ACTIVE)) {
            throw new AppException(ErrorCode.ROOM_HAS_ACTIVE_CONTRACT);
        }
        room.setDeleted(true);
        roomRepository.save(room);
    }

    private Room find(UUID propertyId, UUID roomId) {
        return roomRepository.findByIdAndPropertyIdAndDeletedFalse(roomId, propertyId)
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));
    }

    private Room saveUnique(Room room) {
        try {
            return roomRepository.saveAndFlush(room);
        } catch (DataIntegrityViolationException e) {
            throw new AppException(ErrorCode.ROOM_CODE_EXISTED);
        }
    }
}