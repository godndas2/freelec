package org.sb.aws.service;

import lombok.RequiredArgsConstructor;
import org.sb.aws.entity.posts.Posts;
import org.sb.aws.entity.posts.PostsRepository;
import org.sb.aws.rest.dto.PostsListResponseDto;
import org.sb.aws.rest.dto.PostsResponseDto;
import org.sb.aws.rest.dto.PostsSaveRequestDto;
import org.sb.aws.rest.dto.PostsUpdateRequestDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class PostsService {
    private final PostsRepository postsRepository;

    private static boolean isBlank(String str) {
        int strLen;

        str = str.trim();

        if ((strLen = str.length()) == 0) {
            return true;
        }

        for (int i = 0; i < strLen; i++) {
            if ((!Character.isWhitespace(str.charAt(i)))) {
                return false;
            }
        }
        return true;
    }

    @Transactional
    public Long save(PostsSaveRequestDto requestDto) {
        if (isBlank(requestDto.getTitle()) || isBlank(requestDto.getAuthor())) {
            throw new IllegalArgumentException("제목 또는 작성자가 입력되지 않았습니다");
        }

        return postsRepository.save(requestDto.toEntity()).getId();
    }

    @Transactional
    public Long update(Long id, PostsUpdateRequestDto requestDto) {
        Posts posts = postsRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 없습니다. id=" + id));

        posts.update(requestDto.getTitle(), requestDto.getContent());

        return id;
    }

    @Transactional
    public void delete (Long id) {
        Posts posts = postsRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 없습니다. id=" + id));

        postsRepository.delete(posts);
    }

    @Transactional(readOnly = true)
    public PostsResponseDto findById(Long id) {
        Posts entity = postsRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 없습니다. id=" + id));

        return new PostsResponseDto(entity);
    }

    @Transactional(readOnly = true)
    public List<PostsListResponseDto> findAllDesc() {
        return postsRepository.findAllDesc().stream()
                .map(PostsListResponseDto::new)
                .collect(Collectors.toList());
    }
}
