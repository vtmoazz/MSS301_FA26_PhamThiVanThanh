package fu.se192641.post.service;

import fu.se192641.post.dto.PageDTO;
import fu.se192641.post.dto.PostDTO;
import fu.se192641.post.dto.PostDetailDTO;

public interface PostService {

    PostDTO create(PostDTO dto);

    PostDTO update(Long postId, PostDTO dto);

    PostDetailDTO getById(Long postId);

    void cancel(Long postId);

    PageDTO<PostDetailDTO> list(int page, int size, String advertiserName, String status);
}
