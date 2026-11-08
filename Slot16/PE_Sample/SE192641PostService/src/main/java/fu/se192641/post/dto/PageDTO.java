package fu.se192641.post.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public class PageDTO<T> {

    private int size;
    private int page;
    private int totalPages;
    private long totalElements;
    private boolean first;
    private boolean last;
    private List<T> content;

    public PageDTO() {
    }

    public static <T> PageDTO<T> of(Page<?> source, List<T> content) {
        PageDTO<T> dto = new PageDTO<>();
        dto.size = source.getSize();
        dto.page = source.getNumber();
        dto.totalPages = source.getTotalPages();
        dto.totalElements = source.getTotalElements();
        dto.first = source.isFirst();
        dto.last = source.isLast();
        dto.content = content;
        return dto;
    }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }

    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }

    public long getTotalElements() { return totalElements; }
    public void setTotalElements(long totalElements) { this.totalElements = totalElements; }

    public boolean isFirst() { return first; }
    public void setFirst(boolean first) { this.first = first; }

    public boolean isLast() { return last; }
    public void setLast(boolean last) { this.last = last; }

    public List<T> getContent() { return content; }
    public void setContent(List<T> content) { this.content = content; }
}
