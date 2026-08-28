package com.classitda.common.image;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.classitda.common.exception.ClassitdaException;
import com.classitda.common.exception.CommonErrorCode;
import java.net.URI;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

class ImageUploadUrlIssuerTest {

    private static final ImageProperties PROPERTIES = new ImageProperties(
            "techcourse-project-2026",
            "classitda",
            "https://images.test",
            "ap-northeast-2",
            Duration.ofMinutes(5)
    );

    private static final String NAMESPACE = "studio-images";
    private static final long SIZE = 3_145_728L;

    private S3Presigner s3Presigner;
    private ImageUploadUrlIssuer issuer;

    @BeforeEach
    void 프리사이너를_준비한다() throws Exception {
        s3Presigner = mock(S3Presigner.class);
        PresignedPutObjectRequest presigned = mock(PresignedPutObjectRequest.class);
        given(presigned.url()).willReturn(URI.create("https://s3.test/upload").toURL());
        given(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class))).willReturn(presigned);
        issuer = new ImageUploadUrlIssuer(s3Presigner, PROPERTIES);
    }

    @Test
    void 발급한_키는_업로드_네임스페이스로_시작한다() {
        // given / when
        ImageUploadUrl uploadUrl = issuer.issue(NAMESPACE, "jpg", SIZE);

        // then
        assertThat(uploadUrl.objectKey()).startsWith(NAMESPACE + "/");
        assertThat(uploadUrl.objectKey()).endsWith(".jpg");
        assertThat(uploadUrl.contentType()).isEqualTo("image/jpeg");
        assertThat(uploadUrl.uploadUrl()).isEqualTo("https://s3.test/upload");
    }

    @Test
    void 서명하는_저장_키에는_팀_폴더_접두사가_붙는다() {
        // given
        ArgumentCaptor<PutObjectPresignRequest> captor =
                ArgumentCaptor.forClass(PutObjectPresignRequest.class);

        // when
        ImageUploadUrl uploadUrl = issuer.issue(NAMESPACE, "png", SIZE);

        // then
        verify(s3Presigner).presignPutObject(captor.capture());
        PutObjectPresignRequest presignRequest = captor.getValue();
        assertThat(presignRequest.putObjectRequest().bucket()).isEqualTo("techcourse-project-2026");
        assertThat(presignRequest.putObjectRequest().key())
                .isEqualTo("classitda/" + uploadUrl.objectKey());
        assertThat(presignRequest.putObjectRequest().contentType()).isEqualTo("image/png");
        assertThat(presignRequest.putObjectRequest().contentLength()).isEqualTo(SIZE);
        assertThat(presignRequest.signatureDuration()).isEqualTo(Duration.ofMinutes(5));
    }

    @Test
    void 대문자_확장자도_소문자로_정규화한다() {
        // given / when
        ImageUploadUrl uploadUrl = issuer.issue(NAMESPACE, "JPEG", SIZE);

        // then
        assertThat(uploadUrl.objectKey()).endsWith(".jpeg");
        assertThat(uploadUrl.contentType()).isEqualTo("image/jpeg");
    }

    @Test
    void 오_메가바이트를_넘으면_예외가_발생한다() {
        // given
        long overLimit = 5L * 1024 * 1024 + 1;

        // when / then
        assertThatThrownBy(() -> issuer.issue(NAMESPACE, "jpg", overLimit))
                .isInstanceOf(ClassitdaException.class)
                .hasMessage(CommonErrorCode.IMAGE_SIZE_EXCEEDED.getMessage());
    }

    @Test
    void 정확히_오_메가바이트는_허용한다() {
        // given
        long limit = 5L * 1024 * 1024;

        // when
        ImageUploadUrl uploadUrl = issuer.issue(NAMESPACE, "jpg", limit);

        // then
        assertThat(uploadUrl.objectKey()).startsWith(NAMESPACE + "/");
    }

    @Test
    void 크기가_영이하면_예외가_발생한다() {
        // given / when / then
        assertThatThrownBy(() -> issuer.issue(NAMESPACE, "jpg", 0L))
                .isInstanceOf(ClassitdaException.class)
                .hasMessage(CommonErrorCode.IMAGE_SIZE_EXCEEDED.getMessage());
    }

    @Test
    void 지원하지_않는_확장자는_예외가_발생한다() {
        // given / when / then
        assertThatThrownBy(() -> issuer.issue(NAMESPACE, "gif", SIZE))
                .isInstanceOf(ClassitdaException.class)
                .hasMessage(CommonErrorCode.INVALID_IMAGE_EXTENSION.getMessage());
    }

    @Test
    void 네임스페이스가_슬래시를_포함하면_예외가_발생한다() {
        // given / when / then
        assertThatThrownBy(() -> issuer.issue("studio-images/sub", "jpg", SIZE))
                .isInstanceOf(ClassitdaException.class)
                .hasMessage(CommonErrorCode.INVALID_IMAGE_NAMESPACE.getMessage());
    }

    @Test
    void 다른_네임스페이스로도_발급할_수_있다() {
        // given / when
        ImageUploadUrl uploadUrl = issuer.issue("notice-images", "png", SIZE);

        // then
        assertThat(uploadUrl.objectKey()).startsWith("notice-images/");
    }

    @Test
    void 확장자가_비어_있으면_예외가_발생한다() {
        // given / when / then
        assertThatThrownBy(() -> issuer.issue(NAMESPACE, "  ", SIZE))
                .isInstanceOf(ClassitdaException.class)
                .hasMessage(CommonErrorCode.INVALID_IMAGE_EXTENSION.getMessage());
    }
}
