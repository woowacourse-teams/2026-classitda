package com.pheeeew.sigh.domain;

import com.pheeeew.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "sighs")
@Entity
public class Sigh extends BaseEntity {

    private static final int WGS84_SRID = 4326;
    private static final int MAX_MEMO_LENGTH = 200;
    private static final int MAX_NICKNAME_LENGTH = 50;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_id", nullable = false, updatable = false)
    private UUID requestId;

    @Getter(AccessLevel.NONE)
    @Column(nullable = false, updatable = false, columnDefinition = "geometry(Point,4326)")
    private Point location;

    @Column(length = MAX_MEMO_LENGTH, updatable = false)
    private String memo;

    @Column(length = MAX_NICKNAME_LENGTH, updatable = false)
    private String nickname;

    @Builder
    private Sigh(UUID requestId, Point location, String memo, String nickname) {
        this.requestId = Objects.requireNonNull(requestId);
        this.location = requireWgs84Point(location);
        this.memo = normalizeMemo(memo);
        this.nickname = requireValidNickname(nickname);
    }

    public double getLongitude() {
        return location.getX();
    }

    public double getLatitude() {
        return location.getY();
    }

    private Point requireWgs84Point(Point location) {
        Objects.requireNonNull(location);
        if (location.isEmpty() || location.getSRID() != WGS84_SRID) {
            throw new IllegalArgumentException("위치는 비어 있지 않은 WGS84(SRID 4326) 점 좌표여야 합니다.");
        }
        return location;
    }

    private String normalizeMemo(String memo) {
        if (memo == null) {
            return null;
        }

        String normalizedMemo = memo.strip();
        if (normalizedMemo.isEmpty()) {
            return null;
        }
        if (normalizedMemo.length() > MAX_MEMO_LENGTH) {
            throw new IllegalArgumentException("메모는 200자를 초과할 수 없습니다.");
        }

        return normalizedMemo;
    }

    private String requireValidNickname(String nickname) {
        if (nickname == null) {
            return null;
        }
        if (nickname.isBlank()) {
            throw new IllegalArgumentException("닉네임은 비어 있을 수 없습니다.");
        }
        if (nickname.length() > MAX_NICKNAME_LENGTH) {
            throw new IllegalArgumentException("닉네임은 50자를 초과할 수 없습니다.");
        }

        return nickname;
    }
}
