package com.classitda.studio.application;

import com.classitda.common.exception.ClassitdaException;
import com.classitda.common.exception.CommonErrorCode;
import com.classitda.common.pagination.CursorResponse;
import com.classitda.studio.domain.PermissionCode;
import com.classitda.studio.domain.Room;
import com.classitda.studio.domain.Studio;
import com.classitda.studio.domain.repository.RoomRepository;
import com.classitda.studio.domain.repository.StudioRepository;
import com.classitda.studio.exception.StudioErrorCode;
import com.classitda.studio.exception.StudioException;
import com.classitda.studio.presentation.dto.RoomCreateRequest;
import com.classitda.studio.presentation.dto.RoomResponse;
import com.classitda.studio.presentation.dto.RoomUpdateRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class RoomService {

    private static final Long FIRST_PAGE_CURSOR = 0L;
    private static final int MIN_SIZE = 1;
    private static final int MAX_SIZE = 100;

    private final RoomRepository roomRepository;
    private final StudioPermissionService studioPermissionService;
    private final StudioRepository studioRepository;

    @Transactional
    public RoomResponse save(Long memberId, Long studioId, RoomCreateRequest request) {
        Studio studio = getStudio(studioId);
        studioPermissionService.validate(studio, memberId, PermissionCode.ROOM_MANAGE);
        validateNameNotDuplicated(studioId, request.name());
        try {
            return RoomResponse.from(roomRepository.saveAndFlush(request.toEntity(studio)));
        } catch (DataIntegrityViolationException exception) {
            throw new StudioException(StudioErrorCode.ROOM_NAME_DUPLICATED);
        }
    }

    public CursorResponse<RoomResponse> findWithCursor(Long studioId, String cursor, int size) {
        if (size < MIN_SIZE || size > MAX_SIZE) {
            throw new ClassitdaException(CommonErrorCode.INVALID_INPUT);
        }
        getStudio(studioId);
        Slice<Room> slice = roomRepository.findByStudioIdAndIdGreaterThanOrderByIdAsc(
                studioId, toCursorId(cursor), PageRequest.ofSize(size));
        List<RoomResponse> items = slice.getContent().stream()
                .map(RoomResponse::from)
                .toList();
        return CursorResponse.of(items, slice.hasNext(), toNextCursor(slice.getContent(), slice.hasNext()));
    }

    @Transactional
    public RoomResponse update(Long memberId, Long studioId, Long roomId, RoomUpdateRequest request) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new StudioException(StudioErrorCode.ROOM_NOT_FOUND));
        room.validateBelongsTo(studioId);
        studioPermissionService.validate(room.getStudio(), memberId, PermissionCode.ROOM_MANAGE);
        String name = request.name();
        if (!name.equals(room.getName())) {
            validateNameNotDuplicated(studioId, name);
        }
        room.update(name);
        return RoomResponse.from(room);
    }

    private Studio getStudio(Long studioId) {
        return studioRepository.findById(studioId)
                .orElseThrow(() -> new StudioException(StudioErrorCode.NOT_FOUND));
    }

    private void validateNameNotDuplicated(Long studioId, String name) {
        if (roomRepository.existsByStudioIdAndName(studioId, name)) {
            throw new StudioException(StudioErrorCode.ROOM_NAME_DUPLICATED);
        }
    }

    private Long toCursorId(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return FIRST_PAGE_CURSOR;
        }
        try {
            return Long.parseLong(cursor);
        } catch (NumberFormatException exception) {
            throw new ClassitdaException(CommonErrorCode.INVALID_INPUT);
        }
    }

    private String toNextCursor(List<Room> rooms, boolean hasNext) {
        if (!hasNext || rooms.isEmpty()) {
            return null;
        }
        return String.valueOf(rooms.getLast().getId());
    }
}
