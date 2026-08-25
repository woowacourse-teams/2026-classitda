package com.classitda.studio.domain;

import com.classitda.common.domain.BaseEntity;
import com.classitda.member.domain.Member;
import com.classitda.studio.exception.StudioErrorCode;
import com.classitda.studio.exception.StudioException;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "studio")
@Entity
public class Studio extends BaseEntity {

    public static final String IMAGE_KEY_NAMESPACE = "studio-images";

    private static final String IMAGE_KEY_PREFIX = IMAGE_KEY_NAMESPACE + "/";
    private static final int MAX_IMAGE_OBJECT_KEY_LENGTH = 255;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_member_id", nullable = false)
    private Member owner;

    @Column(nullable = false, length = 50)
    private String name;

    @Embedded
    private Address address;

    @Column(length = 20)
    private String phoneNumber;

    @Column(length = MAX_IMAGE_OBJECT_KEY_LENGTH)
    private String imageObjectKey;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private LocalTime openTime;

    @Column(nullable = false)
    private LocalTime closeTime;

    @Builder
    private Studio(
            Member owner,
            String name,
            Address address,
            String phoneNumber,
            String imageObjectKey,
            String description,
            LocalTime openTime,
            LocalTime closeTime
    ) {
        validateOperatingTime(openTime, closeTime);
        validateAddress(address);
        validateImageObjectKey(imageObjectKey);
        this.owner = owner;
        this.name = name;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.imageObjectKey = imageObjectKey;
        this.description = description;
        this.openTime = openTime;
        this.closeTime = closeTime;
    }

    public boolean isOwner(Long memberId) {
        return owner.getId().equals(memberId);
    }

    public void update(
            String name,
            Address address,
            String phoneNumber,
            String imageObjectKey,
            String description,
            LocalTime openTime,
            LocalTime closeTime
    ) {
        validateOperatingTime(openTime, closeTime);
        validateAddress(address);
        validateImageObjectKey(imageObjectKey);
        this.name = name;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.imageObjectKey = imageObjectKey;
        this.description = description;
        this.openTime = openTime;
        this.closeTime = closeTime;
    }

    public void removeImage() {
        this.imageObjectKey = null;
    }

    private void validateAddress(Address address) {
        if (address == null) {
            throw new StudioException(StudioErrorCode.INVALID_ROAD_ADDRESS);
        }
    }

    private void validateImageObjectKey(String imageObjectKey) {
        if (imageObjectKey == null) {
            return;
        }
        boolean allowed = imageObjectKey.startsWith(IMAGE_KEY_PREFIX)
                && imageObjectKey.length() > IMAGE_KEY_PREFIX.length()
                && imageObjectKey.length() <= MAX_IMAGE_OBJECT_KEY_LENGTH
                && !imageObjectKey.contains("..");
        if (!allowed) {
            throw new StudioException(StudioErrorCode.INVALID_IMAGE_OBJECT_KEY);
        }
    }

    private void validateOperatingTime(LocalTime openTime, LocalTime closeTime) {
        if (openTime == null || closeTime == null || !openTime.isBefore(closeTime)) {
            throw new StudioException(StudioErrorCode.INVALID_OPERATING_TIME);
        }
    }
}
