package com.classitda.core.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

data class KakaoPostcodeResult(
    val zoneCode: String,
    val roadAddress: String,
    val jibunAddress: String,
    val buildingName: String,
) {
    init {
        require(zoneCode.isNotBlank()) { "Kakao 우편번호는 비어 있을 수 없습니다." }
        require(roadAddress.isNotBlank() || jibunAddress.isNotBlank()) {
            "도로명 주소 또는 지번 주소가 필요합니다."
        }
    }

    companion object {
        fun parse(payload: String): Result<KakaoPostcodeResult> =
            parseKakaoPostcodeBridgeMessage(payload).map { message ->
                when (message) {
                    is KakaoPostcodeBridgeMessage.Result -> message.value
                    is KakaoPostcodeBridgeMessage.Error -> error("Kakao 우편번호 wrapper 오류: ${message.reason}")
                }
            }
    }
}

enum class KakaoPostcodeError {
    NETWORK,
    INVALID_PAYLOAD,
    NAVIGATION_BLOCKED,
    UNKNOWN,
}

sealed interface KakaoPostcodeSearchState {
    data object Loading : KakaoPostcodeSearchState

    data object Ready : KakaoPostcodeSearchState

    data class Error(
        val reason: KakaoPostcodeError,
    ) : KakaoPostcodeSearchState
}

@Composable
internal expect fun KakaoPostcodeWebContent(
    onLoadingChanged: (Boolean) -> Unit,
    onResult: (KakaoPostcodeResult) -> Unit,
    onCancelled: () -> Unit,
    onError: (KakaoPostcodeError) -> Unit,
    modifier: Modifier = Modifier,
)

internal const val KAKAO_POSTCODE_BRIDGE_NAME = "ClassitdaKakaoPostcode"
internal const val KAKAO_POSTCODE_BASE_URL = "https://postcode.map.kakao.com/"
internal const val KAKAO_POSTCODE_SCRIPT_URL =
    "https://t1.kakaocdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js"

internal val KAKAO_POSTCODE_ALLOWED_HOSTS =
    setOf(
        "postcode.map.kakao.com",
        "postcode.map.daum.net",
        "t1.kakaocdn.net",
        "t1.daumcdn.net",
    )

internal sealed interface KakaoPostcodeBridgeMessage {
    data class Result(
        val value: KakaoPostcodeResult,
    ) : KakaoPostcodeBridgeMessage

    data class Error(
        val reason: KakaoPostcodeError,
    ) : KakaoPostcodeBridgeMessage
}

internal fun parseKakaoPostcodeBridgeMessage(payload: String): Result<KakaoPostcodeBridgeMessage> =
    runCatching {
        val wirePayload = kakaoPostcodeJson.decodeFromString<KakaoPostcodePayload>(payload)
        wirePayload.error?.let { error ->
            KakaoPostcodeBridgeMessage.Error(error.toKakaoPostcodeError())
        } ?: KakaoPostcodeBridgeMessage.Result(wirePayload.toResult())
    }

internal fun kakaoPostcodeWrapperHtml(): String =
    """
    <!doctype html>
    <html lang="ko">
      <head>
        <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0">
        <style>
          html, body, #postcode { width: 100%; height: 100%; margin: 0; padding: 0; }
          #postcode { max-width: 100%; overflow-x: hidden; }
          body { overflow: hidden; background: #ffffff; }
        </style>
        <script>
          window.__classitdaKakaoPostcodeError = function(reason) {
            var bridge = window.${KAKAO_POSTCODE_BRIDGE_NAME};
            if (bridge && typeof bridge.postError === 'function') {
              bridge.postError(reason);
            }
            var iosBridge = window.webkit && window.webkit.messageHandlers &&
              window.webkit.messageHandlers.${KAKAO_POSTCODE_BRIDGE_NAME};
            if (iosBridge && typeof iosBridge.postMessage === 'function') {
              iosBridge.postMessage(JSON.stringify({ error: reason }));
            }
          };
        </script>
        <script src="$KAKAO_POSTCODE_SCRIPT_URL" onerror="window.__classitdaKakaoPostcodeError('SCRIPT_LOAD')"></script>
      </head>
      <body>
        <div id="postcode"></div>
        <script>
          (function() {
            if (!window.kakao || !window.kakao.Postcode) {
              window.__classitdaKakaoPostcodeError('POSTCODE_UNAVAILABLE');
              return;
            }

            function sendResult(data) {
              var payload = JSON.stringify({
                zonecode: data.zonecode || '',
                roadAddress: data.roadAddress || '',
                jibunAddress: data.jibunAddress || '',
                buildingName: data.buildingName || ''
              });
              var bridge = window.${KAKAO_POSTCODE_BRIDGE_NAME};
              if (bridge && typeof bridge.postMessage === 'function') {
                bridge.postMessage(payload);
              }
              var iosBridge = window.webkit && window.webkit.messageHandlers &&
                window.webkit.messageHandlers.${KAKAO_POSTCODE_BRIDGE_NAME};
              if (iosBridge && typeof iosBridge.postMessage === 'function') {
                iosBridge.postMessage(payload);
              }
            }

            new kakao.Postcode({
              width: '100%',
              height: '100%',
              maxSuggestItems: 5,
              oncomplete: sendResult,
              onresize: function(size) {
                document.getElementById('postcode').style.height = size.height + 'px';
              }
            }).embed(document.getElementById('postcode'));
          })();
        </script>
      </body>
    </html>
    """.trimIndent()

@Serializable
private data class KakaoPostcodePayload(
    @SerialName("zonecode") val zoneCode: String = "",
    val roadAddress: String = "",
    val jibunAddress: String = "",
    val buildingName: String = "",
    val error: String? = null,
) {
    fun toResult(): KakaoPostcodeResult =
        KakaoPostcodeResult(
            zoneCode = zoneCode,
            roadAddress = roadAddress,
            jibunAddress = jibunAddress,
            buildingName = buildingName,
        )
}

private fun String.toKakaoPostcodeError(): KakaoPostcodeError =
    when (this) {
        "NETWORK", "SCRIPT_LOAD", "POSTCODE_UNAVAILABLE" -> KakaoPostcodeError.NETWORK
        else -> KakaoPostcodeError.UNKNOWN
    }

private val kakaoPostcodeJson =
    Json {
        ignoreUnknownKeys = true
    }
