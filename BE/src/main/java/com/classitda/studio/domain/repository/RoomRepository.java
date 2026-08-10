package com.classitda.studio.domain.repository;

import com.classitda.studio.domain.Room;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, Long> {

    boolean existsByStudioIdAndName(Long studioId, String name);

    Slice<Room> findByStudioIdAndIdGreaterThanOrderByIdAsc(Long studioId, Long cursorId, Pageable pageable);
}
