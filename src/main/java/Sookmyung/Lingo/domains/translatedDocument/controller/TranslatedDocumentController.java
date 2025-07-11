package Sookmyung.Lingo.domains.translatedDocument.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import Sookmyung.Lingo.common.dto.ErrorResponse;
import Sookmyung.Lingo.common.util.AuthUtil;
import Sookmyung.Lingo.domains.member.domain.Member;
import Sookmyung.Lingo.domains.translatedDocument.dto.TranslatedDocumentResponseDTO;
import Sookmyung.Lingo.domains.translatedDocument.service.TranslatedDocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api-docs/")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "TranslatedDocument", description = "번역 문서 관련 API")
public class TranslatedDocumentController {

	private final TranslatedDocumentService translatedDocumentService;
	private final AuthUtil authUtil;

	@Operation(
		summary = "내 번역 문서 목록 조회",
		description = "현재 로그인한 사용자의 번역 완료된 문서 목록을 조회합니다.",
		responses = {
			@ApiResponse(responseCode = "200", description = "조회 성공",
				content = @Content(schema = @Schema(implementation = TranslatedDocumentResponseDTO.class))),
			@ApiResponse(responseCode = "401", description = "인증 실패 또는 비로그인 상태",
				content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "500", description = "서버 오류",
				content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
		}
	)
	@GetMapping("/translated-documents-list")
	public ResponseEntity<List<TranslatedDocumentResponseDTO>> getMyTranslatedDocuments() {
		Member member = authUtil.getCurrentMember();
		List<TranslatedDocumentResponseDTO> response =
			translatedDocumentService.getTranslatedDocumentsByMemberId(member.getId());
		return ResponseEntity.ok(response);
	}
}
