package Sookmyung.Lingo.domains.rawDocument.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import Sookmyung.Lingo.common.exception.CustomException;
import Sookmyung.Lingo.common.exception.ErrorCode;
import Sookmyung.Lingo.domains.member.domain.Member;
import Sookmyung.Lingo.domains.rawDocument.domain.RawDocument;
import Sookmyung.Lingo.domains.rawDocument.domain.RawDocumentImage;
import Sookmyung.Lingo.domains.rawDocument.dto.RawDocumentRequestDTO;
import Sookmyung.Lingo.domains.rawDocument.repository.RawDocumentRepository;
import jakarta.transaction.Transactional;

@Service
public class RawDocumentService {

	private final RawDocumentRepository rawDocumentRepository;

	@Autowired
	public RawDocumentService(RawDocumentRepository rawDocumentRepository) {
		this.rawDocumentRepository = rawDocumentRepository;
	}

	@Transactional
	public RawDocument saveRawDocument(RawDocumentRequestDTO rawDocumentRequestDTO, List<String> imageUrls, Member member) {

		if (imageUrls == null || imageUrls.isEmpty()) {
			throw new CustomException(ErrorCode.NO_FILE_UPLOADED);
		}
		if (!rawDocumentRequestDTO.getTotalPages().equals((long) imageUrls.size())) {
			throw new CustomException(ErrorCode.FILE_TOTAL_PAGE_MISMATCH);
		}
		RawDocument rawDocument = rawDocumentRequestDTO.toEntity(member);

		for (int i = 0; i < imageUrls.size(); i++) {
			RawDocumentImage image = RawDocumentImage.builder()
				.rawFilename("original_" + (i + 1))
				.pageNumber((long) (i + 1)) // 페이지 번호
				.rawFilePath(imageUrls.get(i)) // S3 URL
				.build();

			rawDocument.addRawDocumentImage(image);
		}

		return rawDocumentRepository.save(rawDocument);
	}

	public RawDocument getOrThrow(Long id) {
		return rawDocumentRepository.findById(id)
			.orElseThrow(() ->
				new CustomException(ErrorCode.NOT_FOUND_RAW_DOCUMENT));
	}
}